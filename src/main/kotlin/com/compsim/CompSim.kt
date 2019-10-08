package com.compsim

import com.compsim.core.CommandLine
import com.compsim.core.Controller
import com.compsim.gui.fx.CompSimGUI
import com.compsim.gui.fxml.CompSimTestGUI
import com.compsim.util.ErrorLog
import com.compsim.util.Logger.errorln
import com.compsim.util.Logger.infoln
import com.compsim.util.exceptions.GenericException
import javafx.application.Application
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader

object CompSim {
    const val TITLE = "CompSim"
    const val VERSION = "1.0.0"
    var isGraphical = true
    var isSwing = false
    var isLC3 = true
    var isP37X = false
    private var isFXML = false
    private var isPipelined = false

    val isa: String?
        get() = if (isLC3) {
            "LC3 ISA"
        } else {
            if (isP37X) "P37X ISA"
            else null
        }

    var version = "$TITLE Version $VERSION"
        private set

    private fun printUsage() {
        println("\nUsage: java CompSim [-lc3] [-p37x] [-pipeline] [-t | -fxml] [-s <script>]")
        println("\t-lc3 : simulate the LC-3 ISA")
        println("\t-p37x : simulate the P37X ISA")
        println("\t-pipeline : simulate a 5-stage fully-bypassed pipeline")
        println("\t-t : start in command-line mode")
        println("\t-fxml : launch experimental FXML GUI")
        println("\t-s script : run 'script' from a script file")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var str: String? = null
        infoln(version)

        var i = 0
        while (i < args.size) {
            when {
                args[i].equals("-t", ignoreCase = true) -> isGraphical = false
                args[i].equals("-fxml", ignoreCase = true) -> isFXML = true
                args[i].equals("-s", ignoreCase = true) -> {
                    ++i
                    if (i >= args.size) {
                        errorln("-s requires a script filename", false)
                        return
                    }

                    str = args[i]
                }
                args[i].equals("-lc3", ignoreCase = true) -> isLC3 = true
                args[i].equals("-p37x", ignoreCase = true) -> isP37X = true
                else -> {
                    if (!args[i].equals("-pipeline", ignoreCase = true)) {
                        errorln("Arg '${args[i]}' not recognized")
                        printUsage()
                        return
                    }

                    isPipelined = true
                }
            }
            ++i
        }

        if (isLC3 && isP37X) {
            errorln("can't specify more than one ISA", false)
            printUsage()
        } else if (!isLC3 && !isP37X) {
            errorln("ISA not specified", false)
            printUsage()
        } else {
            infoln("$isa")
            val machine = Controller()
            val commandLine = CommandLine(machine)
            if (str != null) {
                commandLine.scheduleCommand("@script $str")
            }

            if (isGraphical) {
                when {
                    isFXML -> {
                        infoln("Loading experimental JavaFX FXML graphical interface")
                        Application.launch(CompSimTestGUI::class.java)
                        infoln("GUI closed...")
                    }
                    else -> {
                        infoln("Loading JavaFX graphical interface")
                        Application.launch(CompSimGUI::class.java)
                        infoln("GUI closed...")
                    }
                }
            } else {
                try {
                    val reader = BufferedReader(InputStreamReader(System.`in`) as Reader)
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

                        while (commandLine.hasMoreCommands && (!machine.isContinueMode || commandLine.isStopQueued)) {
                            val command = commandLine.nextCommand
                            if (str != null && !command.startsWith("@")) {
                                str = null
                            }

                            consoleOutput = try {
                                commandLine.runCommand(command)
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

                        if (str != null && !commandLine.hasMoreCommands) {
                            str = null
                        }
                    }
                } catch (ioException: IOException) {
                    errorln(ioException.message, false)
                    ErrorLog.logError(ioException as Exception)
                }

            }

        }
    }
}
