package graphma.datastructures;

import magma.control.Option;
import magma.data.Seq;
import magma.data.sequence.operator.DataSource;

import java.util.HashMap;

import static data.Config.GRAPH_FORMATS;


public interface Graph<N, E> {

    interface Node<N, E> {
//        Seq<Edge<E, N>> incoming();
//        Seq<Edge<E, N>> outgoing();
        default Option<N> data() {
            return Option.none();
        }
    }
    interface Edge<E, N> {
        Node<N, E> source();
        Node<N, E> target();
        default Option<E> data() {
            return Option.none();
        }
    }
    Seq<Node<N, E>> nodes();
    Seq<Edge<E, N>> edges();


    static Graph<Long, Integer> load(MtxNew.MTXFile file) {
        return new Graph<Long, Integer>() {
            MtxNew.MTXFile _file = file;
            @Override
            public Seq<Node<Long, Integer>> nodes() {
                return Seq.of();
            }
            @Override
            public Seq<Edge<Integer, Long>> edges() {
                var ds = DataSource.of(file);
                var seq = Seq.of(ds);
                return seq;
            }
        };
    }

    public static void main(String[] args) {
        var mtx = new MtxNew.MTXFile(GRAPH_FORMATS.resolve("mtx").resolve("directed_graph.mtx"),
                9999,
                9999,
                9999,
                9999);

        var graph = load(mtx);
        System.out.println("WAITTTTTTT");
        graph.edges().forEach(e -> System.out.println(e.source().data().value() + " " + e.target().data().value()));
    }


//    // TODO definire eigenes interface anstatt Product3<N, ?, ?>
//    interface Builder<N, E> extends Fn2<Traversable<Product3<N, ?, ?>>, Traversable<Product3<E, ?, ?>>, Graph<N, E>> { }
//
//
//    // TODO definire eigenes interface anstatt Product3<N, ?, ?>
//    interface Builder<N, E> extends Fn1<MtxFile, Graph<N, E>> { }
}
