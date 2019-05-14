package com.adlerd.gui.fx.layout

import com.adlerd.Machine
import javafx.scene.control.TitledPane


class DevicesPane(machine: Machine): TitledPane() {

    init {
        //TODO: Remove hard-coded string
        this.text = "Devices"
        this.isCollapsible = false
        this.isAnimated = false
    }
}