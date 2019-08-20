package com.compsim.core

import AsException
import com.adlerd.compsim.util.exceptions.InternalException
import java.io.BufferedOutputStream
import java.io.IOException
import kotlin.experimental.and
import kotlin.experimental.or

class Word {
    var value: Int
        set(value) {
            field = value and '\uffff'.toInt()
        }

    constructor(value: Int) {
        this.value = value
    }

    constructor() {
        this.value = 0
    }

    fun reset() {
        this.value = 0
    }

    fun toHex(): String {
        return toHex(this.value, true)
    }

    fun toHex(showPrefix: Boolean): String {
        return toHex(this.value, showPrefix)
    }

    fun toBinary(): String {
        return toBinary(this.value, true)
    }

    fun toBinary(showPrefix: Boolean): String {
        return toBinary(this.value, showPrefix)
    }

    override fun toString(): String {
        return Integer.toString(this.value)
    }


    @Throws(IOException::class)
    internal fun writeWordToFile(outputStream: BufferedOutputStream) {
        val var2 = (this.value shr 8 and 255).toByte()
        val var3 = (this.value and 255).toByte()
        outputStream.write(var2.toInt())
        outputStream.write(var3.toInt())
    }

    fun getZext(var1: Int, var2: Int): Int {
        var var3 = this.value
        if (var2 > var1) {
            return this.getZext(var2, var1)
        } else if (var1 in 0..15 && var2 <= 15 && var2 >= 0) {
            var3 = var3 and (-1 shl var1 + 1).inv()
            var3 = var3 shr var2
            return var3
        } else {
            throw InternalException("Bits out of range: $var1 $var2")
        }
    }

    fun getSext(var1: Int, var2: Int): Int {
        var var3 = this.value
        if (var2 > var1) {
            return this.getSext(var2, var1)
        } else if (var1 in 0..15 && var2 <= 15 && var2 >= 0) {
            val var4 = var3 and (1 shl var1)
            if (var4 != 0) {
                var3 = var3 or (-1 shl var1)
            } else {
                var3 = var3 and (-1 shl var1 + 1).inv()
            }

            var3 = var3 shr var2
            return var3
        } else {
            throw InternalException("Bits out of range: $var1 $var2")
        }
    }

    fun getBit(var1: Int): Int {
        return this.getZext(var1, var1)
    }

    @Throws(AsException::class)
    private fun setField(var1: Int, var2: Int, var3: Int) {
        if (var3 > var2) {
            throw AsException("High and low bit operands reversed.")
        } else if (var2 in 0..15 && var3 <= 15 && var3 >= 0) {
            val var4: Byte = -1
            var var5 = var4.toInt() shl var2 - var3 + 1
            var5 = var5.inv()
            var5 = var5 shl var3
            this.value = var5 and (var1 shl var3) or (var5.inv() and this.value)
        } else {
            throw AsException("Bits out of range: $var2 $var3")
        }
    }

    @Throws(AsException::class)
    fun setSignedField(var1: Int, var2: Int, var3: Int) {
        if (var3 > var2) {
            throw AsException("High and low bit operands reversed.")
        } else if (var2 in 0..15 && var3 <= 15 && var3 >= 0) {
            val var4 = var1 shr var2 - var3
            if (var4 != 0 && var4 != -1) {
                throw AsException("Immediate out of range: $var1")
            } else {
                this.setField(var1, var2, var3)
                this.setField(var1, var2, var3)
            }
        } else {
            throw InternalException("Bits out of range: $var2 $var3")
        }
    }

    @Throws(AsException::class)
    fun setUnsignedField(var1: Int, lowBit: Int, highBit: Int) {
        if (highBit > lowBit) {
            throw AsException("High and low bit operands reversed.")
        } else if (lowBit in 0..15 && highBit <= 15 && highBit >= 0) {
            val var4 = var1 shr lowBit - highBit + 1
            if (var4 == 0) {
                this.setField(var1, lowBit, highBit)
            } else {
                throw AsException("Immediate out of range: $var1")
            }
        } else {
            throw InternalException("Bits out of range: $lowBit $highBit")
        }
    }

    companion object {

        @JvmOverloads
        fun toHex(num: Int, showPrefix: Boolean = true): String {
            var hexNum = Integer.toHexString(num and '\uffff'.toInt()).toUpperCase()
            if (hexNum.length > 4) {
                Console.println("Converting oversized value $hexNum to hex.")
            }

            while (hexNum.length < 4) {
                hexNum = "0$hexNum"
            }

            return if (showPrefix) "x$hexNum" else hexNum
        }

        @JvmOverloads
        fun toBinary(num: Int, showPrefix: Boolean = true): String {
            var binaryNum = Integer.toBinaryString(num and '\uffff'.toInt()).toUpperCase()
            if (binaryNum.length > 16) {
                Console.println("Converting oversized value $binaryNum to binary.")
            }

            while (binaryNum.length < 16) {
                binaryNum = "0$binaryNum"
            }

            return if (showPrefix) "b$binaryNum" else binaryNum
        }

        fun parseNum(num: String): Int {
            return try {
                when {
                    num.startsWith('x') -> Integer.parseInt(num.replace('x', '0'), 16)
                    num.startsWith('b') -> Integer.parseInt(num.replace('b', '0'), 2)
                    else -> Integer.parseInt(num)
                }
            } catch (var3: NumberFormatException) {
                Integer.MAX_VALUE
            } catch (var3: NullPointerException) {
                Integer.MAX_VALUE
            }
        }

        fun convertByteArray(var0: Byte, var1: Byte): Int {
            val var2: Byte = 0
            val var3: Short = 255
            var var4 = var2 or ((var3 and var0.toShort()).toByte())
            var4 = (var4.toInt() shl 8).toByte()
            var4 = var4 or ((var3 and var1.toShort()).toByte())
            return var4.toInt()
        }
    }
}
