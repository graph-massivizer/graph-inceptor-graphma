package playground.bologna;

import magma.adt.value.product.Product4;
import magma.data.Seq;

import java.util.List;
import java.util.function.Function;

public enum Sketch {
    ;

    // ======================================
    // DB
    // ======================================
    interface DB<Q, R> {
        interface Query {}
        R query(Q query);
        // ======================================
        // WOLFGANG's GRAPH DB
        // ======================================
        // Holds the Virtual Knowledge Graphs and
        // allows to query it
        interface GraphDB extends DB<GraphDB.GrapQuery, GraphDB.RDFGraph> {
            interface GrapQuery extends Query {}
            interface SelectQuery extends Query {}
            interface RDFGraph { // CONSTRUCT
                interface Statement<SUBJECT, PREDICAT, OBJECT, SUBGRAPH_ID>
                        extends Product4<SUBJECT, PREDICAT, OBJECT, SUBGRAPH_ID> {}
                List<Statement> statements();
            }
            interface ParquetFilePointer {
                String path();
                long lo();
                long hi();
            }
            RDFGraph queryGraph(SelectQuery query);
        }
        // ======================================
        // WOLFGANG's GRAPH DB
        // ======================================
        // Holds the Virtual Knowledge Graphs and
        // allows to query it
        interface Examon {
            GraphDB.ParquetFilePointer queryParquet(GraphDB.Query query);
        }
    }



    // ======================================
    // GRAPH DB
    // ======================================
    interface Graph<V, E> {
        interface Edge<E, V> { }
        interface Node<V> { }
        Seq<Edge<E, V>> edges();
        Seq<Node<V>> nodes();
        interface VirtualKnowledgeGraph extends Graph<VirtualKnowledgeGraph.VirtualNode, Edge> {
            interface VirtualNode<V extends Node> extends Graph.Node<V> { }
            interface ComputeNode<V extends Node> extends VirtualNode<ComputeNode> { }
            interface LeafNode<V extends VirtualKnowledgeGraph> extends VirtualNode<LeafNode> {
                DB.GraphDB.SelectQuery queryToExamon();
                DB.GraphDB db();
                default DB.GraphDB.RDFGraph queryDB() {
                    return db().queryGraph(queryToExamon());
                }
            }
            Seq<LeafNode> leafNodes();
        }

        interface TemporalGraph extends Graph<TemporalGraph.TemporalGraphNode, TemporalGraph.TemporalGraphEdge> {
            interface Slice extends Graph<TemporalGraph.TemporalGraphNode, TemporalGraph.TemporalGraphEdge> { }
            interface TemporalGraphEdge extends Edge<TemporalGraphEdge, TemporalGraphNode> { }
            interface TemporalGraphNode<V extends TemporalGraphNode> extends Node<V> { }
            interface ComputerNode extends TemporalGraphNode<ComputerNode> { }
            interface DataNode extends TemporalGraphNode<DataNode> {}
            Seq<Slice> timeSlice();
        }
    }

    static Function<DB.GraphDB.SelectQuery, Graph.VirtualKnowledgeGraph> receiveVirtualize = null;
    static Function<Graph.VirtualKnowledgeGraph, Graph.TemporalGraph> materializeVirtualKnowledgeGraph = null;
}
