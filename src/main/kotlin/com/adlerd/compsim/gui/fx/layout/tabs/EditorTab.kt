package com.adlerd.compsim.gui.fx.layout.tabs

import com.adlerd.compsim.core.Machine
import javafx.scene.control.Tab

class EditorTab(machine: Machine): Tab() {

    init {
        this.text = "Editor"
        this.isClosable = false

    }
}
