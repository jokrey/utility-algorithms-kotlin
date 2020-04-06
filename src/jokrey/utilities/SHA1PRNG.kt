package jokrey.utilities

import jokrey.utilities.bitsandbytes.BitHelper
import java.security.MessageDigest
import kotlin.experimental.and
import kotlin.experimental.xor

/**
 *
 * @author jokrey
 */
class SHA1PRNG : java.util.Random {
    constructor() {setSeed(System.currentTimeMillis())}
    constructor(seed: ByteArray) {setSeed(seed)}
    constructor(seed: Long) {setSeed(seed)}


    var stepCounter = 0
    var state: ByteArray = ByteArray(0)
    val hashInstance = MessageDigest.getInstance("SHA-1")
    var elapsedBytesInState = 0

    private fun step() {
        if(hashInstance==null) return
        state = hashInstance.digest(state xor BitHelper.getBytes(stepCounter))
        stepCounter++
        elapsedBytesInState = 0
    }

    fun setSeed(seed: ByteArray) {
        state = seed
        step()//i.e. the seed is never directly used to generate randomness. This can be important if the seed has issues
    }
    override fun setSeed(seed: Long) = setSeed(BitHelper.getBytes(seed))

    override fun nextBytes(bytes: ByteArray) {
        for(i in bytes.indices) {
            if(elapsedBytesInState >= state.size/2) //somewhere I read that one should only use half the bytes
                step()
            bytes[i] = state[elapsedBytesInState++]
        }
    }
    fun nextBytes(num: Int) : ByteArray {
        val ret = ByteArray(num)
        nextBytes(ret)
        return ret
    }

    //from java's secure random implementation
    override fun next(numBits: Int): Int {
        val numBytes = (numBits + 7) / 8
        val b = ByteArray(numBytes)
        var next = 0
        nextBytes(b)
        for (i in 0 until numBytes) {
            next = (next shl 8) + (b[i] and 0xFF.toByte())
        }
        return next ushr numBytes * 8 - numBits
    }
}

infix fun ByteArray.xor(bytes: ByteArray): ByteArray {
    val result = ByteArray(this.size)
    if (this.size <= bytes.size) {
        for (i in this.indices)
            result[i] = this[i] xor bytes[i]
    } else {
        for (i in this.indices)
            result[i] = this[i] xor bytes[if(i%bytes.size<0)i%bytes.size+bytes.size else i%bytes.size]
    }
    return result
}