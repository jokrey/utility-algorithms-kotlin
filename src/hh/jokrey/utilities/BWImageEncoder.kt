package hh.jokrey.utilities

import hh.jokrey.utilities.bitsandbytes.BitHelper
import java.util.*
import kotlin.math.max

fun tests() {
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(0))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(12))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(128))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(256))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(16777215))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(16777216))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(4294967295))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(4294967296))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(4294967297))))

    println(Arrays.toString(BitHelper.getBits(BitHelper.getMinimalBytes(1))))
    println(Arrays.toString(BitHelper.getBits(BitHelper.getInNBytes(435, 3))))

    val array = Array(12) {it}
    println(Arrays.toString(array))
    var wh_bytecount = 0
    while (wh_bytecount<5) {
        wh_bytecount++
        println(wh_bytecount)
        println(Arrays.toString(  array.sliceArray(0 until wh_bytecount)  ))
        println(Arrays.toString(  array.sliceArray(wh_bytecount until 2*wh_bytecount)  ))
        println()
    }

    println(BitHelper.getIntFrom(byteArrayOf(0,0,0)))
    println(BitHelper.getIntFrom(byteArrayOf(0,0,1)))
    println(BitHelper.getIntFrom(byteArrayOf(0,1,0)))
    println(BitHelper.getIntFrom(byteArrayOf(0,1,0,0)))
    println(BitHelper.getIntFrom(byteArrayOf(120,0,0,0,0,0,0,1,0,0)))

    println(Math.ceil((10*10) /8.0).toInt())
}


fun bwimage_encode(raster:Array<Array<Boolean>>): ByteArray {
    val width = raster.size
    val height = raster[0].size

    val wh_bytes_count = max(
            BitHelper.getMinimalBytes(width.toLong()).size,
            BitHelper.getMinimalBytes(height.toLong()).size)
    val width_bytes = BitHelper.getInNBytes(width.toLong(), wh_bytes_count)
    val height_bytes = BitHelper.getInNBytes(height.toLong(), wh_bytes_count)

    val result = ByteArray(Math.ceil((width*height) /8.0).toInt() + width_bytes.size + height_bytes.size)

    for(i in 0 until width_bytes.size)
        result[i] = width_bytes[i]
    for(i in 0 until height_bytes.size)
        result[width_bytes.size + i] = height_bytes[i]

    var arr_index = width_bytes.size + height_bytes.size
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
    var wh_bytecount = 0
    var width = 0
    var height = 0
    while (true) {
        val stored_bits_count = (array.size - 2*wh_bytecount) * 8
        val new_width = BitHelper.getIntFrom(array.sliceArray(0 until (wh_bytecount+1))).toInt()
        val new_height = BitHelper.getIntFrom(array.sliceArray((wh_bytecount+1) until 2*(wh_bytecount+1))).toInt()

        if((new_width<=0 || new_height<=0) && width!=0 && height!=0 ) break  // indicates a definite wrong read in BitHelper.getIntFrom. Additionally it might confuse the next if clause

        if(width * height <= stored_bits_count && stored_bits_count < new_width*new_height)
            break
        else {
            width=new_width
            height=new_height
        }
        wh_bytecount++
    }

    return bwimage_decode(width, height, 2 * wh_bytecount, array)
}

private fun bwimage_decode(width:Int, height:Int, start_index:Int, array: ByteArray):Array<Array<Boolean>> {
    val raster = Array(width) {Array(height) {false}}

    var arr_index = start_index
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