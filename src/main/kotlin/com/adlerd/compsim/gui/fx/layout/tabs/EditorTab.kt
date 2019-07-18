package com.adlerd.compsim.gui.fx.layout.tabs

import com.adlerd.compsim.core.Machine
import com.adlerd.compsim.gui.fx.control.ToolbarButton
import com.adlerd.compsim.gui.fx.control.ToolbarSeparator
import com.adlerd.compsim.util.Logger
import com.adlerd.compsim.util.Logger.debugln
import com.adlerd.compsim.util.Logger.errorln
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.*
import java.util.*

class EditorTab(machine: Machine): Tab() {
    // TabPane for all open files in the EditorTab
    val fileTabPane = TabPane()

    // The currently selected file tab
    var currentFile: FileTab
        get() = fileTabPane.selectionModel.selectedItem as FileTab
        set(value) = fileTabPane.selectionModel.select(value)

    private val newBtn = ToolbarButton("newBtn", Tooltip("New"))
    private val openBtn = ToolbarButton("openBtn", Tooltip("Open"))
    private val saveBtn = ToolbarButton("saveBtn", Tooltip("Save"))

    private val undoBtn = ToolbarButton("undoBtn", Tooltip("Undo"))
    private val redoBtn = ToolbarButton("redoBtn", Tooltip("Redo"))
    private val findBtn = ToolbarButton("findBtn", Tooltip("Find"))
    private val replaceBtn = ToolbarButton("replaceBtn", Tooltip("Replace"))

    private val convertBase2Btn = ToolbarButton("convertBase2Btn", Tooltip("Convert from base 2"))
    private val convertBase16Btn = ToolbarButton("convertBase16Btn", Tooltip("Convert from base 16"))
    private val assembleBtn = ToolbarButton("assembleBtn", Tooltip("Assemble"))


    init {
        this.text = "Editor"
        this.isClosable = false
        this.content = StackPane(initText(), initInnerPane())
    }

    /**
     *  Initialize the inner BorderPane which houses the Editor ToolBar and all of the tabs for file which are loaded.
     *  @return the set-up BorderPane
     */
    private fun initInnerPane(): BorderPane {
        val innerPane = BorderPane()

        innerPane.top = initEditorToolbar()
        innerPane.center = fileTabPane

        fileTabPane.setOnDragOver {event ->
            if (event.gestureSource !== fileTabPane && event.dragboard.hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
//                event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
                event.acceptTransferModes(*TransferMode.ANY)
            }
            event.consume()
        }

        var droppedFilePath = ""
        fileTabPane.setOnDragDropped {event ->
            val db = event.dragboard
            var success = false
            if (db.hasFiles()) {
                droppedFilePath = db.files[0].path
                success = true
            }
            // Let the source know whether the string was successfully transferred and used
            event.isDropCompleted = success
            event.consume()

            openFile(File(droppedFilePath))
        }

        return innerPane
    }

    /**
     *  Initialize the instructional text to be shown when the Editor Tab has no files loaded.
     *  @return the VBox containing the text
     */
    private fun initText(): VBox {
        val vBox = VBox()

        val noOpenLabel = Label("No files are open...")
        noOpenLabel.font = Font.font("Veranda", FontWeight.BOLD, 24.0)
        noOpenLabel.alignment = Pos.CENTER_LEFT

        val instructionsLabel = Label(
            "Open a file with menu \"File > OpenFile...\"\n" +
                    "Open recent files with menu \"File > Recent Files\"\n" +
                    "Drag and drop files from your file manager"
        )
        instructionsLabel.alignment = Pos.CENTER_LEFT

        vBox.alignment = Pos.CENTER
        vBox.children.addAll(noOpenLabel, instructionsLabel)
        VBox.setVgrow(noOpenLabel, Priority.SOMETIMES)
        VBox.setVgrow(instructionsLabel, Priority.SOMETIMES)

        return vBox
    }

    /**
     * Set up the contents of the Editor's ToolBar
     * @return the fully set-up ToolBar
     */
    private fun initEditorToolbar(): ToolBar {
        val toolbar = ToolBar()

        toolbar.minHeight = 32.0
        toolbar.prefHeight = 32.0
        toolbar.maxHeight = 32.0

        newBtn.isDisable = false
        openBtn.isDisable = false
        saveBtn.isDisable = false
        undoBtn.isDisable = true
        redoBtn.isDisable = true
        findBtn.isDisable = true
        replaceBtn.isDisable = true
        convertBase2Btn.isDisable = true
        convertBase16Btn.isDisable = true
        assembleBtn.isDisable = true

        toolbar.items.addAll(
            newBtn, openBtn, saveBtn,
            ToolbarSeparator(), undoBtn, redoBtn, findBtn, replaceBtn,
            ToolbarSeparator(), convertBase2Btn, convertBase16Btn, assembleBtn
        )

        newBtn.setOnAction { addNewFile() }
        openBtn.setOnAction { openFile() }
        saveBtn.setOnAction { if (!saveFile()) errorln("Failed to save file: ${currentFile.filename}") }
        undoBtn.setOnAction { Logger.infoln("Undo Button") }
        redoBtn.setOnAction { Logger.infoln("Redo Button") }
        findBtn.setOnAction { Logger.infoln("Find Button") }
        replaceBtn.setOnAction { Logger.infoln("Replace Run Button") }
        convertBase2Btn.setOnAction { Logger.infoln("Convert Base 2 Button") }
        convertBase16Btn.setOnAction { Logger.infoln("Convert Base 16 Button") }
        assembleBtn.setOnAction { Logger.infoln("Assemble Button") }

        return toolbar
    }

    /**
     *  Adds a new file to the FileTabPane
     */
    private fun addNewFile() {
        val filename: String
        if (openFiles.size < 1) {
            filename = "New File"
            fileNumber++
        } else {
            filename = "New File $fileNumber"
            fileNumber++
        }
        addNewFile(filename)
    }

    /**
     * Adds a new file to the FileTabPane with a given title to the tab
     * @param filename Name of the file to be added
     */
    private fun addNewFile(filename: String) {
        val fileTab = FileTab(filename)
        fileTab.setOnClosed {
            for (file in openFiles) {
                if (fileTab == file) {
                    openFiles.removeAt(openFiles.indexOf(file))
                    debugln("Successfully removed ${fileTab.text}", this::class.java)
                    break
                }
            }
        }
        fileTabPane.tabs.add(fileTab)
        openFiles.add(fileTab)
    }

    /**
     *  Opens the specified file into a new tab in the editor.
     *  @param file The file to be opened
     */
    fun openFile(file: File) {
        for (openFile in openFiles) {
            if (file.name == openFile.filename) {
                errorln("File ('${file.name}') is already open.")
                return
            }
        }

        if (file != null) {
            addNewFile(file.name)
            val currentFile = openFiles[openFiles.size - 1]
            try {
                debugln("Loading: '${file.name}'", this::class.java)
                Scanner(FileReader(file)).use { scanner ->
                    var line: String
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine() // TODO: Find out why this is throwing a NoSuchElementException on file load
                        currentFile.appendTextArea("$line\n")
                        debugln("Line read: $line", this::class.java)
                    }
                    currentFile.filename = file.name
                    currentFile.filePath = file.path
                    currentFile.isSaved = true
                }
            } catch (e: IOException) {
                errorln("Failed to open file.")
                e.printStackTrace()
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
            }

            debugln("File ${file.name} was successfully opened from ('${currentFile.filePath}')", this::class.java)
        }

    }

    /**
     *  Open a dialogue to select a file to load into the editor.
     */
    fun openFile() {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter(
                "CompSim Files"
                ,"*.asm"
                ,"*.bin"
                ,"*.hex"
        ),
        FileChooser.ExtensionFilter("All Files", "*.*")

        )
        openFile(fileChooser.showOpenDialog(Stage()))
    }

    /**
     *  Save the current file. If the file has not been saved before the user will be requested for a location to save the file.
     *  @return If the file has been successfully saved
     */
    fun saveFile(): Boolean {
        val openFile = currentFile
        // A new file's file path should be null
        val path: String? = openFile.filePath
        val text = openFile.textAreaContent

        // If the file has not been saved before
        if (path == null || path == "" || !openFile.isSaved) {
            saveFileAs()
        } else {
            val file = File(path)
            try {
                BufferedWriter(FileWriter(file)).use { writer -> writer.write(text) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return openFile.isSaved
    }

    /**
     *  Save the current file to a selected location regardless of whether or not it has been saved before.
     *  @return If the file has been successfully saved
     */
    fun saveFileAs(): Boolean{
        val openFile = currentFile
        val fileChooser = FileChooser()
        fileChooser.title = "Save File"
        val file = fileChooser.showSaveDialog(Stage())
        try {
            BufferedWriter(FileWriter(file)).use { writer ->
                file.createNewFile()
                writer.write(openFile.textAreaContent)
                openFile.filePath = file.path
                openFile.filename = file.name
                openFile.isSaved = true
                debugln("File path for ${file.name } is ('${currentFile.filePath}')", this::class.java)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val prevName = currentFile.text
        currentFile.text = file.name
        val newName = currentFile.text

        if (prevName != newName) {
            debugln("[ $prevName -> $newName ]\n", this::class.java)
        }

        return openFile.isSaved
    }

    companion object {
        private var fileNumber = 0
        // List of all open tabs
        val openFiles = ArrayList<FileTab>()
    }
}
