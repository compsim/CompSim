package com.adlerd.gui.swing

import com.adlerd.CommandLine
import com.adlerd.Console
import com.adlerd.util.ErrorLog
import com.adlerd.Machine
import com.adlerd.util.exceptions.GenericException
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.text.BadLocationException

class CommandLinePanel(private val machine: Machine, private val commandLine: CommandLine) : JPanel(GridBagLayout()),
    ActionListener, Console.PrintableConsole {
    private var textField = JTextField(20)
    private var textArea: JTextArea
    var LC3GUI: LC3GUI? = null
        set(value) {
            this.commandLine.LC3GUI = value!!
            field = value
        }

    init {
        this.textField.addActionListener(this)
        this.textField.inputMap.put(KeyStroke.getKeyStroke("UP"), "prevHistory")
        this.textField.inputMap.put(KeyStroke.getKeyStroke("DOWN"), "nextHistory")
        // Previous history command
        this.textField.actionMap.put("prevHistory", object : AbstractAction() {
            override fun actionPerformed(var1: ActionEvent) {
                this@CommandLinePanel.textField.text = this@CommandLinePanel.commandLine.prevHistory
            }
        })
        // Next history command
        this.textField.actionMap.put("nextHistory", object : AbstractAction() {
            override fun actionPerformed(actionEvent: ActionEvent) {
                this@CommandLinePanel.textField.text = this@CommandLinePanel.commandLine.nextHistory
            }
        })
        this.textArea = JTextArea(10, 70)
        this.textArea.isEditable = false
        this.textArea.lineWrap = true
        this.textArea.wrapStyleWord = true
        val jScrollPane = JScrollPane(this.textArea, 22, 30)
        var constraints = GridBagConstraints()
        constraints.gridwidth = 0
        constraints.fill = 2
        this.add(this.textField, constraints)
        constraints = GridBagConstraints()
        constraints.gridwidth = 0
        constraints.fill = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        this.add(jScrollPane, constraints)
        this.minimumSize = Dimension(20, 1)
    }

    /**
     * Clear contents of text area
     */
    override fun clear() {
        val document = this.textArea.document

        try {
            document.remove(0, document.length)
        } catch (var3: BadLocationException) {
            ErrorLog.logError(var3 as Exception)
        }

    }

    override fun actionPerformed(actionEvent: ActionEvent?) {
        var input: String?
        if (actionEvent != null) {
            input = this.textField.text
            this.commandLine.scheduleCommand(input!!)
        }

        while (this.commandLine.hasMoreCommands() && (!this.machine.isContinueMode || this.commandLine.hasQueuedStop())) {
            try {
                input = this.commandLine.runCommand(this.commandLine.getNextCommand())
                if (input != null) {
                    if (input.isNotEmpty()) {
                        Console.println(input)
                    }
                } else {
                    this.LC3GUI!!.confirmExit()
                }
            } catch (genericException: GenericException) {
                genericException.showMessageDialog(this.parent)
            }

        }

        this.textField.selectAll()
        this.textArea.caretPosition = this.textArea.document.length
    }

    override fun print(message: String) {
        this.textArea.append(message)
    }

    fun reset() {
        this.commandLine.reset()
    }
}
