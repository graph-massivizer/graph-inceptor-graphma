package graphs;

public class StaticGraphArray {
    private final int[][] adjList; // Array to hold adjacency lists
    private int numVertices;

    // Constructor to initialize the graph
    public StaticGraphArray(int numVertices, int[][] adjList) {
        this.numVertices = numVertices;
        this.adjList = adjList;
    }

    // Method to get the neighbors of a vertex
    public int[] getNeighbors(int vertex) {
        return adjList[vertex];
    }

    // Method to display the graph
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adjList.length; i++) {
            sb.append(i).append(": ");
            for (int vertex : adjList[i]) {
                sb.append(vertex).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
