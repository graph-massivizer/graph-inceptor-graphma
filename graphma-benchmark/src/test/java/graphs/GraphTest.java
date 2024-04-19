package graphs;

import graphs.StaticGraph;
import org.junit.jupiter.api.Test;

final class GraphTest {

    @Test
    public void test_static_graph() {
        // Define the edges of the graph
        int[][] edges = {
                {0, 1},
                {0, 2},
                {1, 2},
                {1, 3}
        };

        // Create the graph
        StaticGraph graph = new StaticGraph(4, edges);

        // Output the graph
        System.out.println("Graph representation:\n" + graph);
        System.out.println("Neighbors of Vertex 1: ");
        for (int neighbor : graph.getNeighbors(1)) {
            System.out.print(neighbor + " ");
        }
    }
}
