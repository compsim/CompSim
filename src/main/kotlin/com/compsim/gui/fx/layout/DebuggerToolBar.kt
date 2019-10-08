package com.compsim.gui.fx.layout

import com.compsim.core.Controller
import com.compsim.gui.fx.control.ToolbarButton
import com.compsim.gui.fx.control.ToolbarSeparator
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip


class DebuggerToolBar(controller: Controller): ToolBar() {

    val showExecBtn = ToolbarButton("showExec", Tooltip("Scroll to Point of Execution"))
    val stepNextBtn = ToolbarButton("stepNext", Tooltip("Step to Next Line"))
    val stepNextBPBtn = ToolbarButton("stepNextBP", Tooltip("Step to Next BreakPoint"))
    val contExecBtn = ToolbarButton("contExec", Tooltip("Continue With Execution"))

    init {
        this.minHeight = 32.0
        this.prefHeight = 32.0
        this.maxHeight = 32.0


        this.items.addAll(showExecBtn, ToolbarSeparator(), stepNextBtn, stepNextBPBtn, contExecBtn)

//        for (item in this.items) {
//            item.isDisabled = true
//        }
    }
}