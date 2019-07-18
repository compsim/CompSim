package com.adlerd.compsim.gui.fx.layout.tabs

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab

abstract class CustomTab: Tab() {
    val title = this.text ?: "nil title"

    init {
        this.text = title
        this.contextMenu = initContextMenu()
    }

    /**
     * Context menu for modifying tabs
     */
    private fun initContextMenu(): ContextMenu {
        val close = MenuItem("Close")
        close.setOnAction {
            this.tabPane.tabs.remove(this)
        }
        val closeOthers = MenuItem("Close Others")
        closeOthers.setOnAction {
            //TODO: Implement "Close Others" option
        }
        val closeAll = MenuItem("Close All")
        closeAll.setOnAction {
            this.tabPane.tabs.clear()
        }

        return ContextMenu(close, closeAll)
    }

    companion object {
        const val ICON_SIZE = 16.0
    }
}