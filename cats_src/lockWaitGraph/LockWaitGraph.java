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

    public List<List<Edge>> findAllCycles() {
        List<List<Edge>> cycles = new ArrayList<>();

        List<Node> nodes = new ArrayList<>(graph.keySet());

        for (Node start : nodes) {

            Stack<Edge> edgeStack = new Stack<>();
            Stack<Node> nodeStack = new Stack<>();

            HashSet<Node> visited = new HashSet<>();

            dfs(
                start,
                start,
                visited,
                nodeStack,
                edgeStack,
                cycles
            );

        }

        return cycles;
    }


    private boolean dfs(
            Node start,
            Node current,
            HashSet<Node> visited,
            Stack<Node> nodeStack,
            Stack<Edge> edgeStack,
            List<List<Edge>> cycles
    ) {

        visited.add(current);
        nodeStack.push(current);


        for (Edge edge : graph.getOrDefault(current, new ArrayList<>())) {

            Node next = edge.getEndNode();


            // cycle found
            if (next.equals(start)) {

                List<Edge> cycle = new ArrayList<>(edgeStack);
                cycle.add(edge);

                cycles.add(cycle);

            }


            // continue searching
            else if (!visited.contains(next)) {

                edgeStack.push(edge);

                dfs(
                    start,
                    next,
                    visited,
                    nodeStack,
                    edgeStack,
                    cycles
                );

                edgeStack.pop();
            }
        }


        nodeStack.pop();
        visited.remove(current);

        return false;
    }
}

