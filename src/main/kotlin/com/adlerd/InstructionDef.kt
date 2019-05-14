package com.adlerd

import AsException
import IllegalInstructionException
import com.adlerd.util.SymbolTable
import com.adlerd.util.exceptions.IllegalMemAccessException

abstract class InstructionDef {
    lateinit var opcode: String
    private var format = String()
    internal var dReg = Location()
    internal var sReg = Location()
    internal var tReg = Location()
    internal var signedImmed = Location()
    internal var pcOffset = Location()
    internal var unsignedImmed = Location()
    private var mask = 0
    private var match = 0

    open val isDataDirective: Boolean
        get() = false

    open val isCall: Boolean
        get() = false

    open val isBranch: Boolean
        get() = false

    open val isLoad: Boolean
        get() = false

    open val isStore: Boolean
        get() = false

    @Throws(IllegalMemAccessException::class, IllegalInstructionException::class)
    open fun execute(word: Word, var2: Int, registerFile: RegisterFile, memory: Memory, machine: Machine): Int {
        throw IllegalInstructionException("Abstract instruction (or pseudo-instruction)")
    }

    @Throws(IllegalMemAccessException::class)
    open fun getRefAddr(word: Word, var2: Int, registerFile: RegisterFile, memory: Memory): Int {
        return 0
    }

    fun getFormat(): String {
        return "${this.opcode.toUpperCase()} ${this.format}".trim { it <= ' ' }
    }

    @Throws(AsException::class)
    open fun getNextAddress(instruction: Instruction): Int {
        return instruction.address + 1
    }

    open fun getDestinationReg(word: Word): Int {
        return -1
    }

    open fun getSourceReg1(word: Word): Int {
        return -1
    }

    open fun getSourceReg2(word: Word): Int {
        return -1
    }

    fun getDReg(word: Word): Int {
        ISA.check(this.dReg.valid, "Invalid register")
        return word.getZext(this.dReg.start, this.dReg.end)
    }

    fun getSReg(word: Word): Int {
        ISA.check(this.sReg.valid, "Invalid register")
        return word.getZext(this.sReg.start, this.sReg.end)
    }

    fun getTReg(word: Word): Int {
        ISA.check(this.tReg.valid, "Invalid register")
        return word.getZext(this.tReg.start, this.tReg.end)
    }

    fun getSignedImmed(word: Word): Int {
        return word.getSext(this.signedImmed.start, this.signedImmed.end)
    }

    fun getPCOffset(word: Word): Int {
        return word.getSext(this.pcOffset.start, this.pcOffset.end)
    }

    fun getUnsignedImmed(word: Word): Int {
        return word.getZext(this.unsignedImmed.start, this.unsignedImmed.end)
    }

    fun disassemble(word: Word, var2: Int, machine: Machine?): String {
        var var4 = true
        var opcode = this.opcode
        if (this.dReg.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            opcode += "R${this.getDReg(word)}"
        }

        if (this.sReg.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            opcode += "R${this.getSReg(word)}"
        }

        if (this.tReg.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            opcode += "R${this.getTReg(word)}"
        }

        if (this.signedImmed.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            opcode += "#${this.getSignedImmed(word)}"
        }

        if (this.pcOffset.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            val var6 = var2 + this.getPCOffset(word) + 1
            var var7: String? = null
            if (machine != null) {
                var7 = machine.lookupSym(var6)
            }

            opcode += var7 ?: Word.toHex(var6)
        }

        if (this.unsignedImmed.valid) {
            if (var4) {
                opcode += " "
                var4 = false
            } else {
                opcode += ", "
            }

            opcode += "x${Integer.toHexString(this.getUnsignedImmed(word)).toUpperCase()}"
        }

        return opcode
    }

    @Throws(AsException::class)
    open fun encode(symbolTable: SymbolTable, instruction: Instruction, wordList: MutableList<Word>) {
        val var4 = Word()
        var4.value = this.match

        var symbolPos: Int
        try {
            symbolPos = 0
            if (this.dReg.valid) {
                var4.setUnsignedField(instruction.getRegs(symbolPos), this.dReg.start, this.dReg.end)
                ++symbolPos
            }

            if (this.sReg.valid) {
                var4.setUnsignedField(instruction.getRegs(symbolPos), this.sReg.start, this.sReg.end)
                ++symbolPos
            }

            if (this.tReg.valid) {
                var4.setUnsignedField(instruction.getRegs(symbolPos), this.tReg.start, this.tReg.end)
                ++symbolPos
            }
        } catch (var9: AsException) {
            throw AsException(instruction, "Register number out of range")
        }

        try {
            if (this.signedImmed.valid) {
                var4.setSignedField(instruction.offsetImmediate!!, this.signedImmed.start, this.signedImmed.end)
            }

            if (this.unsignedImmed.valid) {
                var4.setUnsignedField(instruction.offsetImmediate!!, this.unsignedImmed.start, this.unsignedImmed.end)
            }
        } catch (var8: AsException) {
            throw AsException(instruction, "Immediate out of range")
        }

        if (this.pcOffset.valid) {
            symbolPos = symbolTable.lookup(instruction.labelRef!!)
            if (symbolPos == -1) {
                throw AsException(instruction, "Undeclared label: ${instruction.labelRef}")
            }

            instruction.offsetImmediate = (symbolPos - (instruction.address + 1))

            try {
                var4.setSignedField(instruction.offsetImmediate!!, this.pcOffset.start, this.pcOffset.end)
            } catch (var7: AsException) {
                throw AsException(instruction, "PC-relative offset out of range")
            }

        }

        wordList.add(var4)
    }

    private fun encodeField(var1: String, var2: Char, var3: String, location: Location): String {
        val var5 = var1.indexOf(var2)
        val var6 = var1.lastIndexOf(var2)
        if (var5 != -1 && var6 != -1) {
            ISA.check(
                var1.substring(var5, var6).matches("[$var2]*".toRegex()),
                "Strange encoding of '$var2': $var1"
            )
            location.valid = true
            location.start = 15 - var5
            location.end = 15 - var6
            this.format = this.format + var3 + " "
            return var1.replace(("" + var2).toRegex(), "x")
        } else {
            return var1
        }
    }

    fun match(word: Word): Boolean {
        return word.value and this.mask == this.match
    }

    fun setEncoding(var1: String) {
        var var1 = var1
        val var2 = var1
        var1 = var1.toLowerCase()
        var1 = var1.replace("\\s".toRegex(), "")
        var1 = var1.replace("[^x10iudstpz]".toRegex(), "")
        ISA.check(var1.length == 16, "Strange encoding: $var2")
        var1 = this.encodeField(var1, 'd', "Reg", this.dReg)
        var1 = this.encodeField(var1, 's', "Reg", this.sReg)
        var1 = this.encodeField(var1, 't', "Reg", this.tReg)
        var1 = this.encodeField(var1, 'i', "Num", this.signedImmed)
        var1 = this.encodeField(var1, 'p', "Label", this.pcOffset)
        var1 = this.encodeField(var1, 'u', "Num", this.unsignedImmed)
        var1 = this.encodeField(var1, 'z', "String", this.unsignedImmed)
        var1 = var1.replace("[^x10]".toRegex(), "")
        ISA.check(var1.length == 16, "Strange encoding: $var2")
        var var3 = var1.replace("0".toRegex(), "1")
        var3 = var3.replace("x".toRegex(), "0")
        this.mask = Integer.parseInt(var3, 2)
        val var4 = var1.replace("x".toRegex(), "0")
        this.match = Integer.parseInt(var4, 2)
    }

    internal inner class Location {
        var valid = false
        var start = -1
        var end = -1
    }
}
