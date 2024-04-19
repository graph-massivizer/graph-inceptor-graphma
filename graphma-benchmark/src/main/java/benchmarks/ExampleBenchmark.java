package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ExampleBenchmark {

    // Parameters example, you can run benchmarks for each parameter with different values
    @Param({"100", "200", "300"})
    private int size;

    private String baseString;
    private int[] numbers;

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ExampleBenchmark.class.getSimpleName())
                .result(Utils.findbenchmarkResultPath().resolve("example.json").toString())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }

    // Setup method for preparing data for each benchmark
    @Setup(Level.Trial)
    public void setup() {
        baseString = "testString";
        numbers = new int[size];
        for (int i = 0; i < size; i++) {
            numbers[i] = i;
        }
    }

    @Benchmark
    public String testStringConcatenation() {
        String result = baseString;
        for (int i = 0; i < size; i++) {
            result += "a";
        }
        return result;
    }

    @Benchmark
    public int testSummation() {
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }

    @Benchmark
    public void init() {
        // This is a dummy benchmark method
        int a = 1;
        int b = 2;
        int sum = a + b;
    }
}