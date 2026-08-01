package com.zenox.arrowmaze.core.util

class SeededRandom(private var seed: Long) {
    private var state = seed

    init {
        if (state == 0L) state = 1L
    }

    fun nextInt(): Int {
        var t = state xor (state ushr 12)
        t = t xor (t shl 25)
        t = t xor (t ushr 27)
        state = t
        return (t and 0x7FFFFFFFL).toInt()
    }

    fun nextInt(bound: Int): Int {
        if (bound <= 0) return 0
        return (nextInt() and 0x7FFFFFFF) % bound
    }

    fun nextFloat(): Float = nextInt().toFloat() / Int.MAX_VALUE.toFloat()

    fun nextBoolean(): Boolean = nextInt() % 2 == 0

    fun shuffle(list: MutableList<*>) {
        for (i in list.size - 1 downTo 1) {
            val j = nextInt(i + 1)
            @Suppress("UNCHECKED_CAST")
            val mutableList = list as MutableList<Any>
            val tmp = mutableList[i]
            mutableList[i] = mutableList[j]
            mutableList[j] = tmp
        }
    }

    companion object {
        fun hashString(str: String): Long {
            var h = 0L
            for (c in str) {
                h = (h shl 5) - h + c.code.toLong()
                h = h xor (h ushr 16)
            }
            return if (h == 0L) 1L else h
        }
    }
}