package model.algorithm

interface TreeNode<T> {

    suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>>

    fun getScore(): Int
}