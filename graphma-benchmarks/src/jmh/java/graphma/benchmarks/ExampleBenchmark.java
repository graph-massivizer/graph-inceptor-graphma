// 		╭━━━╮	    ╭╮ ╭━╮╭━╮
// 		┃╭━╮┃	    ┃┃ ┃┃╰╯┃┃
// 		┃┃╱╰╋━┳━━┳━━┫╰━┫╭╮╭╮┣━━╮
// 		┃┃╭━┫╭┫╭╮┃╭╮┃╭╮┃┃┃┃┃┃╭╮┃
// 		┃╰┻━┃┃┃╭╮┃╰╯┃┃┃┃┃┃┃┃┃╭╮┃
// 		╰━━━┻╯╰╯╰┫╭━┻╯╰┻╯╰╯╰┻╯╰╯
// 				 ┃┃
//

package graphma.benchmarks;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Example JMH benchmark for GraphMa.
 *
 * Run with: ./gradlew :graphma-benchmarks:jmh
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ExampleBenchmark {

    private String testData;

    @Setup
    public void setup() {
        // Initialize test data here
        // You can create graphs from graphma-core here
        testData = "Hello GraphMa";
    }

    @Benchmark
    public int benchmarkStringLength() {
        return testData.length();
    }

    @Benchmark
    public String benchmarkStringConcat() {
        return testData + " - benchmarked";
    }
}

