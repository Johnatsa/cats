package cats_src.lockWaitGraph;

import java.util.*;

public class LockWaitGraph {
    private HashMap<Node, List<Edge>> graph;
    private int nid_glbl;
    private int eid_glbl;

    public LockWaitGraph() {
        graph = new HashMap<Node, List<Edge>>();
        nid_glbl = 0;
        eid_glbl = 0;
    }

    public void addNode(Node n) {
        n.setNid(nid_glbl);
        nid_glbl++;
        graph.put(n, new ArrayList<Edge>());
    }

    public void addEdge(Node n, Edge e) {
        n.addEdge();
        e.setEid(eid_glbl);
        eid_glbl++;
        
        graph.get(n).add(e);
    }

    public List<List<Edge>> getEdgeCycleList() {
        return null;
    }
}