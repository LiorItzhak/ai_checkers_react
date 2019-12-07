package model.algorithm

import kotlinx.css.del
import kotlin.math.max

class NegaMaxAlgo<T>(private val maxDepth: Int): GameTreeAlgo<T> {
    init {
        require(maxDepth>0) { "max depth must be at least 1" }
    }

    override suspend fun getBestMove(node: TreeNode<T>): T {
        val (delta, value) = negaMax(node, maxDepth)
        console.log("best move value $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    private suspend fun <T> negaMax(node: TreeNode<T>, depth: Int, color: Int = 1): Pair<T?, Int> {
        //check if reached maximum depth
        if (depth==0)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        //check if node is not a leaf
        return if (deltas.isEmpty())
            null to color * node.getScore()
        else
            deltas.map { (child, delta) -> delta to -negaMax(child, depth-1, -color).second }
                    .maxBy { (_, value) -> value }!!
    }

//    companion object {
//        private suspend fun <T> minMax(node: TreeNode<T>, depth: Int, isMax: Boolean = true): Pair<TreeNode<T>, T?> {
//            if (depth == 0) return node to null
//
//            val children = node.getChildrenWithDeltas()
//            return if (children.isNotEmpty()) {
//
//                val nodes = children.map { (n, delta) -> minMax(n, depth-1, !isMax).first to delta }
//
//                if (isMax)
//                    nodes.maxBy { (n, _) -> n.getScore() }!!
//                else
//                    nodes.minBy { (n, _) -> n.getScore() }!!
//            } else
//                node to null
//        }
//    }
}

class AlphaBetaAlgo<T>(private val maxDepth: Int): GameTreeAlgo<T> {
    override suspend fun getBestMove(node: TreeNode<T>): T {
        val (delta, value) = alphaBeta(node, maxDepth)
        console.log("best move value $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    private suspend fun alphaBeta(node: TreeNode<T>, depth: Int,
                                  alpha: Int = Int.MIN_VALUE,
                                  beta: Int = Int.MAX_VALUE,
                                  color: Int = 1): Pair<T?, Int> {
        if (depth==0)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        return when {
            deltas.isEmpty() -> null to color * node.getScore() //if node is a leaf return score
            deltas.size==1 -> deltas[0].second to color * deltas[0].first.getScore() //if there is only one child return his score
            else -> {//calculate recursively the best move
                var bestPair = deltas[0].second to alpha
                for ((child, delta) in deltas) {
                    val tmp = -alphaBeta(child, depth-1, -beta, -bestPair.second, -color).second
                    if (tmp > bestPair.second) {
                        bestPair = delta to tmp
                        if (tmp >= beta)
                            return bestPair
                    }
                }
                return bestPair
            }
        }
    }
}