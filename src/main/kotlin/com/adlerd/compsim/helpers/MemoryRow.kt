package com.adlerd.compsim.helpers

data class MemoryRow(var breakpoint: Boolean, var address: String, var valueText: String, var instructionText: String) {

    constructor(breakpoint: Boolean, address: Int, valueText: String, instructionText: String) : this(breakpoint, address.toHex(), valueText, instructionText)

    companion object {
        /**
         * Turn any integer into a hex.
         *
         * @param num is the number to convert to Hex.
         */
        private fun Int.toHex(): String {
            // Hex value of number
            val hex = Integer.toHexString(this)

            return when (hex.length) {
                1 -> "x000$hex"
                2 -> "x00$hex"
                3 -> "x0$hex"
                4 -> "x$hex"
                else -> "ERROR"
            }
        }
    }
}