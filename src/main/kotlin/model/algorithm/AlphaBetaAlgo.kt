package model.algorithm

import kotlinx.coroutines.delay
import kotlin.math.sign

class AlphaBetaAlgo<T>: GameTreeAlgo<T> {

    private var maxDepth: Int = 0
    private val cache = HashMap<TreeNode<T>, Pair<Pair<T, Double>, Int>>()

    override suspend fun getBestMove(node: TreeNode<T>, depth: Int): T {
        maxDepth = depth
        require(depth>0) { "max depth must be at least 1" }
        i = 1
        val (delta, value) = alphaBeta(node, depth)
//        console.log("alpha beta iteration is: $i")
        console.log("alpha beta found move with depth: $depth and score: $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    var i =1
    suspend fun alphaBeta(node: TreeNode<T>, height: Int,
                          alpha: Double = -1_000.0,
                          beta: Double = 1_000.0,
                          color: Int = 1): Pair<T?, Double> {
        //only for single thread environment - allows context switch
        if (i++ % 3000 == 0) {
            delay(1)
//            console.info("alpha $i")
        }
        cache[node]?.let { (result, d) ->
            if (d>=height) {
//                console.log("found in cache: $result with depth: $d")
                return result
            }
        }

        if (height == 0 || node.isTerminal)
            return null to ((color * node.getScore()) +
                    sign(/*color */node.getScore()) * height.toDouble()/(maxDepth+1))

        val deltas = node.getChildrenWithDeltas()
        var bestPair = deltas[0].second to alpha
        deltas.forEach { (child, delta) ->
            val tmp = -alphaBeta(child, height - 1, -beta, -bestPair.second, -color).second
            if (tmp > bestPair.second) {
                bestPair = delta to tmp
                if (tmp >= beta) {
                    cache[node] = bestPair to height
                    return bestPair
                }
            }
        }
        cache[node] = bestPair to height
        return bestPair
    }

    override fun disposeCache() {
        cache.clear()
    }
}