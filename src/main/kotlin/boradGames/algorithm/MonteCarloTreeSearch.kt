package boradGames.algorithm

import kotlinx.coroutines.delay
import kotlin.math.*


/**
 *This class uses to create a Monte-Carlo Tree Search to determine the most promising action to take
 *
 * @param ucb1Alpha is used to change the UCB1 constance
 * @param usePreviousSearchInfo used to enable the search use previous calculations (previous search) as a base for the new one
 * @param returnWhenOnlyOneOptionAvailable use to force the search to stop immediately if only one action available,
 * use it only if [usePreviousSearchInfo] enabled (true)
 * @param cacheStates uses to enable the search to cache states (may require a lot of memory)
 * @param logger is a function that call when a log is available.
 */
class MonteCarloTreeSearch<T : StaticState>(private val ucb1Alpha: Double = 1.414,
                                            private val usePreviousSearchInfo: Boolean = true,
                                            private val returnWhenOnlyOneOptionAvailable: Boolean = false,
                                            cacheStates: Boolean = true,
                                            private val logger: ((String) -> Unit)? = null) {


    /**
     * This function start a monte-carlo search from the given state
     * @param rootState is the state that you want to evaluate its children
     * @param maxDepth allow you to limit the depth of the tree (and the simulations)
     * use it only if you can evaluate any state (not only the terminals)
     * the [maxDepth] usually used if you have a vert deep problem (or endless problem)
     * @param maxIterations is the maximum iteration to calculate, you must use this parameter if you dont stop the search by yourself
     * @param onChooseChanged is a function that called when the most promising child is changed
     */
    suspend fun search(rootState: T, maxIterations: Int? = null, maxDepth: Int? = null, onChooseChanged: ((T) -> Unit)? = null): T {
        numOfUsedCachedStates = 0//only for debugging
        //if previous calculations cached, use the cached node as root
        var cachedNodes = cachedChosenNode?.children
        while (cachedNodes != null && cachedNodes.isNotEmpty() && cachedNodes[0].state.perspective != rootState.perspective) {
            cachedNodes = cachedNodes.flatMap { node -> node.children.orEmpty() }
        }
        //initialize the root node, if cacheStates take from it from the previous tree, else create a new node
        val rootNode = cachedNodes?.firstOrNull { node -> node.state == rootState }?.apply { parent = null } ?: Node(rootState)
        //initialize the number of visits and the depth, uses for update the previous cached calculations if the cacheStates enable
        val numOfVisitsAtStart = rootNode.numOfVisits
        var chosenNode: Node<T>? = null
        try {
            if (returnWhenOnlyOneOptionAvailable || !usePreviousSearchInfo) {
                //if only one next children available return it without any calculation
                rootNode.state.getChildren()
                        .let { if (it.size == 1) rootNode.children?.get(0)?.let { c -> chosenNode = c; return c.state } }
            }

            //while the maxIterations haven't reached, do another iteration
            while (maxIterations?.let { rootNode.numOfVisits - numOfVisitsAtStart < it } != false) {
                searchIteration(rootNode, maxDepth?.plus(rootNode.depth))
                //get the child that have been visited the most (most promising child)
                rootNode.children?.maxBy { it.numOfVisits }?.let {
                    if (chosenNode != it) {
                        //if the new most promising child is different than the last one, change it and notify the observers
                        chosenNode = it
                        logger?.invoke("debug: MCTS backup :${it.state} = ${it.estimate()} | ${it.numOfVisits}/${rootNode.numOfVisits}")
                        onChooseChanged?.invoke(it.state)
                    }
                }
                //only for single threaded environments - allows context switch//
                if (rootNode.numOfVisits % 150 == 0) {
                    delay(1); logger?.invoke("mcts weight= ${chosenNode!!.estimate()} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits}")
                }
            }
        } finally {
            //clear cache and log the final results to the console
            statesCache?.clear()
            cachedChosenNode = if (usePreviousSearchInfo) chosenNode?.apply { parent = null } else null
            //calculate the final result statistics
            logger?.let {l->
                val mean = chosenNode?.let { it.weight / it.numOfVisits } ?: 0.0
                val variance = chosenNode?.let { it.weights.map { w -> (w - mean).pow(2) }.sum() / (it.weights.size - 1) }
                        ?: 0.0
                val std = sqrt(variance)
                val sd = chosenNode?.let { std / sqrt(it.numOfVisits.toDouble()) } ?: 0.0
                val log1 = "finally: weight= ${chosenNode?.estimate()} | ${chosenNode!!.numOfVisits}/${rootNode.numOfVisits} | visits from previous search= $numOfVisitsAtStart,depth=${rootNode.depth} |used cached state =$numOfUsedCachedStates"
                val log2 = "finally: mean= ${(mean * 1000).roundToLong() / 1000.0} +- ${(2 * sd * 1000).roundToLong() / 1000.0} with 95% confidence, sample std ${(std * 1000).roundToLong() / 1000.0}"
                l.invoke(log1); l.invoke(log2)
            }
        }
        return chosenNode?.state ?: throw Exception("their is no chosen node")
    }


    private val ucb1: Node<*>.() -> Double = {
        val parent = parent ?: throw IllegalStateException("cant calculate ucb1 formula without a parent node")
        when (numOfVisits) {
            0 -> Double.POSITIVE_INFINITY
            else -> weight / numOfVisits + ucb1Alpha * sqrt(ln(parent.numOfVisits.toDouble()) / numOfVisits)
        }
    }


    private fun searchIteration(rootNode: Node<T>, maxDepth: Int? = null) {

        //Selection
        var node = rootNode
        while (!node.isLeaf) {
            node = node.children!!.maxBy(ucb1)!!
        }

        //if the node has been visited (simulated) before and the node is not deeper than the maxDepth, expand it
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


    //uses for logging
    private fun Node<*>.estimate() = ((weight / numOfVisits) * 1000).roundToLong() / 1000.0

    ///caching
    private var cachedChosenNode: Node<T>? = null
    private val statesCache: MutableMap<StaticState, List<StaticState>>? = if (cacheStates) mutableMapOf() else null
    private var numOfUsedCachedStates = 0

    private inner class Node<T : StaticState>(val state: T, var parent: Node<T>? = null) {
        val depth: Int = (parent?.depth ?: -1) + 1
        var numOfVisits: Int = 0
            private set
        var weight: Double = 0.0
            private set
        var children: List<Node<T>>? = null
            private set
        val isLeaf: Boolean
            get() = children?.isEmpty() != false //null or true

        val weights: MutableList<Double> by lazy { mutableListOf<Double>() }

        @Suppress("UNCHECKED_CAST")
        fun expand() {
            if (children == null) {
                children = (statesCache?.let { numOfUsedCachedStates++;it.getOrElse(state) { numOfUsedCachedStates--;null } }
                        ?: state.getChildren()).map { Node(it as T, this) }
                statesCache?.remove(state)
            }
        }

        fun rollOut(maxDepth: Int? = null): StaticState {
            var endState  :StaticState = this.state
            var i = 0
            while (maxDepth?.let { depth + i++ < it } != false/*null or true*/ && !endState.isTerminal) {
                val children = (statesCache?.let { numOfUsedCachedStates++; it.getOrElse(endState) { numOfUsedCachedStates--;null } })
                        ?: endState.getChildren().apply { statesCache?.set(endState, this) }
                endState = children.random()
            }
            return endState
        }

        fun backpropagation(state: StaticState) {
            var ancestor: Node<T>? = this
            val evaluations = mutableMapOf<Int, Double>()
            while (ancestor != null) {
                ancestor.numOfVisits++
                val ancestorParent = ancestor.parent
                if (ancestorParent != null) {
                    val w = evaluations.getOrPut(ancestorParent.state.perspective) { state.evaluate(ancestorParent.state.perspective) }
                    ancestor.weight += w
                    if (ancestorParent.parent == null)
                        ancestor.weights.add(w)
                }
                ancestor = ancestor.parent
            }
        }


    }

}


/**
 * This interface
 */
interface StaticState {
    /**
     * @return true if this state has no children
     * note that you can simply just return if [getChildren] is empty
     * This value will improve the performance if you can determine if a state is terminal without calculating its children
     */
    val isTerminal: Boolean

    /**
     * This function uses to evaluate the score of the state from a specific perspective
     * @return a score of type double, usually normalized (range between 0 to 1 or -1 to 1)
     * if you dont limit the depth of the searches you can only evaluate the terminal states
     * Note that if you limit the depth you MUST implement a valid a evaluation to any state
     */
    fun evaluate(perspective: Int): Double

    /**
     * @return the child states of this state, if none return an empty list
     */
    fun getChildren(): List<StaticState>

    /**
     * This value is representing the state perspective
     * You can use this parameter to create a multi-player tree with different perspective (goals)
     */
    val perspective: Int

    /**
     * For caching you must implements [equals] and [hashCode]
     */
    override fun equals(other: Any?): Boolean

    /**
     * For caching you must implements [equals] and [hashCode]
     */
    override fun hashCode(): Int
}