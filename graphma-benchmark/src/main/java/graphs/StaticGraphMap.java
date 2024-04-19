package graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticGraphMap {
    private final Map<Integer, List<Integer>> adjList; // Map to hold adjacency lists
    private int numVertices;

    // Constructor to initialize the graph
    public StaticGraphMap(int numVertices, Map<Integer, List<Integer>> adjList) {
        this.numVertices = numVertices;
        this.adjList = adjList;
    }

    // Method to get the neighbors of a vertex
    public List<Integer> getNeighbors(int vertex) {
        return adjList.getOrDefault(vertex, List.of());
    }

    // Method to display the graph
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int vertex : adjList.keySet()) {
            sb.append(vertex).append(": ");
            for (int neighbor : adjList.get(vertex)) {
                sb.append(neighbor).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
