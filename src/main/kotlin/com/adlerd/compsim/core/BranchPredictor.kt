package com.adlerd.compsim.core

class BranchPredictor(val machine: Machine, predictorSize: Int) {
    private var predictor: Array<IntArray>? = null
    private var indexMask = 0

    init {
        var var3 = 0
        var var4 = predictorSize
        val var5: Byte = 1
        for (var6 in 0..15) {
            if (var4 and var5.toInt() == var5.toInt()) {
                ++var3

                for (var7 in 0 until var6) {
                    this.indexMask = this.indexMask shl 1
                    this.indexMask = this.indexMask or 1
                }
            }

            var4 = var4 shr 1
        }
        if (var3 != 1) {
            throw IllegalArgumentException("Branch predictor size must be a power of two.")
        } else {
            this.predictor = Array(predictorSize) { IntArray(2) }
        }
    }

    fun getPredictedPC(var1: Int): Int {
        val var2 = var1 and this.indexMask
        var var3 = var1 + 1
        if (this.predictor!![var2][0] == var1) {
            var3 = this.predictor!![var2][1]
        }

        return var3
    }

    fun update(var1: Int, var2: Int) {
        this.predictor!![var1 and this.indexMask][0] = var1
        this.predictor!![var1 and this.indexMask][1] = var2
    }

    override fun toString(): String {
        var var1 = ""

        for (var2 in this.predictor!!.indices) {
            var1 += "$var2: tag: ${this.predictor!![var2][0]} pred: ${this.predictor!![var2][1]}"
        }

        return var1
    }

    fun reset() {
        for (var1 in this.predictor!!.indices) {
            this.predictor!![var1][0] = 0
            this.predictor!![var1][1] = 0
        }

    }

    companion object {
        private val TAG = 0
        private val PREDICTION = 1
    }
}
