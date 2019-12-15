package model.algorithm

interface TreeNode<T> {

    suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>>

    fun getScore(): Int

    val isTerminal : Boolean

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean
}