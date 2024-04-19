package benchmarks;

import generators.RandomGraphGenerator;
import graphs.StaticGraphArray;
import graphs.StaticGraphMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(value = 1, jvmArgs = {"-Xms6g", "-Xmx15g"})
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StaticGraphBenchmark {

    private StaticGraphArray graphArray;
    private StaticGraphMap graphMap;

    @Param({"10000000"})
    private int numVertices;

    @Param({"10", "100", "1000", "10000", "1000000", "10000000"})
    private int numEdges;

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(StaticGraphBenchmark.class.getSimpleName())
                .result(Utils.findbenchmarkResultPath().resolve("static_graph.json").toString())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        RandomGraphGenerator generator = new RandomGraphGenerator(numVertices, numEdges);
        int[][] adjListArray = generator.getAdjacencyList();
        graphArray = new StaticGraphArray(numVertices, adjListArray);

        Map<Integer, List<Integer>> adjListMap = generator.getAdjacencyListMap();
        graphMap = new StaticGraphMap(numVertices, adjListMap);
    }

    @Benchmark
    public void traverseAllNeighborsArray(Blackhole blackhole) {
        // Traverse all vertices and access each vertex's neighbors for array implementation
        for (int i = 0; i < numVertices; i++) {
            int[] neighbors = graphArray.getNeighbors(i);
            blackhole.consume(neighbors);  // Consume neighbors to prevent dead code elimination
        }
    }

    @Benchmark
    public void traverseAllNeighborsMap(Blackhole blackhole) {
        // Traverse all vertices and access each vertex's neighbors for map implementation
        for (int i = 0; i < numVertices; i++) {
            List<Integer> neighbors = graphMap.getNeighbors(i);
            blackhole.consume(neighbors);  // Consume neighbors to prevent dead code elimination
        }
    }
}
