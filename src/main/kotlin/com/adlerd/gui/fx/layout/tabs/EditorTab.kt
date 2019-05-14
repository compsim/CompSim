package com.adlerd.gui.fx.layout.tabs

import com.adlerd.Machine
import javafx.scene.control.Tab

class EditorTab(machine: Machine): Tab() {

    init {
        this.text = "Editor"
        this.isClosable = false

    }
}
