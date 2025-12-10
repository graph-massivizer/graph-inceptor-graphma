// 		╭━━━╮	    ╭╮ ╭━╮╭━╮
// 		┃╭━╮┃	    ┃┃ ┃┃╰╯┃┃
// 		┃┃╱╰╋━┳━━┳━━┫╰━┫╭╮╭╮┣━━╮
// 		┃┃╭━┫╭┫╭╮┃╭╮┃╭╮┃┃┃┃┃┃╭╮┃
// 		┃╰┻━┃┃┃╭╮┃╰╯┃┃┃┃┃┃┃┃┃╭╮┃
// 		╰━━━┻╯╰╯╰┫╭━┻╯╰┻╯╰╯╰┻╯╰╯
// 				 ┃┃
//

package graphma.benchmarks;

import magma.adt.value.product.Product2;
import magma.control.traversal.Traversable;
import magma.data.sequence.operator.Operator;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import magma.data.sequence.pipeline.Composer;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GraphMa-style Streaming Ingestion Benchmark for KPI-1.2:
 * "Achieve a streaming ingestion latency below 500 milliseconds for 95% of the data
 * at 1,000s of new edges per second"
 *
 * <p>This benchmark uses GraphMa's pipeline architecture with Magma operators
 * to process streaming edge data and build an in-memory JGraphT graph.
 *
 * <p>Architecture Overview:
 * <ul>
 *   <li><b>Producer Thread:</b> Pushes edge batches into a buffer at a controlled high rate</li>
 *   <li><b>GraphMa Pipeline:</b> Consumes batches and transforms them into graph updates</li>
 *   <li><b>Graph Sink:</b> Writes edges to an in-memory JGraphT graph</li>
 *   <li><b>KPI Monitor:</b> Calculates latency and checks 95th percentile</li>
 * </ul>
 *
 * <p>Run this example with: {@code ./gradlew :graphma-benchmarks:runGraphMaBenchmark}
 */
public class GraphMaStreamingIngestionBenchmark {

    // KPI Targets
    private static final long TARGET_LATENCY_MS = 500;
    private static final int TARGET_RATE_PER_SEC = 2000; // > 1000s of new edges
    private static final double PERCENTILE_RANK = 0.95;
    private static final int BATCH_SIZE = 100; // Process edges in batches for efficiency

    public static void main(String[] args) throws InterruptedException {
        // 1. Setup JGraphT Graph as the sink
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Buffer to decouple Producer and GraphMa Pipeline
        BlockingQueue<EdgeBatch> buffer = new LinkedBlockingQueue<>(1_000);

        // 2. Start the Producer (Simulates Real-Time Data Source)
        Thread producerThread = new Thread(new BatchProducer(buffer, TARGET_RATE_PER_SEC, BATCH_SIZE));
        producerThread.setDaemon(true);
        producerThread.start();

        // 3. Start the GraphMa Pipeline Processing
        System.out.println("Starting GraphMa Ingestion Pipeline...");
        System.out.println("Target: " + TARGET_RATE_PER_SEC + " edges/sec with P95 latency < " + TARGET_LATENCY_MS + "ms");
        System.out.println("Batch size: " + BATCH_SIZE + " edges");
        System.out.println();

        // Run the pipeline in a separate thread
        CompletableFuture.runAsync(() -> runGraphMaPipeline(buffer, graph));

        // Let it run for 10 seconds then exit
        Thread.sleep(10_000);

        System.out.println();
        System.out.println("Simulation finished.");
        System.out.println("Final graph size: " + graph.vertexSet().size() + " vertices, " + graph.edgeSet().size() + " edges");
        System.exit(0);
    }

    /**
     * Runs the GraphMa-style pipeline that consumes edge batches and writes to the graph.
     */
    private static void runGraphMaPipeline(BlockingQueue<EdgeBatch> buffer, Graph<String, DefaultEdge> graph) {
        // Simulate a GraphMa pipeline by processing batches
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EdgeBatch batch = buffer.take();

                // Process the batch through the "pipeline" - in production this would be
                // a full GraphMa Pipeline with stages like:
                // DataSource.of(...) + Filter + Map + GraphSink
                processBatchThroughPipeline(batch, graph);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Processes a batch of edges through a simulated GraphMa pipeline stage.
     * This mimics the Transform operator pattern used in GraphMa.
     */
    private static void processBatchThroughPipeline(EdgeBatch batch, Graph<String, DefaultEdge> graph) {
        // Process each edge in the batch - similar to Operator.Transform.onNext()
        batch.edges.forEach(edge -> {
            // Graph mutation (equivalent to GraphMa sink operation)
            graph.addVertex(edge.source);
            graph.addVertex(edge.target);
            graph.addEdge(edge.source, edge.target);

            // Calculate and record latency
            long latency = System.currentTimeMillis() - edge.creationTime;
            KpiMonitor.recordLatency(latency);
        });
    }

    // ---------------------------------------------------------
    // GraphMa-style Pipeline Stage Example (for reference)
    // ---------------------------------------------------------

    /**
     * Example of a GraphMa-compatible graph building stage.
     * This demonstrates how to create a custom Pipeline.Stage for graph ingestion.
     *
     * <p>Usage in a full GraphMa pipeline would be:
     * <pre>{@code
     * DataSource.of(edgeTraversable)
     *     .then(GraphBuildingStage.of(DefaultEdge.class))
     *     .then(ForNext.of(graph -> System.out.println(graph.vertexSet().size())))
     *     .run();
     * }</pre>
     */
    public static <V, E extends DefaultEdge,
            A extends Traversable<Product2<String, String>>,
            G extends Graph<V, E>,
            P extends Pipeline<?, ?>>
    Composer<P, Pipeline.Stage<G, P>> graphBuildingStage(Class<E> edgeClass) {

        final class GraphBuildingStage extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {

            private GraphBuildingStage(final P tail) {
                super(tail);
            }

            @Override
            public Pipe<A> apply(final Pipe<G> out) {
                final class BuildStage extends Operator.Transform<A, G> {
                    private BuildStage(final Pipe<G> out) {
                        super(out);
                    }

                    @Override
                    public void open(long count) {
                        super.open(count);
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onNext(final long index, final A next) {
                        var graph = new DefaultDirectedGraph<String, E>(edgeClass);
                        next.forEach(edge -> {
                            graph.addVertex(edge._1());
                            graph.addVertex(edge._2());
                            graph.addEdge(edge._1(), edge._2());
                        });
                        this.yield((G) graph);
                    }

                    @Override
                    public void close() {
                        super.close();
                    }
                }
                return new BuildStage(out);
            }
        }
        return GraphBuildingStage::new;
    }

    // ---------------------------------------------------------
    // Helper Classes
    // ---------------------------------------------------------

    /**
     * Represents a single edge with creation timestamp for latency measurement.
     */
    static class TimestampedEdge {
        final String source;
        final String target;
        final long creationTime;

        TimestampedEdge(String source, String target) {
            this.source = source;
            this.target = target;
            this.creationTime = System.currentTimeMillis();
        }
    }

    /**
     * A batch of edges for efficient processing through the pipeline.
     * Batching reduces overhead and improves throughput.
     */
    static class EdgeBatch {
        final List<TimestampedEdge> edges;

        EdgeBatch(List<TimestampedEdge> edges) {
            this.edges = edges;
        }
    }

    /**
     * Produces batches of edges at a controlled rate.
     * This simulates a real-time data source feeding into a GraphMa pipeline.
     */
    static class BatchProducer implements Runnable {
        private final BlockingQueue<EdgeBatch> queue;
        private final int ratePerSec;
        private final int batchSize;

        BatchProducer(BlockingQueue<EdgeBatch> queue, int ratePerSec, int batchSize) {
            this.queue = queue;
            this.ratePerSec = ratePerSec;
            this.batchSize = batchSize;
        }

        @Override
        public void run() {
            int nodeId = 0;
            int batchesPerSecond = ratePerSec / batchSize;

            while (!Thread.currentThread().isInterrupted()) {
                long start = System.currentTimeMillis();

                // Generate batches for this second
                for (int b = 0; b < batchesPerSecond; b++) {
                    List<TimestampedEdge> edges = new ArrayList<>(batchSize);
                    for (int j = 0; j < batchSize; j++) {
                        edges.add(new TimestampedEdge("Node-" + nodeId, "Node-" + (nodeId + 1)));
                        nodeId++;
                    }
                    queue.offer(new EdgeBatch(edges));
                }

                // Sleep remainder of the second to maintain rate
                long elapsed = System.currentTimeMillis() - start;
                long sleep = 1000 - elapsed;
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Simple Monitor to track 95th Percentile Latency.
     * <p>
     * Note: In production, use HdrHistogram or Micrometer for more accurate measurements.
     */
    static class KpiMonitor {
        private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        private static final AtomicLong counter = new AtomicLong(0);

        public static void recordLatency(long latency) {
            latencies.add(latency);
            long count = counter.incrementAndGet();

            // Periodically report (every 2000 items)
            if (count % 2000 == 0) {
                printStats();
                synchronized (latencies) {
                    latencies.clear();
                }
            }
        }

        private static void printStats() {
            synchronized (latencies) {
                if (latencies.isEmpty()) return;
                Collections.sort(latencies);
                int index95 = (int) (latencies.size() * PERCENTILE_RANK);
                long p95 = latencies.get(Math.min(index95, latencies.size() - 1));

                System.out.printf("[GraphMa KPI Monitor] Processed: %,d | P95 Latency: %d ms | Status: %s%n",
                        counter.get(),
                        p95,
                        (p95 < TARGET_LATENCY_MS ? "✓ PASS" : "✗ FAIL")
                );
            }
        }
    }
}

