package graphma.compute.operator;

import org.jgrapht.Graph;

enum Utils {
    ;

    /**
     * Checks if the graph contains a loop (self-loop).
     *
     * @param graph The graph to check for loops.
     * @param <V> The vertex type.
     * @param <E> The edge type.
     * @return true if there is at least one loop in the graph, false otherwise.
     */
     static <V, E> boolean containsLoop(Graph<V, E> graph) {
        for (E edge : graph.edgeSet()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (source.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
