package model.algorithm

interface GameTreeAlgo<T> {

    suspend fun getBestMove(node: TreeNode<T>) :  T
}