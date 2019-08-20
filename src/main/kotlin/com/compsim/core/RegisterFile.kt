package com.compsim.core

import com.adlerd.compsim.CompSim
import com.adlerd.compsim.util.exceptions.IllegalMemAccessException
import javax.swing.table.AbstractTableModel

class RegisterFile(private val machine: Machine) : AbstractTableModel() {
    private val colNames = arrayOf("Register", "Value", "Register", "Value")
    private val PC: Word
    private val MPR: Word
    private val PSR: Word
    private val MCR: Word
    private val regArr: Array<Word> = Array(8) { Word() }
    private var dirty: Boolean = false
    var mostRecentlyWrittenValue: Int = 0
        private set

    val isDirty: Boolean
        get() {
            val var1 = this.dirty
            this.dirty = false
            return var1
        }

    var pc: Int
        get() = this.PC.value
        set(value) {
            val var2 = this.PC.value
            this.PC.value = value
            this.fireTableCellUpdated(indRow[8], indCol[8])
            this.machine.memory.fireTableRowsUpdated(var2, var2)
            this.machine.memory.fireTableRowsUpdated(value, value)
        }

    val n: Boolean
        get() = this.PSR.getBit(2) == 1

    val z: Boolean
        get() = this.PSR.getBit(1) == 1

    val p: Boolean
        get() = this.PSR.getBit(0) == 1

    var privMode: Boolean
        get() = this.PSR.getBit(15) == 1
        set(var1) {
            var var2 = this.PSR.value
            if (!var1) {
                var2 = var2 and 32767
            } else {
                var2 = var2 or 32768
            }

            this.psr = var2
        }

    var psr: Int
        get() = this.PSR.value
        set(var1) {
            this.PSR.value = var1
            this.fireTableCellUpdated(indRow[10], indCol[10])
            this.fireTableCellUpdated(indRow[11], indCol[11])
        }

    //  It looks like in Unicode-16BE and Unicode-32BE the decimal form of '耀' is 32768
    var clockMCR: Boolean
        get() = this.mcr and '耀'.toInt() != 0
        set(var1) = if (var1) {
            this.mcr = this.MCR.value or '耀'.toInt()
        } else {
            this.mcr = this.MCR.value and 32767
        }

    var mcr: Int
        get() = this.MCR.value
        set(var1) {
            this.MCR.value = var1
        }

    var mpr: Int
        get() = this.MPR.value
        set(var1) {
            this.MPR.value = var1
            this.fireTableCellUpdated(indRow[9], indCol[9])
        }

    init {
        if (!CompSim.isLC3) {
            indNames[11] = ""
        }

        for (var2 in 0..7) {
            this.regArr[var2] = Word()
        }

        this.PC = Word()
        this.MPR = Word()
        this.MCR = Word()
        this.PSR = Word()
        this.reset()
    }

    fun reset() {
        for (var1 in 0..7) {
            this.regArr[var1].value = 0
        }

        this.PC.value = 512
        this.MPR.value = 0
        this.MCR.value = 32768
        this.PSR.value = 2
        this.privMode = true
        this.fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return 6
    }

    override fun getColumnCount(): Int {
        return this.colNames.size
    }

    override fun getColumnName(var1: Int): String {
        return this.colNames[var1]
    }

    override fun isCellEditable(var1: Int, var2: Int): Boolean {
        return var2 == 1 || var2 == 3
    }

    override fun getValueAt(var1: Int, var2: Int): Any? {
        when (var2) {
            0 -> return indNames[var1]
            1 -> return this.regArr[var1].toHex()
            2 -> return indNames[var1 + 6]
            else -> {
                if (var2 == 3) {
                    if (var1 < 2) {
                        return this.regArr[var1 + 6].toHex()
                    }

                    if (var1 == 2) {
                        return this.PC.toHex()
                    }

                    if (var1 == 3) {
                        return this.MPR.toHex()
                    }

                    if (var1 == 4) {
                        return this.PSR.toHex()
                    }

                    if (var1 == 5) {
                        return if (CompSim.isLC3) {
                            this.printCC()
                        } else {
                            ""
                        }
                    }
                }

                return null
            }
        }
    }

    override fun setValueAt(var1: Any?, var2: Int, var3: Int) {
        if (var3 == 1) {
            this.regArr[var2].value = Word.parseNum(var1.toString())
        } else if (var3 == 3) {
            if (var2 < 2) {
                this.regArr[var2 + 6].value = Word.parseNum(var1.toString())
            } else {
                if (var2 == 5) {
                    this.setNZP(var1.toString())
                    return
                }

                if (var1 == null && var2 == 3) {
                    this.fireTableCellUpdated(var2, var3)
                    return
                }

                val var4 = Word.parseNum(var1.toString())
                if (var2 == 2) {
                    this.pc = var4
                    if (this.machine.LC3GUI != null) {
                        this.machine.LC3GUI!!.scrollToPC()
                    }
                } else if (var2 == 3) {
                    this.mpr = var4
                } else if (var2 == 4) {
                    this.psr = var4
                }
            }
        }

        this.fireTableCellUpdated(var2, var3)
    }

    fun incPC(var1: Int) {
        this.pc = this.PC.value + var1
    }

    @Throws(IndexOutOfBoundsException::class)
    fun printRegister(var1: Int): String {
        return if (var1 in 0..7) {
            this.regArr[var1].toHex()
        } else {
            throw IndexOutOfBoundsException("Register index must be from 0 to 7")
        }
    }

    @Throws(IndexOutOfBoundsException::class)
    fun getRegister(var1: Int): Int {
        return if (var1 in 0..7) {
            this.regArr[var1].value
        } else {
            throw IndexOutOfBoundsException("Register index must be from 0 to 7")
        }
    }

    fun setRegister(var1: Int, var2: Int) {
        if (var1 in 0..7) {
            this.dirty = true
            this.mostRecentlyWrittenValue = var2
            this.regArr[var1].value = var2
            this.fireTableCellUpdated(indRow[var1], indCol[var1])
        } else {
            throw IndexOutOfBoundsException("Register index must be from 0 to 7")
        }
    }

    @Throws(IllegalMemAccessException::class)
    fun checkAddr(address: Int) {
        val var2 = this.privMode
        if (address in 0..65535) {
            if (!var2) {
                val var3 = address shr 12
                val var4 = 1 shl var3
                val var5 = this.mpr
                if (var4 and var5 == 0) {
                    throw IllegalMemAccessException(address)
                }
            }
        } else {
            throw IllegalMemAccessException(address)
        }
    }

    @Throws(IllegalMemAccessException::class)
    fun checkAddr(address: Word) {
        this.checkAddr(address.value)
    }

    fun printCC(): String {
        return if (this.n xor this.z xor this.p && (!this.n || !this.z || !this.p)) {
            if (this.n) {
                "N"
            } else if (this.z) {
                "Z"
            } else {
                if (this.p) "P" else "unset"
            }
        } else {
            "invalid"
        }
    }

    fun setNZP(var1: Int) {
        var var1 = var1
        var var2 = this.PSR.value
        var2 = var2 and -8
        var1 = var1 and 65535
        var2 = when {
            var1 and '耀'.toInt() != 0 -> var2 or 4
            var1 == 0 -> var2 or 2
            else -> var2 or 1
        }

        this.psr = var2
    }

    fun setNZP(var1: String) {
        var var2 = var1
        var2 = var2.toLowerCase().trim { it <= ' ' }
        if (var2 != "n" && var2 != "z" && var2 != "p") {
            Console.println("Condition codes must be set assemble one of `n', `z' or `p'")
        } else {
            when (var2) {
                "n" -> this.setN()
                "z" -> this.setZ()
                else -> this.setP()
            }

        }
    }

    fun setN() {
        this.setNZP(32768)
    }

    fun setZ() {
        this.setNZP(0)
    }

    fun setP() {
        this.setNZP(1)
    }

    override fun toString(): String {
        var string = "["

        for (var2 in 0..7) {
            string += "R$var2: ${this.regArr[var2].toHex()}" + if (var2 != 7) "," else ""
        }

        string = "$string]"
        string += "\nPC = ${this.PC.toHex()}"
        string += "\nMPR = ${this.MPR.toHex()}"
        string += "\nPSR = ${this.PSR.toHex()}"
        string += "\nCC = ${this.printCC()}"
        return string
    }

    companion object {
        val NUM_REGISTERS = 8
        private val NUM_ROWS = 12
        private val PC_ROW = 8
        private val MPR_ROW = 9
        private val PSR_ROW = 10
        private val CC_ROW = 11
        private val indNames = arrayOf("R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "PC", "MPR", "PSR", "CC")
        private val indRow = intArrayOf(0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5)
        private val indCol = intArrayOf(1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3)

        fun isLegalRegister(var0: Int): Boolean {
            return var0 in 0..8
        }
    }
}
