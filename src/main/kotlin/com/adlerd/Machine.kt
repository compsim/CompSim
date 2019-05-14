package com.adlerd

import IllegalInstructionException
import com.adlerd.Memory.Companion.MEM_SIZE
import com.adlerd.gui.fx.CompSimGUI
import com.adlerd.util.exceptions.GenericException
import com.adlerd.util.exceptions.IllegalMemAccessException
import com.adlerd.gui.swing.LC3GUI
import com.adlerd.helpers.MemoryRow
import com.adlerd.util.ErrorLog
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.awt.Container
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.*
import java.util.*
import javax.sound.midi.MetaMessage
import javax.swing.SwingUtilities
import kotlin.collections.ArrayList

class Machine : Runnable {

    val dataArrayList = ArrayList<MemoryRow>(MEM_SIZE)
    val data: ObservableList<MemoryRow>
    val memory: Memory
    val registers: RegisterFile
    val branchPredictor: BranchPredictor
    var LC3GUI: LC3GUI? = null
    val notifyOnStop: LinkedList<ActionListener> = LinkedList()
    var traceWriter: PrintWriter? = null
    val symbolTable = Hashtable<String, Int>()
    val inverseTable = Hashtable<Int, String>()
    val addrToInsnTable = Hashtable<Int, Boolean>()
    var cycleCount = 0
    var instructionCount = 0
    var loadStallCount = 0
    var branchStallCount = 0
    private var stopImmediately = false
    var isContinueMode = false
        private set
    val isTraceEnabled: Boolean
        get() = this.traceWriter != null

    init {
        if (CompSim.isP37X) {
            P37X().init()
        } else if (CompSim.isLC3) {
            LC3().init()
        }

//        this.data = ArrayList<MemoryRow>(MEM_SIZE) as ObservableList<MemoryRow>
        this.data = FXCollections.observableArrayList<MemoryRow>(dataArrayList)
        this.memory = Memory(this)
        this.registers = RegisterFile(this)
        this.branchPredictor = BranchPredictor(this, 8)
    }

    fun setStoppedListener(actionListener: ActionListener) {
        this.notifyOnStop.add(actionListener)
    }

    fun reset() {
        this.symbolTable.clear()
        this.inverseTable.clear()
        this.addrToInsnTable.clear()
        this.memory.reset()
        this.registers.reset()
        if (this.LC3GUI != null) {
            this.LC3GUI!!.reset()
        }

        if (this.isTraceEnabled) {
            this.disableTrace()
        }

        this.cycleCount = 0
        this.instructionCount = 0
        this.loadStallCount = 0
        this.branchStallCount = 0
    }

    fun cleanup() {
        ErrorLog.logClose()
        if (this.isTraceEnabled) {
            this.disableTrace()
        }

    }

    fun disableTrace() {
        this.traceWriter!!.close()
        this.traceWriter = null
    }

    fun loadSymbolTable(symbolFile: File): String {
        try {
            val reader = BufferedReader(FileReader(symbolFile))
            var var4 = 0

            while (reader.ready()) {
                val line = reader.readLine()
                ++var4
                if (var4 >= 5) {
                    val var6 = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (var6.size >= 3) {
                        val var7 = Word.parseNum("x" + var6[2])
                        if ("$" == var6[1]) {
                            this.addrToInsnTable[var7] = true
                        } else {
                            this.symbolTable[var6[1].toLowerCase()] = var7
                            this.inverseTable[var7] = var6[1]
                        }
                    }
                }
            }

            return "Loaded symbol symbolFile '${symbolFile.path}'"
        } catch (ioException: IOException) {
            return "Could not load symbol symbolFile '${symbolFile.path}'"
        }

    }

    fun setContinueMode() {
        this.isContinueMode = true
    }

    fun clearContinueMode() {
        this.isContinueMode = false
    }

    fun loadObjectFile(objectFile: File): String {
        val var2 = ByteArray(2)
        val var4 = objectFile.path
        if (!var4.endsWith(".obj")) {
            return "Error: object filename '$var4' does not end with .obj"
        } else {
            var var3: String
            try {
                val var5 = FileInputStream(objectFile)
                var5.read(var2)
                var var6 = Word.convertByteArray(var2[0], var2[1])

                while (true) {
                    if (var5.read(var2) != 2) {
                        var5.close()
                        var3 = "Loaded object file '$var4'"
                        break
                    }

                    val var7 = var6
                    if (this.symbolTable.contains(var7)) {
                        val var8 = this.inverseTable[var7] as String
                        this.symbolTable.remove(var8.toLowerCase())
                        this.inverseTable.remove(var7)
                    }

                    this.memory.write(var6, Word.convertByteArray(var2[0], var2[1]))
                    ++var6
                }
            } catch (var9: IOException) {
                return "Error: Could not load object file '$var4'"
            }

            var var10 = var4
            if (var4.endsWith(".obj")) {
                var10 = var4.substring(0, var4.length - 4)
            }

            var10 = "$var10.sym"
            var3 = var3 + "\n" + this.loadSymbolTable(File(var10))
            return var3
        }
    }

    fun setKeyboardInputStream(var1: File): String {
        var var2: String
        try {
            this.memory.keyBoardDevice.setInputStream(FileInputStream(var1))
            this.memory.keyBoardDevice.setInputMode(KeyboardDevice.SCRIPT_MODE)
            var2 = "Keyboard input file '" + var1.path + "' enabled"
            if (this.LC3GUI != null) {
                this.LC3GUI!!.setTextConsoleEnabled(false)
            }
        } catch (var4: FileNotFoundException) {
            var2 = "Could not open keyboard input file '" + var1.path + "'"
            if (this.LC3GUI != null) {
                this.LC3GUI!!.setTextConsoleEnabled(true)
            }
        }

        return var2
    }

    @Throws(GenericException::class)
    fun executeStep() {
        this.registers.clockMCR = true
        this.stopImmediately = false
        this.executePumpedContinues(1)
        this.updateStatusLabel()
        if (this.LC3GUI != null) {
            this.LC3GUI!!.scrollToPC(0)
        }

    }

    @Throws(GenericException::class)
    fun executeNext() {
        if (ISA.isCall(this.memory.read(this.registers.pc)!!)) {
            this.memory.setNextBreakPoint((this.registers.pc + 1) % 65536)
            this.executeMany()
        } else {
            this.executeStep()
        }

    }

    @Synchronized
    fun stopExecution(var1: Boolean): String {
        return this.stopExecution(0, var1)
    }

    @Synchronized
    fun stopExecution(var1: Int, var2: Boolean): String {
        this.stopImmediately = true
        this.clearContinueMode()
        this.updateStatusLabel()
        if (this.LC3GUI != null) {
            this.LC3GUI!!.scrollToPC(var1)
        }

        this.memory.fireTableDataChanged()
        if (var2) {
            val var3 = this.notifyOnStop.listIterator(0)

            while (var3.hasNext()) {
                var3.next().actionPerformed(null as ActionEvent?)
            }
        }

        return "Stopped at " + Word.toHex(this.registers.pc)
    }

    @Throws(GenericException::class)
    fun executePumpedContinues() {
        this.executePumpedContinues(400)
    }

    @Throws(GenericException::class)
    fun executePumpedContinues(var1: Int) {
        var var2 = var1
        this.registers.clockMCR = true
        if (this.LC3GUI != null) {
            this.LC3GUI!!.setStatusLabelRunning()
        }

        while (!this.stopImmediately && var2 > 0) {
            try {
                val var3 = this.registers.pc
                this.registers.checkAddr(var3)
                val var4 = this.memory.getInst(var3)
                val var5 = ISA.lookupTable[var4.value]
                    ?: throw IllegalInstructionException("Undefined instruction:  ${var4.toHex()}")

                val var6 = var5.execute(var4, var3, this.registers, this.memory, this)
                this.registers.pc = var6
                ++this.cycleCount
                ++this.instructionCount
                val var7 = this.branchPredictor.getPredictedPC(var3)
                if (var6 != var7) {
                    this.cycleCount += 2
                    this.branchStallCount += 2
                    this.branchPredictor.update(var3, var6)
                }

                if (var5.isLoad) {
                    val var8 = this.memory.getInst(var6)
                    val var9 = ISA.lookupTable[var8.value]
                        ?: throw IllegalInstructionException("Undefined instruction:  ${var8.toHex()}")

                    if (!var9.isStore) {
                        val var10 = var5.getDestinationReg(var4)
                        if (var10 >= 0 && (var10 == var9.getSourceReg1(var8) || var10 == var9.getSourceReg2(var8))) {
                            ++this.cycleCount
                            ++this.loadStallCount
                        }
                    }
                }

                if (this.isTraceEnabled) {
                    this.generateTrace(var5, var3, var4)
                }

                if (this.memory.isBreakPointSet(this.registers.pc)) {
                    val var12 = "Hit breakpoint at ${Word.toHex(this.registers.pc)}"
                    Console.println(var12)
                    this.stopExecution(true)
                }

                if (this.memory.isNextBreakPointSet(this.registers.pc)) {
                    this.stopExecution(true)
                    this.memory.clearNextBreakPoint(this.registers.pc)
                }

                --var2
            } catch (var11: GenericException) {
                this.stopExecution(true)
                throw var11
            }

        }

        if (this.isContinueMode) {
            SwingUtilities.invokeLater(this)
        }

    }

    @Synchronized
    @Throws(GenericException::class)
    fun executeMany() {
        this.setContinueMode()
        this.stopImmediately = false

        try {
            this.executePumpedContinues()
        } catch (genericException: GenericException) {
            this.stopExecution(true)
            throw genericException
        }

    }

    @Throws(IllegalMemAccessException::class)
    fun generateTrace(var1: InstructionDef, var2: Int, var3: Word) {
        if (this.isTraceEnabled && this.traceWriter != null) {
            val printWriter = this.traceWriter!!
            printWriter.print(Word.toHex(var2, false))
            printWriter.print(" ")
            printWriter.print(var3.toHex(false))
            printWriter.print(" ")
            if (this.registers.isDirty) {
                printWriter.print(Word.toHex(1, false))
                printWriter.print(" ")
                printWriter.print(Word.toHex(this.registers.mostRecentlyWrittenValue, false))
            } else {
                printWriter.print(Word.toHex(0, false))
                printWriter.print(" ")
                printWriter.print(Word.toHex(0, false))
            }

            printWriter.print(" ")
            if (var1.isStore) {
                printWriter.print(Word.toHex(1, false))
                printWriter.print(" ")
                printWriter.print(
                    Word.toHex(
                        var1.getRefAddr(var3, var2, this.registers, this.memory),
                        false
                    )
                )
                printWriter.print(" ")
                printWriter.print(
                    Word.toHex(
                        this.registers.getRegister(var1.getDReg(var3)),
                        false
                    )
                )
            } else {
                printWriter.print(Word.toHex(0, false))
                printWriter.print(" ")
                printWriter.print(Word.toHex(0, false))
                printWriter.print(" ")
                printWriter.print(Word.toHex(0, false))
            }

            printWriter.println(" ")
            printWriter.flush()
        }

    }

    fun lookupSym(position: Int): String? {
        return this.inverseTable[position]
    }

    fun lookupSym(symbol: String): Int {
        val var2 = this.symbolTable[symbol.toLowerCase()]
        return var2 ?: Integer.MAX_VALUE
    }

    fun lookupAddrToInsn(var1: Int): Boolean {
        return this.addrToInsnTable[var1] != null
    }

    fun existSym(var1: String): Boolean {
        return this.symbolTable[var1.toLowerCase()] != null
    }

    fun getAddress(symbol: String): Int {
        var var2 = Word.parseNum(symbol)
        if (var2 == Integer.MAX_VALUE) {
            var2 = this.lookupSym(symbol)
        }

        return var2
    }

    override fun run() {
        try {
            this.executePumpedContinues()
        } catch (genericException: GenericException) {
            if (this.LC3GUI != null) {
                genericException.showMessageDialog((null as Container))
            }
            Console.println(genericException.message!!)
        }

    }

    fun updateStatusLabel() {
        if (this.LC3GUI != null) {
            if (!this.registers.clockMCR) {
                this.LC3GUI!!.setStatusLabelHalted()
            } else if (this.isContinueMode) {
                this.LC3GUI!!.setStatusLabelRunning()
            } else {
                this.LC3GUI!!.setStatusLabelSuspended()
            }
        }

    }

    companion object {
        val NUM_CONTINUES = 400
    }
}
