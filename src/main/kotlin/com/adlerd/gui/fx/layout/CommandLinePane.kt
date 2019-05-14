package com.adlerd.gui.fx.layout

import com.adlerd.CommandLine
import com.adlerd.Machine
import com.adlerd.util.Logger.outputln
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class CommandLinePane(private val machine: Machine): TitledPane()
//        ,EventHandler<ActionEvent>
{

    val consoleVBox = VBox()
    private val commandLine: CommandLine
    private val inputField = TextField()
    private val outputArea = TextArea()

    init {
        this.text = "Console"
        this.isCollapsible = true
        this.isExpanded = false
        this.isAnimated = false

//        this.minHeight = 200.0
//        this.prefHeight = 200.0

        VBox.setVgrow(outputArea, Priority.ALWAYS)
        VBox.setVgrow(inputField, Priority.NEVER)

        // TODO: Fix issue where outputArea and ScrollPane will not fill right
        this.commandLine = CommandLine(machine)

        inputField.onKeyPressed = EventHandler { keyEvent ->
            if (keyEvent.code == KeyCode.ENTER) {
                if (inputField.text.isNotBlank()) {
                    commandLine.addToHistory(inputField.text)
                    writeToConsole(inputField.text)
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
            outputln("Mouse click in command output window consumed")
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

    fun clearConsole() {
        outputArea.clear()
//        CommandOutputWindowPane.clearConsole()
    }

    fun writeToConsole(message: String) {
        outputArea.appendText("$message\n")
//        CommandOutputWindowPane.writeToConsole()
    }

//    override fun handle(event: ActionEvent) {
//        var str = inputField.text
//        this.commandLine.scheduleCommand(str)

//        while (this.commandLine.hasMoreCommands() && (!machine.isContinueMode || commandLine.hasQueuedStop())) {
//            try {
//                str = this.commandLine.runCommand(commandLine.nextCommand)
//
//                if (str.isNotBlank()) {
//                    writeToConsole(str)
//                } else {
//                    errorln("Failed to write to com.adlerd.Console!")
//                }
//            } catch (localCustomException: CustomException) {
//                writeToConsole(localCustomException.message.toString())
//            }
//        }
//        inputField.selectAll()
//        outputArea.positionCaret(outputArea.paragraphs.size)
//    }
}