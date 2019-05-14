package com.adlerd.gui.fx.control

import com.adlerd.helpers.Runner
import javafx.scene.control.ComboBox

/**
 * Selector for future implementation of a multi-project function
 */
class ToolbarComboBox(title: String): ComboBox<Runner>() {

    constructor(): this("")

    init {
        this.minHeight = COMBO_HEIGHT
        this.prefHeight = COMBO_HEIGHT
        this.maxHeight = COMBO_HEIGHT
//        this.setMinSize(COMBO_WIDTH, COMBO_HEIGHT)
//        this.setPrefSize(COMBO_WIDTH, COMBO_HEIGHT)
        this.promptText = "Select project..."
        if (title.isNotEmpty()) {
            this.accessibleText = title
        }
    }

    companion object {
        private const val COMBO_WIDTH = 150.0
        private const val COMBO_HEIGHT = 24.0
    }
}
