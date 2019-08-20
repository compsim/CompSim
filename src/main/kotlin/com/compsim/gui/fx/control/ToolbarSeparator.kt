package com.compsim.gui.fx.control

import javafx.geometry.Orientation
import javafx.geometry.VPos
import javafx.scene.control.Separator

class ToolbarSeparator: Separator() {

    init {
        this.valignment = VPos.CENTER
        this.orientation = Orientation.VERTICAL
    }
}