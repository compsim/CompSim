package com.adlerd.compsim.gui.fx.layout

import com.adlerd.compsim.core.Machine
import com.adlerd.compsim.gui.fx.control.ToolbarButton
import com.adlerd.compsim.gui.fx.control.ToolbarComboBox
import com.adlerd.compsim.gui.fx.control.ToolbarSeparator
import com.adlerd.compsim.util.Logger.infoln
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip

class MainToolbar(machine: Machine): ToolBar() {

    private val openBtn = ToolbarButton("openBtn", Tooltip("Open"))
    private val saveBtn = ToolbarButton("saveBtn", Tooltip("Save"))
    private val backBtn = ToolbarButton("backBtn", Tooltip("Back"))
    private val forwardBtn = ToolbarButton("forwardBtn", Tooltip("Forward"))
    private val runBtn = ToolbarButton("runBtn", Tooltip("Play"))
    private val debugBtn = ToolbarButton("debugBtn", Tooltip("Debug"))
    private val stopBtn = ToolbarButton("stopBtn", Tooltip("Stop"))
    private val findBtn = ToolbarButton("findBtn", Tooltip("Find"))
    private val runComboBox = ToolbarComboBox("Select Project...")

    init {
        this.minHeight = 32.0
        this.prefHeight = 32.0
        this.maxHeight = 32.0

        openBtn.isDisable = false
        saveBtn.isDisable = false
        backBtn.isDisable = false
        forwardBtn.isDisable = false
        runBtn.isDisable = false
        debugBtn.isDisable = false
        stopBtn.isDisable = true
        findBtn.isDisable = false
        runComboBox.isDisable = true

        this.items.addAll(openBtn, saveBtn,
            ToolbarSeparator(), backBtn, forwardBtn,
            ToolbarSeparator(), runComboBox, runBtn, debugBtn, stopBtn,
            ToolbarSeparator(), findBtn)

        openBtn.setOnAction {
            infoln("Open File Button")
        }

        saveBtn.setOnAction {
            infoln("Save File Button")
        }

        backBtn.setOnAction {
            infoln("Back Button")
        }

        forwardBtn.setOnAction {
            infoln("Forward Button")
        }

        runBtn.setOnAction {
            infoln("Run Button")
        }

        debugBtn.setOnAction {
            infoln("Debug Run Button")
        }

        stopBtn.setOnAction {
            infoln("Stop Execution Button")
        }

        findBtn.setOnAction {
            infoln("Search Button")
        }
    }
}