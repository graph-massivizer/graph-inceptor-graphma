package formats;

import data.suitesparse.SSDB;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.lazy.Filter;
import magma.data.sequence.operator.lazy.Map;
import magma.data.sequence.operator.strict.ForNext;
import org.junit.jupiter.api.Test;

public class MtxTest {

    @Test
    public void test_ssbd() {
        ForNext.build(System.out::println)
                .compose(Map.build(r -> r + "\n-------\n"))
                .compose(Filter.build((SSDB.MTXFile mtx) -> mtx.lines() < 40 && mtx.lines() > 20))
                .apply(DataSource.of(SSDB.SMALL))
                .evaluate();
    }
}
