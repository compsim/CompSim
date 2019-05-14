package com.adlerd.gui.fx.layout

import com.adlerd.Machine
import com.adlerd.tc.gui.control.ToolbarButton
import com.adlerd.gui.fx.control.ToolbarComboBox
import com.adlerd.tc.gui.control.ToolbarSeparator
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip

class MainToolbar(machine: Machine): ToolBar() {

    val openBtn = ToolbarButton("openBtn", Tooltip("Open"))
    val saveBtn = ToolbarButton("saveBtn", Tooltip("Save"))
    val backBtn = ToolbarButton("backwardBtn", Tooltip("Back"))
    val forwardBtn = ToolbarButton("forwardBtn", Tooltip("Forward"))
    val playBtn = ToolbarButton("runBtn", Tooltip("Play"))
    val debugBtn = ToolbarButton("debugBtn", Tooltip("Debug"))
    val stopBtn = ToolbarButton("stopBtn", Tooltip("Stop"))
    val searchBtn = ToolbarButton("searchBtn", Tooltip("Search"))
    val runComboBox = ToolbarComboBox("Select Project...")

    init {
        this.minHeight = 32.0
        this.prefHeight = 32.0
        this.maxHeight = 32.0

        openBtn.isDisable = false
        saveBtn.isDisable = false
        backBtn.isDisable = false
        forwardBtn.isDisable = false
        playBtn.isDisable = false
        debugBtn.isDisable = false
        stopBtn.isDisable = true
        searchBtn.isDisable = false
        runComboBox.isDisable = true

        this.items.addAll(openBtn, saveBtn, ToolbarSeparator(), backBtn, forwardBtn, ToolbarSeparator(), runComboBox, playBtn, debugBtn, stopBtn, ToolbarSeparator(), searchBtn)
    }
}