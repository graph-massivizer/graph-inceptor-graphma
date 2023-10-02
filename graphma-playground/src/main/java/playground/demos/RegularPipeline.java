package playground.demos;

import magma.control.function.Fn;
import magma.control.traversal.Traversable;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.FlatMap;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.Convert;
import magma.data.sequence.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

public enum RegularPipeline {
    ;

    static void test_1() {
        DataSource<Integer> sink = DataSource.of(1, 2, 3, 4, 5, 6);
        var res = Convert.ToList.build(Fn.function(ArrayList::new))
                .compose(Map.<Integer, String, Pipeline<?, ?>>build(i -> i.toString() + "a"))
                .compose(Filter.<Integer, Pipeline<?, ?>>build(v -> v > 3))
                .apply(sink);
        System.out.println("RESULT: " + res.evaluate());
    }


    static void test_2() {
        var traversable = Traversable.of(
                List.of(1, 2, 3, 4, 5, 6),
                List.of(1, 2, 3, 4, 5, 6),
                List.of(1, 2, 3, 4, 5, 6)
        );
        DataSource<List<Integer>> sink = DataSource.of(traversable);
        var res = Convert.ToList.build(Fn.function(ArrayList::new))
                .compose(Map.<Integer, String, Pipeline<?, ?>>build(i -> i.toString() + "a"))
                .compose(Filter.<Integer, Pipeline<?, ?>>build(v -> v > 3))
                .compose(FlatMap.<Traversable<Integer>, Integer, Pipeline<?, ?>>build(t -> t))
                .compose(Map.<List<Integer>, Traversable<Integer>, Pipeline<?, ?>>build(l -> Traversable.of(l)))
                .apply(sink);
        System.out.println("RESULT: " + res.evaluate());
    }
}
