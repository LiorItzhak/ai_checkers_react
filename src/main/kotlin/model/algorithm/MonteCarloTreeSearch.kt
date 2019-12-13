package model.algorithm

import kotlinx.coroutines.delay
import kotlin.math.ln
import kotlin.math.sqrt


class MonteCarloTreeSearch<T : StaticState> {
    suspend fun search(rootState: T, maxIterations: Int? = null, maxDepth: Int? = null, onChooseChanged: ((T) -> Unit)? = null): T {
        val rootNode = Node(rootState)
        rootNode.state.getChildren().let { if (it.size == 1) return it[0] as T }
        var chosenNode: Node? = null
        try {
            while (maxIterations?.let { rootNode.numOfVisits < it } != false) {
                searchIteration(rootNode, maxDepth)
                rootNode.children?.maxBy { it.numOfVisits }?.let {
                    if (it != chosenNode) {
                        chosenNode = it
                        console.info("debug: MCTS backup :${it.state} = ${it.weight / it.numOfVisits}  | ${it.numOfVisits}/${rootNode.numOfVisits}")
                        onChooseChanged?.invoke(it.state as T)
                    }
                }
                //only for single threaded environments - allows context switch//
                if (rootNode.numOfVisits % 150 == 0) {
                    delay(1);console.info("numOfVisits ${rootNode.numOfVisits} ||${chosenNode!!.weight / chosenNode!!.numOfVisits} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
                }
            }
        } finally {
            console.info("finally: ${chosenNode!!.weight / chosenNode!!.numOfVisits} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
        }

        return chosenNode!!.state as T
    }


    private val ucb1: Node.() -> Double = {
        when (numOfVisits) {
            0 -> Double.POSITIVE_INFINITY
            else -> weight / numOfVisits + 100 * sqrt(ln(parent!!.numOfVisits.toDouble()) / numOfVisits)
        }
    }

    private fun searchIteration(rootNode: Node, maxDepth: Int? = null) {

        //Selection
        var node = rootNode
        while (!node.isLeaf) {
            node = node.children!!.maxBy(ucb1)!!
        }
        //console.info("node depth = ${node.depth}")


        if (node.numOfVisits != 0 && maxDepth?.let { node.depth < it } != false) {
            //Expand
            node.expand()
            node = node.children!!.getOrNull(0) ?: node
        }

        //Simulate
        node.rollOut(maxDepth)

        //Backpropagation
        node.backpropagation()
    }
}


class Node(val state: StaticState, val parent: Node? = null) {
    val depth: Int = (parent?.depth?:-1) + 1
    var numOfVisits: Int = 0
        private set
    var weight: Double = 0.0
        private set
    var children: List<Node>? = null
        private set

    val isLeaf: Boolean
        get() = children?.isEmpty() != false //null or true


    fun expand() {
        if (children == null) {
            children = state.getChildren().map { Node(it, this) }
        }else TODO("fix this bug! dont suppose to be here! montecarlo alg")
    }

    fun rollOut(maxDepth: Int? = null) {
        var endState = this.state
        var i = 0
        while (!endState.isTerminal && maxDepth?.let { depth + i++ < it } != false) {
            endState = endState.getChildren().random()
        }

        weight += if (endState.perspective == parent?.state?.perspective ?: 2) endState.evaluate() else -endState.evaluate()//.endState.evaluate(/*parent!!.state.perspective*/)
        // console.info("endState weight = ${endState.evaluate()}")
        numOfVisits++
    }

    fun backpropagation() {
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

    fun evaluate(): Double

    fun getChildren(): List<StaticState>

    val perspective: Int
}