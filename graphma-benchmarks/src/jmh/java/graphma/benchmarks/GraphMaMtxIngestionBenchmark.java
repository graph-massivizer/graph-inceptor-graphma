// 		╭━━━╮	    ╭╮ ╭━╮╭━╮
// 		┃╭━╮┃	    ┃┃ ┃┃╰╯┃┃
// 		┃┃╱╰╋━┳━━┳━━┫╰━┫╭╮╭╮┣━━╮
// 		┃┃╭━┫╭┫╭╮┃╭╮┃╭╮┃┃┃┃┃┃╭╮┃
// 		┃╰┻━┃┃┃╭╮┃╰╯┃┃┃┃┃┃┃┃┃╭╮┃
// 		╰━━━┻╯╰╯╰┫╭━┻╯╰┻╯╰╯╰┻╯╰╯
// 				 ┃┃
//

package graphma.benchmarks;

import data.suitesparse.SSDB;
import formats.Mtx;
import magma.control.traversal.Traversable;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.Operator;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.strict.ForNext;
import magma.data.sequence.pipeline.Composer;
import magma.data.sequence.pipeline.Pipe;
import magma.data.sequence.pipeline.Pipeline;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GraphMa MTX File Ingestion Benchmark for KPI-1.2:
 * "Achieve a streaming ingestion latency below 500 milliseconds for 95% of the data
 * at 1,000s of new edges per second"
 *
 * <p>This benchmark demonstrates reading edges from MTX files using GraphMa's
 * pipeline architecture and ingesting them into a JGraphT graph using a proper
 * GraphMa operator pattern (similar to {@code MtxToUndirectedGraph}).
 *
 * <p>Key Features:
 * <ul>
 *   <li>Uses GraphMa's pipeline composition pattern</li>
 *   <li>Implements {@link GraphIngestionOperator} as a proper {@link Pipeline.Stage}</li>
 *   <li>Follows the same pattern as {@code MtxToUndirectedGraph}</li>
 *   <li>Measures ingestion latency per edge and reports P95 latency</li>
 * </ul>
 *
 * <p>Run with: {@code ./gradlew :graphma-benchmarks:runMtxIngestionBenchmark}
 */
public class GraphMaMtxIngestionBenchmark {

    // KPI Targets
    private static final long TARGET_LATENCY_MS = 500;
    private static final double PERCENTILE_RANK = 0.95;

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("GraphMa MTX File Ingestion Benchmark");
        System.out.println("=".repeat(70));
        System.out.println();

        // Reset KPI monitor for fresh measurements
        KpiMonitor.reset();

        System.out.println("Reading edges from SuiteSparse SMALL dataset");
        System.out.println("Target: P95 latency < " + TARGET_LATENCY_MS + "ms");
        System.out.println();

        // =====================================================================
        // GraphMa Pipeline using the proper operator pattern
        // Similar to ClusteringTest: ForNext.build(...).compose(...).apply(...).evaluate()
        // =====================================================================

        System.out.println("--- Running GraphMa Pipeline ---");
        long startTime = System.currentTimeMillis();

        // Build and execute the pipeline using GraphMa's compose pattern
        ForNext.build((Graph<Long, DefaultEdge> graph) -> {
                    System.out.printf("  Ingested graph: %d vertices, %d edges%n",
                            graph.vertexSet().size(), graph.edgeSet().size());
                })
                .compose(GraphIngestionOperator.of(DefaultEdge.class))  // Our custom operator
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 100 && mtx.lines() > 10))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();

        long duration = System.currentTimeMillis() - startTime;

        // Print final results
        System.out.println();
        System.out.println("Pipeline complete:");
        System.out.println("Total time: " + duration + "ms");
        System.out.printf("Edges processed: %,d%n", KpiMonitor.getCount());
        System.out.printf("P95 Latency: %d ms%n", KpiMonitor.getP95Latency());

        // =====================================================================
        // Second run with more graphs to simulate higher throughput
        // =====================================================================

        System.out.println();
        System.out.println("--- Running high-throughput simulation ---");
        KpiMonitor.reset();
        startTime = System.currentTimeMillis();

        // Process more graphs (up to 200 edges per file)
        ForNext.build((Graph<Long, DefaultEdge> graph) -> {
                    // Silent processing for throughput test
                })
                .compose(GraphIngestionOperator.of(DefaultEdge.class))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 200))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();

        duration = System.currentTimeMillis() - startTime;
        long edgesProcessed = KpiMonitor.getCount();
        double edgesPerSecond = duration > 0 ? (edgesProcessed * 1000.0) / duration : 0;

        System.out.println();
        System.out.println("High-throughput simulation complete:");
        System.out.println("Total time: " + duration + "ms");
        System.out.printf("Edges processed: %,d%n", edgesProcessed);
        System.out.printf("Throughput: %.0f edges/second%n", edgesPerSecond);
        System.out.printf("P95 Latency: %d ms%n", KpiMonitor.getP95Latency());

        // Final KPI check
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("KPI-1.2 Assessment:");
        long p95 = KpiMonitor.getP95Latency();
        boolean latencyPassed = p95 < TARGET_LATENCY_MS;
        boolean throughputPassed = edgesPerSecond > 1000;
        System.out.printf("P95 Latency: %d ms (target: < %d ms) - %s%n",
                p95, TARGET_LATENCY_MS, latencyPassed ? "✓ PASS" : "✗ FAIL");
        System.out.printf("Throughput: %.0f edges/sec (target: > 1000 edges/sec) - %s%n",
                edgesPerSecond, throughputPassed ? "✓ PASS" : "✗ FAIL");
        System.out.println("=".repeat(70));
    }

    // =========================================================================
    // GraphMa Operator: GraphIngestionOperator
    // Follows the same pattern as MtxToUndirectedGraph
    // =========================================================================

    /**
     * A GraphMa operator that ingests MTX edge data into a JGraphT directed graph
     * while measuring latency for KPI tracking.
     *
     * <p>This operator follows the same pattern as {@code MtxToUndirectedGraph}:
     * <ul>
     *   <li>Implements as an enum with static factory method</li>
     *   <li>Returns a {@link Composer} for pipeline integration</li>
     *   <li>Uses {@link Pipeline.Stage} and {@link Operator.Transform}</li>
     *   <li>Maintains state (latency measurements) during processing</li>
     * </ul>
     *
     * <p>Usage in a GraphMa pipeline:
     * <pre>{@code
     * ForNext.build(graph -> System.out.println(graph.vertexSet().size()))
     *     .compose(GraphIngestionOperator.of(DefaultEdge.class))
     *     .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 100))
     *     .apply(DataSource.of(SSDB.SMALL))
     *     .evaluate();
     * }</pre>
     */
    public enum GraphIngestionOperator {
        ;

        /**
         * Creates a GraphMa operator that transforms MTX file data into a JGraphT directed graph.
         *
         * @param <V>       The vertex type of the resulting graph (Long for MTX files)
         * @param <E>       The edge class extending {@link DefaultEdge}
         * @param <A>       The traversable collection type containing MTX edge data
         * @param <G>       The graph type extending {@link org.jgrapht.Graph}
         * @param <P>       The pipeline type extending {@link Pipeline}
         * @param edgeClass The class object for the edge type
         * @return A {@link Composer} that creates a {@link Pipeline.Stage} for graph ingestion
         */
        public static <V, E extends DefaultEdge,
                A extends Traversable<Mtx.Long2LongEdge>,
                G extends Graph<V, E>,
                P extends Pipeline<?, ?>>

        Composer<P, Pipeline.Stage<G, P>> of(Class<E> edgeClass) {

            final class _GraphIngestionOperator extends Pipeline.AbstractBase<P> implements Pipeline.Stage<G, P> {

                private _GraphIngestionOperator(final P tail) {
                    super(tail);
                }

                @Override
                public Pipe<A> apply(final Pipe<G> out) {

                    final class IngestionStage extends Operator.Transform<A, G> {

                        private IngestionStage(final Pipe<G> out) {
                            super(out);
                        }

                        @Override
                        public void open(long count) {
                            super.open(count);
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public void onNext(final long index, final A next) {
                            // Create a new directed graph for this MTX file
                            var graph = new DefaultDirectedGraph<Long, E>(edgeClass);

                            // Ingest each edge with latency measurement
                            next.forEach(edge -> {
                                long startNanos = System.nanoTime();

                                // Add vertices and edge to the graph
                                graph.addVertex(edge.source());
                                graph.addVertex(edge.target());
                                graph.addEdge(edge.source(), edge.target());

                                // Record latency
                                long latencyNanos = System.nanoTime() - startNanos;
                                KpiMonitor.recordLatency(latencyNanos / 1_000_000); // Convert to ms
                            });

                            // Yield the completed graph to the next stage
                            this.yield((G) graph);
                        }

                        @Override
                        public void close() {
                            super.close();
                        }
                    }
                    return new IngestionStage(out);
                }
            }
            return _GraphIngestionOperator::new;
        }
    }

    // =========================================================================
    // KPI Monitor - Static utility for latency tracking across pipeline stages
    // =========================================================================

    /**
     * Static KPI monitor for tracking latency measurements across the pipeline.
     */
    static class KpiMonitor {
        private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        private static final AtomicLong counter = new AtomicLong(0);

        public static void reset() {
            synchronized (latencies) {
                latencies.clear();
            }
            counter.set(0);
        }

        public static void recordLatency(long latencyMs) {
            latencies.add(latencyMs);
            counter.incrementAndGet();
        }

        public static long getCount() {
            return counter.get();
        }

        public static long getP95Latency() {
            synchronized (latencies) {
                if (latencies.isEmpty()) return 0;
                List<Long> sorted = new ArrayList<>(latencies);
                Collections.sort(sorted);
                int index = (int) (sorted.size() * PERCENTILE_RANK);
                return sorted.get(Math.min(index, sorted.size() - 1));
            }
        }
    }
}

