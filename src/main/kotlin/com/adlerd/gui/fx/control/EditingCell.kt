package com.adlerd.gui.fx.control

import com.adlerd.helpers.MemoryRow
import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode

// EditingCell - for editing capability in a TableCell
class EditingCell : TableCell<MemoryRow, String>() {
    private lateinit var textField: TextField


    private val string: String
        get() = if (item == null) "" else item.toString()

    override fun startEdit() {
        super.startEdit()
        createTextField()
        text = ""
        graphic = textField
        textField.selectAll()
    }


    override fun cancelEdit() {
        super.cancelEdit()
        text = item as String
    }


    override fun updateItem(item: String, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                textField.text = string
                text = null
                graphic = textField
            } else {
                text = string
            }
        }
    }

    private fun createTextField() {
        textField = TextField(string)
        textField.minWidth = this.width - this.graphicTextGap * 2
        textField.onKeyReleased = EventHandler { keyEvent ->
            if (keyEvent.code == KeyCode.ENTER) {
                commitEdit(textField.text)
            } else if (keyEvent.code == KeyCode.ESCAPE) {
                cancelEdit()
            }
        }
    }
}