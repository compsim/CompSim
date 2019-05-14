package com.adlerd.gui.fx

import com.adlerd.CompSim
import com.adlerd.Machine
import com.adlerd.Memory.Companion.MEM_SIZE
import com.adlerd.gui.fx.layout.CommandLinePane
import com.adlerd.gui.fx.layout.MainToolbar
import com.adlerd.gui.fx.layout.tabs.DebuggerTab
import com.adlerd.gui.fx.layout.tabs.EditorTab
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class CompSimGUI: Application() {
//
//    lateinit var window: Stage
//
//    override fun start(primaryStage: Stage) {
//        window = primaryStage
//
//        val root = FXMLLoader.load<Parent>(CompSimGUI::class.java.getResource("/CompSimGUI.fxml"))
//        val scene = Scene (root)
//        scene.stylesheets.add(CompSimGUI::class.java.getResource("/icons.css").toExternalForm())
//
//        window.scene = scene
//        window.title = "com.adlerd.CompSim ${CompSim.VERSION}"
//        window.show()
//    }

    val machine = Machine()
    private val root = BorderPane()

    private val mainToolbar = MainToolbar(machine)
    private val programTabs = TabPane()
    private val editorTab = EditorTab(machine)
    private val debuggerTab = DebuggerTab(machine)
    private val consolePane = CommandLinePane(machine)


    override fun start(stage: Stage) {

//        programTabs.tabs.addAll(editorTab, debuggerTab)
        programTabs.tabs.addAll(debuggerTab, editorTab)

        root.top = mainToolbar
        root.center = programTabs
        root.bottom = consolePane

        stage.scene = Scene(root, 1024.0, 768.0)
//        CSSFX.start()
        stage.scene.stylesheets.add(CompSimGUI::class.java.getResource("/icons.css").toExternalForm())
        stage.minWidth = 1024.0
        stage.minHeight = 768.0
        stage.title = CompSim.getVersion()
        stage.show()

        resetMemoryPane(MEM_SIZE)
        updateMemoryRow(4, true, "x6969", ".String Text 6969")
        updateMemoryRow(5, true, "x6969", ".String Text 6969")
    }

    fun resetMemoryPane(memorySize: Int) {
        this.debuggerTab.memoryPane.generateMemoryRows(memorySize)
    }

    fun updateMemoryRow(index: Int, breakpoint: Boolean, value: String, instruction: String) {
        this.debuggerTab.memoryPane.updateMemoryRow(index, breakpoint, value, instruction)
    }
}
