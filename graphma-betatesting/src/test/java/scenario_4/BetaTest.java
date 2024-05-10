package scenario_4;

import formats.Mtx;
import magma.adt.control.traversal.Traversal;
import magma.control.function.Fn1;
import magma.control.traversal.Traverser;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;
import magma.data.sequence.operator.strict.ForNext;
import magma.value.index.Range;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Objects;

import static java.lang.Math.min;

public class BetaTest {

    static final Path TEST_FILE = Path
            .of(System.getProperty("user.dir"))
            .resolve("graphma-betatesting")
            .resolve("src")
            .resolve("test")
            .resolve("java")
            .resolve("scenario_4")
            .resolve("edgeList.csv");

    @Test
    public void test_scenario_4_tryNext() {
        var traverser = new EdgeListTraverser(null, 0, TEST_FILE); // TODO adjust
        while (traverser.tryNext(e -> System.out.println(e.source() + " --> " + e.target())));
    }

    @Test
    public void test_scenario_4_forNext() {
        var traverser = new EdgeListTraverser(null, 0, TEST_FILE); // TODO adjust
        var dataSource = DataSource.of(traverser);
        ForNext.build(System.out::println)
                .apply(dataSource)
                .evaluate();
    }

    @Test
    public void test_scenario_4_whileNext() {
        var traverser = new EdgeListTraverser(null, 0, TEST_FILE); // TODO adjust
        var dataSource = DataSource.of(traverser);
        Seq.of(dataSource)
                .anyMatch(edge -> Objects.equals(edge, 1));
    }

    /**
     * Implementation of a {@link Traverser} specific to edge list csv files, capable of parsing edges from the file's content.
     */
    static final class EdgeListTraverser extends Traversal.Control.Context implements Traverser<Mtx.Long2LongEdge> {
        private final Cursor cursor;
        private final BufferedReader reader;
        private final long sx;
        private final long lo;
        private final long hi;
        private long ix;
        private long rows;
        private long cols;
        private long entries;
        private final char[] buffer;
        private int numCharsRead;
        private int bx;

        private final long[] line = new long[2];

        /**
         * Constructs an MtxTraverser to parse edges from an MTX file.
         * This constructor initializes the reader and starts reading from the specified position.
         * It sets up parsing based on the MTX format specifications to handle graph representations.
         *
         * @param slice the range of lines to read
         * @param pos the starting position within the file
         * @param path the path to the MTX file
         */
        private EdgeListTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new Cursor();
            this.reader = Utils.newReader.apply(path);
            this.buffer = new char[8192];
            this.bx = 0;
            this.numCharsRead = Utils.readBuffer.apply(reader, buffer);
            this.lo = Range.lo(slice);
            this.hi = min(Range.hi(slice), entries);
            this.sx = this.ix = pos;
        }

        private final class Cursor implements Mtx.Long2LongEdge {
            @Override public long source() { return line[0]; }
            @Override public long target() { return line[1]; }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryNext(Fn1.Consumer<? super Mtx.Long2LongEdge> action) {
            if (null == action) throw new NullPointerException();
            // TODO IMPLEMENT
            Utils.closeReader.apply(reader);
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forNext(Fn1.Consumer<? super Mtx.Long2LongEdge> action) {
            if (null == action) throw new NullPointerException();
            if (this.ix < this.hi) {
                // TODO IMPLEMENT
            }
            Utils.closeReader.apply(reader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super Mtx.Long2LongEdge>> context) {
            if (null == context) throw new NullPointerException();
            // Hoist boundary checks, state and array accesses.
            if (this.ix < this.hi) {
                // TODO IMPLEMENT
            }
            Utils.closeReader.apply(reader);
            return Traversal.Status.DONE;
        }
    }
}
