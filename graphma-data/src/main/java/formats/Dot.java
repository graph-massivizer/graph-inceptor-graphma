package formats;

import magma.adt.control.traversal.Traversal;
import magma.control.function.Fn1;
import magma.control.function.Fn2;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.value.index.Range;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public enum Dot {
    ;

    public interface String2StringEdge {
        String source();
        String target();
    }

    public static Traverser<String2StringEdge> traverse(Path mtxPth, final Range slice, final long position) {
        if (Files.notExists(mtxPth) || Range.isEmpty(slice))
            return Traverser.empty();
        return new Dot.DotTraverser(slice, position, mtxPth);
    }

    public record DotFile(Path pth, int rows, int cols, int lines,
                          int numCol) implements Traversable<String2StringEdge> {
        public Traverser<String2StringEdge> traverse() {
            return Dot.traverse(pth, Range.of(0, lines), 0);
        }
    }

    static final Fn2.Checked<BufferedReader, char[], Integer> readBuffer = Reader::read;
    static final Fn1.Checked<Path, BufferedReader> newReader = pth -> new BufferedReader(new FileReader(pth.toFile()));
    static final Fn1.Checked<BufferedReader, Boolean> closeReader = rea -> {
        rea.close();
        return true;
    };

    // TODO Memory mapped files for performance
    // TODO change cursor
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

        private DotTraverser(final Range slice, final long pos, final Path path) {
            System.out.println("CONSTRUCTOR");
            this.cursor = new DotTraverser.Cursor();
            this.reader = newReader.apply(path);
            this.buffer = new char[8192];
            this.bx = 0;

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
