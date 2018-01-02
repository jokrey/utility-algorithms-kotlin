package utilities

import utilities.bitsandbytes.BitHelper
import kotlin.math.log2
import kotlin.math.sqrt

fun bwimage_encode(raster:Array<Array<Boolean>>): ByteArray {
    val width = raster.size
    val height = raster[0].size

    if(width == 0)
        throw IllegalArgumentException("raster has to contain data")
    if(width != height)  //algorithm could handle it without this check, but decoding may be wrong
        throw IllegalArgumentException("raster width and height have to be equal for now") //TODO for now! Later any width and height are supposed to be accepted
    if(log2((width*height).toDouble()) % 1 != 0.0)
        throw IllegalArgumentException("width*height has to be a power of two") //TODO for now! Later any width and height are supposed to be accepted
    if(width*height < 8)
        throw IllegalArgumentException("raster has to be at least size 16 for now") //TODO for now! Later any width and height are supposed to be accepted

    var result = ByteArray((width*height) /8)
    var arr_index = 0
    var bit_index = 7
    for (y in 0 until height) {
        for(x in 0 until width) {
            if(raster[x][y])
                result[arr_index] = BitHelper.setBit(result[arr_index], bit_index)

            bit_index--
            if(bit_index<0) {
                arr_index++
                bit_index=7
            }
        }
    }
    return result
}

fun bwimage_decode(array: ByteArray):Array<Array<Boolean>> {
    val stored_bits_count = array.size.toDouble()*8
    val width = sqrt(stored_bits_count).toInt()
    val height = sqrt(stored_bits_count).toInt()

    if(sqrt(stored_bits_count) % 1 != 0.0)
         throw IllegalArgumentException("raster width and height have to be equal for now") //TODO for now! Later any width and height are supposed to be accepted
    if(log2(stored_bits_count) % 1 != 0.0)
        throw IllegalArgumentException("width*height has to be a power of two") //TODO for now! Later any width and height are supposed to be accepted

    val raster = Array(width) {Array(height) {false}}

    var arr_index = 0
    var bit_index = 7
    for (y in 0 until height) {
        for (x in 0 until width) {
            raster[x][y] = (BitHelper.getBit(array[arr_index], bit_index) == 1)

            bit_index--
            if(bit_index<0) {
                arr_index++
                bit_index=7
            }
        }
    }

    return raster
}