package com.ismartcoding.plain.lib.qrcode

internal class RSBlock(val totalCount: Int, val dataCount: Int) {
    companion object {
        private val RS_BLOCK_TABLE = arrayOf(
            intArrayOf(1, 26, 19),
            intArrayOf(1, 44, 34),
            intArrayOf(1, 70, 55),
            intArrayOf(1, 100, 80),
            intArrayOf(1, 134, 108),
            intArrayOf(2, 86, 68),
            intArrayOf(2, 98, 78),
            intArrayOf(2, 121, 97),
            intArrayOf(2, 146, 116),
            intArrayOf(2, 86, 68, 2, 87, 69),
            intArrayOf(4, 101, 81),
            intArrayOf(2, 116, 92, 2, 117, 93),
            intArrayOf(4, 133, 107),
            intArrayOf(3, 145, 115, 1, 146, 116),
            intArrayOf(5, 109, 87, 1, 110, 88),
            intArrayOf(5, 122, 98, 1, 123, 99),
            intArrayOf(1, 135, 107, 5, 136, 108),
            intArrayOf(5, 150, 120, 1, 151, 121),
            intArrayOf(3, 141, 113, 4, 142, 114),
            intArrayOf(3, 135, 107, 5, 136, 108),
            intArrayOf(4, 144, 116, 4, 145, 117),
            intArrayOf(2, 139, 111, 7, 140, 112),
            intArrayOf(4, 151, 121, 5, 152, 122),
            intArrayOf(6, 147, 117, 4, 148, 118),
            intArrayOf(8, 132, 106, 4, 133, 107),
            intArrayOf(10, 142, 114, 2, 143, 115),
            intArrayOf(8, 152, 122, 4, 153, 123),
            intArrayOf(3, 147, 117, 10, 148, 118),
            intArrayOf(7, 146, 116, 7, 147, 117),
            intArrayOf(5, 145, 115, 10, 146, 116),
            intArrayOf(13, 145, 115, 3, 146, 116),
            intArrayOf(17, 145, 115),
            intArrayOf(17, 145, 115, 1, 146, 116),
            intArrayOf(13, 145, 115, 6, 146, 116),
            intArrayOf(12, 151, 121, 7, 152, 122),
            intArrayOf(6, 151, 121, 14, 152, 122),
            intArrayOf(17, 152, 122, 4, 153, 123),
            intArrayOf(4, 152, 122, 18, 153, 123),
            intArrayOf(20, 147, 117, 4, 148, 118),
            intArrayOf(19, 148, 118, 6, 149, 119)
        )

        fun getRSBlocks(typeNumber: Int): Array<RSBlock> =
            RS_BLOCK_TABLE[typeNumber - 1]
                .let { rsBlock ->
                    if (rsBlock.size == 3) {
                        val block = RSBlock(rsBlock[1], rsBlock[2])
                        Array(rsBlock[0]) { block }
                    } else {
                        val blocksSize = rsBlock[0] + rsBlock[3]
                        val firstBlock = RSBlock(rsBlock[1], rsBlock[2])
                        val secondBlock = RSBlock(rsBlock[4], rsBlock[5])

                        Array(blocksSize) {
                            if (it < rsBlock[0]) {
                                firstBlock
                            } else {
                                secondBlock
                            }
                        }
                    }
                }
    }
}
