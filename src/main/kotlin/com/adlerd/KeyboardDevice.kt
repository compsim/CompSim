package com.adlerd

import com.adlerd.util.ErrorLog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class KeyboardDevice {
    private var kbin: BufferedReader? = null
    private var defkbin: BufferedReader? = null
    private var current = 0
    private var mode: Int = 0
    private var defmode: Int = 0

    init {
        this.kbin = BufferedReader(InputStreamReader(System.`in`))
        this.mode = INTERACTIVE_MODE
        this.defkbin = this.kbin
        this.defmode = this.mode
    }

    fun setDefaultInputStream() {
        this.defkbin = this.kbin
    }

    fun setDefaultInputMode() {
        this.defmode = this.mode
    }

    fun setInputStream(var1: InputStream) {
        this.kbin = BufferedReader(InputStreamReader(var1))
    }

    fun setInputMode(var1: Int) {
        this.mode = var1
    }

    fun reset() {
        this.kbin = this.defkbin
        this.mode = this.defmode
        this.current = 0
    }

    fun status(): Word {
        return if (this.available()) KB_AVAILABLE else KB_UNAVAILABLE
    }

    fun available(): Boolean {
        try {
            if (this.kbin!!.ready()) {
                this.kbin!!.mark(1)
                if (this.kbin!!.read() == TIMER_TICK.toInt()) {
                    this.kbin!!.reset()
                    return false
                }

                this.kbin!!.reset()
                return true
            }
        } catch (var2: IOException) {
            ErrorLog.logError(var2 as Exception)
        }

        return false
    }

    fun read(): Word {
        val var1 = CharArray(CBUFSIZE)

        try {
            if (this.available()) {
                if (this.mode == INTERACTIVE_MODE) {
                    val var2 = this.kbin!!.read(var1, 0, CBUFSIZE)
                    this.current = var1[var2 - 1].toInt()
                } else {
                    this.current = this.kbin!!.read()
                }
            }
        } catch (var3: IOException) {
            ErrorLog.logError(var3 as Exception)
        }

        return Word(this.current)
    }

    fun hasTimerTick(): Boolean {
        try {
            this.kbin!!.mark(1)
            if (this.kbin!!.ready()) {
                if (this.kbin!!.read() == TIMER_TICK.toInt()) {
                    return true
                }

                this.kbin!!.reset()
                return false
            }
        } catch (var2: IOException) {
            ErrorLog.logError(var2 as Exception)
        }

        return false
    }

    companion object {
        private val KB_AVAILABLE = Word(32768)
        private val KB_UNAVAILABLE = Word(0)
        private val CBUFSIZE = 128
        private val TIMER_TICK = '.'
        var SCRIPT_MODE = 0
        var INTERACTIVE_MODE = 1
    }
}
