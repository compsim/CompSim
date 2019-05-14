package com.adlerd

import com.adlerd.util.exceptions.GenericException
import com.adlerd.gui.fx.CompSimGUI
import com.adlerd.gui.swing.LC3GUI
import com.adlerd.util.ErrorLog
import com.adlerd.util.Logger.infoln
import javafx.application.Application
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.swing.SwingUtilities

object CompSim {
    const val TITLE = "CompSim"
    const val VERSION = "1.0.0"
    var isGraphical = true
    var isSwing = false
    var isPipelined = false
    var isLC3 = true
    var isP37X = false

    val isa: String?
        get() = if (isLC3) {
            "LC3 ISA"
        } else {
            if (isP37X) "P37X ISA"
            else null
        }

    fun getVersion(): String {
        return "$TITLE Version $VERSION"
    }

    private fun printUsage() {
        println("\nUsage: java com.adlerd.CompSim [-lc3] [-p37x] [-pipeline] [-t] [-s <script>]")
        println("  -lc3 : simulate the LC-3 com.adlerd.ISA")
        println("  -p37x : simulate the com.adlerd.P37X com.adlerd.ISA")
        println("  -pipeline : simulate a 5-stage fully-bypassed pipeline")
        println("  -t : start in command-line mode")
        println("  -s script : run 'script' from a script file")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var str: String? = null
        println("${getVersion()}\n")

        var i = 0
        while (i < args.size) {
            when {
                args[i].equals("-t", ignoreCase = true) -> isGraphical = false
                args[i].equals("-s", ignoreCase = true) -> {
                    ++i
                    if (i >= args.size) {
                        println("Error: -s requires a script filename")
                        return
                    }

                    str = args[i]
                }
                args[i].equals("-lc3", ignoreCase = true) -> isLC3 = true
                args[i].equals("-p37x", ignoreCase = true) -> isP37X = true
                else -> {
                    if (!args[i].equals("-pipeline", ignoreCase = true)) {
                        println("Arg '" + args[i] + "' not recognized")
                        printUsage()
                        return
                    }

                    isPipelined = true
                }
            }
            ++i
        }

        if (isLC3 && isP37X) {
            println("Error: can't specify more than one com.adlerd.ISA")
            printUsage()
        } else if (!isLC3 && !isP37X) {
            println("Error: ISA not specified")
            printUsage()
        } else {
            println(isa)
            val machine = Machine()
            val commandLine = CommandLine(machine)
            if (str != null) {
                commandLine.scheduleCommand("@script $str")
            }

            if (isGraphical) {
                if (isSwing) {
                    println("Loading graphical interface\n")
                    LC3GUI.initLookAndFeel()
                    val gui = LC3GUI(machine, commandLine)
                    machine.LC3GUI = gui
                    SwingUtilities.invokeLater(TempRun(gui))
                } else {
                    Application.launch(CompSimGUI::class.java)
                    infoln("GUI closed...")
                }
            } else {
                try {
                    val reader = BufferedReader(InputStreamReader(System.`in`))
                    var consoleOutput: String

                    while (true) {
                        if (!machine.isContinueMode) {
                            print(CommandLine.PROMPT)
                        }

                        if (str == null) {
                            val line = reader.readLine()
                            if (line != null) {
                                commandLine.scheduleCommand(line)
                            }
                        }

                        while (commandLine.hasMoreCommands() && (!machine.isContinueMode || commandLine.hasQueuedStop())) {
                            val var7 = commandLine.getNextCommand()
                            if (str != null && !var7.startsWith("@")) {
                                str = null
                            }

                            consoleOutput = try {
                                commandLine.runCommand(var7)
                            } catch (genericException: GenericException) {
                                genericException.exceptionDescription
                            } catch (numberFormatException: NumberFormatException) {
                                "NumberFormatException: ${numberFormatException.message}"
                            }

                            if (consoleOutput == null) {
                                machine.cleanup()
                                println("Bye!")
                                return
                            }

                            println(consoleOutput)
                        }

                        if (str != null && !commandLine.hasMoreCommands()) {
                            str = null
                        }
                    }
                } catch (var11: IOException) {
                    ErrorLog.logError(var11 as Exception)
                }

            }

        }
    }
}
