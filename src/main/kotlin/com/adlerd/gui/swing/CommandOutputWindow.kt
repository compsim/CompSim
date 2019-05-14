package com.adlerd.gui.swing

import com.adlerd.Console
import com.adlerd.util.ErrorLog
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.BadLocationException

class CommandOutputWindow(title: String) : JFrame(title), Console.PrintableConsole {
    private val textArea = JTextArea()

    init {
        this.textArea.isEditable = false
        this.textArea.lineWrap = true
        this.textArea.wrapStyleWord = true
        val jScrollPane = JScrollPane(this.textArea, 22, 30)
        this.contentPane.add(jScrollPane)
    }

    override fun print(message: String) {
        this.textArea.append(message)
    }

    override fun clear() {
        val document = this.textArea.document

        try {
            document.remove(0, document.length)
        } catch (badLocationException: BadLocationException) {
            ErrorLog.logError(badLocationException as Exception)
        }

    }
}