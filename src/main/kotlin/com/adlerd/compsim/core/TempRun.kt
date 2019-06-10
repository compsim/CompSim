package com.adlerd.compsim.core

import com.adlerd.compsim.gui.swing.LC3GUI

internal class TempRun(var LC3GUI: LC3GUI) : Runnable {

    override fun run() {
        this.LC3GUI.setUpGUI()
    }
}