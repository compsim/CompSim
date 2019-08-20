package com.compsim.util

import java.util.*

class SymbolTable {
    var table = Hashtable<String, Int>()

    val labels: Enumeration<String>
        get() = this.table.keys()

    fun insert(var1: String, var2: Int): Boolean {
        return if (this.lookup(var1) != -1) {
            false
        } else {
            this.table[var1] = var2
            true
        }
    }

    fun lookup(symbol: String): Int {
        return this.table[symbol] ?: -1
    }
}
