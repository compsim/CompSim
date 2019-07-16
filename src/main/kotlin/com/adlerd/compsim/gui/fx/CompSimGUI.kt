package com.adlerd.compsim.gui.fx

import com.adlerd.compsim.CompSim
import com.adlerd.compsim.core.Console
import com.adlerd.compsim.core.Machine
import com.adlerd.compsim.core.Memory.Companion.MEM_SIZE
import com.adlerd.compsim.gui.fx.layout.ConsolePane
import com.adlerd.compsim.gui.fx.layout.MainToolbar
import com.adlerd.compsim.gui.fx.layout.tabs.DebuggerTab
import com.adlerd.compsim.gui.fx.layout.tabs.EditorTab
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class CompSimGUI: Application() {

    val machine = Machine()
    private val root = BorderPane()

    private val mainToolbar = MainToolbar(machine)
    private val programTabs = TabPane()
    private val editorTab = EditorTab(machine)
    private val debuggerTab = DebuggerTab(machine)
    private val consolePane = ConsolePane(machine)


    override fun start(stage: Stage) {

        programTabs.tabs.addAll(debuggerTab, editorTab)

        root.top = mainToolbar
        root.center = programTabs
        root.bottom = consolePane

        stage.scene = Scene(root, 1024.0, 768.0)
        stage.scene.stylesheets.add(CompSimGUI::class.java.getResource("/icons.css").toExternalForm())
        stage.minWidth = 1024.0
        stage.minHeight = 768.0
        stage.title = "${CompSim.version} JavaFX"
        stage.show()

        Console.registerConsole(this.consolePane)

        resetMemoryPane(MEM_SIZE)
//        updateMemoryRow(4, true, "x6969", ".String Text 6969")
//        updateMemoryRow(5, true, "x6969", ".String Text 6969")
    }

    fun resetMemoryPane(memorySize: Int) {
        this.debuggerTab.memoryPane.generateMemoryRows(memorySize)
    }

    fun updateMemoryRow(index: Int, breakpoint: Boolean, value: String, instruction: String) {
        this.debuggerTab.memoryPane.updateMemoryRow(index, breakpoint, value, instruction)
    }
}
