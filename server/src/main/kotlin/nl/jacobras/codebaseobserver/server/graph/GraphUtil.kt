package nl.jacobras.codebaseobserver.server.graph

import org.jgrapht.Graph
import org.jgrapht.alg.scoring.BetweennessCentrality
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph

object GraphUtil {

    fun calculateBetweennessCentralityScore(modules: Map<String, List<String>>): Map<String, Double> {
        val betweennessCentrality = BetweennessCentrality(toGraph(modules))
        return betweennessCentrality.scores.map { it.key to it.value }.toMap()
    }

    private fun toGraph(modules: Map<String, List<String>>): Graph<String, DefaultEdge> {
        val graph = DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge::class.java)
        for ((module, dependencies) in modules) {
            graph.addVertex(module)
            for (dependency in dependencies) {
                graph.addVertex(dependency)
                graph.addEdge(module, dependency)
            }
        }

        return graph
    }
}