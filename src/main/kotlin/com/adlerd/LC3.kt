package com.adlerd

import IllegalInstructionException
import com.adlerd.util.exceptions.IllegalMemAccessException

class LC3 : ISA() {
    public override fun init() {
        super.init()
        createDef("ADD", "0001 ddd sss 0 00 ttt", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word))
                registerFile.setRegister(this.getDReg(word), var6)
                registerFile.setNZP(var6)
                return var2 + 1
            }
        })
        createDef("SUB", "0001 ddd sss 0 10 ttt", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word))
                registerFile.setRegister(this.getDReg(word), var6)
                registerFile.setNZP(var6)
                return var2 + 1
            }
        })
        createDef("ADD", "0001 ddd sss 1 iiiii", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val register = registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word)
                registerFile.setRegister(this.getDReg(word), register)
                registerFile.setNZP(register)
                return var2 + 1
            }
        })
        createDef("AND", "0101 ddd sss 0 00 ttt", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val register =
                    registerFile.getRegister(this.getSReg(word)) and registerFile.getRegister(this.getTReg(word))
                registerFile.setRegister(this.getDReg(word), register)
                registerFile.setNZP(register)
                return var2 + 1
            }
        })
        createDef("AND", "0101 ddd sss 1 iiiii", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val register = registerFile.getRegister(this.getSReg(word)) and this.getSignedImmed(word)
                registerFile.setRegister(this.getDReg(word), register)
                registerFile.setNZP(register)
                return var2 + 1
            }
        })
        createDef("BR", "0000 111 ppppppppp", BranchDef())
        createDef("BRnzp", "0000 111 ppppppppp", BranchDef())
        createDef("BRp", "0000 001 ppppppppp", BranchDef())
        createDef("BRz", "0000 010 ppppppppp", BranchDef())
        createDef("BRzp", "0000 011 ppppppppp", BranchDef())
        createDef("BRn", "0000 100 ppppppppp", BranchDef())
        createDef("BRnp", "0000 101 ppppppppp", BranchDef())
        createDef("BRnz", "0000 110 ppppppppp", BranchDef())
        createDef("RET", "1100 000 111 000000", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                return registerFile.getRegister(7)
            }
        })
        createDef("JMP", "1100 000 ddd 000000", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                return registerFile.getRegister(this.getDReg(word))
            }
        })
        createDef("RTT", "1100 000 111 000001", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                registerFile.privMode = false
                return registerFile.getRegister(7)
            }
        })
        createDef("JMPT", "1100 000 ddd 000001", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                registerFile.privMode = false
                return registerFile.getRegister(this.getDReg(word))
            }
        })
        createDef("JSR", "0100 1 ppppppppppp", object : InstructionDef() {
            override val isCall: Boolean
                get() = true

            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                registerFile.setRegister(7, var2 + 1)
                return var2 + 1 + this.getPCOffset(word)
            }
        })
        createDef("JSRR", "0100 000 ddd 000000", object : InstructionDef() {
            override val isCall: Boolean
                get() = true

            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getDReg(word))
                registerFile.setRegister(7, var2 + 1)
                return var6
            }
        })
        createDef("LD", "0010 ddd ppppppppp", object : InstructionDef() {
            override val isLoad: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return var2 + 1 + this.getPCOffset(word)
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
                registerFile.setRegister(this.getDReg(word), var6)
                registerFile.setNZP(var6)
                return var2 + 1
            }
        })
        createDef("LDI", "1010 ddd ppppppppp", object : InstructionDef() {
            override val isLoad: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
                val var7 = memory.checkAndRead(var6)!!.value
                registerFile.setRegister(this.getDReg(word), var7)
                registerFile.setNZP(var7)
                return var2 + 1
            }
        })
        createDef("LDR", "0110 ddd sss iiiiii", object : InstructionDef() {
            override val isLoad: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word)
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 =
                    memory.checkAndRead(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word))!!.value
                registerFile.setRegister(this.getDReg(word), var6)
                registerFile.setNZP(var6)
                return var2 + 1
            }
        })
        createDef("LEA", "1110 ddd ppppppppp", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                registerFile.setRegister(this.getDReg(word), var2 + 1 + this.getPCOffset(word))
                registerFile.setNZP(var2 + 1 + this.getPCOffset(word))
                return var2 + 1
            }
        })
        createDef("ST", "0011 ddd ppppppppp", object : InstructionDef() {
            override val isStore: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return var2 + 1 + this.getPCOffset(word)
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getDReg(word))
                memory.checkAndWrite(var2 + 1 + this.getPCOffset(word), var6)
                return var2 + 1
            }
        })
        createDef("STI", "1011 ddd ppppppppp", object : InstructionDef() {
            override val isStore: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
                val register = registerFile.getRegister(this.getDReg(word))
                memory.checkAndWrite(var6, register)
                return var2 + 1
            }
        })
        createDef("STR", "0111 ddd sss iiiiii", object : InstructionDef() {
            override val isStore: Boolean
                get() = true

            @Throws(IllegalMemAccessException::class)
            override fun getRefAddr(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory
            ): Int {
                return registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word)
            }

            @Throws(IllegalMemAccessException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getDReg(word))
                memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), var6)
                return var2 + 1
            }
        })
        createDef("NOT", "1001 ddd sss 111111", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val register = registerFile.getRegister(this.getSReg(word)).inv()
                registerFile.setRegister(this.getDReg(word), register)
                registerFile.setNZP(register)
                return var2 + 1
            }
        })
        createDef("MUL", "1101 ddd sss 0 00 ttt", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val register =
                    registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word))
                registerFile.setRegister(this.getDReg(word), register)
                registerFile.setNZP(register)
                return var2 + 1
            }
        })
        createDef("MUL", "1101 ddd sss 1 iiiii", object : InstructionDef() {
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                val var6 = registerFile.getRegister(this.getSReg(word)) * this.getSignedImmed(word)
                registerFile.setRegister(this.getDReg(word), var6)
                registerFile.setNZP(var6)
                return var2 + 1
            }
        })
        createDef("RTI", "1000 000000000000", object : InstructionDef() {
            @Throws(IllegalInstructionException::class)
            override fun execute(
                word: Word,
                var2: Int,
                registerFile: RegisterFile,
                memory: Memory,
                machine: Machine
            ): Int {
                if (registerFile.privMode) {
                    val var6 = registerFile.getRegister(6)
                    val var7 = memory.read(var6)!!.value
                    registerFile.setRegister(6, var6 + 1)
                    val var8 = memory.read(var6)!!.value
                    registerFile.psr = var8
                    return var7
                } else {
                    throw IllegalInstructionException("RTI can only be executed in privileged mode")
                }
            }
        })
        createDef("GETC", "1111 0000 00100000", TrapDef())
        createDef("OUT", "1111 0000 00100001", TrapDef())
        createDef("PUTS", "1111 0000 00100010", TrapDef())
        createDef("IN", "1111 0000 00100011", TrapDef())
        createDef("PUTSP", "1111 0000 00100100", TrapDef())
        createDef("HALT", "1111 0000 00100101", TrapDef())
        createDef("TRAP", "1111 0000 uuuuuuuu", TrapDef())
    }

    private inner class TrapDef internal constructor() : InstructionDef() {

        override val isCall: Boolean
            get() = true

        @Throws(IllegalMemAccessException::class, IllegalInstructionException::class)
        override fun execute(var1: Word, var2: Int, var3: RegisterFile, var4: Memory, var5: Machine): Int {
            var3.privMode = true
            var3.setRegister(7, var2 + 1)
            return var4.read(var1.getZext(8, 0))!!.value
        }

        // $FF: synthetic method
        internal constructor(var2: Any) : this()
    }

    private inner class BranchDef internal constructor() : InstructionDef() {

        override val isBranch: Boolean
            get() = true

        @Throws(IllegalMemAccessException::class, IllegalInstructionException::class)
        override fun execute(word: Word, var2: Int, registerFile: RegisterFile, memory: Memory, machine: Machine): Int {
            return if ((word.getBit(11) != 1 || !registerFile.n) && (word.getBit(10) != 1 || !registerFile.z) && (word.getBit(
                    9
                ) != 1 || !registerFile.p)
            ) var2 + 1 else var2 + 1 + this.getPCOffset(
                word
            )
        }

        // $FF: synthetic method
        internal constructor(var2: Any) : this()
    }
}
