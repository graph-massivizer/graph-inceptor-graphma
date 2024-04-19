package generators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomGraphGenerator {
    private int numVertices;
    private List<List<Integer>> adjacencyList;
    private int[][] adjArray;

    public RandomGraphGenerator(int numVertices, int numEdges) {
        this.numVertices = numVertices;
        initializeAdjacencyList(numVertices);
        generateRandomGraph(numEdges);
        convertListToArray();
    }

    private void initializeAdjacencyList(int numVertices) {
        adjacencyList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    private void generateRandomGraph(int numEdges) {
        Random random = new Random();
        Set<String> addedEdges = new HashSet<>();

        while (addedEdges.size() < numEdges) {
            int v1 = random.nextInt(numVertices);
            int v2 = random.nextInt(numVertices);
            if (v1 != v2) {
                String edge = v1 < v2 ? v1 + "-" + v2 : v2 + "-" + v1;
                if (addedEdges.add(edge)) {
                    adjacencyList.get(v1).add(v2);
                    adjacencyList.get(v2).add(v1);
                }
            }
        }
    }

    private void convertListToArray() {
        adjArray = new int[numVertices][];
        for (int i = 0; i < numVertices; i++) {
            List<Integer> neighbors = adjacencyList.get(i);
            adjArray[i] = neighbors.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    public void printGraph() {
        for (int i = 0; i < adjArray.length; i++) {
            System.out.print(i + " -> ");
            for (int j : adjArray[i]) {
                System.out.print(j + " ");
            }
            System.out.println();
        }
    }

    public int[][] getAdjacencyList() {
        return adjArray;
    }

    public int getNumVertices() {
        return numVertices;
    }
}


