package com.adlerd

import AsException
import IllegalInstructionException
import com.adlerd.util.SymbolTable
import com.adlerd.util.exceptions.IllegalMemAccessException
import com.adlerd.util.exceptions.InternalException
import java.util.*

open class ISA {

    protected open fun init() {
        createDef(".ORIG", "xxxx iiiiiiiiiiii", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
                if (wordList.size != 0) {
                    throw AsException(".ORIG can only appear at the beginning of a file")
                } else {
                    wordList.add(Word(instruction.offsetImmediate!!))
                }
            }

            @Throws(AsException::class)
            override fun getNextAddress(instruction: Instruction): Int {
                return instruction.offsetImmediate!!
            }
        })
        createDef(".FILL", "xxxx iiiiiiiiiiii", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
                wordList.add(Word(instruction.offsetImmediate!!))
            }
        })
        createDef(".FILL", "xxxx pppppppppppp", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
                val var4 = symbolTable.lookup(instruction.labelRef!!)
                if (var4 == -1) {
                    throw AsException(instruction, "Undeclared label: '${instruction.labelRef}'")
                } else {
                    wordList.add(Word(var4))
                }
            }
        })
        createDef(".BLKW", "xxxx iiiiiiiiiiii", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
                val var4 = instruction.offsetImmediate!!

                for (var5 in 0 until var4) {
                    wordList.add(Word(0))
                }

            }

            @Throws(AsException::class)
            override fun getNextAddress(instruction: Instruction): Int {
                return instruction.address + instruction.offsetImmediate!!
            }
        })
        createDef(".STRINGZ", "xxxx zzzzzzzzzzzz", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
                for (var4 in 0 until instruction.stringz!!.length) {
                    wordList.add(Word(instruction.stringz!![var4].toInt()))
                }

                wordList.add(Word(0))
            }

            @Throws(AsException::class)
            override fun getNextAddress(instruction: Instruction): Int {
                return instruction.address + instruction.stringz!!.length + 1
            }
        })
        createDef(".END", "xxxx xxxxxxxxxxxx", object : InstructionDef() {

            override val isDataDirective: Boolean
                get() = true

            @Throws(AsException::class)
            override fun encode(
                symbolTable: SymbolTable,
                instruction: Instruction,
                wordList: MutableList<Word>
            ) {
            }

            override fun getNextAddress(instruction: Instruction): Int {
                return instruction.address
            }
        })
    }

    companion object {
        var lookupTable = arrayOfNulls<InstructionDef>(65536)
        var opcodeSet = HashSet<String>()
        var formatToDef = Hashtable<String, InstructionDef>()


        @Deprecated("")
        @Throws(IllegalMemAccessException::class, IllegalInstructionException::class)
        fun execute(registerFile: RegisterFile, memory: Memory, machine: Machine) {
            val address = registerFile.pc
            registerFile.checkAddr(address)
            val word = memory.getInst(address)
            val instructionDef = lookupTable[word.value]
            if (instructionDef == null) {
                throw IllegalInstructionException("Undefined instruction:  ${word.toHex()}")
            } else {
                val var6 = instructionDef.execute(word, address, registerFile, memory, machine)
                registerFile.pc = var6
                ++machine.cycleCount
                ++machine.instructionCount
                val var7 = machine.branchPredictor.getPredictedPC(address)
                if (var6 != var7) {
                    machine.cycleCount += 2
                    machine.branchStallCount += 2
                    machine.branchPredictor.update(address, var6)
                }

                if (instructionDef.isLoad) {
                    val var8 = machine.memory.getInst(var6)
                    val var9 = lookupTable[var8.value]
                        ?: throw IllegalInstructionException("Undefined instruction:  ${var8.toHex()}")

                    if (!var9.isStore) {
                        val destinationReg = instructionDef.getDestinationReg(word)
                        if (destinationReg >= 0 && (destinationReg == var9.getSourceReg1(var8) || destinationReg == var9.getSourceReg2(
                                var8
                            ))
                        ) {
                            ++machine.cycleCount
                            ++machine.loadStallCount
                        }
                    }
                }

                if (machine.isTraceEnabled) {
                    machine.generateTrace(instructionDef, address, word)
                }

            }
        }

        fun disassemble(word: Word, var1: Int, machine: Machine): String {
            return if (!machine.lookupAddrToInsn(var1) && !CompSim.isLC3) {
                ""
            } else {
                val var3 = lookupTable[word.value]
                if (var3 == null) ".FILL " + word.toHex() else var3.disassemble(word, var1, machine)
            }
        }

        fun isOpcode(opcode: String): Boolean {
            return opcodeSet.contains(opcode.toUpperCase())
        }

        @Throws(AsException::class)
        fun checkFormat(instruction: Instruction, var1: Int) {
            if (formatToDef[instruction.format] == null) {
                throw AsException(instruction, "Unexpected instruction format: actual: '${instruction.format}'")
            }
        }

        @Throws(AsException::class)
        fun encode(instruction: Instruction, var1: List<*>) {
            val var2 = instruction.format
            val var3 = formatToDef[var2]
            if (var3 == null) {
                instruction.error("Unknown instruction format: $var2")
            }

        }

        @Throws(IllegalInstructionException::class)
        fun isCall(var0: Word): Boolean {
            val var1 = lookupTable[var0.value]
            return var1?.isCall ?: throw IllegalInstructionException("Undefined instruction: ${var0.toHex()}")
        }

        fun createDef(var0: String, var1: String?, instruction: InstructionDef) {
            instruction.opcode = var0
            if (var1 != null) {
                instruction.setEncoding(var1)
                if (!instruction.isDataDirective) {
                    var var3 = 0
                    var var4 = 0

                    for (row in 0..65534) {
                        if (instruction.match(Word(row))) {
                            if (lookupTable[row] == null) {
                                ++var3
                                lookupTable[row] = instruction
                            } else {
                                ++var4
                            }
                        }
                    }

                    check(
                        var3 > 0 || var4 > 0,
                        "Useless instruction defined, probably an error, opcode=$var0"
                    )
                }
            }

            formatToDef[instruction.getFormat()] = instruction
            opcodeSet.add(instruction.opcode.toUpperCase())
        }

        fun check(var0: Boolean, var1: String) {
            if (!var0) {
                throw InternalException(var1)
            }
        }

        @Throws(AsException::class)
        protected fun labelRefToPCOffset(symbolTable: SymbolTable, instruction: Instruction, var2: Int) {
            val var3 = instruction.address + 1
            val var4 = symbolTable.lookup(instruction.labelRef!!)
            val var5 = var4 - var3
            if (var4 == -1) {
                throw AsException(instruction, "Undeclared label '${instruction.labelRef}'")
            } else if (var5 >= -(1 shl var2 - 1) && var5 <= 1 shl var2 - 1) {
                instruction.offsetImmediate = var5
            } else {
                throw AsException(instruction, "Jump offset longer than $var2 bits")
            }
        }
    }
}
