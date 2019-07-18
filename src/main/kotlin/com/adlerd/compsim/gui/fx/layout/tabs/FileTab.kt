package com.adlerd.compsim.gui.fx.layout.tabs

import com.adlerd.compsim.util.Logger
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
class FileTab(var filename: String): CustomTab() {

    private val textArea: CodeArea = CodeArea()
    var isSaved = false
    var filePath: String? = null

    var textAreaContent: String
        get() = this.textArea.text
        set(text) {
            this.textArea.clear()
            this.textArea.appendText(text)
        }

    val fileExtension = filename.substringAfterLast('.')

    init {
        this.textArea.paragraphGraphicFactory = LineNumberFactory.get(textArea)

        // Text selection test
        textArea.setOnContextMenuRequested { e ->

        }

        //
        textArea.setOnKeyPressed { e ->
            // Position of the caret in the text field
            val pos = textArea.caretPositionProperty().value

            when(e.code) {
                else -> Logger.debugln("KEY: ${e.code} | CARET POS: $pos", this::class.java)
            }
        }

        this.text = this.filename
        this.content = VirtualizedScrollPane(textArea)
    }

    fun clear() {
        this.textArea.clear()
    }

    fun setTextArea(text: String) {
        clearTextArea()
        this.textArea.appendText(text)
    }

    fun appendTextArea(text: String) {
        this.textArea.appendText(text)
    }

    private fun clearTextArea() {
        this.textArea.clear()
    }

    fun hasFileExtension(): Boolean {
        return fileExtension.isNotBlank()
    }
}
