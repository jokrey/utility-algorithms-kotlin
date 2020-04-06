package jokrey.utilities

/**
 *
 * @author jokrey
 */

fun List<ByteArray>.findIndex(identity: ByteArray): Int {
    for((i, v) in this.withIndex())
        if(v.contentEquals(identity))
            return i
    return -1
}

fun plusInBounds(orig: Int, plus: Int, bounds: Int): Int {
    val res = (orig + plus) % bounds
    return if(res < 0) res + bounds else res
}
fun diffInBounds(orig: Int, elsewhere: Int, bounds: Int) : Int {
    if(orig<0 || orig>=bounds || elsewhere<0 || elsewhere>=bounds) throw IllegalArgumentException("invalid input: orig=$orig, elsewhere=$elsewhere, bounds=$bounds")
    if(elsewhere >= orig) return elsewhere-orig
    return (bounds - elsewhere) + orig
}

fun Collection<ByteArray>.matchesAll(pubKs: Collection<ByteArray>) =
        this.size == pubKs.size && pubKs.containsAll(this) { v1, v2 -> v1.contentEquals(v2) }

fun <E, T> Collection<E>.containsAll(elements: Collection<T>, matcher: (E, T) -> Boolean) = elements.all { this.contains(it, matcher) }
fun <E, T> Collection<E>.contains(element: T, matcher: (E, T) -> Boolean) = this.any { matcher(it, element) }