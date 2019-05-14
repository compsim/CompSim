package com.adlerd.gui.fx.layout.tabs

import com.adlerd.Machine
import com.adlerd.gui.fx.layout.DevicesPane
import com.adlerd.gui.fx.layout.MemoryPane
import com.adlerd.gui.fx.layout.RegistersPane
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class DebuggerTab(machine: Machine): Tab() {

    val memoryPane = MemoryPane(machine)
    val registersPane = RegistersPane(machine)
    val devicesPane = DevicesPane(machine)

    val leftVBox = VBox()
    val rootPane = BorderPane()

    init {
        this.text = "Debugger"
        this.isClosable = false

        VBox.setVgrow(registersPane, Priority.NEVER)
        VBox.setVgrow(devicesPane, Priority.ALWAYS)

        leftVBox.minWidth = 250.0
        leftVBox.prefWidth = 250.0
        leftVBox.maxWidth = 250.0
        leftVBox.children.addAll(registersPane, devicesPane)

        rootPane.left = leftVBox
        rootPane.center = memoryPane

        this.content = rootPane
    }
}
