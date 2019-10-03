package com.compsim.gui.fx.layout

import com.compsim.core.CommandLine
import com.compsim.core.Console
import com.compsim.core.Machine
import com.compsim.util.exceptions.GenericException
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.system.exitProcess

class ConsolePane(private val machine: Machine): TitledPane(), Console.PrintableConsole {
    private val commandLine: CommandLine
    private val consoleVBox = VBox()
    private val inputField = TextField()
    private val outputArea = TextArea()

    init {
        this.text = "Console"
        this.isCollapsible = true
        this.isExpanded = true
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
        // Add event filter to consume the mouse click and focus on the input text field thus
        // making focusing on the output text area impossible.
        outputArea.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            inputField.requestFocus()
            event.consume()
        }

        outputArea.isFocusTraversable = false
        outputArea.isEditable = false
        outputArea.isWrapText = false
//        outputArea.minHeight = 150.0
//        outputArea.prefHeight = 150.0

        // Console pane contents
        consoleVBox.children.addAll(outputArea, inputField)
        this.content = consoleVBox
    }


    override fun print(message: String) {
        outputArea.appendText("$message\n")
//        CommandOutputWindowPane.writeToConsole()
    }

    fun printSeq(p0: String?, p1: MutableList<Node>?) {

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
//                    errorln("Failed to write to com.compsim.Console!")
//                }
//            } catch (localCustomException: CustomException) {
//                writeToConsole(localCustomException.message.toString())
//            }
//        }
//        inputField.selectAll()
//        outputArea.positionCaret(outputArea.paragraphs.size)
//    }
}