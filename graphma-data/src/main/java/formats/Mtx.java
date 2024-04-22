package formats;

import magma.adt.control.traversal.Traversal;
import magma.adt.value.product.Product2;
import magma.control.function.*;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.value.index.Range;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import static formats.Utils.*;
import static java.lang.Math.min;

/**
 * Provides utilities for parsing and traversing Matrix Market (MTX) files.
 * This class supports creating traversable views of edge data from MTX format files, which are commonly used to store
 * sparse matrices and can represent graphs in terms of adjacency matrices.
 * <p>
 * Usage involves creating a {@link Traverser} that can iterate over the edges represented in an MTX file,
 * allowing for custom actions to be performed on each discovered edge.
 * </p>
 *
 * This is a preliminary implementation. The idea is to first prototype complete
 * pipelines in order to subsequently improve them.
 */
public enum Mtx {
    ;

    /**
     * Represents an edge in a graph, containing the identifiers for source and target nodes as long values.
     *
     * TODO: These edges shouldn't be here but collected in a different class file. Will be done when actual datastructures are added
     */
    public interface Long2LongEdge {
        long source();
        long target();
    }

    /**
     * Creates a traverser to iterate over the edges in an MTX file, starting from a specified position.
     * This method considers only edges within the specified range in the file.
     *
     * @param mtxPth the path to the MTX file
     * @param slice the range of lines in the file to process
     * @param position the starting position within the range
     * @return a {@link Traverser} for edges, or an empty traverser if the file does not exist or the range is empty
     */
    public static Traverser<Long2LongEdge> traverse(Path mtxPth, final Range slice, final long position) {
        if (Files.notExists(mtxPth) || Range.isEmpty(slice))
            return Traverser.empty();
        return new MtxTraverser(slice, position, mtxPth);
    }

    /**
     * Record to encapsulate an MTX file's metadata and provide a method to create a traverser for its content.
     */
    public record MTXFile(Path pth, int rows, int cols, int lines, int numCol) implements Traversable<Long2LongEdge> {
        public Traverser<Mtx.Long2LongEdge> traverse() {
            return Mtx.traverse(pth, Range.of(0, lines), 0);
        }
    }

    /**
     * Implementation of a {@link Traverser} specific to MTX files, capable of parsing edges from the file's content.
     */
    static final class MtxTraverser extends Traversal.Control.Context implements Traverser<Long2LongEdge> {
        private final MtxTraverser.Cursor cursor;
        private final BufferedReader reader;
        private final long sx;
        private final long lo;
        private final long hi;
        private long ix; // line in den daten

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
        private MtxTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new MtxTraverser.Cursor();
            this.reader = newReader.apply(path);
            this.buffer = new char[8192];
            this.bx = 0;

            this.numCharsRead = readBuffer.apply(reader, buffer);
            initHeader();

            for (long i = 0; i < pos; i++) {
                moveToNextLine();
            }

            this.lo = Range.lo(slice);
            this.hi = min(Range.hi(slice), entries);
            this.sx = this.ix = pos;
//            System.out.println("HI: " + Range.hi(slice));
//            System.out.println("LO: " + lo);
//            System.out.println("HI: " + hi);
//            System.out.println("IX: " + ix);
//            System.out.println("SYMMETRY: " + entries);
        }

        /**
         * Initializes the header parsing to read the matrix dimensions and number of edges (non-zero entries).
         * This sets up the subsequent parsing operations by moving the buffer to the start of the data entries.
         */
        private void initHeader() {
            while (bx < numCharsRead && buffer[bx] == '%') {
                moveToNextLine();
            }

            int start = bx;
            int end = findSpaceOrNewLine(buffer, start);
            rows = parseLong(buffer, start, end);

            start = end + 1;
            end = findSpaceOrNewLine(buffer, start);
            cols = parseLong(buffer, start, end);

            start = end + 1;
            end = findSpaceOrNewLine(buffer, start);
            entries = parseLong(buffer, start, end);

            moveToNextLine();
        }

        private int findSpaceOrNewLine(char[] chars, int start) {
            for (int i = start; i < numCharsRead; i++) {
                if (chars[i] == ' ' || chars[i] == '\n') {
                    return i;
                }
            }
            return numCharsRead;
        }

        private void moveToNextLine() {
            while (bx < numCharsRead && buffer[bx] != '\n') {
                bx++;
            }
            bx++;
            ix++;
            if (bx >= numCharsRead) {
                numCharsRead = readBuffer.apply(reader, buffer);
                bx = 0;
            }
        }

        private final class Cursor implements Long2LongEdge {
            @Override public long source() { return line[0]; }
            @Override public long target() { return line[1]; }
        }

        private long parseLong(char[] chars, int start, int end) {
            long result = 0;
            boolean negative = false;
            int i = start;

            if (chars[i] == '-') {
                negative = true;
                i++;
            }

            for (; i < end; i++) {
                if (chars[i] < '0' || chars[i] > '9') {
                    System.out.println(String.copyValueOf(chars));
                    throw new NumberFormatException("Invalid character: " + chars[i]);
                }
                result = result * 10 + (chars[i] - '0');
            }

            return negative ? -result : result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryNext(Fn1.Consumer<? super Long2LongEdge> action) {
//            System.out.println("TRY NEXT");
            if (null == action) throw new NullPointerException();
            if (ix < hi) {
                final long[] line = this.line;
                int start = bx;
                int end = findSpaceOrNewLine(buffer, start);
                line[0] = parseLong(buffer, start, end);

                start = end + 1;
                end = findSpaceOrNewLine(buffer, start);
                line[1] = parseLong(buffer, start, end);

                moveToNextLine();
                action.accept(cursor);
                return true;
            }
            closeReader.apply(reader);
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forNext(Fn1.Consumer<? super Long2LongEdge> action) {
//            System.out.println("FOR NEXT");
            if (null == action) throw new NullPointerException();
            if (this.ix < this.hi) {
                do {
                    final long[] line = this.line;
                    int start = bx;
                    int end = findSpaceOrNewLine(buffer, start);
                    line[0] = parseLong(buffer, start, end);

                    start = end + 1;
                    end = findSpaceOrNewLine(buffer, start);
                    line[1] = parseLong(buffer, start, end);

                    moveToNextLine();
                    action.accept(cursor);
                }
                while (ix < hi);
            }
            closeReader.apply(reader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super Long2LongEdge>> context) {
//            System.out.println("WHILE NEXT");
            if (null == context) throw new NullPointerException();
            // Hoist boundary checks, state and array accesses.
            if (this.ix < this.hi) {
                final var reader = this.reader;
                // Propagate context control state.
                final var action = bind(context);
                do {
                    final long[] line = this.line;
                    int start = bx;
                    int end = findSpaceOrNewLine(buffer, start);
                    line[0] = parseLong(buffer, start, end);

                    start = end + 1;
                    end = findSpaceOrNewLine(buffer, start);
                    line[1] = parseLong(buffer, start, end);

                    moveToNextLine();
                    action.accept(cursor);
                } while (active && ix < hi);
                if (!active) {
                    closeReader.apply(reader);
                    return Traversal.Status.EXIT;
                }
            }
            closeReader.apply(reader);
            return Traversal.Status.DONE;
        }
    }
}
