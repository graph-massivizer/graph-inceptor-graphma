package graphma.compute.operator;

import data.suitesparse.SSDB;
import graphma.compute.operator.cluster.LabelPropagation;
import graphma.compute.operator.transform.MtxToJGraphTGraph;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;

final class ClusterTest {

    @Test
    public void test_label_propagation() {
        LabelPropagation.print()
                .compose(LabelPropagation.cluster())
                .compose(MtxToJGraphTGraph.graph(DefaultEdge.class))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
