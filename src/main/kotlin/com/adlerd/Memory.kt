package com.adlerd

import com.adlerd.util.TimerDevice
import com.adlerd.util.exceptions.IllegalMemAccessException
import javax.swing.table.AbstractTableModel

class Memory(private val machine: Machine) : AbstractTableModel() {
    private val memoryArray = Array(65536) { Word() }
    private val colNames = arrayOf("BP", "Address", "Value", "com.adlerd.Instruction")
    private val nextBreakPoints = BooleanArray(65536)
    private val breakPoints = BooleanArray(65536)
    val keyBoardDevice = KeyboardDevice()
    val monitorDevice = MonitorDevice()
    private val timerDevice = TimerDevice()

    init {

        for (var2 in 0..65535) {
            this.memoryArray[var2] = Word()
            this.breakPoints[var2] = false
        }

        this.timerDevice.setTimer()
    }

    fun reset() {
        for (var1 in 0..65535) {
            this.memoryArray[var1].reset()
        }

        this.keyBoardDevice.reset()
        this.monitorDevice.reset()
        this.timerDevice.reset()
        this.clearAllBreakPoints()
        this.fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return this.memoryArray.size
    }

    override fun getColumnCount(): Int {
        return this.colNames.size
    }

    override fun getColumnName(var1: Int): String {
        return this.colNames[var1]
    }

    override fun isCellEditable(var1: Int, var2: Int): Boolean {
        return (var2 == 2 || var2 == 0) && var1 < 65024
    }

    fun isBreakPointSet(var1: Int): Boolean {
        return this.breakPoints[var1]
    }

    fun setBreakPoint(var1: String): String {
        val var3 = this.machine.getAddress(var1)
        var var2: String
        if (var3 != Integer.MAX_VALUE) {
            var2 = this.setBreakPoint(var3)
            if (this.machine.existSym(var1)) {
                var2 = "$var2 ('$var1')"
            }
        } else {
            var2 = "Error: Invalid address or label ('$var1')"
        }

        return var2
    }

    fun setBreakPoint(var1: Int): String {
        if (var1 >= 0 && var1 < 65536) {
            this.breakPoints[var1] = true
            this.fireTableCellUpdated(var1, -1)
            return "Breakpoint set at " + Word.toHex(var1)
        } else {
            return "Error: Invalid address or label"
        }
    }

    fun clearBreakPoint(var1: String): String {
        val var3 = this.machine.getAddress(var1)
        var var2: String
        if (var3 != Integer.MAX_VALUE) {
            var2 = this.clearBreakPoint(var3)
            if (this.machine.existSym(var1)) {
                var2 = "$var2 ('$var1')"
            }
        } else {
            var2 = "Error: Invalid address or label ('$var1')"
        }

        return var2
    }

    fun clearBreakPoint(var1: Int): String {
        if (var1 >= 0 && var1 < 65536) {
            this.breakPoints[var1] = false
            this.fireTableCellUpdated(var1, -1)
            return "Breakpoint cleared at " + Word.toHex(var1)
        } else {
            return "Error: Invalid address or label"
        }
    }

    fun clearAllBreakPoints() {
        for (var1 in 0..65535) {
            this.breakPoints[var1] = false
            this.nextBreakPoints[var1] = false
        }

    }

    fun setNextBreakPoint(var1: Int) {
        assert(0 <= var1 && var1 < 65536)

        this.nextBreakPoints[var1] = true
    }

    fun isNextBreakPointSet(var1: Int): Boolean {
        assert(0 <= var1 && var1 < 65536)

        return this.nextBreakPoints[var1]
    }

    fun clearNextBreakPoint(var1: Int) {
        assert(0 <= var1 && var1 < 65536)

        this.nextBreakPoints[var1] = false
    }

    override fun getValueAt(var1: Int, var2: Int): Any? {
        var var3: Any? = null
        when (var2) {
            0 -> var3 = this.isBreakPointSet(var1)
            1 -> {
                var3 = Word.toHex(var1)
                val var4 = this.machine.lookupSym(var1)
                if (var4 != null) {
                    var3 = "${"$var3 "}$var4"
                }
            }
            2 -> var3 = if (var1 < 65024) {
                this.memoryArray[var1].toHex()
            } else {
                "???"
            }
            3 -> var3 = if (var1 < 65024) {
                ISA.disassemble(this.memoryArray[var1], var1, this.machine)
            } else {
                "Use 'list' to query"
            }
        }

        return var3
    }

    fun getInst(address: Int): Word {
        return this.memoryArray[address]
    }

    @Throws(IllegalMemAccessException::class)
    fun checkAndRead(address: Int): Word? {
        this.machine.registers.checkAddr(address)
        return this.read(address)
    }

    fun read(var1: Int): Word? {
        val var2: Word?
        when (var1) {
            65024 -> var2 = this.keyBoardDevice.status()
            65026 -> var2 = this.keyBoardDevice.read()
            65028 -> var2 = this.monitorDevice.status()
            65032 -> var2 = this.timerDevice.status()
            65034 -> var2 = Word(this.timerDevice.interval.toInt())
            65042 -> var2 = Word(this.machine.registers.mpr)
            65534 -> var2 = Word(this.machine.registers.mcr)
            else -> {
                if (var1 < 0 || var1 >= 65536) {
                    return null
                }

                var2 = this.memoryArray[var1]
            }
        }

        return var2
    }

    override fun setValueAt(var1: Any?, var2: Int, var3: Int) {
        if (var3 == 2) {
            this.write(var2, Word.parseNum((var1 as String?)!!))
            this.fireTableCellUpdated(var2, var3)
        }

        if (var3 == 0) {
            if ((var1 as Boolean?)!!) {
                Console.println(this.setBreakPoint(var2))
            } else {
                Console.println(this.clearBreakPoint(var2))
            }

        }
    }

    @Throws(IllegalMemAccessException::class)
    fun checkAndWrite(var1: Int, var2: Int) {
        this.machine.registers.checkAddr(var1)
        this.write(var1, var2)
    }

    fun write(var1: Int, var2: Int) {
        when (var1) {
            65030 -> {
                this.monitorDevice.write(var2.toChar())
                this.fireTableCellUpdated(var1, 3)
            }
            65034 -> {
                this.timerDevice.setTimer(var2.toLong())
                if (var2 == 0) {
                    this.timerDevice.isEnabled = false
                } else {
                    this.timerDevice.isEnabled = true
                    if (var2 == 1) {
                        this.timerDevice.setTimer(this.keyBoardDevice)
                    }
                }
            }
            65042 -> this.machine.registers.mpr = var2
            65534 -> {
                this.machine.registers.mcr = var2
                if (var2 and '耀'.toInt() == 0) {
                    this.machine.stopExecution(1, true)
                } else {
                    this.machine.updateStatusLabel()
                }
            }
        }

        this.memoryArray[var1].value = var2
        this.fireTableCellUpdated(var1, 3)
    }

    companion object {
        val MEM_SIZE = 65536
        val BEGIN_DEVICE_REGISTERS = 65024
        val KBSR = 65024
        val KBDR = 65026
        val DSR = 65028
        val DDR = 65030
        val TMR = 65032
        val TMI = 65034
        val DISABLE_TIMER = 0
        val MANUAL_TIMER_MODE = 1
        val MPR = 65042
        val MCR = 65534
        val BREAKPOINT_COLUMN = 0
        val ADDRESS_COLUMN = 1
        val VALUE_COLUMN = 2
        val INSN_COLUMN = 3
    }
}
