package model.algorithm

import kotlinx.coroutines.delay

class AlphaBetaAlgo<T>(private val maxDepth: Int): GameTreeAlgo<T> {


    override suspend fun getBestMove(node: TreeNode<T>): T {
        i = 1
        val (delta, value) = alphaBeta(node, maxDepth)
        console.log("best move value $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    var i =1
    suspend fun alphaBeta(node: TreeNode<T>, depth: Int,
                                  alpha: Int = Int.MIN_VALUE,
                                  beta: Int = Int.MAX_VALUE,
                                  color: Int = 1): Pair<T?, Int> {

        if(i++%10000==0) delay(10)//only for single thread environment - allows context switch

       // console.info("(${i++})")
        if (depth==0)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        return when {
            deltas.isEmpty() -> null to color * node.getScore() //if node is a leaf return score
//            deltas.size==1 -> deltas[0].second to color * deltas[0].first.getScore() //if there is only one child return his score
            else -> {//calculate recursively the best move
                var bestPair = deltas[0].second to alpha
                deltas.forEach { (child, delta) ->
                    val tmp = -alphaBeta(child, depth-1, -beta, -bestPair.second, -color).second
                    if (tmp > bestPair.second) {
                        bestPair = delta to tmp
                        if (tmp >= beta) {
                            return bestPair
                        }
                    }
                }
                return bestPair
            }
        }
    }
}