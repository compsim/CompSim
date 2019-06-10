package com.adlerd.compsim.gui.fx.control

import javafx.beans.property.BooleanProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell


/**
 *  CheckBoxTableCell for creating a CheckBox in a table cell
 */
class CheckBoxTableCell<S, T> : TableCell<S, T>() {
    private val checkBox: CheckBox
    private var ov: ObservableValue<T>? = null

    init {
        this.checkBox = CheckBox()
        this.checkBox.alignment = Pos.CENTER

        alignment = Pos.CENTER
        graphic = checkBox
    }

    public override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            setGraphic(null)
        } else {
            graphic = checkBox
            if (ov is BooleanProperty) {
                checkBox.selectedProperty().unbindBidirectional((ov as BooleanProperty))
            }
            ov = tableColumn.getCellObservableValue(index)
            if (ov is BooleanProperty) {
                checkBox.selectedProperty().bindBidirectional((ov as BooleanProperty))
            }
        }
    }
}