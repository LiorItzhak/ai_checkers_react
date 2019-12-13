package model.algorithm

import kotlinx.coroutines.delay

class AlphaBetaAlgo<T>(private val maxDepth: Int): GameTreeAlgo<T> {

    override suspend fun getBestMove(node: TreeNode<T>): T {
        i = 1
//        cache.clear()
        val (delta, value) = alphaBeta(node, maxDepth)
        console.log("alpha beta found move with score: $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    var i =1
    suspend fun alphaBeta(node: TreeNode<T>, depth: Int,
                                  alpha: Int = Int.MIN_VALUE,
                                  beta: Int = Int.MAX_VALUE,
                                  color: Int = 1): Pair<T?, Int> {
//        cache[node]?.let { (cachedValue, (cachedDepth, cachedColor)) ->
//            if(cachedDepth>=depth) {
//                console.log("alpha beta found node in cache")
//                return cachedValue
//            }
//        }

        if(i++%4000==0) {delay(1);console.info("alpha $i")}//only for single thread environment - allows context switch

       // console.info("(${i++})")
        if (depth==0)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        return when {
            deltas.isEmpty() -> null to color * node.getScore() //if node is a leaf return score
            else -> {//calculate recursively the best move
                var bestPair = deltas[0].second to alpha
                deltas.forEach { (child, delta) ->
                    val tmp = -alphaBeta(child, depth-1, -beta, -bestPair.second, -color).second
                    if (tmp > bestPair.second) {
                        bestPair = delta to tmp
                        if (tmp >= beta) {
//                            cache[node] = bestPair to depth
                            return bestPair
                        }
                    }
                }
//                cache[node] = bestPair to depth
                return bestPair
            }
        }
    }
}