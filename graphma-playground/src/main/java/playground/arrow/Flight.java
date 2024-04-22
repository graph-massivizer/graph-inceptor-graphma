package playground.arrow;

import org.apache.arrow.flight.*;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import java.util.Collections;

public enum Flight {
    ;

    // Define the Flight Producer
    static class SampleProducer extends NoOpFlightProducer {
        @Override
        public void getStream(CallContext context, Ticket ticket, ServerStreamListener listener) {
            Field field = new Field("intColumn", FieldType.nullable(org.apache.arrow.vector.types.Types.MinorType.INT.getType()), null);
            Schema schema = new Schema(Collections.singletonList(field));
            VectorSchemaRoot root = VectorSchemaRoot.create(schema, new RootAllocator());
            listener.start(root);



            IntVector intVector = (IntVector) root.getVector("intColumn");
            intVector.allocateNew(10);
            intVector.setValueCount(10);
            for (int i = 0; i < 10; i++) {
                intVector.set(i, i);
            }
            listener.putNext();
            listener.completed();


            // You'd typically set the data for your vector here
            // For simplicity, we're just signaling the end of the stream without adding data
            listener.completed();
        }
    }

    public static void main(String[] args) {
        // Start the Producer on a new thread
        Thread producerThread = new Thread(() -> {
            try {
                FlightServer server = FlightServer.builder(
                        new RootAllocator(Long.MAX_VALUE),
                        Location.forGrpcInsecure("localhost", 9090),
                        new SampleProducer()
                ).build();
                server.start();
                System.out.println("Producer started at localhost:9090");
                server.awaitTermination();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        producerThread.start();
        // Give some time for the producer to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start the Consumer on a new thread
        Thread consumerThread = new Thread(() -> {
            final BufferAllocator bufferAllocator = new RootAllocator(Long.MAX_VALUE);
            // Connect to the Arrow Flight server
            try (FlightClient client = FlightClient.builder()
                    .allocator(bufferAllocator)
                    .location(Location.forGrpcInsecure("localhost", 9090))
                    .build()) {
                // Define the Flight Descriptor (tells the server what data you want)
                FlightDescriptor descriptor = FlightDescriptor.path("sample");
                System.out.println(descriptor);
                // Retrieve stream from server
                FlightStream stream = client.getStream(new Ticket(new byte[0]));
                // Process incoming data
                while (stream.next()) {
                    VectorSchemaRoot root = stream.getRoot();
                    IntVector intVector = (IntVector) root.getVector("intColumn");
                    for (int i = 0; i < root.getRowCount(); i++) {
                        System.out.println(intVector.get(i));
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        consumerThread.start();
    }
}

