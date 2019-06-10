package com.adlerd.compsim.core

import com.adlerd.compsim.CompSim
import com.adlerd.compsim.util.ErrorLog
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*

class MonitorDevice {
    private var outputStreamWriter: OutputStreamWriter? = null
    private var mlist: LinkedList<ActionListener>? = null

    constructor() {
        if (!CompSim.isGraphical) {
            this.outputStreamWriter = OutputStreamWriter(System.out)
        } else {
            this.mlist = LinkedList()
        }

    }

    constructor(var1: OutputStream) {
        this.outputStreamWriter = OutputStreamWriter(var1)
    }

    fun addActionListener(actionListener: ActionListener) {
        this.mlist!!.add(actionListener)
    }

    fun status(): Word {
        return if (this.ready()) MONITOR_READY else MONITOR_NOTREADY
    }

    fun ready(): Boolean {
        return if (CompSim.isGraphical) {
            true
        } else {
            try {
                this.outputStreamWriter!!.flush()
                true
            } catch (var2: IOException) {
                ErrorLog.logError(var2 as Exception)
                false
            }

        }
    }

    fun reset() {
        if (CompSim.isGraphical) {
            val var1 = this.mlist!!.listIterator()

            while (var1.hasNext()) {
                val var2 = var1.next()
                var2.actionPerformed(ActionEvent(1, 0, null as String?))
            }
        }

    }

    fun write(char: Char) {
        if (CompSim.isGraphical) {
            val var2 = this.mlist!!.listIterator()

            while (var2.hasNext()) {
                val var3 = var2.next()
                var3.actionPerformed(ActionEvent(char, 0, null as String?))
            }
        } else {
            try {
                this.outputStreamWriter!!.write(char.toInt())
                this.outputStreamWriter!!.flush()
            } catch (var4: IOException) {
                ErrorLog.logError(var4 as Exception)
            }

        }

    }

    companion object {
        private val MONITOR_READY = Word(32768)
        private val MONITOR_NOTREADY = Word(0)
    }
}
