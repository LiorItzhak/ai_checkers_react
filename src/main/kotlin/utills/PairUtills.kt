
inline fun cartesianFor(a: Pair<Int, Int>, b: Pair<Int, Int>, block: (Int, Int) -> Unit) {
    for (i in a.first until a.second)
        for (j in b.first until b.second)
            block(i, j)
}

inline fun cartesianFor(a: Pair<Int, Int>, b: Pair<Int, Int>, block: (Pair<Int, Int>) -> Unit) {
    for (i in a.first until a.second)
        for (j in b.first until b.second)
            block(i to j)
}

inline fun cartesianFor(a: Int, b: Int, block: (Int, Int) -> Unit) {
    for (i in 0 until a)
        for (j in 0 until b)
            block(i, j)
}

inline fun cartesianFor(a: Int, b: Int, block: (Pair<Int, Int>) -> Unit) {
    for (i in 0 until a)
        for (j in 0 until b)
            block(i to j)
}

operator fun Pair<Int, Int>.plus(pair: Pair<Int, Int>) = Pair(this.first + pair.first, this.second + pair.second)

operator fun Pair<Int, Int>.times(i: Int) = Pair(i * first, i * second)

operator fun Int .times(i: Pair<Int, Int>) = Pair(i.first * this, i.second * this)