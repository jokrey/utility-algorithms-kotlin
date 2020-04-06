package jokrey.utilities

import java.security.KeyPair
import java.util.*
import jokrey.utilities.misc.RSAAuthHelper

/**
 *
 * @author jokrey
 */

fun generateKeyPairs(num: Int) = Array(num) { RSAAuthHelper.generateKeyPair() }
fun encodeKeyPair(pair: KeyPair) = base64Encode(pair.public.encoded)+":"+ base64Encode(pair.private.encoded)
fun decodeKeyPair(encodedPair: String): KeyPair {
    val split = encodedPair.trim().split(":")
    return RSAAuthHelper.readKeyPair(base64Decode(split[0]), base64Decode(split[1]))
}
fun extractPublicKeyFromEncodedKeyPair(encodedPair: String): String = encodedPair.trim().split(":")[0]
fun extractPrivateKeyFromEncodedKeyPair(encodedPair: String): String = encodedPair.trim().split(":")[1]
fun encodeKeyPairs(pairs: Array<KeyPair>) = pairs.joinToString(",") { encodeKeyPair(it) }
fun decodeKeyPairs(encodedPairs: String) = encodedPairs.split(",").map { decodeKeyPair(it) }
fun extractPublicKeys(pairs: Array<KeyPair>) = pairs.map { it.public.encoded }
fun extractAndEncodePublicKeys(pairs: Array<KeyPair>) = encodePublicKeys(extractPublicKeys(pairs))
fun encodePublicKeys(publicKeys: Iterable<ByteArray>) = publicKeys.joinToString(",") { base64Encode(it) }
fun decodePublicKeys(encodedKeys: String) = encodedKeys.split(",").map { base64Decode(it.trim()) }
fun extractKeyPair(encodedPairs: String, index: Int) = decodeKeyPairs(encodedPairs)[index]

fun base64Encode(a: ByteArray): String = Base64.getEncoder().encodeToString(a)
fun base64Decode(s: String): ByteArray = Base64.getDecoder().decode(s)