package formats;

import magma.adt.control.traversal.Traversal;
import magma.adt.value.product.Product2;
import magma.control.function.*;
import magma.control.traversal.Traversable;
import magma.control.traversal.Traverser;
import magma.value.index.Range;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Gml {
    ;

    static final Fn2.Checked<BufferedReader, char[], Integer> readBuffer = Reader::read;
    static final Fn1.Checked<Path, BufferedReader> newReader = pth -> new BufferedReader(new FileReader(pth.toFile()));
    static final Fn1.Checked<BufferedReader, Boolean> closeReader = rea -> { rea.close(); return true; };

    public static Traverser<Product2<String, String>> traverse(Path gmlPath, final Range slice, final long position) {
        if (Files.notExists(gmlPath) || Range.isEmpty(slice))
            return Traverser.empty();
        return new GmlTraverser(slice, position, gmlPath);
    }

    static final class GmlTraverser extends Traversal.Control.Context implements Traverser<Product2<String, String>> {
        private final Cursor cursor;
        private final BufferedReader reader;
        private final long lo;
        private final long hi;
        private long ix;

        private final char[] buffer;
        private int numCharsRead;
        private int bx;

        private GmlTraverser(final Range slice, final long pos, final Path path) {
            this.cursor = new Cursor();
            this.reader = newReader.apply(path);
            this.buffer = new char[8192];
            this.bx = 0;
            this.lo = Range.lo(slice);
            this.hi = Math.min(Range.hi(slice), Long.MAX_VALUE);  // Assuming max lines if not known
            this.ix = pos;

            try {
                while ((numCharsRead = readBuffer.apply(reader, buffer)) != -1) {
                    parseBuffer();
                }
            } finally {
                closeReader.apply(reader);
            }
        }

        private void parseBuffer() {
            String data = new String(buffer, 0, numCharsRead);
            Matcher nodeMatcher = Pattern.compile("node \\[\\s*id (\\d+)").matcher(data);
            Matcher edgeMatcher = Pattern.compile("edge \\[\\s*source (\\d+)\\s*target (\\d+)").matcher(data);

            while (edgeMatcher.find()) {
                String source = edgeMatcher.group(1);
                String target = edgeMatcher.group(2);
                cursor.setEdge(source, target);
                if (ix >= lo && ix < hi) {
                    // This is where you would "yield" an edge in an actual generator-based implementation
                }
                ix++;
            }
        }

        @Override
        public boolean tryNext(Fn1.Consumer<? super Product2<String, String>> action) {
            // Method logic to try next item, can refer to edge parsing logic
            return false;
        }

        @Override
        public void forNext(Fn1.Consumer<? super Product2<String, String>> action) {
            // Continuous read and action invoke as long as there are edges
        }

        @Override
        public Traversal.Status whileNext(Fn1<Traversal.Control, Fn1.Consumer<? super Product2<String, String>>> context) {
            // While loop control based on traversal conditions
            return Traversal.Status.DONE;
        }

        private final class Cursor implements Product2<String, String> {
            private String source;
            private String target;

            void setEdge(String src, String tgt) {
                this.source = src;
                this.target = tgt;
            }

            @Override
            public String _1() { return source; }
            @Override
            public String _2() { return target; }
        }
    }
}