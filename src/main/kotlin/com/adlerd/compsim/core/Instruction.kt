package com.adlerd.compsim.core

import AsException
import java.util.*
import java.util.regex.Pattern

class Instruction(private var instructionLine: String, private var lineNumber: Int) {
    var originalLine = ""
        private set
    var format = ""
        private set
    var address: Int = 0
    var opcode: String? = null
        private set
    var label: String? = null
        private set
    var labelRef: String? = null
        private set
    private val regs = Vector<Int>()
    var stringz: String? = null
        private set
    var offsetImmediate: Int? = null
        @Throws(AsException::class)
        get() = if (field == null) {
            throw AsException(this, "Internal error: no offset/immediate when expected")
        } else {
            field
        }


    init {
        this.originalLine = instructionLine
        val commentPos = instructionLine.indexOf(';')
        if (commentPos != -1) {
            instructionLine = instructionLine.substring(0, commentPos)
        }

        instructionLine = instructionLine.replace("\\\"", "\u0000")
        val pattern = Pattern.compile("([^\"]*)[\"]([^\"]*)[\"](.*)").matcher(instructionLine)
        if (pattern.matches()) {
            this.stringz = pattern.group(2)
            this.stringz = this.stringz!!.replace("\u0000", "\"")
            this.stringz = this.stringz!!.replace("\\n", "\n")
            this.stringz = this.stringz!!.replace("\\t", "\t")
            this.stringz = this.stringz!!.replace("\\0", "\u0000")
            instructionLine = "${pattern.group(1)} ${pattern.group(3)}"
        }

        instructionLine = instructionLine.toUpperCase()
        instructionLine = instructionLine.replace(",", " ")
        instructionLine = instructionLine.trim { it <= ' ' }
        if (instructionLine.isNotEmpty()) {
            val instArray = instructionLine.split("[\\s]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (index in instArray.indices) {
                var opcode = instArray[index]
                if (ISA.isOpcode(opcode)) {
                    this.opcode = opcode
                    this.format += "$opcode "
                } else if (opcode.matches("[#]?[-]?[\\d]+".toRegex())) {
                    opcode = opcode.replace("#", "")
                    this.offsetImmediate = Integer.parseInt(opcode, 10)
                    this.format += "Num "
                } else if (opcode.matches("[B][01]+".toRegex())) {
                    opcode = opcode.replace("B", "")
                    this.offsetImmediate = Integer.parseInt(opcode, 2)
                    this.format += "Num "
                } else if (opcode.matches("[0]?[X][ABCDEF\\d]+".toRegex())) {
                    opcode = opcode.replace("0X", "")
                    opcode = opcode.replace("X", "")
                    this.offsetImmediate = Integer.parseInt(opcode, 16)
                    this.format += "Num "
                } else if (opcode.matches("R[\\d]+".toRegex())) {
                    opcode = opcode.replace("R", "")
                    this.regs.add(Integer.parseInt(opcode, 10))
                    this.format += "Reg "
                } else if (index == 0 && opcode.matches("[\\w_][\\w_\\d]*[:]?".toRegex())) {
                    opcode = opcode.replace(":", "")
                    this.label = opcode
                } else {
                    if (index == 0 || !opcode.matches("[\\w_][\\w_\\d]*".toRegex())) {
                        throw AsException(
                            this,
                            "Unrecognizable token: `$opcode` on line  $lineNumber ($index  ${this.originalLine})\n"
                        )
                    }

                    this.labelRef = opcode
                    this.format += "Label "
                }
            }

            if (this.stringz != null) {
                this.format += "String"
            }

            this.format = this.format.trim { it <= ' ' }
            if (this.opcode == null) {
                if (this.format.isNotEmpty()) {
                    throw AsException(this, "Unexpected instruction format")
                }
            } else {
                ISA.checkFormat(this, this.lineNumber)
            }

        }
    }

    fun getRegs(var1: Int): Int {
        return this.regs[var1]
    }

    /**
     *  Function to throw Assembly Exceptions
     */
    @Throws(AsException::class)
    fun error(message: String) {
        throw AsException(this, message)
    }

    @Throws(AsException::class)
    fun splitLabels(instructionList: MutableList<Instruction>) {
        if (this.opcode != null || this.label != null) {
            if (this.opcode != null && this.label != null) {
                instructionList.add(Instruction(this.label!!, this.lineNumber))
                this.label = null
                instructionList.add(this)
            } else {
                instructionList.add(this)
            }
        }

    }
}
