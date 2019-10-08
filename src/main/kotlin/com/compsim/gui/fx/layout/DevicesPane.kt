package com.compsim.gui.fx.layout

import com.compsim.core.Controller
import javafx.scene.control.TitledPane


class DevicesPane(controller: Controller): TitledPane() {

    init {
        this.text = "Devices"
        this.isCollapsible = false
        this.isAnimated = false
    }
}