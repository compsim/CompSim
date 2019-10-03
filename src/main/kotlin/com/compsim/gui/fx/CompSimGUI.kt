package com.compsim.gui.fx

import com.compsim.CompSim
import com.compsim.core.Console
import com.compsim.core.Machine
import com.compsim.core.Memory.Companion.MEM_SIZE
import com.compsim.gui.fx.layout.ConsolePane
import com.compsim.gui.fx.layout.MainToolbar
import com.compsim.gui.fx.layout.tabs.DebuggerTab
import com.compsim.gui.fx.layout.tabs.EditorTab
import com.compsim.util.Loader
import com.compsim.util.Loader.loadImg
import com.compsim.util.Logger.debugln
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.fxmisc.cssfx.CSSFX
import java.awt.Desktop
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import kotlin.system.exitProcess

class CompSimGUI: Application() {

    val machine = Machine()
    private val root = BorderPane()
    private lateinit var menuBar: MenuBar

    private val mainToolbar = MainToolbar(machine)
    private val programTabs = TabPane()
    private val editorTab = EditorTab(machine)
    private val debuggerTab = DebuggerTab(machine)
    private val consolePane = ConsolePane(machine)

    // File menu
    private val fileMenu = Menu("File")
    private val openFileItem = MenuItem("Open File...")
    private val closeFileItem = MenuItem("Close File")
    private val saveItem = MenuItem("Save")
    private val saveAsItem = MenuItem("Save As...")
    private val recentFilesMenu = Menu("Recent Files")
    private val exitItem = Menu("Exit")
    // Edit menu
    private val editMenu = Menu("Edit")
    private val cutItem = MenuItem("Cut")
    private val copyItem = MenuItem("Copy")
    private val pasteItem = MenuItem("Paste")
    private val selectAllItem = MenuItem("Select all")
    private val findItem = MenuItem("Find...")
    private val replaceItem = MenuItem("Replace...")
    // Help menu
    private val helpMenu = Menu("Help")
    private val wikiItem = MenuItem("Wiki")
    private val prefItem = MenuItem("Preferences...")
    private val aboutItem = MenuItem("About...")


    override fun start(window: Stage) {

        CSSFX.start()

        menuBar = initMenuBar()
        menuBar.isUseSystemMenuBar = true

        programTabs.tabs.addAll(debuggerTab, editorTab)

        root.top = VBox(menuBar, mainToolbar)
        root.center = programTabs
        root.bottom = consolePane


        window.scene = Scene(root, 1024.0, 768.0)
//        window.scene.stylesheets.add(CompSimGUI::class.java.getResource("/icons.css").toString())
        window.scene.stylesheets.add(Loader.loadRes("icons.css"))
        window.minWidth = 1024.0
        window.minHeight = 768.0
        window.title = "${CompSim.version} JavaFX"
        window.icons.add(loadImg("github/CompSimIcon.png"))

        // Show window
        window.show()

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



    /**
     *  Initialize the MenuBar for this application. All action events, looks, and images for this
     *  program's menu bar items are initialized here.
     *  @return the fully initialized MenuBar
     */
    private fun initMenuBar(): MenuBar {
        val bar = MenuBar()

        bar.prefWidthProperty().bind(root.widthProperty())
        bar.useSystemMenuBarProperty()

        // File menu
        openFileItem.graphic = ImageView(loadImg("icons/open.png", MENU_ICON_SIZE))
        openFileItem.setOnAction { editorTab.openFile() }
        closeFileItem.setOnAction { this.editorTab.fileTabPane.tabs.remove(this.editorTab.currentFile) }
        saveItem.graphic = ImageView(loadImg("icons/save.png", MENU_ICON_SIZE))
        saveItem.isDisable = true
        saveItem.setOnAction { editorTab.saveFile() }
        saveAsItem.isDisable = true
        saveAsItem.graphic = ImageView(loadImg("icons/save_all.png", MENU_ICON_SIZE))
        saveAsItem.setOnAction { editorTab.saveFileAs() }
        exitItem.setOnAction { exitProcess(1) }
        fileMenu.items.addAll(openFileItem, SeparatorMenuItem(), closeFileItem, SeparatorMenuItem(), saveItem, saveAsItem, SeparatorMenuItem(), recentFilesMenu, SeparatorMenuItem(), exitItem)

        // Edit menu
        cutItem.isDisable = true
//        cutItem.graphic = ImageView(Image(CompSimGUI::class.java.getResource("/img/copy.png").toExternalForm(), MENU_ICON_SIZE, MENU_ICON_SIZE, true, false))
        cutItem.graphic = ImageView(loadImg("icons/copy.png", MENU_ICON_SIZE))
        cutItem.setOnAction {  }
        copyItem.isDisable = true
//        copyItem.graphic = ImageView(Image(CompSimGUI::class.java.getResource("/img/copy.png").toExternalForm(), MENU_ICON_SIZE, MENU_ICON_SIZE, true, false))
        copyItem.setOnAction {  }
        pasteItem.isDisable = true
//        pasteItem.graphic = ImageView(Image(CompSimGUI::class.java.getResource("/img/paste.png").toExternalForm(), MENU_ICON_SIZE, MENU_ICON_SIZE, false, true))
        pasteItem.setOnAction { debugln("Clipboard: ${Clipboard.getSystemClipboard().string}", this::class.java) }
        selectAllItem.isDisable = true
        selectAllItem.setOnAction {  }
        findItem.isDisable = true
        findItem.setOnAction {  }
        editMenu.items.addAll(copyItem, pasteItem, SeparatorMenuItem(), selectAllItem, SeparatorMenuItem(), findItem)

        // Help menu
        wikiItem.setOnAction {
            try {
                Desktop.getDesktop().browse(URL("https://github.com/dadler64/CompSim/wiki").toURI())
            } catch (e: IOException) {
                println("ERROR: Could not open link")
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                println("ERROR: Bad URI")
                e.printStackTrace()
            }
        }
//        prefItem.graphic = ImageView(Image(CompSimGUI::class.java.getResource("/img/preferences.png").toExternalForm(), MENU_ICON_SIZE, MENU_ICON_SIZE, false, true))
        prefItem.setOnAction {  }
        aboutItem.setOnAction {  }
        helpMenu.items.addAll(wikiItem, SeparatorMenuItem(), prefItem, aboutItem)


        bar.menus.addAll(fileMenu, editMenu, helpMenu)

        // Initialize accelerators for the menu items
        initAccelerators()

        return bar
    }

    /**
     *  Instantiate accelerators for menu items
     */
    private fun initAccelerators() {
        val system = System.getProperty("os.name")
        println("Current System: $system")

        val controlKey = if (system.startsWith(prefix = "mac", ignoreCase = true)) KeyCombination.META_DOWN else KeyCombination.CONTROL_DOWN

        openFileItem.accelerator = KeyCodeCombination(KeyCode.O, controlKey)
        closeFileItem.accelerator = KeyCodeCombination(KeyCode.W, controlKey)
        saveItem.accelerator = KeyCodeCombination(KeyCode.S, controlKey)
        saveAsItem.accelerator = KeyCodeCombination(KeyCode.S, controlKey, KeyCombination.ALT_DOWN)
        exitItem.accelerator = KeyCodeCombination(KeyCode.Q, controlKey)
        copyItem.accelerator = KeyCodeCombination(KeyCode.C, controlKey)
        pasteItem.accelerator = KeyCodeCombination(KeyCode.V, controlKey)
        selectAllItem.accelerator = KeyCodeCombination(KeyCode.A, controlKey)
        findItem.accelerator = KeyCodeCombination(KeyCode.F, controlKey)
        prefItem.accelerator = KeyCodeCombination(KeyCode.COMMA, controlKey)
        aboutItem.accelerator = KeyCodeCombination(KeyCode.F1)
    }

    companion object {
        private const val MENU_ICON_SIZE = 16.0
    }
}
