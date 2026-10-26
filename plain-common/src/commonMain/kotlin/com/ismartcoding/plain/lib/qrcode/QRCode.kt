package com.ismartcoding.plain.lib.qrcode

import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.applyMaskPattern
import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.setupPositionAdjustPattern
import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.setupPositionProbePattern
import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.setupTimingPattern
import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.setupTypeInfo
import com.ismartcoding.plain.lib.qrcode.QRCodeSetup.setupTypeNumber

internal class QRCode(data: String) {
    private val usesUtf8Eci = data.any { it.code > 0x7F }
    private val qrCodeData: QRData = when (QRUtil.getDataType(data)) {
        QRCodeDataType.NUMBERS -> QRNumber(data)
        QRCodeDataType.UPPER_ALPHA_NUM -> QRAlphaNum(data)
        QRCodeDataType.DEFAULT -> QR8BitByte(data)
    }

    companion object {
        private const val PAD0 = 0xEC
        private const val PAD1 = 0x11
        private const val ECI_MODE = 0x7
        private const val UTF8_ECI_ASSIGNMENT = 26
    }

    private fun chooseVersion(): Int {
        val payload = BitBuffer()
        qrCodeData.write(payload)
        for (version in 1..40) {
            val countBits = qrCodeData.getLengthInBits(version)
            val capacityBits = RSBlock.getRSBlocks(version).sumOf { it.dataCount } * 8
            if (qrCodeData.length() < (1 shl countBits) &&
                dataHeader(version).lengthInBits.toLong() + payload.lengthInBits <= capacityBits
            ) {
                return version
            }
        }
        throw IllegalArgumentException("Data exceeds QR code capacity")
    }

    private fun dataHeader(type: Int): BitBuffer {
        val buffer = BitBuffer()
        if (usesUtf8Eci) {
            buffer.put(ECI_MODE, 4)
            buffer.put(UTF8_ECI_ASSIGNMENT, 8)
        }
        buffer.put(qrCodeData.dataType.value, 4)
        buffer.put(qrCodeData.length(), qrCodeData.getLengthInBits(type))
        return buffer
    }

    fun encode(): Array<BooleanArray> {
        val type = chooseVersion()
        val moduleCount = type * 4 + 17
        val modules: Array<Array<Boolean?>> =
            Array(moduleCount) { Array(moduleCount) { null } }

        setupPositionProbePattern(0, 0, modules)
        setupPositionProbePattern(0, moduleCount - 7, modules)
        setupPositionProbePattern(moduleCount - 7, 0, modules)

        setupPositionAdjustPattern(type, modules)
        setupTimingPattern(moduleCount, modules)
        setupTypeInfo(moduleCount, modules)

        if (type >= 7) {
            setupTypeNumber(type, moduleCount, modules)
        }

        val data = createData(type)

        applyMaskPattern(data, moduleCount, modules)

        return Array(moduleCount) { row ->
            BooleanArray(moduleCount) { col -> modules[row][col] == true }
        }
    }

    private fun createData(type: Int): IntArray {
        val rsBlocks = RSBlock.getRSBlocks(type)
        val buffer = dataHeader(type)
        qrCodeData.write(buffer)

        val totalDataCount = rsBlocks.sumOf { it.dataCount } * 8

        if (buffer.lengthInBits + 4 <= totalDataCount) {
            buffer.put(0, 4)
        }

        while (buffer.lengthInBits % 8 != 0) {
            buffer.put(false)
        }

        while (true) {
            if (buffer.lengthInBits >= totalDataCount) {
                break
            }

            buffer.put(PAD0, 8)

            if (buffer.lengthInBits >= totalDataCount) {
                break
            }

            buffer.put(PAD1, 8)
        }

        return createBytes(buffer, rsBlocks)
    }

    private fun createBytes(buffer: BitBuffer, rsBlocks: Array<RSBlock>): IntArray {
        var offset = 0
        var maxDcCount = 0
        var maxEcCount = 0
        var totalCodeCount = 0
        val dcData = Array(rsBlocks.size) { IntArray(0) }
        val ecData = Array(rsBlocks.size) { IntArray(0) }

        rsBlocks.forEachIndexed { i, it ->
            val dcCount = it.dataCount
            val ecCount = it.totalCount - dcCount

            totalCodeCount += it.totalCount
            maxDcCount = maxDcCount.coerceAtLeast(dcCount)
            maxEcCount = maxEcCount.coerceAtLeast(ecCount)
            dcData[i] = IntArray(dcCount) { idx -> 0xff and buffer.buffer[idx + offset] }
            offset += dcCount
            val rsPoly = QRUtil.getErrorCorrectPolynomial(ecCount)
            val rawPoly = Polynomial(dcData[i], rsPoly.len() - 1)
            val modPoly = rawPoly.mod(rsPoly)
            val ecDataSize = rsPoly.len() - 1

            ecData[i] = IntArray(ecDataSize) { idx ->
                val modIndex = idx + modPoly.len() - ecDataSize
                if ((modIndex >= 0)) modPoly[modIndex] else 0
            }
        }

        var index = 0
        val data = IntArray(totalCodeCount)

        for (i in 0 until maxDcCount) {
            for (r in rsBlocks.indices) {
                if (i < dcData[r].size) {
                    data[index++] = dcData[r][i]
                }
            }
        }

        for (i in 0 until maxEcCount) {
            for (r in rsBlocks.indices) {
                if (i < ecData[r].size) {
                    data[index++] = ecData[r][i]
                }
            }
        }

        return data
    }
}
