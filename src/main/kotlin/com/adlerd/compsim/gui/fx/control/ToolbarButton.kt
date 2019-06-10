package com.adlerd.compsim.gui.fx.control

import javafx.scene.control.Button
import javafx.scene.control.Tooltip

class ToolbarButton(buttonID: String, tooltip: Tooltip?): Button() {

    constructor(tooltip: Tooltip) : this("", tooltip)

    constructor(imagePath: String) : this(imagePath, null)

    constructor() : this("", null)

    init {
        this.setMinSize(
            BUTTON_SIZE,
            BUTTON_SIZE
        )
        this.setPrefSize(
            BUTTON_SIZE,
            BUTTON_SIZE
        )
        this.setMaxSize(
            BUTTON_SIZE,
            BUTTON_SIZE
        )

        if (buttonID.isNotEmpty()) {
            this.id = buttonID
        }

        if (tooltip != null) {
            this.tooltip = tooltip
        }
    }

    companion object {
        private const val BUTTON_SIZE = 24.0
    }
}