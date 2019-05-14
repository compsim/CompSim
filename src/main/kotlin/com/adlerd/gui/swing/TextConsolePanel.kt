package com.adlerd.gui.swing

import com.adlerd.Console
import com.adlerd.util.ErrorLog
import com.adlerd.KeyboardDevice
import com.adlerd.MonitorDevice
import java.awt.Color
import java.awt.event.*
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.BadLocationException

class TextConsolePanel internal constructor(private val keyboard: KeyboardDevice, private val monitor: MonitorDevice) :
    JPanel(), KeyListener, FocusListener, ActionListener {
    private val screen = JTextArea(5, 21)
    private val spane: JScrollPane
    private var kbin: PipedInputStream? = null
    private val kbout: PipedOutputStream

    init {
        this.screen.isEditable = false
        this.screen.addKeyListener(this)
        this.screen.addFocusListener(this)
        this.screen.lineWrap = true
        this.screen.wrapStyleWord = true
        this.spane = JScrollPane(this.screen, 22, 30)
        this.kbout = PipedOutputStream()

        try {
            this.kbin = PipedInputStream(this.kbout)
        } catch (var4: IOException) {
            ErrorLog.logError(var4 as Exception)
        }

        keyboard.setInputStream(this.kbin!!)
        keyboard.setDefaultInputStream()
        keyboard.setInputMode(KeyboardDevice.INTERACTIVE_MODE)
        keyboard.setDefaultInputMode()
        monitor.addActionListener(this)
        this.add(this.spane)
    }

    override fun actionPerformed(event: ActionEvent) {
        val eventSource = event.source
        if (eventSource is Int) {
            val doc = this.screen.document

            try {
                doc.remove(0, doc.length)
            } catch (badLocationException: BadLocationException) {
                Console.println(badLocationException.message!!)
            }

        } else {
            val var6 = event.source as String
            this.screen.append(var6)
        }

    }

    override fun keyReleased(var1: KeyEvent) {}

    override fun keyPressed(var1: KeyEvent) {}

    override fun keyTyped(var1: KeyEvent) {
        val var2 = var1.keyChar

        try {
            this.kbout.write(var2.toInt())
            this.kbout.flush()
        } catch (var4: IOException) {
            ErrorLog.logError(var4 as Exception)
        }

    }

    override fun focusGained(var1: FocusEvent) {
        this.screen.background = Color.yellow
    }

    override fun focusLost(var1: FocusEvent) {
        this.screen.background = Color.white
    }

    override fun setEnabled(var1: Boolean) {
        this.screen.isEnabled = var1
        if (var1) {
            this.screen.background = Color.white
        } else {
            this.screen.background = Color.gray
        }

    }
}
