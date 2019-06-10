package com.adlerd.compsim.util

import com.adlerd.compsim.core.KeyboardDevice
import com.adlerd.compsim.core.Word

class TimerDevice {
    private var mode: Int = 0
    var isEnabled = false
    private var lastTime: Long = 0
    var interval: Long = 0
        private set
    private var kb: KeyboardDevice? = null

    init {
        this.mode = AUTOMATIC_TIMER
        this.isEnabled = true
    }

    fun setTimer() {
        this.mode = AUTOMATIC_TIMER
        this.interval = TIMER_INTERVAL
        this.lastTime = System.currentTimeMillis()
    }

    fun setTimer(var1: Long) {
        this.mode = AUTOMATIC_TIMER
        this.interval = var1
        this.lastTime = System.currentTimeMillis()
    }

    fun setTimer(var1: KeyboardDevice) {
        this.mode = MANUAL_TIMER
        this.interval = 1L
        this.kb = var1
    }

    fun reset() {
        this.mode = AUTOMATIC_TIMER
        this.setTimer(TIMER_INTERVAL)
    }

    fun status(): Word {
        return if (this.hasGoneOff()) TIMER_SET else TIMER_UNSET
    }

    fun hasGoneOff(): Boolean {
        if (!this.isEnabled) {
            return false
        } else if (this.mode == AUTOMATIC_TIMER) {
            val var1 = System.currentTimeMillis()
            if (var1 - this.lastTime > this.interval) {
                this.lastTime = var1
                return true
            } else {
                return false
            }
        } else {
            return this.kb!!.hasTimerTick()
        }
    }

    companion object {
        private val TIMER_SET = Word(32768)
        private val TIMER_UNSET = Word(0)
        private val MANUAL_TIMER = 0
        private val AUTOMATIC_TIMER = 1
        private val TIMER_INTERVAL = 500L
    }
}
