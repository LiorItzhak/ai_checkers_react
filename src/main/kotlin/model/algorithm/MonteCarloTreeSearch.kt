package model.algorithm

import kotlinx.coroutines.delay
import kotlin.math.ln
import kotlin.math.sqrt


class MonteCarloTreeSearch<T : StaticState>(private val ucb1Alpha: Double = 1.414, private val usePreviousSearchInfo: Boolean = true) {
    suspend fun search(rootState: T, maxIterations: Int? = null, maxDepth: Int? = null, onChooseChanged: ((T) -> Unit)? = null): T {
        val rootNode = cachedChosenNode?.children?.firstOrNull { node -> node.state == rootState } ?: Node(rootState)
        console.info("log here ${rootNode.numOfVisits} | ${rootNode.depth} || ${rootNode.parent?.parent?.parent == null}")
        val n = rootNode.numOfVisits
        var chosenNode: Node<T>? = null
        try {
            rootNode.state.getChildren().let {
                if (it.size == 1) {
                    chosenNode = rootNode.children?.get(0)
                    return chosenNode!!.state
                }
            }

            while (maxIterations?.let { rootNode.numOfVisits - n < it } != false) {
                searchIteration(rootNode, maxDepth?.plus(rootNode.depth))
                rootNode.children?.maxBy { it.numOfVisits }?.let {
                    if (chosenNode != it) {
                        chosenNode = it
                        console.info("debug: MCTS backup :${it.state} = ${it.weight / it.numOfVisits}  | ${it.numOfVisits}/${rootNode.numOfVisits}")
                        onChooseChanged?.invoke(it.state)
                    }
                }
                //only for single threaded environments - allows context switch//
                if (rootNode.numOfVisits % 100 == 0) {
                    delay(1);console.info("mcts w= ${chosenNode!!.weight / chosenNode!!.numOfVisits} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
                }
            }
        } finally {
            cachedChosenNode = if (usePreviousSearchInfo)  chosenNode?.apply { parent = null } else null
            console.info("finally: ${chosenNode!!.weight / chosenNode!!.numOfVisits} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
        }

        return chosenNode!!.state
    }


    private val ucb1: Node<*>.() -> Double = {
        when (numOfVisits) {
            0 -> Double.POSITIVE_INFINITY
            else -> weight / numOfVisits + ucb1Alpha * sqrt(ln(parent!!.numOfVisits.toDouble()) / numOfVisits)
        }
    }

    private fun searchIteration(rootNode: Node<T>, maxDepth: Int? = null) {

        //Selection
        var node = rootNode
        while (!node.isLeaf) {
            node = node.children!!.maxBy(ucb1)!!
        }

        if (node.numOfVisits != 0 && maxDepth?.let { node.depth < it } != false) {
            //Expand
            node.expand()
            node = node.children!!.firstOrNull() ?: node
        }

        //Simulate
        val w = node.rollOut(maxDepth)

        //Backpropagation
        node.backpropagation(w)
    }


    ///caching
    private var cachedChosenNode: Node<T>? = null


}


class Node<T : StaticState>(val state: T, var parent: Node<T>? = null) {
    val depth: Int = (parent?.depth ?: -1) + 1
    var numOfVisits: Int = 0
    var weight: Double = 0.0

    var children: List<Node<T>>? = null

    val isLeaf: Boolean
        get() = children?.isEmpty() != false //null or true


    fun expand() {
        if (children == null) {
            children = state.getChildren().map { Node(it as T, this) }
        }
    }

    fun rollOut(maxDepth: Int? = null): Double {
        var endState = this.state
        var i = 0
        while (!endState.isTerminal && maxDepth?.let { depth + i++ < it } != false/*null or true*/) {
            endState = endState.getChildren().random() as T
        }

        return if (endState.perspective == parent?.state?.perspective ?: -1) endState.evaluate() else -endState.evaluate()//.endState.evaluate(/*parent!!.state.perspective*/)
        // console.info("endState weight = ${endState.evaluate()}")
    }

    fun backpropagation(weight: Double) {
        numOfVisits++
        this.weight += weight
        var ancestor = parent
        while (ancestor != null) {
            ancestor.numOfVisits++
            ancestor.weight += if (state.perspective == ancestor.state.perspective) weight else -weight
            ancestor = ancestor.parent
        }
    }


}

interface StaticState {
    val isTerminal: Boolean

    fun evaluate(): Double //normalized -1 to 1

    fun getChildren(): List<StaticState>

    val perspective: Int

    override fun equals(other: Any?): Boolean
}