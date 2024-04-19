package playground.demos;

import data.suitesparse.SSDB;
import formats.Mtx;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Peek;
import magma.data.sequence.operator.lazy.Take;
import magma.data.sequence.operator.strict.ForNext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import static data.Config.TEMP;
import static playground.storage.Persist.exporter;

public enum GraphToFile {
    ;

    public static void main(String[] args) {
        ForNext.build((Graph<Long, DefaultEdge> graph) -> exporter(TEMP.resolve("test.graphml"), "graphml").accept(graph))
                .compose(Peek.build(System.out::println))
                .compose(GraphPipeline.Graph.graph(DefaultEdge.class))
                .compose(Take.build(1))
                .compose(Filter.build((Mtx.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
