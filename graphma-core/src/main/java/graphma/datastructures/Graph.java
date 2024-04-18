package graphma.datastructures;

public interface Graph<N, E> {

//
//    interface Node<N, E> {
//        Seq<Edge<E, N>> incoming();
//        Seq<Edge<E, N>> outgoing();
//        default Option<N> data() {
//            return Option.none();
//        }
//    }
//    interface Edge<E, N> {
//        Node<N, E> source();
//        Node<N, E> target();
//        default Option<E> data() {
//            return Option.none();
//        }
//    }
//    Seq<Node<N, E>> nodes();
//    Seq<Edge<E, N>> edges();
//
//
//
//
//
//
//    static <N, E> Graph<N, E> load(MtxFile file) {
//
//
//        return new Graph<N, E>() {
//            @Override
//            public Seq<Node<N, E>> nodes() {
//                return Seq.of();
//            }
//
//            @Override
//            public Seq<Edge<E, N>> edges() {
//                return Seq.of();
//            }
//        }
//
//    }
//
//
//    // TODO definire eigenes interface anstatt Product3<N, ?, ?>
//    interface Builder<N, E> extends Fn2<Traversable<Product3<N, ?, ?>>, Traversable<Product3<E, ?, ?>>, Graph<N, E>> {
//
//    }
//
//
//    // TODO definire eigenes interface anstatt Product3<N, ?, ?>
//    interface Builder<N, E> extends Fn1<MtxFile, Graph<N, E>> {
//
//    }
}
