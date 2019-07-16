package com.adlerd.compsim.gui.fx.layout

import com.adlerd.compsim.core.CommandLine
import com.adlerd.compsim.core.Console
import com.adlerd.compsim.core.Machine
import com.adlerd.compsim.util.exceptions.GenericException
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.system.exitProcess

class ConsolePane(private val machine: Machine): TitledPane(), Console.PrintableConsole {
    private val consoleVBox = VBox()
    private val commandLine: CommandLine
    private val inputField = TextField()
    private val outputArea = TextArea()

    init {
        this.text = "Console"
        this.isCollapsible = true
        this.isExpanded = false
        this.isAnimated = true

//        this.minHeight = 200.0
//        this.prefHeight = 200.0

        VBox.setVgrow(outputArea, Priority.ALWAYS)
        VBox.setVgrow(inputField, Priority.NEVER)

        // TODO: Fix issue where outputArea and ScrollPane will not fill right
        this.commandLine = CommandLine(machine)

        inputField.onKeyPressed = EventHandler { keyEvent ->
            var input: String
            if (keyEvent.code == KeyCode.ENTER) {
                if (inputField.text.isNotBlank()) {
                    input = inputField.text
                    this.commandLine.scheduleCommand(input)

                    while (this.commandLine.hasMoreCommands && (!this.machine.isContinueMode || this.commandLine.isStopQueued)) {
                        try {
                            input = this.commandLine.runCommand(this.commandLine.nextCommand)
                            if (input != null) {
                                if (input.isNotEmpty()) {
                                    Console.println(input)
                                }
                            } else {
                                // TODO
//                                this.confirmExit()
                                exitProcess(1)
                            }
                        } catch (genericException: GenericException) {
//                            genericException.showMessageDialog()
                            genericException.printStackTrace()
                        }
                    }
//                    writeToConsole(inputField.text)
                     inputField.clear()
                }
            }
            if (keyEvent.code == KeyCode.UP) {
                inputField.text = commandLine.prevHistory
            }
            if (keyEvent.code == KeyCode.DOWN) {
                inputField.text = commandLine.nextHistory
            }
        }
        // Add event filter to consume the mouse click making focusing on the text area impossible
        outputArea.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
//            infoln("Mouse click in command output window consumed")
            event.consume()
        }
        outputArea.isFocusTraversable = false
        outputArea.isEditable = false
        outputArea.isWrapText = false
//        outputArea.minHeight = 150.0
//        outputArea.prefHeight = 150.0
        consoleVBox.children.addAll(
                outputArea
                ,
                inputField
        )

        this.content = consoleVBox
//        this.spacing = 4.0
    }


    override fun print(message: String) {
        outputArea.appendText("$message\n")
//        CommandOutputWindowPane.writeToConsole()
    }

    override fun clear() {
        outputArea.clear()
//        CommandOutputWindowPane.clearConsole()
    }


//    override fun handle(event: ActionEvent) {
//        var str = inputField.text
//        this.commandLine.scheduleCommand(str)
//
//        while (this.commandLine.hasMoreCommands() && (!machine.isContinueMode || commandLine.isStopQueued())) {
//            try {
//                str = this.commandLine.runCommand(commandLine.nextCommand)
//
//                if (str.isNotBlank()) {
//                    writeToConsole(str)
//                } else {
//                    errorln("Failed to write to com.adlerd.compsim.Console!")
//                }
//            } catch (localCustomException: CustomException) {
//                writeToConsole(localCustomException.message.toString())
//            }
//        }
//        inputField.selectAll()
//        outputArea.positionCaret(outputArea.paragraphs.size)
//    }
}