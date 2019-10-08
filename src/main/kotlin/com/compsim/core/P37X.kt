package com.compsim.core

import com.compsim.util.exceptions.IllegalMemAccessException

class P37X : ISA() {
    public override fun init() {
        super.init()
        createDef(
            "ADD",
            "0000 ddd sss ttt 100",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "SUB",
            "0000 ddd sss ttt 101",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "MUL",
            "0000 ddd sss ttt 110",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "OR",
            "0001 ddd sss ttt 000",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) or registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "NOT",
            "0001 ddd sss xxx 001",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getSReg(word)).inv()
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "AND",
            "0001 ddd sss ttt 010",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) and registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "XOR",
            "0001 ddd sss ttt 011",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) xor registerFile.getRegister(this.getTReg(word))
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "SLL",
            "0001 ddd sss ttt 100",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) shl (registerFile.getRegister(this.getTReg(word)) and 15)
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "SRL",
            "0001 ddd sss ttt 101",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getSReg(word))
                        .ushr(registerFile.getRegister(this.getTReg(word)) and 15)
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "SRA",
            "0001 ddd sss ttt 110",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getTReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 =
                        registerFile.getRegister(this.getSReg(word)) shr (registerFile.getRegister(this.getTReg(word)) and 15)
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef("GETC", "0010 0000 00100000", TrapDef())
        createDef("PUTC", "0010 0000 00100001", TrapDef())
        createDef("PUTS", "0010 0000 00100010", TrapDef())
        createDef("EGETC", "0010 0000 00100011", TrapDef())
        createDef("HALT", "0010 0000 00100101", TrapDef())
        createDef("TRAP", "0010 0000 uuuuuuuu", TrapDef())
        createDef(
            "RTT",
            "0011 ddd xxxxxxxxx",
            object : InstructionDef() {
                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    registerFile.privMode = false
                    return registerFile.getRegister(this.getDReg(word))
                }
            })
        createDef(
            "JUMP",
            "0100 pppppppppppp",
            object : InstructionDef() {
                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    return var2 + 1 + this.getPCOffset(word)
                }
            })
        createDef(
            "JUMPR",
            "0101 ddd xxxxxxxxx",
            object : InstructionDef() {
                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    return registerFile.getRegister(this.getDReg(word))
                }
            })
        createDef(
            "JSR",
            "0110 pppppppppppp",
            object : InstructionDef() {

                override val isCall: Boolean
                    get() = true

                override fun getDestinationReg(word: Word): Int {
                    return 7
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    registerFile.setRegister(7, var2 + 1)
                    return var2 + 1 + this.getPCOffset(word)
                }
            })
        createDef(
            "JSRR",
            "0111 ddd xxxxxxxxx",
            object : InstructionDef() {

                override val isCall: Boolean
                    get() = true

                override fun getDestinationReg(word: Word): Int {
                    return 7
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word))
                    registerFile.setRegister(7, var2 + 1)
                    return var6
                }
            })
        createDef(
            "NOOP",
            "1000 xxx 000 xxxxxx",
            object : InstructionDef() {
                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    return var2 + 1
                }
            })
        createDef(
            "BRP",
            "1000 ddd 001 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 != 0 && var6 and '耀'.toInt() == 0) var2 + 1 + this.getPCOffset(word) else var2 + 1
//                TODO: change char (above) to hardcoded Int (below)
//                 return if (var6 != 0 && var6 and 8000 == 0) var2 + 1 + this.getPCOffset(word) else var2 + 1
                }
            })
        createDef(
            "BRZ",
            "1000 ddd 010 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 == 0) var2 + 1 + this.getPCOffset(word) else var2 + 1
                }
            })
        createDef(
            "BRZP",
            "1000 ddd 011 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 != 0 && var6 and '耀'.toInt() != 0) var2 + 1 else var2 + 1 + this.getPCOffset(word)
                }
            })
        createDef(
            "BRN",
            "1000 ddd 100 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 and '耀'.toInt() != 0) var2 + 1 + this.getPCOffset(word) else var2 + 1
                }
            })
        createDef(
            "BRNP",
            "1000 ddd 101 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 != 0) var2 + 1 + this.getPCOffset(word) else var2 + 1
                }
            })
        createDef(
            "BRNZ",
            "1000 ddd 110 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) and '\uffff'.toInt()
                    return if (var6 != 0 && var6 and '耀'.toInt() == 0) var2 + 1 else var2 + 1 + this.getPCOffset(word)
                }
            })
        createDef(
            "BRNZP",
            "1000 ddd 111 pppppp",
            object : InstructionDef() {
                override val isBranch: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    return var2 + 1 + this.getPCOffset(word)
                }
            })
        createDef(
            "CONST",
            "1001 ddd iiiiiiiii",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    registerFile.setRegister(this.getDReg(word), this.getSignedImmed(word))
                    return var2 + 1
                }
            })
        createDef(
            "INC",
            "1010 ddd iiiiiiiii",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word)) + this.getSignedImmed(word)
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "LEA",
            "1011 ddd ppppppppp",
            object : InstructionDef() {
                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun execute(
                    word: Word,
                    var2: Int,
                    registerFile: RegisterFile,
                    memory: Memory,
                    controller: Controller
                ): Int {
                    registerFile.setRegister(this.getDReg(word), var2 + 1 + this.getPCOffset(word))
                    return var2 + 1
                }
            })
        createDef(
            "LDR",
            "1100 ddd sss iiiiii",
            object : InstructionDef() {
                override val isLoad: Boolean
                    get() = true

                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg1(word: Word): Int {
                    return this.getSReg(word)
                }

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
                    controller: Controller
                ): Int {
                    val var6 =
                        memory.checkAndRead(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word))!!.value
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "STR",
            "1101 ddd sss iiiiii",
            object : InstructionDef() {
                override val isStore: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

                override fun getSourceReg2(word: Word): Int {
                    return this.getSReg(word)
                }

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
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word))
                    memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "LD",
            "1110 ddd ppppppppp",
            object : InstructionDef() {
                override val isLoad: Boolean
                    get() = true

                override fun getDestinationReg(word: Word): Int {
                    return this.getDReg(word)
                }

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
                    controller: Controller
                ): Int {
                    val var6 = memory.checkAndRead(var2 + 1 + this.getPCOffset(word))!!.value
                    registerFile.setRegister(this.getDReg(word), var6)
                    return var2 + 1
                }
            })
        createDef(
            "ST",
            "1111 ddd ppppppppp",
            object : InstructionDef() {
                override val isStore: Boolean
                    get() = true

                override fun getSourceReg1(word: Word): Int {
                    return this.getDReg(word)
                }

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
                    controller: Controller
                ): Int {
                    val var6 = registerFile.getRegister(this.getDReg(word))
                    memory.checkAndWrite(var2 + 1 + this.getPCOffset(word), var6)
                    return var2 + 1
                }
            })
    }

    private inner class TrapDef internal constructor() : InstructionDef() {

        override val isCall: Boolean
            get() = true

        override fun execute(word: Word, var2: Int, registerFile: RegisterFile, memory: Memory, controller: Controller): Int {
            registerFile.privMode = true
            registerFile.setRegister(7, var2 + 1)
            return word.getZext(8, 0)
        }

        // $FF: synthetic method
        internal constructor(var2: Any) : this()
    }
}
