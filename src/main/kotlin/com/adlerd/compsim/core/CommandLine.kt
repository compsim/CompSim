package com.adlerd.compsim.core

import AsException
import com.adlerd.compsim.CompSim
import com.adlerd.compsim.gui.swing.LC3GUI
import com.adlerd.compsim.util.ErrorLog
import com.adlerd.compsim.util.exceptions.GenericException
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class CommandLine(val machine: Machine) {

    private val commandQueue = LinkedList<String>()
    private val prevHistoryStack = Stack<String>()
    private val nextHistoryStack = Stack<String>()
    private val commands = Hashtable<String, Command>()
    private val commandsSet: TreeSet<Command>
    private var checksPassed = 0
    private var checksFailed = 0
    private var checksPassedCumulative = 0
    private var checksFailedCumulative = 0
    lateinit var LC3GUI: LC3GUI

    init {
        // IMPORTANT:
        //  Add all commands to the commands HashTable.
        //  Without this the commands won't load and will not function.
        this.setupCommands()

        // Init the CommandSet TreeSet
        this.commandsSet = TreeSet(object : Comparator<Command> {
            override fun compare(command1: Command, command2: Command): Int {
                val command1str = command1.usage.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val command2str = command2.usage.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                return command1str.compareTo(command2str)
            }

            override fun equals(other: Any?): Boolean {
                val otherObject = (other as Command).usage.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val command = (this as Command).usage.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                return otherObject == command
            }
        })
        this.commandsSet.addAll(this.commands.values)
    }

    /**
     * Schedule command into the CommandQueue
     * @param command is the command to be scheduled
     */
    fun scheduleCommand(command: String) {
        if (command.equals("stop", ignoreCase = true)) {
            this.commandQueue.addFirst(command)
        } else {
            this.commandQueue.add(command)
        }
    }

    /**
     * Schedule script commands in the CommandQueue
     * @param input is the list of commands from the script
     */
    fun scheduleScriptCommands(input: ArrayList<String>) {
        val lines = input.listIterator(input.size)

        while (lines.hasPrevious()) {
            val line = lines.previous()
            this.commandQueue.addFirst(line)
        }
    }

    /**
     * check if the CommandQueue has more commands
     */
    val hasMoreCommands: Boolean
        get() = this.commandQueue.size != 0

    /**
     * Return the next command from the CommandQueue
     */
    val nextCommand: String
        get() = this.commandQueue.removeFirst() as String

    /**
     * Check if the "stop" command is queued
     */
    val isStopQueued: Boolean
        get() = (this.commandQueue.first as String).equals("stop", ignoreCase = true)

    /**
     * Add to the HistoryStack
     * @param command is the command to be added to the HistoryStack
     */
    fun addToHistory(command: String) {
        if (this.prevHistoryStack.empty()) {
            this.prevHistoryStack.push(command)
        } else if (this.prevHistoryStack.peek() != command) {
            this.prevHistoryStack.push(command)
        }

    }

    /**
     * Return the previous command in the HistoryStack
     */
    val prevHistory: String?
        get() {
            return if (this.prevHistoryStack.empty()) {
                null
            } else {
                val var1 = this.prevHistoryStack.pop() as String
                this.nextHistoryStack.push(var1)
                var1
            }
        }

    /**
     * Return the next command in the HistoryStack
     */
    val nextHistory: String?
        get() {
            return if (this.nextHistoryStack.empty()) {
                null
            } else {
                val var1 = this.nextHistoryStack.pop() as String
                this.prevHistoryStack.push(var1)
                var1
            }
        }

    /**
     * Function to reset the HistoryStack
     */
    private fun resetHistoryStack() {
        while (!this.nextHistoryStack.empty()) {
            this.prevHistoryStack.push(this.nextHistoryStack.pop())
        }

    }

    /**
     *  Function which holds all commands for the program
     *  Each command should be self exaplanatory and more information will be found in the command section of the wiki
     *
     *  Additional commands can be added using the tutorial in the wiki
     */
    private fun setupCommands() {
        this.commands["help"] = object : Command {
            override val usage: String
                get() = "h[elp] [command]"

            override val help: String
                get() = "Print out help for all available commands, or for just a specified command."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                when {
                    argSize > 2 -> return this.usage
                    argSize != 1 -> {
                        val localCommand = this@CommandLine.commands[argArray[1].toLowerCase()] as Command
                        return if (localCommand == null) {
                            "${argArray[1]}: command not found"
                        } else {
                            "usage: ${localCommand.usage}\n${localCommand.help}"
                        }
                    }
                    else -> {
                        var output = ""

                        val cmdIterator = this@CommandLine.commandsSet.iterator()
                        while (cmdIterator.hasNext()) {
                            val command = cmdIterator.next()
                            val usage = command.usage
                            val cmd = usage.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                            output += "$cmd usage: $usage\n"
                        }

                        return output
                    }
                }
            }
        }
        this.commands["h"] = this.commands["help"]
        this.commands["quit"] = object : Command {
            override val usage: String
                get() = "quit"

            override val help: String
                get() = "Quit the simulator."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                if (argSize != 1)
                    return this.usage
                else
                    exitProcess(-1)
            }
        }
        this.commands["next"] = object : Command {
            override val usage: String
                get() = "n[ext]"
            override val help: String
                get() = "Executes the next instruction."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return if (argSize != 1) {
                    this.usage
                } else {
                    this@CommandLine.machine.executeNext()
                    ""
                }
            }
        }
        this.commands["n"] = this.commands["next"]
        this.commands["step"] = object : Command {
            override val usage: String
                get() = "s[tep]"
            override val help: String
                get() = "Steps into the next instruction."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                this@CommandLine.machine.executeStep()
                return ""
            }

        }
        this.commands["s"] = this.commands["step"]
        this.commands["continue"] = object : Command {
            override val usage: String
                get() = "c[ontinue]"
            override val help: String
                get() = "Continues running instructions until the next breakpoint is hit."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                Console.println("use the 'stop' command to interrupt execution")
                this@CommandLine.machine.executeMany()
                return ""
            }

        }
        this.commands["c"] = this.commands["continue"]
        this.commands["stop"] = object : Command {
            override val usage: String
                get() = "stop"
            override val help: String
                get() = "Stops execution temporarily."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return this@CommandLine.machine.stopExecution(true)
            }

        }
        this.commands["reset"] = object : Command {
            override val usage: String
                get() = "reset"
            override val help: String
                get() = "Reset the machine and simulator."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                when {
                    argSize != 1 -> return this.usage
                    else -> {
                        this@CommandLine.machine.stopExecution(false)
                        this@CommandLine.machine.reset()
                        this@CommandLine.checksPassed = 0
                        this@CommandLine.checksFailed = 0
                        return "System reset..."
                    }
                }
            }

        }
        this.commands["print"] = object : Command {
            override val usage: String
                get() = "p[rint]"
            override val help: String
                get() = "Prints out all registers, PC, MPR and PSR."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return when {
                    argSize != 1 -> this.usage
                    else -> this@CommandLine.machine.registers.toString()
                }
            }

        }
        this.commands["p"] = this.commands["print"]
        this.commands["input"] = object : Command {
            override val usage: String
                get() = "input <filename>"
            override val help: String
                get() = "Specifies a file to read the input from instead of keyboard device (simulator must be restarted to restore normal keyboard input)."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return when {
                    argSize != 2 -> this.usage
                    else -> {
                        val inputFile = File(argArray[1])
                        if (inputFile.exists())
                            this@CommandLine.machine.setKeyboardInputStream(inputFile)
                        else
                            "Error: file ${argArray[1]} does not exist."
                    }
                }
            }

        }
        this.commands["break"] = object : Command {
            override val usage: String
                get() = "b[reak] [ set | clear ] [ mem_addr | label ]"
            override val help: String
                get() = "Sets or clears break point at specified memory address or label."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return when {
                    argSize != 3 -> this.usage
                    argArray[1].toLowerCase().equals(
                        "set",
                        ignoreCase = true
                    ) -> this@CommandLine.machine.memory.setBreakPoint(argArray[2])
                    argArray[1].toLowerCase().equals(
                        "clear",
                        ignoreCase = true
                    ) -> this@CommandLine.machine.memory.clearBreakPoint(argArray[2])
                    else -> this.usage
                }
            }

        }
        this.commands["b"] = this.commands["break"]
        this.commands["script"] = object : Command {
            override val usage: String
                get() = "script <filename>"
            override val help: String
                get() = "Specifies a file from which to read commands."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                when {
                    argSize != 2 -> return this.usage
                    else -> {
                        val scriptFile = File(argArray[1])
                        try {
                            val reader = BufferedReader(FileReader(scriptFile))
                            val lines = ArrayList<String>()

                            while (true) {
                                val line = reader.readLine()
                                if (line != null) {
                                    this@CommandLine.scheduleScriptCommands(lines)
                                    return ""
                                }

                                lines.add("@$line")
                            }
                        } catch (ioException: IOException) {
                            return "${ioException.message}"
                        }
                    }
                }
            }

        }
        this.commands["load"] = object : Command {
            override val usage: String
                get() = "l[oa]d <filename>"
            override val help: String
                get() = "Loads an object file into the memory."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return when {
                    argSize != 2 -> this.usage
                    else -> this@CommandLine.machine.loadObjectFile(File(argArray[1]))
                }
            }

        }
        this.commands["ld"] = this.commands["load"]
        this.commands["check"] = object : Command {
            override val usage: String
                get() = "check [ count | cumulative | reset | PC | reg | PSR | MPR | mem_addr | label | N | Z | P ] [ mem_addr | label ] [ value | label ]"
            override val help: String
                get() = "\tVerifies that a particular value resides in a register or in a memory location, or that a condition code is set.\n" +
                        "Samples:\n" +
                        "'check PC LABEL' checks if the PC points to wherever LABEL points.\n" +
                        "'check LABEL VALUE' checks if the value stored in memory at the location pointed to by LABEL is equal to VALUE.\n" +
                        "'check VALUE LABEL' checks if the value stored in memory at VALUE is equal to the location pointed to by LABEL (probably not very useful). To find out where a label points, use 'list' instead.\n"

            fun check(var1: Boolean, args: Array<String>, var3: String): String {
                val isTrue = "TRUE"
                val isFalse = "FALSE"
                var checkMsg = "("

                for (index in 0..args.size) {
                    checkMsg += args[index]
                    checkMsg += if (index == args.size - 1) ")" else " "
                }

                return if (var1) {
                    this@CommandLine.checksPassed++
                    this@CommandLine.checksPassedCumulative++
                    "$isTrue $checkMsg"
                } else {
                    this@CommandLine.checksFailed++
                    this@CommandLine.checksFailedCumulative++
                    "$isFalse $checkMsg (actual value: $var3)"
                }
            }

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                when (argSize) {
                    in 2..4 -> {
                        if (argSize == 2) {
                            val var12: String
                            when {
                                argArray[1].equals("count", ignoreCase = true) -> {
                                    var12 = if (this@CommandLine.checksPassed == 1) "check" else "checks"
                                    return "${this@CommandLine.checksPassed} $var12  passed, ${this@CommandLine.checksFailed} failed"
                                }
                                argArray[1].equals("cumulative", ignoreCase = true) -> {
                                    var12 = if (this@CommandLine.checksPassedCumulative == 1) "check" else "checks"
                                    return " -> ${this@CommandLine.checksPassedCumulative} $var12  passed, ${this@CommandLine.checksFailedCumulative} failed"
                                }
                                argArray[1].equals("reset", ignoreCase = true) -> {
                                    this@CommandLine.checksPassed = 0
                                    this@CommandLine.checksFailed = 0
                                    this@CommandLine.checksPassedCumulative = 0
                                    this@CommandLine.checksFailedCumulative = 0
                                    return "check counts reset"
                                }
                                else -> {
                                    val registers = this@CommandLine.machine.registers
                                    return if ((argArray[1].toLowerCase() != "n" || !registers.n) && (argArray[1].toLowerCase() != "z" || !registers.z) && (argArray[1].toLowerCase() != "p" || !registers.p))
                                        this.check(false, argArray, registers.printCC())
                                    else
                                        this.check(true, argArray, "")
                                }
                            }
                        } else {
                            var regValue = Word.parseNum(argArray[argSize - 1])
                            if (regValue == Int.MAX_VALUE) {
                                regValue = this@CommandLine.machine.lookupSym(argArray[argSize - 1])
                                if (regValue == Int.MAX_VALUE) {
                                    return "Bad value or label: ${argArray[argSize - 1]}"
                                }
                            }

                            val isRegister = this@CommandLine.checkRegister(argArray[1], regValue)
                            if (isRegister != null) {
                                return this.check(isRegister, argArray, this@CommandLine.getRegister(argArray[1]))
                            } else {
                                val startAddress = this@CommandLine.machine.getAddress(argArray[1])
                                when (startAddress) {
                                    Int.MAX_VALUE -> return "Bad register, value or label: ${argArray[1]}"
                                    in 0..65535 -> {
                                        val endAddress: Int
                                        if (argSize == 3) {
                                            endAddress = startAddress
                                        } else {
                                            endAddress = this@CommandLine.machine.getAddress(argArray[2])
                                            if (endAddress == Int.MAX_VALUE) {
                                                return "Bad register, value or label: ${argArray[1]}"
                                            }

                                            if (endAddress < 0 || endAddress >= 65536) {
                                                return "Address " + argArray[2] + " out of bounds"
                                            }

                                            if (endAddress < startAddress) {
                                                return "Second address in range (" + argArray[2] + ") must be >= first (" + argArray[1] + ")"
                                            }
                                        }

                                        var address: Word?
                                        var var8 = true
                                        var var9 = ""

                                        for (index in startAddress..endAddress) {
                                            address = this@CommandLine.machine.memory.read(index)
                                            if (address == null) {
                                                return "Bad register, value or label: ${argArray[1]}"
                                            }

                                            if (address.value != regValue and '\uffff'.toInt()) {
                                                var8 = false
                                                var9 += if (var9.isEmpty()) "" else ", "
                                                var9 += "$${Word.toHex(index)}:${address.toHex()}"
                                            }
                                        }

                                        return this.check(var8, argArray, var9)
                                    }
                                    else -> return "Address ${argArray[1]} out of bounds"
                                }
                            }
                        }
                    }
                    else -> return this.usage
                }
            }

        }
        this.commands["dump"] = object : Command {
            override val usage: String
                get() = "d[ump] [-check | -coe | -readmemh | -disasm] from_mem_addr to_mem_addr dumpfile"
            override val help: String
                get() = "dumps a range of memory values to a specified file assemble raw values.\n" +
                        "\t-check: dump assemble 'check' commands that can be run assemble an LC-3 script.\n" +
                        "\t-coe dump a Xilinx coregen image\n" +
                        "\t-readmemh dump a file readable by Verilog's \$readmemh() system task.\n" +
                        "\t-disasm dump disassembled instructions."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                var dunpFlag = 0
                if (argSize in 4..5) {
                    if (argSize == 5) {
                        dunpFlag = when {
                            argArray[1].equals("-check", true) -> 1
                            argArray[1].equals("-coe", true) -> 2
                            argArray[1].equals("-readmemh", true) -> 3
                            else -> {
                                if (argArray[1].equals("-disarm", true))
                                    return "Unrecognized flag: ${argArray[1]}\n${this.usage}"
                                4
                            }
                        }
                    }

                    val startAddress = this@CommandLine.machine.getAddress(argArray[argSize - 3])
                    val endAddress = this@CommandLine.machine.getAddress(argArray[argSize - 2])
                    if (startAddress == Int.MAX_VALUE) {
                        return "Error: Invalid register, address, or label  ('${argArray[argSize - 3]}')"
                    } else if (startAddress in 0..65535) {
                        if (endAddress == Int.MAX_VALUE) {
                            return "Error: Invalid register, address, or label  ('${argArray[argSize - 2]}')"
                        } else if (endAddress in 0..65535) {
                            if (endAddress < startAddress) {
                                return "Second address in range (${argArray[argSize - 2]}) must be >= first (${argArray[argSize - 3]})"
                            } else {
                                var word: Word?
                                val dumpFile = File(argArray[argSize - 1])

                                val writer: PrintWriter
                                try {
                                    if (!dumpFile.createNewFile()) {
                                        return "File ${argArray[argSize - 1]} already exists. Choose a different filename."
                                    }

                                    writer = PrintWriter(BufferedWriter(FileWriter(dumpFile)))
                                } catch (ioException: IOException) {
                                    ErrorLog.logError(ioException)
                                    return "Error opening file: ${dumpFile.name}"
                                }

                                if (dunpFlag == 2) {
                                    writer.println("MEMORY_INITIALIZATION_RADIX=2;")
                                    writer.println("MEMORY_INITIALIZATION_VECTOR=")
                                }

                                for (index in startAddress..endAddress) {
                                    word = this@CommandLine.machine.memory.read(index)
                                    if (word == null) {
                                        return "Bad register, value or label: ${argArray[argSize - 3]}"
                                    }

                                    when (dunpFlag) {
                                        0 -> writer.println(word.toHex())
                                        1 -> writer.println("check ${Word.toHex(index)} ${word.toHex()}")
                                        2 -> {
                                            if (index < endAddress) {
                                                writer.println("${word.toBinary().substring(1)},")
                                            } else {
                                                writer.println("${word.toBinary().substring(1)};")
                                            }
                                        }
                                        3 -> writer.println(word.toHex().substring(1))
                                        4 -> writer.println(
                                            ISA.disassemble(
                                                word,
                                                index,
                                                this@CommandLine.machine
                                            )
                                        )
                                        else -> assert(false) { "Invalid flag to `dump' command: ${argArray[1]}" }
                                    }
                                }

                                writer.close()
                                return "com.adlerd.compsim.Memory dumped."
                            }
                        } else {
                            return "Address ${argArray[argSize - 2]} out of bounds"
                        }
                    } else {
                        return "Address ${argArray[argSize - 3]} out of bounds"
                    }
                } else {
                    return this.usage
                }
            }

        }
        this.commands["d"] = this.commands["dump"]
        this.commands["trace"] = object : Command {
            override val usage: String
                get() = "trace [on <trace-file> | off]"
            override val help: String
                get() = "For each instruction executed, this command dumps a subset of processor state to a file, to create a trace that can be used to verify correctness of execution. The state consists of, in order, (1) PC, (2) current insn, (3) regfile write-enable, (4) regfile data in, (5) data memory write-enable, (6) data memory address, and (7) data memory data in. These values are written in hex to <trace-file>, one line for each instruction executed. Note that trace files can get very large very quickly!\n" +
                        "\tSometimes a signal may be a don't-care value - if we're not writing to the regfile, the `regfile data in' value is undefined - but the write-enable values should allow don't-care signals to be determined in all cases."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                if (argSize in 2..3) {
                    if (argSize == 3) {
                        if (!argArray[1].equals("on", ignoreCase = true)) {
                            return this.usage
                        } else if (this@CommandLine.machine.isTraceEnabled) {
                            return "Tracing is already on."
                        } else {
                            val file = File(argArray[argSize - 1])

                            val writer: PrintWriter
                            try {
                                if (!file.createNewFile()) {
                                    return "File ${argArray[argSize - 1]} already exists."
                                }

                                writer = PrintWriter(BufferedWriter(FileWriter(file)), true)
                            } catch (ioException: IOException) {
                                ErrorLog.logError(ioException)
                                return "Error opening file: ${file.name}"
                            }

                            this@CommandLine.machine.traceWriter = writer
                            return "Tracing is on."
                        }
                    } else {
                        assert(argSize == 2)

                        if (!argArray[1].equals("off", ignoreCase = true)) {
                            return this.usage
                        } else if (!this@CommandLine.machine.isTraceEnabled) {
                            return "Tracing is already off."
                        } else {
                            this@CommandLine.machine.traceWriter?.flush()
                            this@CommandLine.machine.traceWriter?.close()
                            this@CommandLine.machine.disableTrace()
                            return "tracing is off."
                        }
                    }
                } else {
                    return this.usage
                }
            }

        }
        this.commands["counters"] = object : Command {
            override val usage: String
                get() = "counters"
            override val help: String
                get() = "Print out values of internal performance counters."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return if (argSize != 1) {
                    this.usage
                } else {
                    var output = "Cycle count: ${this@CommandLine.machine.cycleCount}\n"
                    output += "com.adlerd.compsim.Instruction count: ${this@CommandLine.machine.instructionCount}\n"
                    output += "Load stall count: ${this@CommandLine.machine.loadStallCount}\n"
                    output += "Branch stall count: ${this@CommandLine.machine.branchStallCount}\n"
                    output
                }
            }

        }
        this.commands["set"] = object : Command {
            override val usage: String
                get() = "set [ PC | reg | PSR | MPR | mem_addr | label ] [ mem_addr | label ] [ value | N | Z | P ]"
            override val help: String
                get() = "Sets the value of a register/PC/PSR/label/memory location/memory range or set the condition codes individually."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                if (argSize in 2..4) {
                    if (argSize == 2) {
                        return setConditionCodes(argArray[1])
                    } else {
                        var symbol = Word.parseNum(argArray[argSize - 1])
                        if (symbol == Integer.MAX_VALUE) {
                            symbol = this@CommandLine.machine.lookupSym(argArray[argSize - 1])
                        }

                        if (symbol == Integer.MAX_VALUE) {
                            return "Error: Invalid value (${argArray[argSize - 1]})"
                        } else {
                            if (argSize == 3) {
                                val register = this@CommandLine.setRegister(argArray[1], symbol)
                                if (register != null) {
                                    return register
                                }
                            }

                            val startAddr = this@CommandLine.machine.getAddress(argArray[1])
                            if (startAddr == Integer.MAX_VALUE) {
                                return "Error: Invalid register, address, or label  ('${argArray[1]}')"
                            } else if (startAddr in 0..65535) {
                                val endAddr: Int
                                if (argSize == 3) {
                                    endAddr = startAddr
                                } else {
                                    endAddr = this@CommandLine.machine.getAddress(argArray[2])
                                    if (endAddr == Integer.MAX_VALUE) {
                                        return "Error: Invalid register, address, or label  ('${argArray[1]}')"
                                    }

                                    if (endAddr < 0 || endAddr >= 65536) {
                                        return "Address ${argArray[2]} out of bounds"
                                    }

                                    if (endAddr < startAddr) {
                                        return "Second address in range (${argArray[2]}) must be >= first (${argArray[1]})"
                                    }
                                }

                                for (address in startAddr..endAddr) {
                                    this@CommandLine.machine.memory.write(address, symbol)
                                }

                                return if (argSize == 3) "com.adlerd.compsim.Memory location ${Word.toHex(
                                    startAddr
                                )} updated to ${argArray[argSize - 1]}" else "com.adlerd.compsim.Memory locations ${Word.toHex(
                                    endAddr
                                )} to ${Word.toHex(endAddr)} updated to ${argArray[argSize - 1]}"
                            } else {
                                return "Address ${argArray[1]} out of bounds"
                            }
                        }
                    }
                } else {
                    return this.usage
                }
            }

        }
        this.commands["list"] = object : Command {
            override val usage: String
                get() = "l[ist] [ addr1 | label1 [addr2 | label2] ]"
            override val help: String
                get() = "Lists the contents of memory locations (default address is PC. Specify range by giving 2 arguments)."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                if (argSize > 3) {
                    return this.usage
                } else if (argSize == 1) {
                    this@CommandLine.scrollToPC()
                    return "${Word.toHex(this@CommandLine.machine.registers.pc)} : ${this@CommandLine.machine.memory.getInst(
                        this@CommandLine.machine.registers.pc
                    ).toHex()} : ${ISA.disassemble(
                        this@CommandLine.machine.memory.getInst(this@CommandLine.machine.registers.pc),
                        this@CommandLine.machine.registers.pc,
                        this@CommandLine.machine
                    )}"
                } else {
                    val endAddr: Int
                    if (argSize == 2) {
                        val register = this@CommandLine.getRegister(argArray[1])
                        if (register != null) {
                            return "${argArray[1]} : $register"
                        } else {
                            endAddr = this@CommandLine.machine.getAddress(argArray[1])
                            if (endAddr == Integer.MAX_VALUE) {
                                return "Error: Invalid address or label (${argArray[1]})"
                            } else {
                                if (CompSim.isGraphical && endAddr < 65024) {
                                    this@CommandLine.LC3GUI.scrollToIndex(endAddr)
                                }

                                return "${Word.toHex(endAddr)} : ${this@CommandLine.machine.memory.read(endAddr)!!.toHex()} : ${ISA.disassemble(
                                    this@CommandLine.machine.memory.read(endAddr)!!,
                                    endAddr,
                                    this@CommandLine.machine
                                )}"
                            }
                        }
                    } else {
                        val startAddr = this@CommandLine.machine.getAddress(argArray[1])
                        endAddr = this@CommandLine.machine.getAddress(argArray[2])
                        if (startAddr == Integer.MAX_VALUE) {
                            return "Error: Invalid address or label (${argArray[1]})"
                        } else if (endAddr == Integer.MAX_VALUE) {
                            return "Error: Invalid address or label (${argArray[2]})"
                        } else if (endAddr < startAddr) {
                            return "Error: addr2 should be larger than addr1"
                        } else {
                            val output = StringBuffer()

                            for (address in startAddr..endAddr) {
                                output.append(
                                    "${Word.toHex(address)} : ${this@CommandLine.machine.memory.read(address)!!.toHex()} : ${ISA.disassemble(
                                        this@CommandLine.machine.memory.read(address) as Word,
                                        address,
                                        this@CommandLine.machine
                                    )}"
                                )
                                if (address != endAddr) {
                                    output.append("\n")
                                }
                            }

                            if (CompSim.isGraphical) {
                                this@CommandLine.LC3GUI.scrollToIndex(startAddr)
                            }

                            return String(output)
                        }
                    }
                }
            }

        }
        this.commands["l"] = this.commands["list"]
        this.commands["assemble"] = object : Command {
            override val usage: String
                get() = "assemble [-warn] <filename>"
            override val help: String
                get() = "Assembles <filename> showing errors and (optionally) warnings, and leaves a .obj file in the same directory."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                if (argSize in 2..3) {
                    val stringArr = Array(argSize - 1) { String() }
                    var var4 = ""
                    stringArr[0] = argArray[1]
                    var4 += argArray[1]
                    if (argSize == 3) {
                        stringArr[1] = argArray[2]
                        var4 += " ${argArray[2]}"
                    }

                    val assembler = Assembler()
                    var numWarnings = ""

                    try {
                        numWarnings = assembler.assemble(stringArr)
                        if (numWarnings.isNotEmpty()) {
                            return "$numWarnings Warnings encountered during assembly (but assembly completed w/o errors)."
                        }
                    } catch (asException: AsException) {
                        return "${asException.message!!}\nErrors encountered during assembly."
                    }

                    return "Assembly of '$var4' completed without errors or warnings."
                } else {
                    return this.usage
                }
            }

        }
        this.commands["as"] = this.commands["assemble"]
        this.commands["clear"] = object : Command {
            override val usage: String
                get() = "clear"
            override val help: String
                get() = "Clears the commandline output window. Available only in LC3GUI mode."

            override fun doCommand(argArray: Array<String>, argSize: Int): String {
                return if (CompSim.isGraphical) {
                    Console.clear()
                    ""
                } else {
                    "Error: clear is only available in LC3GUI mode"
                }
            }

        }
    }

    /**
     * Function to run each command in the program
     * @param command is the command to be run
     * @return the output of the command
     * @throws GenericException
     */
    @Throws(GenericException::class, NumberFormatException::class)
    fun runCommand(command: String?): String {
        var cmd = command
        if (cmd == null) {
            return ""
        } else {
            if (!cmd.startsWith("@")) {
                this.resetHistoryStack()
                this.addToHistory(cmd)
            } else {
                cmd = cmd.replaceFirst("^@".toRegex(), "")
            }

            var cmdArgs = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var argSize = cmdArgs.size
            if (argSize == 0) {
                return ""
            } else {
                val commandText = cmdArgs[0].toLowerCase()
                if (commandText == "") {
                    return ""
                } else {
                    var index = -1

                    for (i in cmdArgs.indices) {
                        if (cmdArgs[i].startsWith("#")) {
                            index = i
                            break
                        }
                    }

                    if (index == 0) {
                        return ""
                    } else {
                        if (index >= 0) {
//                            val var8 = arrayOfNulls<String>(index) as Array<String>
                            val var8 = Array(index) { String() }

                            for (i in 0 until index) {
                                var8[i] = cmdArgs[i]
                            }

                            cmdArgs = var8
                            argSize = var8.size
                        }

                        val commandToBeRun = this.commands[commandText]
                        return commandToBeRun?.doCommand(cmdArgs, argSize) ?: "Unknown command: $commandText"
                    }
                }
            }
        }
    }

    /**
     * Scroll the memory pane to the PC location
     */
    fun scrollToPC() {
        if (CompSim.isGraphical) {
            if (CompSim.isSwing) {
                this.LC3GUI.scrollToPC()
            } else {
                // TODO
//                this.machine.scrollToPC()
            }
        }
    }

    /**
     * Sets a register to the given value
     * @param register register to be set
     * @param regValue value to be written to the register
     * @return update success or error message
     */
    fun setRegister(register: String, regValue: Int): String? {
        var update = "Register ${register.toUpperCase()} updated to regValue ${Word.toHex(
            regValue
        )}"
        when {
            register.equals("pc", ignoreCase = true) -> {
                this.machine.registers.pc = regValue
                this.scrollToPC()
            }
            register.equals("psr", ignoreCase = true) -> this.machine.registers.psr = regValue
            register.equals("mpr", ignoreCase = true) -> {
                val memory = this.machine.memory
                this.machine.memory
                memory.write(65042, regValue)
            }
            register.startsWith("r", ignoreCase = true) && register.length == 2 -> {
                val reg = register.substring(1, 2).toInt()
                this.machine.registers.setRegister(reg, regValue)
            }
            else -> update = "Error: Register ${register.toUpperCase()} failed to update"
        }
        return update
    }

    /**
     * Sets the PSR register's value
     * @param psrValue the value (N, Z, or P) for the PSR register
     * @return a message about success or failure to set the PSR
     */
    fun setConditionCodes(psrValue: String): String {
        return when {
            psrValue.equals("n", ignoreCase = true) -> {
                this.machine.registers.setN()
                "PSR N bit set"
            }
            psrValue.equals("z", ignoreCase = true) -> {
                this.machine.registers.setZ()
                "PSR Z bit set"
            }
            psrValue.equals("p", ignoreCase = true) -> {
                this.machine.registers.setP()
                "PSR P bit set"
            }
            else -> "Error: PSR bit $psrValue failed to update"
        }
    }

    /**
     * Get the hex value of a register
     *
     * @param register the register you are querying
     * @return the current value of the register
     */
    fun getRegister(register: String): String {
        val regValue = when {
            register.equals("pc", ignoreCase = true) -> this.machine.registers.pc
            register.equals("psr", ignoreCase = true) -> this.machine.registers.psr
            register.equals("mpr", ignoreCase = true) -> this.machine.registers.mpr
            // TODO: See if the following if statement can be flipped for less complexity
            else -> {
                if (!register.startsWith("r", ignoreCase = true) || register.length != 2) {
                    return ""
                }
                val reg = register.substring(1, 2).toInt()
                this.machine.registers.getRegister(reg)
            }
        }

        return Word.toHex(regValue)
    }

    /**
     * Checks if a register contains a given value
     *
     * @param register register to be queried
     * @param expectedValue the value that the register is being compared to
     * @return true if the register contains the expected value or false if otherwise
     */
    fun checkRegister(register: String, expectedValue: Int): Boolean? {
        val registerValue = Word.parseNum(this.getRegister(register))
        return if (registerValue == Integer.MAX_VALUE) {
            println("Error: Value of $register is out of bounds.")
//            false
            null
        } else {
            val expected = Word(expectedValue)
            registerValue == expected.value
        }
    }

    /**
     * Reset the check stats
     */
    fun reset() {
        this.checksPassed = 0
        this.checksFailed = 0
    }

    companion object {
        val NEWLINE = System.getProperty("line.separator")
        val PROMPT = "$NEWLINE>> "
    }

    /**
     * Interface which dictates the required fields for every command
     */
    private interface Command {

        /**
         * Description of how the command is to be used
         */
        val usage: String

        /**
         * Help line for the command
         */
        val help: String

        /**
         * Function which holds code for the command's action
         *
         * @param argArray the command entered converted into an array
         * @param argSize the size of 'argArray'
         * @return the output of the command
         */
        @Throws(GenericException::class)
        fun doCommand(argArray: Array<String>, argSize: Int): String
    }
}
