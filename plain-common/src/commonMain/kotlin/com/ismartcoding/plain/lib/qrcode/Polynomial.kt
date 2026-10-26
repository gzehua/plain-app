package com.ismartcoding.plain.lib.qrcode

import com.ismartcoding.plain.lib.qrcode.QRMath.gexp
import com.ismartcoding.plain.lib.qrcode.QRMath.glog

internal class Polynomial(num: IntArray, shift: Int = 0) {
    private val data: IntArray

    init {
        val offset = num.indexOfFirst { it != 0 }
            .let { if (it < 0) num.size else it }
        data = IntArray(num.size - offset + shift)
        num.copyInto(data, startIndex = offset)
    }

    operator fun get(i: Int) = data[i]

    fun len(): Int = data.size

    fun multiply(other: Polynomial): Polynomial {
        val result = IntArray(len() + other.len() - 1)
        for (i in 0 until len()) {
            if (this[i] == 0) continue
            for (j in 0 until other.len()) {
                if (other[j] == 0) continue
                result[i + j] = result[i + j] xor gexp(glog(this[i]) + glog(other[j]))
            }
        }
        return Polynomial(result)
    }

    fun mod(other: Polynomial): Polynomial {
        if (len() < other.len()) return this
        val ratio = glog(this[0]) - glog(other[0])
        val result = data.copyOf()
        other.data.forEachIndexed { i, coefficient ->
            if (coefficient != 0) result[i] = result[i] xor gexp(glog(coefficient) + ratio)
        }
        return Polynomial(result).mod(other)
    }
}
