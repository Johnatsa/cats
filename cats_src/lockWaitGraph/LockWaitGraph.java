package cats_src.lockWaitGraph;

import java.util.*;

public class LockWaitGraph {
    private HashMap<Node, List<Edge>> graph;

    public LockWaitGraph() {
        graph = new HashMap<Node, List<Edge>>();
    }

    public void addNode(Node n) {
        graph.put(n, new ArrayList<Edge>());
    }

    public void addEdge(Node n, Edge e) {
        graph.get(n).add(e);
    }

}