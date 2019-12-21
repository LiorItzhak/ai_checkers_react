package model.algorithm

import kotlinx.coroutines.delay
import kotlin.math.*


@Suppress("UNCHECKED_CAST")
class MonteCarloTreeSearch<T : StaticState>(private val ucb1Alpha: Double = 1.414,
                                            private val usePreviousSearchInfo: Boolean = true,
                                            private val returnWhenOnlyOneOptionAvailable: Boolean = false,
                                            cacheStates: Boolean = true) {
    suspend fun search(rootState: T, maxIterations: Int? = null, maxDepth: Int? = null, onChooseChanged: ((T) -> Unit)? = null): T {
        numOfUsedCachedStates = 0//debug
        val rootNode = cachedChosenNode?.children?.firstOrNull { node -> node.state == rootState } ?: Node(rootState)
        rootNode.parent = null
        val n = rootNode.numOfVisits;
        val d = rootNode.depth
        var chosenNode: Node<T>? = null
        try {
            if (returnWhenOnlyOneOptionAvailable||!usePreviousSearchInfo)
                rootNode.state.getChildren()
                        .let { if (it.size == 1) rootNode.children?.get(0)?.let {c-> chosenNode = c; return c.state }}

            while (maxIterations?.let { rootNode.numOfVisits - n < it } != false) {
                searchIteration(rootNode, maxDepth?.plus(rootNode.depth))
                rootNode.children?.maxBy { it.numOfVisits }?.let {
                    if (chosenNode != it) {
                        chosenNode = it
                        console.log("debug: MCTS backup :${it.state} = ${it.estimate()}% | ${it.numOfVisits}/${rootNode.numOfVisits}")
                        onChooseChanged?.invoke(it.state)
                    }
                }
                //only for single threaded environments - allows context switch//
                if (rootNode.numOfVisits % 100 == 0) {
                    delay(1);console.info("mcts v =${chosenNode!!.estimate()}% | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
                }
            }
        } finally {
            statesCache?.clear()
            cachedChosenNode = if (usePreviousSearchInfo) chosenNode?.apply { parent = null } else null
            val mean = chosenNode?.let { it.weight / it.numOfVisits } ?: 0.0
            val variance = chosenNode?.let { it.weights.map { w -> (w - mean).pow(2) }.sum() / (it.weights.size-1) } ?: 0.0
            val std = sqrt(variance)
            val sd = chosenNode?.let { std / sqrt(it.numOfVisits.toDouble()) } ?: 0.0
           // console.log("finally: ${chosenNode?.weights?.map { w -> (w - mean).pow(2) }?.sum()}  // ${chosenNode?.weights?.size}")
           // console.log("finally: ${chosenNode?.weights}")

            console.info("finally: value= ${chosenNode?.estimate()} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits} | visits from previous search= $n,depth=$d |used cached state =$numOfUsedCachedStates")
            console.info("finally: mean= ${(mean*1000).roundToLong()/1000.0} +- ${(2 * sd*1000).roundToLong()/1000.0} with 95% confidence, sample std ${(std*1000).roundToLong()/1000.0}")
        }
        return chosenNode!!.state
    }


    private val ucb1: Node<*>.() -> Double = {
        when (numOfVisits) {
            0 -> Double.POSITIVE_INFINITY
            else -> weight / numOfVisits + ucb1Alpha * sqrt(ln(parent!!.numOfVisits.toDouble()) / numOfVisits)
        }
    }

    private fun Node<*>.estimate() = ((weight / numOfVisits) * 1000).roundToLong() /1000.0

    private fun searchIteration(rootNode: Node<T>, maxDepth: Int? = null) {

        //Selection
        var node = rootNode
        while (!node.isLeaf) {
            node = node.children!!.maxBy(ucb1)!!
        }

        if (node.numOfVisits != 0 && maxDepth?.let { node.depth < it } != false) {
            //Expand
            node.expand()
            node = node.children!!.firstOrNull() ?: node//if the node have no children use the node itself
        }

        //Simulate
        val evaluation = node.rollOut(maxDepth)

        //Backpropagation
        node.backpropagation(evaluation)
    }


    ///caching
    private var cachedChosenNode: Node<T>? = null
    private val statesCache: MutableMap<StaticState, List<StaticState>>? = if (cacheStates) mutableMapOf() else null
    var numOfUsedCachedStates = 0

    inner class Node<T : StaticState>(val state: T, var parent: Node<T>? = null) {
        val depth: Int = (parent?.depth ?: -1) + 1
        var numOfVisits: Int = 0
            private set
        var weight: Double = 0.0
            private set
        var children: List<Node<T>>? = null
            private set
        val isLeaf: Boolean
            get() = children?.isEmpty() != false //null or true

        val weights :MutableList<Double> by lazy { mutableListOf<Double>() }


        fun expand() {
            if (children == null) {
                children = (statesCache?.let { numOfUsedCachedStates++;it.getOrElse(state) { numOfUsedCachedStates--;null } } ?: state.getChildren()).map { Node(it as T, this) }
                statesCache?.remove(state)
            }
        }

        fun rollOut(maxDepth: Int? = null): StaticState {
            var endState = this.state
            var i = 0
            while (maxDepth?.let { depth + i++ < it } != false/*null or true*/ && !endState.isTerminal) {
                val children = (statesCache?.let { numOfUsedCachedStates++; it.getOrElse(endState) { numOfUsedCachedStates--;null } })
                        ?: endState.getChildren().apply { statesCache?.set(endState, this) }
                endState = children.random() as T
            }
            return endState
        }

        fun backpropagation(state: StaticState) {
            var ancestor: Node<T>? = this
            val evaluations = mutableMapOf<Int, Double>()
            while (ancestor != null) {
                ancestor.numOfVisits++
                val ancestorParent = ancestor.parent
                if(ancestorParent!=null){
                    val w = evaluations.getOrPut(ancestorParent.state.perspective) { state.evaluate(ancestorParent.state.perspective) }
                    ancestor.weight += w
                    if(ancestorParent.parent==null)
                        ancestor.weights.add(w)
                }
                ancestor = ancestor.parent
            }
        }


    }

}


interface StaticState {
    val isTerminal: Boolean

    fun evaluate(perspective: Int): Double //0 to 1

    fun getChildren(): List<StaticState>

    val perspective: Int

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}