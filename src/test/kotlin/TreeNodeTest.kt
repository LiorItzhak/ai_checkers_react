import model.algorithm.TreeNode
import kotlin.js.Date
import kotlin.random.Random

class TreeNodeTest:TreeNode<Any?> {

    private val children by lazy {
        (0..Random(Date().getMilliseconds())
                .nextInt(10))
                .map { TreeNodeTest() to null }
    }

    private val score by lazy {
        Random(Date().getMilliseconds())
                .nextInt(100) - 50
    }

    override suspend fun getChildrenWithDeltas() = children

    override fun getScore() = score
}