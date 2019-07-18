package com.adlerd.compsim.core

import IllegalInstructionException
import com.adlerd.compsim.CompSim
import com.adlerd.compsim.core.Memory.Companion.MEM_SIZE
import com.adlerd.compsim.gui.swing.LC3GUI
import com.adlerd.compsim.helpers.MemoryRow
import com.adlerd.compsim.util.ErrorLog
import com.adlerd.compsim.util.exceptions.GenericException
import com.adlerd.compsim.util.exceptions.IllegalMemAccessException
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.awt.Container
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.*
import java.util.*
import javax.swing.SwingUtilities
import kotlin.collections.ArrayList

open class Machine : Runnable {

    private val dataArrayList = ArrayList<MemoryRow>(MEM_SIZE)
    val data: ObservableList<MemoryRow>
    val memory: Memory
    val registers: RegisterFile
    val branchPredictor: BranchPredictor
    var LC3GUI: LC3GUI? = null
    val notifyOnStop: LinkedList<ActionListener> = LinkedList()
    var traceWriter: PrintWriter? = null
    val symbolTable = Hashtable<String, Int>()
    val inverseTable = Hashtable<Int, String>()
    val addressToInstructionTable = Hashtable<Int, Boolean>()
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

        // Observable list of memory rows where all of the memory in the memory table is stored
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
        this.addressToInstructionTable.clear()
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
                        val var7 = Word.parseNum("x${var6[2]}")
                        if ("$" == var6[1]) {
                            this.addressToInstructionTable[var7] = true
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
        val fileHeader = ByteArray(2)
        val path = objectFile.path
        if (!path.endsWith(".obj")) {
            return "Error: object filename '$path' does not end with .obj"
        } else {
            var result: String
            try {
                val inputStream = FileInputStream(objectFile)
                inputStream.read(fileHeader)
                var var6 = Word.convertByteArray(fileHeader[0], fileHeader[1])

                while (true) {
                    if (inputStream.read(fileHeader) != 2) {
                        inputStream.close()
                        result = "Loaded object file '$path'"
                        break
                    }

                    val var7 = var6
                    if (this.symbolTable.contains(var7)) {
                        val var8 = this.inverseTable[var7] as String
                        this.symbolTable.remove(var8.toLowerCase())
                        this.inverseTable.remove(var7)
                    }

                    this.memory.write(var6, Word.convertByteArray(fileHeader[0], fileHeader[1]))
                    ++var6
                }
            } catch (var9: IOException) {
                return "Error: Could not load object file '$path'"
            }

            var var10 = path
            if (path.endsWith(".obj")) {
                var10 = path.substring(0, path.length - 4)
            }

            var10 = "$var10.sym"
            result = "$result\n${this.loadSymbolTable(File(var10))}"
            return result
        }
    }

    fun setKeyboardInputStream(var1: File): String {
        var var2: String
        try {
            this.memory.keyBoardDevice.setInputStream(FileInputStream(var1))
            this.memory.keyBoardDevice.setInputMode(KeyboardDevice.SCRIPT_MODE)
            var2 = "Keyboard input file '${var1.path}' enabled"
            if (this.LC3GUI != null) {
                this.LC3GUI!!.setTextConsoleEnabled(false)
            }
        } catch (var4: FileNotFoundException) {
            var2 = "Could not open keyboard input file '${var1.path}'"
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

        return "Stopped at ${Word.toHex(this.registers.pc)}"
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
                    ?: throw IllegalInstructionException("Undefined instruction: ${var4.toHex()}")

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
                        ?: throw IllegalInstructionException("Undefined instruction: ${var8.toHex()}")

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
                printWriter.print(
                    Word.toHex(
                        this.registers.mostRecentlyWrittenValue,
                        false
                    )
                )
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

    fun lookupAddressToInstruction(address: Int): Boolean {
        return this.addressToInstructionTable[address] != null
    }

    fun existSym(symbol: String): Boolean {
        return this.symbolTable[symbol.toLowerCase()] != null
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
        const val NUM_CONTINUES = 400
    }
}
