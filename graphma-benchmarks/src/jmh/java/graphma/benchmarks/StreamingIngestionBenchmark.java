// 		╭━━━╮	    ╭╮ ╭━╮╭━╮
// 		┃╭━╮┃	    ┃┃ ┃┃╰╯┃┃
// 		┃┃╱╰╋━┳━━┳━━┫╰━┫╭╮╭╮┣━━╮
// 		┃┃╭━┫╭┫╭╮┃╭╮┃╭╮┃┃┃┃┃┃╭╮┃
// 		┃╰┻━┃┃┃╭╮┃╰╯┃┃┃┃┃┃┃┃┃╭╮┃
// 		╰━━━┻╯╰╯╰┫╭━┻╯╰┻╯╰╯╰┻╯╰╯
// 				 ┃┃
//

package graphma.benchmarks;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Streaming Ingestion Benchmark for KPI-1.2:
 * "Achieve a streaming ingestion latency below 500 milliseconds for 95% of the data
 * at 1,000s of new edges per second"
 *
 * <p>Architecture Overview:
 * <ul>
 *   <li><b>Producer Thread:</b> Pushes data into a buffer (BlockingQueue) at a controlled high rate</li>
 *   <li><b>Java Stream (Consumer):</b> Reads from the queue, processes the data, and updates the graph</li>
 *   <li><b>Graph:</b> JGraphT wrapped in synchronizedGraph for thread-safety</li>
 *   <li><b>KPI Monitor:</b> Calculates latency (Processing Time - Creation Time) and checks 95th percentile</li>
 * </ul>
 *
 * <p>Run this example with: {@code ./gradlew :graphma-benchmarks:run} or directly via main method.
 */
public class StreamingIngestionBenchmark {

    // KPI Targets
    private static final long TARGET_LATENCY_MS = 500;
    private static final int TARGET_RATE_PER_SEC = 2000; // > 1000s of new edges
    private static final double PERCENTILE_RANK = 0.95;

    public static void main(String[] args) throws InterruptedException {
        // 1. Setup Thread-Safe JGraphT Graph
        // JGraphT is not thread-safe by default, so we synchronize it for concurrent access
        Graph<String, DefaultEdge> graph = Collections.synchronizedGraph(
                new DefaultDirectedGraph<>(DefaultEdge.class)
        );

        // Buffer to decouple Producer and Consumer
        BlockingQueue<EdgeEvent> buffer = new LinkedBlockingQueue<>(10_000);

        // 2. Start the Producer (Simulates Real-Time Data Source)
        Thread producerThread = new Thread(new DataProducer(buffer, TARGET_RATE_PER_SEC));
        producerThread.setDaemon(true);
        producerThread.start();

        // 3. Start the Stream Processing Pipeline
        System.out.println("Starting Ingestion Pipeline...");
        System.out.println("Target: " + TARGET_RATE_PER_SEC + " edges/sec with P95 latency < " + TARGET_LATENCY_MS + "ms");
        System.out.println();

        // We run the stream in a separate thread so main doesn't block immediately
        CompletableFuture.runAsync(() -> processStream(buffer, graph));

        // Let it run for 10 seconds then exit
        Thread.sleep(10_000);

        System.out.println();
        System.out.println("Simulation finished.");
        System.out.println("Final graph size: " + graph.vertexSet().size() + " vertices, " + graph.edgeSet().size() + " edges");
        System.exit(0);
    }

    /**
     * The Core Pipeline: Reads from buffer -> Writes to Graph -> Measures Latency
     */
    private static void processStream(BlockingQueue<EdgeEvent> buffer, Graph<String, DefaultEdge> graph) {
        // Create an infinite stream from the queue
        Stream.generate(() -> {
                    try {
                        // Take blocks if queue is empty, effectively waiting for real-time data
                        return buffer.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                })
                .takeWhile(event -> event != null) // Safety check
                .forEach(event -> {
                    // --- CRITICAL SECTION: Graph Write ---
                    graph.addVertex(event.source);
                    graph.addVertex(event.target);
                    graph.addEdge(event.source, event.target);
                    // -------------------------------------

                    // Calculate Latency
                    long ingestionTime = System.currentTimeMillis();
                    long latency = ingestionTime - event.creationTime;

                    // Update KPI Monitor
                    KpiMonitor.recordLatency(latency);
                });
    }

    // ---------------------------------------------------------
    // Helper Classes
    // ---------------------------------------------------------

    /**
     * Represents a raw data packet with a creation timestamp
     */
    static class EdgeEvent {
        final String source;
        final String target;
        final long creationTime;

        public EdgeEvent(String source, String target) {
            this.source = source;
            this.target = target;
            this.creationTime = System.currentTimeMillis();
        }
    }

    /**
     * Simulates a high-throughput data source
     */
    static class DataProducer implements Runnable {
        private final BlockingQueue<EdgeEvent> queue;
        private final int ratePerSec;

        public DataProducer(BlockingQueue<EdgeEvent> queue, int ratePerSec) {
            this.queue = queue;
            this.ratePerSec = ratePerSec;
        }

        @Override
        public void run() {
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                long start = System.currentTimeMillis();

                // Burst generate edges for this second
                for (int j = 0; j < ratePerSec; j++) {
                    // Generate dummy nodes "Node-0" -> "Node-1", etc.
                    queue.offer(new EdgeEvent("Node-" + i, "Node-" + (i + 1)));
                    i++;
                }

                // Sleep remainder of the second to maintain rate (rough approximation)
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
     * Simple Monitor to track 95th Percentile Latency
     * <p>
     * Note: In production, use HdrHistogram or Micrometer for more accurate measurements.
     */
    static class KpiMonitor {
        // Using a synchronized list here for simplicity of the example.
        private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        private static final AtomicLong counter = new AtomicLong(0);

        public static void recordLatency(long latency) {
            latencies.add(latency);
            long count = counter.incrementAndGet();

            // Periodically report (every 2000 items)
            if (count % 2000 == 0) {
                printStats();
                // clear old stats to keep memory low for long runs
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

                System.out.printf("[KPI Monitor] Processed: %,d | P95 Latency: %d ms | Status: %s%n",
                        counter.get(),
                        p95,
                        (p95 < TARGET_LATENCY_MS ? "✓ PASS" : "✗ FAIL")
                );
            }
        }
    }
}

