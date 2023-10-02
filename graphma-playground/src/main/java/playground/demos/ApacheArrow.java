package playground.demos;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.IntVector;

public enum ApacheArrow {
    ;

    // simple example
    static void test_1() {
        try(
                BufferAllocator allocator = new RootAllocator();
                IntVector intVector = new IntVector("fixed-size-primitive-layout", allocator);
        ){
            intVector.allocateNew(3);
            intVector.set(0,1);
            intVector.setNull(1);
            intVector.set(2,2);
            intVector.setValueCount(3);
            System.out.println("Vector created in memory: " + intVector);
        }
    }


}
