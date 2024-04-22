package playground.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.Types;

public class SimpleArrowExample {

    public static void main(String[] args) {
        // Create a new root allocator, which will provide memory management for Arrow.
        try (BufferAllocator allocator = new RootAllocator(Long.MAX_VALUE)) {
            // Create an IntVector instance, which represents a column of integers.
            IntVector intVector = new IntVector("intColumn", FieldType.nullable(Types.MinorType.INT.getType()), allocator);

            try {
                // Allocate memory for storing 5 integers.
                intVector.allocateNew(5);

                // Write data to the buffer.
                for (int i = 0; i < 5; i++) {
                    intVector.set(i, i * 10);  // Store 0, 10, 20, 30, 40
                }
                intVector.setValueCount(5);  // Set how many values are in the vector.

                // Read data from the buffer.
                for (int i = 0; i < intVector.getValueCount(); i++) {
                    System.out.println("Value at index " + i + ": " + intVector.get(i));
                }
            } finally {
                // Clean up resources by closing the vector.
                intVector.close();
            }
        }
    }
}

