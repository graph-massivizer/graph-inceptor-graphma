package playground.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.complex.ListVector;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;

import java.util.concurrent.ThreadLocalRandom;

public class ArrowAdjacencyList {
    public static void main(String[] args) {
        try (BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE)) {
            // Define the inner type for the ListVector, which is an integer.
            Field innerIntField = new Field("int", FieldType.nullable(new ArrowType.Int(32, true)), null);
            ListVector listVector = ListVector.empty("adjList", allocator);
            listVector.initializeChildrenFromFields(java.util.Collections.singletonList(innerIntField));

            try {
                listVector.allocateNew();

                // Adding data to the ListVector
                for (int i = 0; i < 5; i++) {
                    listVector.startNewValue(i);
                    IntVector innerVector = (IntVector) listVector.getChildrenFromFields().get(0);
                    innerVector.allocateNewSafe();


                    var numbNeighbours = ThreadLocalRandom.current().nextInt(5);

                    System.out.println("NUM NEI: " + numbNeighbours);
                    for (int j = 0; j < numbNeighbours; j++) {
                        var rand = ThreadLocalRandom.current().nextInt(5);
                        System.out.println("RAN: " + rand);
                        innerVector.setSafe(j, rand);
                    }
                    innerVector.setValueCount(numbNeighbours);
//                    // Set an example connection from each vertex to another
//                    innerVector.setSafe(0, (i + 1) % 5); // Link to the next vertex
//                    innerVector.setValueCount(1); // Confirm the number of entries in the inner vector

                    listVector.endValue(i, 1);
                }
                listVector.setValueCount(5); // Confirm the number of vertices

                // Reading data from ListVector

                System.out.println("VALUE: " + listVector.getValueCount());
                for (int i = 0; i < listVector.getValueCount(); i++) {
                    System.out.println(listVector.getDataVector());
//                    IntVector innerVector = (IntVector) listVector.getVector();
//                    System.out.print("Vertex " + i + " connects to: ");
//                    for (int j = 0; j < innerVector.getValueCount(); j++) {
//                        System.out.print(innerVector.get(j) + " ");
//                    }
//                    System.out.println();
                }
            } finally {
                listVector.close();
            }
        }
    }
}

