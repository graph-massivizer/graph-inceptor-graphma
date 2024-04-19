package graphma.datastructures;

import magma.adt.control.traversal.Traversal;
import magma.control.Option;
import magma.control.function.Fn1;
import magma.control.function.Fn2;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.value.index.Range;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Math.min;

enum MtxNew {
    ;

//    public interface Long2LongEdge extends Graph.Edge<Long2LongEdge, Long> {
//        Graph.Node<Long, Long2LongEdge> source();
//        Graph.Node<Long, Long2LongEdge> target();
//    }
//
//    public interface LongNode extends Graph.Node<Long, Long2LongEdge> {
////        Long value();
//    }

    static final Fn2.Checked<BufferedReader, char[], Integer> readBuffer = Reader::read;
    static final Fn1.Checked<Path, BufferedReader> newReader = pth -> new BufferedReader(new FileReader(pth.toFile()));
    static final Fn1.Checked<BufferedReader, Boolean> closeReader = rea -> { rea.close(); return true; };

    public static Traverser<Graph.Edge<Integer, Long>> traverse(Path mtxPth, final Range slice, final long position) {
        if (Files.notExists(mtxPth) || Range.isEmpty(slice))
            return Traverser.empty();
        return new MtxTraverser(slice, position, mtxPth);
    }

    record MTXFile(Path pth, int rows, int cols, int lines, int numCol) implements Traversable<Graph.Edge<Integer, Long>> {
        public Traverser<Graph.Edge<Integer, Long>> traverse() {
            return MtxNew.traverse(pth, Range.of(0, lines), 0);
        }
    }

    static final class MtxTraverser extends Traversal.Control.Context implements Traverser<Graph.Edge<Integer, Long>> {
        private final Cursor cursor;
        private final SrcNode srcNode;
        private final TgtNode tgtNode;
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

        private MtxTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new Cursor();
            this.srcNode = new SrcNode();
            this.tgtNode = new TgtNode();
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

        private final class SrcNode implements Graph.Node<Long, Integer> {
            public Option<Long> data() {
                return Option.of(line[0]);
            }
        }

        private final class TgtNode implements Graph.Node<Long, Integer> {
            public Option<Long> data() {
                return Option.of(line[1]);
            }
        }

//        private final class SrcNode implements LongNode {
//            public Option<Long> data() {
//                return Option.of(line[0]);
//            }
//        }
//
//        private final class TgtNode implements LongNode {
//            public Option<Long> data() {
//                return Option.of(line[1]);
//            }
//        }

//        private final class Cursor implements Long2LongEdge {
//            @Override public Graph.Node<Long, Long2LongEdge> source() { return srcNode; }
//            @Override public Graph.Node<Long, Long2LongEdge> target() { return tgtNode; }
//        }

        private final class Cursor implements Graph.Edge<Integer, Long> {
            @Override public Graph.Node<Long, Integer> source() { return srcNode; }
            @Override public Graph.Node<Long, Integer> target() { return tgtNode; }
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

        @Override
        public boolean tryNext(Fn1.Consumer<? super Graph.Edge<Integer, Long>> action) {
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

        @Override
        public void forNext(Fn1.Consumer<? super Graph.Edge<Integer, Long>> action) {
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

        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super Graph.Edge<Integer, Long>>> context) {
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
