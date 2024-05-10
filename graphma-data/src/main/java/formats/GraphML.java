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
 * Provides utilities for parsing and traversing GraphML files.
 * This class supports creating traversable views of GraphML data, specifically focusing on extracting edges between nodes.
 * <p>
 * Usage involves creating a {@link Traverser} that can iterate over edges defined within a GraphML file,
 * allowing for custom actions to be performed on each discovered edge.
 * </p>
 *
 * This is a preliminary implementation. The idea is to first prototype complete
 * pipelines in order to subsequently improve them.
 *
 */
public enum GraphML {
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
     * Creates a traverser to iterate over the edges in a GraphML file, starting from a specified position.
     * This method only considers edges within the specified range in the file.
     *
     * @param mtxPth the path to the GraphML file
     * @param slice the range of lines in the file to process
     * @param position the starting position within the range
     * @return a {@link Traverser} for edges, or an empty traverser if the file does not exist or the range is empty
     */
    public static Traverser<String2StringEdge> traverse(Path mtxPth, final Range slice, final long position) {
        if (Files.notExists(mtxPth) || Range.isEmpty(slice))
            return Traverser.empty();
        return new GraphML.GraphMLTraverser(slice, position, mtxPth);
    }

    /**
     * Record to encapsulate a GraphML file's metadata and provide a method to create a traverser for its content.
     */
    public record GraphMLFile(Path pth, int rows, int cols, int lines,
                          int numCol) implements Traversable<String2StringEdge> {
        /**
         * Creates a traverser to iterate over all edges defined in the associated GraphML file.
         * @return a traverser for edges
         */
        public Traverser<String2StringEdge> traverse() {
            return GraphML.traverse(pth, Range.of(0, lines), 0);
        }
    }

    /**
     * Implementation of a {@link Traverser} specific to GraphML files, capable of parsing edges from the file's content.
     */
    static final class GraphMLTraverser extends Traversal.Control.Context implements Traverser<GraphML.String2StringEdge> {
        private final GraphML.GraphMLTraverser.Cursor cursor;
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
         * Constructs a GraphMLTraverser to parse edges from a GraphML file.
         * This constructor initializes the reader and starts reading from the specified position.
         * It determines whether the graph is directed and prepares to parse edges.
         *
         * @param slice the range of lines to read
         * @param pos the starting position within the file
         * @param path the path to the GraphML file
         */
        private GraphMLTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new GraphML.GraphMLTraverser.Cursor();
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
            boolean inGraphBlock = false; // Track if we are within the <graph> tags

            try {
                while (bx < numCharsRead) {
                    char currentChar = buffer[bx++];

                    // Check for line breaks or buffer end to process the accumulated line
                    if (currentChar == '\n' || currentChar == '\r' || bx == numCharsRead) {
                        String line = headerBuilder.toString().trim();
                        if (line.startsWith("<graph")) {
                            inGraphBlock = true;
                            // Parse attributes of the graph element, e.g., directed
                            if (line.contains("edgedefault=\"directed\"")) {
                                isDirected = true;
                            } else if (line.contains("edgedefault=\"undirected\"")) {
                                isDirected = false;
                            }
                            System.out.println("Graph type determined: " + (isDirected ? "Directed" : "Undirected"));
                        } else if (line.startsWith("</graph")) {
                            inGraphBlock = false; // End of graph block
                            System.out.println("End of graph block reached.");
                            throw Exceptions.illegalState("THIS SHOULD NOT HAPPEN");
                        } else if (inGraphBlock && line.startsWith("<edge")) {
                            System.out.println("Reached first edge, stopping header parsing.");
                            bx -= (line.length() + 1); // Move back to the start of the <edge> tag
//                            debugCharArray(buffer, bx);
                            break; // Stop before processing any edges
                        }
                        // Reset headerBuilder for the next line
                        headerBuilder.setLength(0);
                    } else {
                        // Accumulate characters into the builder
                        headerBuilder.append(currentChar);
                    }
                    // If we reach the end of the buffer, try to reload
                    if (bx >= numCharsRead) {
                        if (!readMore()) break; // If no more data to read, exit
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse header due to an error.", e);
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
        public boolean tryNext(Fn1.Consumer<? super GraphML.String2StringEdge> action) {
            System.out.println("TRY NEXT");
            if (action == null) throw new NullPointerException("Action must not be null.");

            StringBuilder edgeBuilder = new StringBuilder();
            boolean inEdge = false;

            try {
                while (ix < hi) {
                    if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                        if (!readMore()) {
                            closeReader.apply(reader);
                            return false;  // No more data to read, end of file
                        }
                    }

                    // Process character by character
                    char currentChar = buffer[bx++];
                    if (currentChar == '\n' || currentChar == '\r' || bx == numCharsRead) {
                        String line = edgeBuilder.toString().trim();
                        if (line.contains("<edge") && !inEdge) {
//                            System.out.println("START EDGE");
                            // Start of an edge element
                            inEdge = true;
                        }
                        if (line.contains("/>") && inEdge) {
//                            System.out.println("END EDGE: " + edgeBuilder.toString());
                            // Complete edge element read, process it
                            processEdge(edgeBuilder.toString(), action);
                            edgeBuilder.setLength(0);
                            inEdge = false;
                            return true; // Successfully processed an edge
                        }
                        if (inEdge) {
//                            System.out.println("IN EDGE");
                            // Continue building the edge element if within an edge block
                            edgeBuilder.append(line);
                            System.out.println(edgeBuilder);
                        } else {
                            // Reset edgeBuilder for the next potential edge
                            edgeBuilder.setLength(0);
                        }
                        ix++; // Increment line index
                    } else {
                        // Keep building the edge element
                        edgeBuilder.append(currentChar);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing the next edge: " + e.getMessage(), e);
            }
            closeReader.apply(reader);
            return false; // If we reach here, no more edges to process or end of file
        }

        private void processEdge(String edgeData, Fn1.Consumer<? super GraphML.String2StringEdge> action) {
            Pattern edgePattern = Pattern.compile(
                    "source=\"(\\w+)\".*?target=\"(\\w+)\"",
                    Pattern.DOTALL
            );

            Matcher matcher = edgePattern.matcher(edgeData);
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
        public void forNext(Fn1.Consumer<? super GraphML.String2StringEdge> action) {
            System.out.println("FOR NEXT");
            if (action == null) throw new NullPointerException("Action must not be null.");

            StringBuilder edgeBuilder = new StringBuilder();
            boolean inEdge = false;

            try {
                while (ix < hi) {
                    if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                        if (!readMore()) {
                            closeReader.apply(reader);
                            break;  // No more data to read, end of file
                        }
                    }

                    // Process character by character
                    char currentChar = buffer[bx++];
                    if (currentChar == '\n' || currentChar == '\r' || bx == numCharsRead) {
                        String line = edgeBuilder.toString().trim();
                        if (line.contains("<edge") && !inEdge) {
                            // Start of an edge element
                            inEdge = true;
                        }
                        if (line.contains("/>") && inEdge) {
                            // Complete edge element read, process it
                            processEdge(edgeBuilder.toString(), action);
                            edgeBuilder.setLength(0);  // Reset for the next edge
                            inEdge = false;
                        }
                        if (inEdge) {
                            // Continue building the edge element if within an edge block
                            edgeBuilder.append(line);
                        } else {
                            // Reset edgeBuilder for the next potential edge
                            edgeBuilder.setLength(0);
                        }
                        ix++; // Increment line index
                    } else {
                        // Keep building the edge element
                        edgeBuilder.append(currentChar);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while processing edges: " + e.getMessage(), e);
            }
            closeReader.apply(reader);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super GraphML.String2StringEdge>> context) {
            System.out.println("WHILE NEXT");

            // Prepare the action from the context
            Fn1.Consumer<? super GraphML.String2StringEdge> action = bind(context);
            StringBuilder edgeBuilder = new StringBuilder();
            boolean inEdge = false;

            try {
                while (ix < hi) {
                    if (bx >= numCharsRead) {  // If buffer is exhausted, reload it
                        if (!readMore()) {
                            closeReader.apply(reader);
                            return Traversal.Status.DONE;  // End of file reached
                        }
                    }

                    // Process character by character
                    char currentChar = buffer[bx++];
                    if (currentChar == '\n' || currentChar == '\r' || bx == numCharsRead) {
                        String line = edgeBuilder.toString().trim();
                        if (line.contains("<edge") && !inEdge) {
                            // Start of an edge element
                            inEdge = true;
                        }
                        if (line.contains("/>") && inEdge) {
                            // Complete edge element read, process it
                            processEdge(edgeBuilder.toString(), action);
                            edgeBuilder.setLength(0);  // Reset for the next edge
                            inEdge = false;

                            // Check if the traversal should exit based on the context's control state
                            if (!active) {
                                closeReader.apply(reader);
                                return Traversal.Status.EXIT;
                            }
                        }
                        if (inEdge) {
                            // Continue building the edge element if within an edge block
                            edgeBuilder.append(line);
                        } else {
                            // Reset edgeBuilder for the next potential edge
                            edgeBuilder.setLength(0);
                        }
                        ix++; // Increment line index
                    } else {
                        // Keep building the edge element
                        edgeBuilder.append(currentChar);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while processing edges in whileNext: " + e.getMessage(), e);
            }
            closeReader.apply(reader);
            return Traversal.Status.DONE;  // All data processed or range exceeded
        }

        private final class Cursor implements GraphML.String2StringEdge {
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
