package model.algorithm

import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.delay

class AlphaBetaAlgo<T>: GameTreeAlgo<T> {

    private val cache = HashMap<TreeNode<T>, Pair<Pair<T, Int>, Int>>()

    override suspend fun getBestMove(node: TreeNode<T>, depth: Int): T {
        require(depth>0) { "max depth must be at least 1" }
        i = 1
        val (delta, value) = alphaBeta(node, depth)
        console.log("alpha beta depth is: $i")
        console.log("alpha beta found move with depth: $depth and score: $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    var i =1
    suspend fun alphaBeta(node: TreeNode<T>, depth: Int,
                          alpha: Int = -1_000,
                          beta: Int = 1_000,
                          color: Int = 1): Pair<T?, Int> {
        //only for single thread environment - allows context switch
        if (i++ % 4000 == 0) {
            delay(1)
            console.info("alpha $i")
        }
        cache[node]?.let { (result, d) ->
            if (d>=depth) {
//                console.log("found in cache: $result with depth: $d")
                return result
            }
        }

        if (depth == 0 || node.isTerminal)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        var bestPair = deltas[0].second to alpha
        deltas.forEach { (child, delta) ->
            val tmp = -alphaBeta(child, depth - 1, -beta, -bestPair.second, -color).second
            if (tmp > bestPair.second) {
                bestPair = delta to tmp
                if (tmp >= beta) {
                    cache[node] = bestPair to depth
                    return bestPair
                }
            }
        }
        cache[node] = bestPair to depth
        return bestPair
    }

    override fun disposeCache() {
        cache.clear()
    }
}