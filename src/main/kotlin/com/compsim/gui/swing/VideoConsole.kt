package com.compsim.gui.swing

import com.adlerd.compsim.core.Machine
import com.adlerd.compsim.core.Word
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import javax.swing.JPanel
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener

class VideoConsole(private val machine: Machine) : JPanel(), TableModelListener {
    private var image: BufferedImage? = null

    init {
        val dimension = Dimension(256, 248)
        this.preferredSize = dimension
        this.minimumSize = dimension
        this.maximumSize = dimension
        this.image = BufferedImage(256, 248, 9)
        val background = this.image!!.createGraphics()
        background.color = Color.black
        background.fillRect(0, 0, 256, 248)
    }

    fun reset() {
        val var1 = this.image!!.createGraphics()
        var1.color = Color.black
        var1.fillRect(0, 0, 256, 248)
        this.repaint()
    }

    override fun tableChanged(tableModelEvent: TableModelEvent) {
        val firstRow = tableModelEvent.firstRow
        val lastRow = tableModelEvent.lastRow
        if (firstRow == 0 && lastRow == 65535) {
            this.reset()
        } else {
            if (firstRow < 49152 || firstRow > 65024) {
                return
            }

            val var4: Byte = 2
//            val var5 = firstRow - '쀀'.toInt()
            val var5 = firstRow - 49152
            val var6 = var5 / 128 * var4
            val var7 = var5 % 128 * var4
            val var8 =
                convertToRGB(this.machine.memory.read(firstRow)!!)

            for (var9 in 0 until var4) {
                for (var10 in 0 until var4) {
                    this.image!!.setRGB(var7 + var10, var6 + var9, var8)
                }
            }

            this.repaint(var7, var6, var4.toInt(), var4.toInt())
        }

    }

    public override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val var2 = graphics as Graphics2D
        if (this.image == null) {
            val var3 = this.width
            val var4 = this.height
            this.image = this.createImage(var3, var4) as BufferedImage
            val var5 = this.image!!.createGraphics()
            var5.color = Color.white
            var5.fillRect(0, 0, var3, var4)
        }

        var2.drawImage(this.image, null as BufferedImageOp?, 0, 0)
    }

    companion object {
        private val START = 49152
        private val NROWS = 128
        private val NCOLS = 124
        private val END = 65024
        private val SCALING = 2
        private val WIDTH = 256
        private val HEIGHT = 248

        private fun convertToRGB(word: Word): Int {
            return Color(word.getZext(14, 10) * 8, word.getZext(9, 5) * 8, word.getZext(4, 0) * 8).rgb
        }
    }
}