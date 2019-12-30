package boradGames.algorithm

class NegaMaxAlgo<T>(): GameTreeAlgo<T> {

    override suspend fun getBestMove(node: TreeNode<T>, depth: Int): T {
        require(depth > 0) { "max depth must be at least 1" }
        val (delta, value) = negaMax(node, depth)
        console.log("best move value $value")
        if (delta != null)
            return delta
        throw RuntimeException("No move found")
    }

    suspend fun <T> negaMax(node: TreeNode<T>, depth: Int, color: Int = 1): Pair<T?, Double> {
        //check if reached maximum depth
        if (depth == 0)
            return null to color * node.getScore()

        val deltas = node.getChildrenWithDeltas()
        //check if node is not a leaf
        return if (deltas.isEmpty())
            null to color * node.getScore()
        else
            deltas.map { (child, delta) -> delta to -negaMax(child, depth - 1, -color).second }
                    .maxBy { (_, value) -> value }!!
    }

    override fun disposeCache() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}