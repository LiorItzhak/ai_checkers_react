import model.algorithm.AlphaBetaAlgo
import model.algorithm.NegaMaxAlgo
import kotlin.test.Test
import kotlin.test.assertEquals
import Sok.Internal.runTest

class AlgoTest {

    @Test
    fun testAlphaBetaEqualNegaMax() {
        runTest {
            val negaMax = NegaMaxAlgo<Any?>(10)
            val alphaBeta = AlphaBetaAlgo<Any?>(10)
            val node = TreeNodeTest()
            assertEquals(negaMax.negaMax(node, 10).second,
                    alphaBeta.alphaBeta(node, 10).second)
        }
    }
}