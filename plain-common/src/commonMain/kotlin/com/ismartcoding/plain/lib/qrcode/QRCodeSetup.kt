package com.ismartcoding.plain.lib.qrcode

internal object QRCodeSetup {
    private const val DEFAULT_PROBE_SIZE = 7
    private const val FORMAT_BITS_L_MASK_0 = 0x77C4

    fun setupPositionProbePattern(
        rowOffset: Int,
        colOffset: Int,
        modules: Array<Array<Boolean?>>,
    ) {
        val probeSize = DEFAULT_PROBE_SIZE
        for (row in -1..probeSize) {
            for (col in -1..probeSize) {
                if (row + rowOffset !in modules.indices || col + colOffset !in modules.indices) continue
                modules[row + rowOffset][col + colOffset] =
                    col in 0 until probeSize && (row == 0 || row == probeSize - 1) ||
                    row in 0 until probeSize && (col == 0 || col == probeSize - 1) ||
                    row in 2..4 && col in 2..4
            }
        }
    }

    fun setupPositionAdjustPattern(type: Int, modules: Array<Array<Boolean?>>) {
        val pos = QRUtil.getPatternPosition(type)

        for (i in pos.indices) {
            for (j in pos.indices) {
                val row = pos[i]
                val col = pos[j]

                if (modules[row][col] != null) {
                    continue
                }

                for (r in -2..2) {
                    for (c in -2..2) {
                        modules[row + r][col + c] =
                            r == -2 || r == 2 || c == -2 || c == 2 || r == 0 && c == 0
                    }
                }
            }
        }
    }

    fun setupTimingPattern(moduleCount: Int, modules: Array<Array<Boolean?>>) {
        for (r in 8 until moduleCount - 8) {
            if (modules[r][6] != null) {
                continue
            }

            modules[r][6] = r % 2 == 0
        }

        for (c in 8 until moduleCount - 8) {
            if (modules[6][c] != null) {
                continue
            }

            modules[6][c] = c % 2 == 0
        }
    }

    fun setupTypeInfo(
        moduleCount: Int,
        modules: Array<Array<Boolean?>>
    ) {
        for (i in 0..14) {
            val mod = FORMAT_BITS_L_MASK_0 shr i and 1 == 1

            if (i < 6) {
                modules[i][8] = mod
            } else if (i < 8) {
                modules[i + 1][8] = mod
            } else {
                modules[moduleCount - 15 + i][8] = mod
            }
        }

        for (i in 0..14) {
            val mod = FORMAT_BITS_L_MASK_0 shr i and 1 == 1

            if (i < 8) {
                modules[8][moduleCount - i - 1] = mod
            } else if (i < 9) {
                modules[8][15 - i] = mod
            } else {
                modules[8][15 - i - 1] = mod
            }
        }

        modules[moduleCount - 8][8] = true
    }

    fun setupTypeNumber(type: Int, moduleCount: Int, modules: Array<Array<Boolean?>>) {
        val bits = QRUtil.getBCHTypeNumber(type)

        for (i in 0..17) {
            val mod = bits shr i and 1 == 1
            modules[i / 3][i % 3 + moduleCount - 11] = mod
        }

        for (i in 0..17) {
            val mod = bits shr i and 1 == 1
            modules[i % 3 + moduleCount - 11][i / 3] = mod
        }
    }

    fun applyMaskPattern(
        data: IntArray,
        moduleCount: Int,
        modules: Array<Array<Boolean?>>
    ) {
        var inc = -1
        var bitIndex = 7
        var byteIndex = 0
        var row = moduleCount - 1
        var col = moduleCount - 1

        while (col > 0) {
            if (col == 6) {
                col--
            }

            while (true) {
                for (c in 0..1) {
                    if (modules[row][col - c] == null) {
                        var dark = false

                        if (byteIndex < data.size) {
                            dark = (data[byteIndex] ushr bitIndex) and 1 == 1
                        }

                        val mask = (row + col - c) % 2 == 0
                        if (mask) {
                            dark = !dark
                        }

                        modules[row][col - c] = dark

                        bitIndex--
                        if (bitIndex == -1) {
                            byteIndex++
                            bitIndex = 7
                        }
                    }
                }

                row += inc
                if (row !in 0..<moduleCount) {
                    row -= inc
                    inc = -inc
                    break
                }
            }

            col -= 2
        }
    }
}
