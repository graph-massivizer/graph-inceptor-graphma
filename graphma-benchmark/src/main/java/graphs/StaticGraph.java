package graphs;

public class StaticGraph {
    private final int[][] adjList; // Array to hold adjacency lists
    private int numVertices;

    // Constructor to initialize the graph
    public StaticGraph(int numVertices, int[][] edges) {
        this.numVertices = numVertices;
        adjList = new int[numVertices][];

        // Temporary storage to count edges for each vertex
        int[] edgeCount = new int[numVertices];
        for (int[] edge : edges) {
            edgeCount[edge[0]]++;
            edgeCount[edge[1]]++;
        }

        // Initialize the adjacency lists
        for (int i = 0; i < numVertices; i++) {
            adjList[i] = new int[edgeCount[i]];
        }

        // Reset edge count for actual edge insertion
        int[] currentEdgeIndex = new int[numVertices];

        // Populate the adjacency lists
        for (int[] edge : edges) {
            int vertex1 = edge[0];
            int vertex2 = edge[1];
            adjList[vertex1][currentEdgeIndex[vertex1]++] = vertex2;
            adjList[vertex2][currentEdgeIndex[vertex2]++] = vertex1;
        }
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
