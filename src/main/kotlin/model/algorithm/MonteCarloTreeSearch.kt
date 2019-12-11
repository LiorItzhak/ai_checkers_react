package model.algorithm

import kotlinx.coroutines.delay
import kotlin.math.ln
import kotlin.math.sqrt


class MonteCarloTreeSearch<T : StaticState> {
   suspend fun search(rootState: T, maxIterations: Int? = null, maxDepth: Int? = null, onChooseChanged :((T)->Unit)?=null): T {
        val rootNode = Node(rootState)
        rootNode.state.getChildren().also { if (it.size == 1) return it[0] as T }
        var chosenNode : Node?=null
        var i = 0
        while (maxIterations?.let { i++ < it } != false) {
             searchIteration(rootNode, maxDepth)
            rootNode.children?.maxBy { it.numOfVisits }?.let {
                if(it!=chosenNode) {
                    chosenNode = it
                    onChooseChanged?.invoke(it.state as T)
                }
            }
            if(i%200==0) delay(1)//only for single threaded environments - allows context switch
        }
        return rootNode.children?.maxBy { it.numOfVisits }!!.state as T
    }


    private val ucb1: Node.() -> Double = {
        when (numOfVisits) {
            0 -> Double.POSITIVE_INFINITY
            else -> {
                val perspectiveWeight = if (state.perspective == parent!!.state.perspective) weight else -weight
                perspectiveWeight / numOfVisits + 2 * sqrt(ln(parent.numOfVisits.toDouble()) / numOfVisits)
            }
        }
    }

    private fun searchIteration(rootNode: Node, maxDepth: Int? = null) {

        //Selection
        var node = rootNode
        while (!node.isLeaf) {
            node = node.children?.maxBy(ucb1)!!
        }


        if (node.numOfVisits != 0 && maxDepth?.let { node.depth < it } != false) {
            //Expand
            node.expand()
            node = node.children?.getOrNull(0) ?: return
        }

        //Simulate
        node.rollOut(maxDepth)

        //Backpropagation
        node.backpropagation()
    }
}


class Node(val state: StaticState, val parent: Node? = null) {
    val depth: Int = parent?.depth?.apply { +1 } ?: 0
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
        }
    }

    fun rollOut(maxDepth: Int? = null) {
        var endState = this.state
        var i = 0
        while (!endState.isTerminal && maxDepth?.let { depth + i++ < it } != false) {
            endState = endState.getChildren().random()
        }

        weight += if (state.perspective == endState.perspective) endState.evaluate() else -endState.evaluate()
        numOfVisits++
    }

    fun backpropagation() {
        var ancestor = this.parent
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