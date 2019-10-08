package com.compsim.gui.fx.control

import com.compsim.util.Loader.loadImg
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView

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
        this.graphic = ImageView(loadImg("icons/$buttonID.png", 16.0, 16.0))
    }

    companion object {
        private const val BUTTON_SIZE = 24.0
    }
}