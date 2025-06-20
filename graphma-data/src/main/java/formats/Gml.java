package formats;

import magma.adt.control.traversal.Traversal;
import magma.control.exception.Exceptions;
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


/**
 * Provides utilities for parsing and traversing Graph Modeling Language (GML) files.
 * This class supports creating traversable views of GML data, focusing on extracting edges between nodes.
 * <p>
 * Usage involves creating a {@link Traverser} that can iterate over edges defined within a GML file,
 * allowing for custom actions to be performed on each edge encountered.
 * </p>
 *
 * This is a preliminary implementation. The idea is to first prototype complete
 * pipelines in order to subsequently improve them.
 *
 * TODO make fully functional, only prototyped
 */
public enum Gml {
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
     * Creates a traverser to iterate over the edges in a GML file, starting from a specified position.
     * This method only considers edges within the specified range in the file.
     *
     * @param gmlPath the path to the GML file
     * @param slice the range of lines in the file to process
     * @param position the starting position within the range
     * @return a {@link Traverser} for edges, or an empty traverser if the file does not exist or the range is empty
     */
    public static Traverser<String2StringEdge> traverse(Path gmlPath, final Range slice, final long position) {
        if (Files.notExists(gmlPath) || Range.isEmpty(slice))
            return Traverser.empty();
        return new GmlTraverser(slice, position, gmlPath);
    }

    /**
     * Record to encapsulate a GML file's metadata and provide a method to create a traverser for its content.
     */
    public record GmlFile(Path pth, int rows, int cols, int lines,
                          int numCol) implements Traversable<String2StringEdge> {
        /**
         * Creates a traverser to iterate over all edges defined in the associated GML file.
         * @return a traverser for edges
         */
        public Traverser<String2StringEdge> traverse() {
            return Gml.traverse(pth, Range.of(0, lines), 0);
        }
    }

    /**
     * Implementation of a {@link Traverser} specific to GML files, capable of parsing edges from the file's content.
     */
    static final class GmlTraverser extends Traversal.Control.Context implements Traverser<String2StringEdge> {
        private final Cursor cursor;
        private final BufferedReader reader;
        private final long sx;
        private final long lo;
        private final long hi;
        private long ix;

        private long entries;

        private final char[] buffer;
        private int numCharsRead;
        private int bx;

        private boolean isDirected;
        private final String[] line = new String[2];

        /**
         * Constructs a GmlTraverser to parse edges from a GML file.
         * This constructor initializes the reader and starts reading from the specified position.
         * It determines whether the graph is directed and prepares to parse edges.
         *
         * @param slice the range of lines to read
         * @param pos the starting position within the file
         * @param path the path to the GML file
         */
        private GmlTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new Cursor();
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
            this.hi = Math.min(Range.hi(slice), Long.MAX_VALUE);  // Assuming max lines if not known
            this.sx = this.ix = pos;

//            TODO DEBUG STUFF, DELETE!
            System.out.println("HI: " + Range.hi(slice));
            System.out.println("LO: " + lo);
            System.out.println("HI: " + hi);
            System.out.println("IX: " + ix);
        }

        /**
         * Initializes reading by processing the header to determine if the graph is directed or undirected.
         */
        private void initHeader() {
            StringBuilder headerBuilder = new StringBuilder();
            boolean inGraphBlock = false;

            while (bx < numCharsRead) {
                // Check if current character is a line break to process the accumulated line
                if (buffer[bx] == '\n' || buffer[bx] == '\r') {
                    String line = headerBuilder.toString().trim();
                    if (line.startsWith("graph [")) {
                        inGraphBlock = true; // Start of graph block
                        System.out.println("Graph block started.");
                    } else if (line.startsWith("directed") && inGraphBlock) {
                        // Extract directed information
                        isDirected = line.contains(" 1");
                        System.out.println("Graph type determined: " + (isDirected ? "Directed" : "Undirected"));
                    } else if (line.equals("]") && inGraphBlock) {
                        // End of graph block
                        throw Exceptions.illegalState();
                    } else if ((line.startsWith("node") || line.startsWith("edge")) && inGraphBlock) {
                        // Stop before nodes or edges start
                        System.out.println("Reached start of nodes or edges.");
                        bx = bx - 6;
                        break;
                    }

                    headerBuilder.setLength(0); // Clear the builder for the next line
                    bx++;  // Move past the newline character
                } else {
                    headerBuilder.append(buffer[bx]); // Accumulate characters into the builder
                }
                bx++;

                // Check if we need to read more from the file
                if (bx >= numCharsRead) {
                    if (!readMore()) break; // If no more data to read, exit
                }
            }
        }

        private boolean readMore() {
            numCharsRead = readBuffer.apply(reader, buffer);
            bx = 0; // Reset buffer index
            return numCharsRead != -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryNext(Fn1.Consumer<? super String2StringEdge> action) {
            StringBuilder blockBuilder = new StringBuilder();
            boolean inEdgeBlock = false;
            System.out.println("TRY NEXT");

            while (ix < hi) {
                if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) { // End of file reached
                        closeReader.apply(reader);
                        return false;
                    }
                    bx = 0;  // Reset buffer index
                }

                // Build the complete line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++; // Move past the newline character
                ix++; // Increment the line index

                String _line = lineBuilder.toString().trim();
                if (_line.startsWith("edge [")) {
                    inEdgeBlock = true; // Start collecting lines for an edge block
                    blockBuilder.append(_line).append(" "); // Add the start of the edge block
                } else if (_line.equals("]") && inEdgeBlock) {
                    // End of the current edge block
                    blockBuilder.append(_line); // Append the closing line
                    processEdgeBlock(blockBuilder.toString(), action);
                    blockBuilder.setLength(0); // Reset for potentially next block
                    return true; // Successfully processed an edge
                } else if (inEdgeBlock) {
                    blockBuilder.append(_line).append(" "); // Continue accumulating lines within the edge block
                }
            }

            return false; // If we reach here, no more edges to process or end of file
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forNext(Fn1.Consumer<? super String2StringEdge> action) {
            System.out.println("FOR NEXT");
            StringBuilder blockBuilder = new StringBuilder();
            boolean inEdgeBlock = false;

            while (ix < hi) {
                if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) break;  // End of file reached
                    bx = 0;  // Reset buffer index
                }

                // Build the complete line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++; // Move past the newline character
                ix++; // Increment the line index

                String _line = lineBuilder.toString().trim();
                if (_line.startsWith("edge [")) {
                    inEdgeBlock = true; // Start collecting lines for an edge block
                } else if (_line.equals("]") && inEdgeBlock) {
                    // Process the collected block
                    inEdgeBlock = false;
                    processEdgeBlock(blockBuilder.toString(), action);
//                    System.out.println(blockBuilder.toString());
                    blockBuilder.setLength(0); // Reset for the next block
                } else if (inEdgeBlock) {
                    blockBuilder.append(_line).append(" "); // Continue accumulating lines within the edge block
                }
            }
            closeReader.apply(reader);
        }

        private void processEdgeBlock(String block, Fn1.Consumer<? super String2StringEdge> action) {
            Pattern edgePattern = Pattern.compile(
                    "source\\s+(\\d+)\\s*[^\\[]*target\\s+(\\d+)",
                    Pattern.DOTALL  // DOTALL flag to match across lines
            );

            Matcher matcher = edgePattern.matcher(block);
            if (matcher.find()) {
                line[0] = matcher.group(1);
                line[1] = matcher.group(2);
                action.accept(cursor);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super String2StringEdge>> context) {
            StringBuilder blockBuilder = new StringBuilder();
            boolean inEdgeBlock = false;
            System.out.println("WHILE NEXT");

            // Prepare the action from the context
            Fn1.Consumer<? super String2StringEdge> action = bind(context);

            while (ix < hi) {
                if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                    numCharsRead = readBuffer.apply(reader, buffer);
                    if (numCharsRead == -1) { // End of file reached
                        closeReader.apply(reader);
                        return Traversal.Status.DONE;  // All data processed
                    }
                    bx = 0;  // Reset buffer index
                }

                // Build the complete line from the buffer
                StringBuilder lineBuilder = new StringBuilder();
                while (bx < numCharsRead && buffer[bx] != '\n') {
                    lineBuilder.append(buffer[bx++]);
                }
                bx++; // Move past the newline character
                ix++; // Increment the line index

                String _line = lineBuilder.toString().trim();
                if (_line.startsWith("edge [")) {
                    inEdgeBlock = true; // Start collecting lines for an edge block
                    blockBuilder.append(_line).append(" "); // Add the start of the edge block
                } else if (_line.equals("]") && inEdgeBlock) {
                    // End of the current edge block
                    blockBuilder.append(_line); // Append the closing line
                    processEdgeBlock(blockBuilder.toString(), action);
                    blockBuilder.setLength(0); // Reset for potentially next block

                    // Check if the traversal should exit based on the context's control state
                    if (!active) {
                        closeReader.apply(reader);
                        return Traversal.Status.EXIT;
                    }
                } else if (inEdgeBlock) {
                    blockBuilder.append(_line).append(" "); // Continue accumulating lines within the edge block
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