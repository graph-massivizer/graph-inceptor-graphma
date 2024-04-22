package formats;

import magma.adt.control.traversal.Traversal;
import magma.control.function.Fn1;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.value.index.Range;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static formats.Utils.*;
import static java.lang.Math.min;

/**
 * Dot class is designed to read and traverse .dot (Graphviz) graph description files.
 * It provides functionalities to interpret and iterate over the graph's edges.
 *
 * This is a preliminary implementation. The idea is to first prototype complete
 * pipelines in order to subsequently improve them.
 */
public enum Dot {
    ;

    /**
     * Interface representing a directed or undirected edge in a graph with source and target identified by strings.
     * Within the traverser there is only one instance of this interface to avoid massive object creation.
     *
     * TODO: These edges shouldn't be here but collected in a different class file. Will be done when actual datastructures are added
     */
    public interface String2StringEdge {
        String source();
        String target();
    }

    /**
     * Creates a traverser for a given .dot file based on specified range and position.
     * The function will read edges from the file starting at the given file position.
     *
     * @param mtxPth the path to the .dot file.
     * @param slice a range indicating the part of the file to read.
     * @param position the starting position in the file for reading.
     * @return a Traverser capable of iterating over edges defined within the specified file range.
     */
    public static Traverser<String2StringEdge> traverse(Path mtxPth, final Range slice, final long position) {
        if (Files.notExists(mtxPth) || Range.isEmpty(slice))
            return Traverser.empty();
        return new Dot.DotTraverser(slice, position, mtxPth);
    }

    /**
     * DotFile record encapsulates a .dot file's path and structural information such as rows, columns, and number of lines.
     */
    public record DotFile(Path pth, int rows, int cols, int lines,
                          int numCol) implements Traversable<String2StringEdge> {
        public Traverser<String2StringEdge> traverse() {
            return Dot.traverse(pth, Range.of(0, lines), 0);
        }
    }

    /**
     * DotTraverser class implements the Traverser interface to navigate through the graph described in a .dot file.
     * It uses regular expressions to parse graph edges and provides methods to iterate over them.
     *
     * TODO Memory mapped files for performance
     * TODO change cursor
     */
    static final class DotTraverser extends Traversal.Control.Context implements Traverser<String2StringEdge> {
        private final DotTraverser.Cursor cursor;
        private final BufferedReader reader;
        private final long sx;
        private final long lo;
        private final long hi;
        private long ix;            // line in den daten

        private long entries;

        private final char[] buffer;
        private int numCharsRead;
        private int bx;

        private boolean isDirected;
        private final String[] line = new String[2];

        /**
         * Constructor initializes a new DotTraverser instance for a specific section of a .dot file.
         *
         * @param slice the segment of the file to be processed.
         * @param pos the starting position in the file.
         * @param path the path to the .dot file.
         */
        private DotTraverser(final Range slice, final long pos, final Path path) {
            System.out.println("CONSTRUCTOR");
            this.cursor = new DotTraverser.Cursor();
            this.reader = newReader.apply(path);
            this.buffer = new char[8192];
            this.bx = 0;

            // TODO this reads the entire file once before we actually start parsing. Not very performant
            try {
                this.entries = countLines(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.numCharsRead = readBuffer.apply(reader, buffer);
            System.out.println("NUM CHARS READ: " + numCharsRead);
            System.out.println("BUFFER SIZE:    " + buffer.length);
//            System.out.println(buffer);
            initHeader();
            this.lo = Range.lo(slice);
            this.hi = min(Range.hi(slice), entries);
            this.sx = this.ix = pos;
//            TODO DEBUG STUFF, DELETE!
//            System.out.println("HI: " + Range.hi(slice));
//            System.out.println("LO: " + lo);
//            System.out.println("HI: " + hi);
//            System.out.println("IX: " + ix);
//            System.out.println("SYMMETRY: " + entries);
        }

        /**
         * Initializes reading by processing the header to determine if the graph is directed or undirected.
         */
        private void initHeader() {
            StringBuilder headerBuilder = new StringBuilder();
            while (bx < numCharsRead) {
                // Read until the end of the first meaningful line or the buffer's end.
                if (buffer[bx] == '\n' || buffer[bx] == '\r') {
                    // Process the line to determine the graph's directedness.
                    String headerLine = headerBuilder.toString().trim();
                    if (headerLine.startsWith("digraph")) {
                        isDirected = true;
                    } else if (headerLine.startsWith("graph")) {
                        isDirected = false;
                    }
                    System.out.println("Graph type determined: " + (isDirected ? "Directed" : "Undirected"));
                    bx++; // Move past the newline character
                    return; // Exit after processing the first line indicating graph type.
                } else {
                    headerBuilder.append(buffer[bx]);
                }
                bx++;
            }

            // In case the first line or relevant info is longer than the buffer size:
            // TODO this is somehow weird. The condition IS NOT ALWAYS TRUE!
            if (bx >= numCharsRead) {
//                System.out.println("TTTTTTTT");
                numCharsRead = readBuffer.apply(reader, buffer);
                bx = 0; // Reset buffer index
                if (numCharsRead != -1) { // Check if there's more data to read
                    initHeader(); // Recursively read the next segment of the header
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryNext(Fn1.Consumer<? super String2StringEdge> action) {
            System.out.println("TRY NEXT");
            if (null == action) throw new NullPointerException("Action must not be null.");
//            TODO DEBUG STUFF, DELETE!
//            debugCharArray(buffer, bx);
//            System.out.println("IX: " + ix);
//            System.out.println("HI: " + ix);

            Pattern edgePattern = isDirected
                    ? Pattern.compile("\\s*(\\S+)\\s*->\\s*(\\S+)\\s*;")
                    : Pattern.compile("\\s*(\\S+)\\s*--\\s*(\\S+)\\s*;");

            while (true) {
                if (ix >= hi) {  // Check if current index exceeds the high boundary of the slice
                    closeReader.apply(reader);
                    return false;
                }
                if (bx >= numCharsRead) { // Need to reload the buffer
                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) return false; // End of file reached
                    bx = 0; // Reset buffer index
                }

                // Read the line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++; // Move past the newline character
                ix++; // Increment the line index

                String _line = lineBuilder.toString();
                Matcher matcher = edgePattern.matcher(_line);
                if (matcher.find()) {
                    line[0] = matcher.group(1);
                    line[1] = matcher.group(2);
                    action.accept(cursor);
                    return true; // Successfully processed an edge
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forNext(Fn1.Consumer<? super String2StringEdge> action) {
            System.out.println("FOR NEXT");
            if (action == null) throw new NullPointerException("Action must not be null.");

            Pattern edgePattern = isDirected
                    ? Pattern.compile("\\s*(\\S+)\\s*->\\s*(\\S+)\\s*;")
                    : Pattern.compile("\\s*(\\S+)\\s*--\\s*(\\S+)\\s*;");

            while (ix < hi) {
                if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) break;  // End of file reached
                    bx = 0;  // Reset buffer index
                    break;
                }

                // Read the line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++;  // Move past the newline character
                ix++;  // Increment the line index

                String _line = lineBuilder.toString();
                Matcher matcher = edgePattern.matcher(_line);
                if (matcher.find()) {
                    line[0] = matcher.group(1);
                    line[1] = matcher.group(2);
                    action.accept(cursor);  // Perform action on each valid edge
                }
            }
            closeReader.apply(reader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super String2StringEdge>> context) {
            System.out.println("WHILE NEXT");
            if (context == null) throw new NullPointerException("Context must not be null.");

            Pattern edgePattern = isDirected
                    ? Pattern.compile("\\s*(\\S+)\\s*->\\s*(\\S+)\\s*;")
                    : Pattern.compile("\\s*(\\S+)\\s*--\\s*(\\S+)\\s*;");

            // Prepare the action from the context
            Fn1.Consumer<? super String2StringEdge> action = bind(context);

            while (ix < hi) {
                if (bx >= numCharsRead) {  // If buffer is exhausted, reload it

                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) {
                        closeReader.apply(reader);
                        return Traversal.Status.DONE;  // End of file reached
                    }
                    bx = 0;  // Reset buffer index
                    return Traversal.Status.NONE;

                }

                // Read the line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++;  // Move past the newline character
                ix++;  // Increment the line index

                String _line = lineBuilder.toString();
                Matcher matcher = edgePattern.matcher(_line);
                if (matcher.find()) {
                    line[0] = matcher.group(1);
                    line[1] = matcher.group(2);
                    action.accept(cursor);  // Perform action on each valid edge

                    // Check if the traversal should exit based on the context's control state
                    if (!active) {
                        closeReader.apply(reader);
                        return Traversal.Status.EXIT;
                    }
                }
            }
            closeReader.apply(reader);
            return Traversal.Status.DONE;  // All data processed or range exceeded
        }

        private final class Cursor implements String2StringEdge {
            @Override
            public String source() {
                return line[0];
            }

            @Override
            public String target() {
                return line[1];
            }
        }
    }

    // TODO integrate
    private static long countLines(Path path) throws IOException {
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            long lines = 0;
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                if (b == '\n') lines++;
            }
            return lines + 1; // Add one because last line does not end with a newline character
        }
    }

    // TODO DELETE
    // Method to print char array contents with the char at a specific index colored
    private static void debugCharArray(char[] buffer, int index) {
        if (buffer == null) {
            System.out.println("Buffer is null");
            return;
        }

        System.out.println("char[] Buffer Contents at " + index + "; ");

        // Iterate through the char array
        for (int i = 0; i < buffer.length; i++) {
            if (i == index) {
                // Print the char at the index in red
                System.out.print("X");
            } else {
                // Print the char in default color
                System.out.print(buffer[i]);
            }
        }
        System.out.println(); // Newline after finishing the buffer
    }
}
