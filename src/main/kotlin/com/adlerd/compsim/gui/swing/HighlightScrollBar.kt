package com.adlerd.compsim.gui.swing

import com.adlerd.compsim.core.Machine
import java.awt.Color
import java.awt.Cursor
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JScrollBar
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

/**
 * Scrollbar that adds a red highight on the scroll bar
 * depending on where a breakpoint is set in the memeory
 * table.
 */
class HighlightScrollBar(private val machine: Machine) : JScrollBar(), TableModelListener {
    private val highlights = Hashtable<Int, JButton>()
    private var scaleFactor = 1.0
    private val pcButton: JButton? = null

    override fun tableChanged(tableModel: TableModelEvent) {
        val model = tableModel.source as TableModel
        this.scaleFactor = model.rowCount.toDouble() / (this.height - 30).toDouble()
        val rowIndex = tableModel.firstRow
        val highlight: JButton?
        if (model.getValueAt(rowIndex, 0) == true) {
            highlight = JButton()
            highlight.toolTipText = model.getValueAt(rowIndex, 1) as String
            highlight.actionCommand = rowIndex.toString()
            highlight.addActionListener(this.machine.LC3GUI)
            highlight.setSize(this.width - 5, 4)
            highlight.foreground = LC3GUI.BreakPointColor
            highlight.background = LC3GUI.BreakPointColor
            highlight.border = BorderFactory.createLineBorder(Color.RED)
            highlight.isOpaque = true
            highlight.cursor = Cursor(Cursor.HAND_CURSOR)
            highlight.setLocation(3, (rowIndex.toDouble() / this.scaleFactor).toInt() + 15)
            this.highlights[rowIndex] = highlight
            this.add(highlight)
        } else {
            assert(model.getValueAt(rowIndex, 0) == java.lang.Boolean.FALSE)


            highlight = this.highlights.remove(rowIndex)
            if (highlight != null) {
                this.remove(highlight)
            }
        }

        this.repaint(0L, 0, (rowIndex.toDouble() / this.scaleFactor).toInt() + 15, this.width, 4)
    }

    companion object {
        private val MARK_HEIGHT = 4
        private val SCROLL_BUTTON_SIZE = 15
    }
}
