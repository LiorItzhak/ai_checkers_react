package boradGames.algorithm

interface GameTreeAlgo<T> {

    suspend fun getBestMove(node: TreeNode<T>, depth: Int) :  T

    fun disposeCache()
}