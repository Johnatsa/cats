package cats_src.lockWaitGraph;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
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

    public void findAllCycles() {
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
    
        // --- convert to tids ---
        List<List<Integer>> cycleTids = new ArrayList<>();

        for (List<Edge> cycle : cycles) {

            List<Integer> tids = new ArrayList<>();

            for (Edge edge : cycle) {
                tids.add(edge.getTid());
            }

            cycleTids.add(tids);
        }

        // ---- wait nodes ----
        List<Integer> eternalWaits = findWaitNodeTids(graph);

        // --- Wrapper ---
        AnalysisResult result = new AnalysisResult(cycleTids, eternalWaits);

        // --- Build json ---
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(
                new File("graph_cycles.json"),
                result
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private List<Integer> findWaitNodeTids(HashMap<Node, List<Edge>> graph) {

        HashMap<Node, List<Edge>> incoming = new HashMap<>();

        // Build incoming edges
        for (Node from : graph.keySet()) {

            for (Edge edge : graph.get(from)) {

                incoming
                    .computeIfAbsent(edge.getEndNode(), k -> new ArrayList<>())
                    .add(edge);
            }
        }


        List<Integer> result = new ArrayList<>();


        for (Node node : graph.keySet()) {

            if (!(node instanceof WaitNode))
                continue;


            List<Edge> inEdges = incoming.get(node);

            if (inEdges == null || inEdges.isEmpty())
                continue;


            boolean onlyWait = true;


            for (Edge e : inEdges) {

                if (!(e instanceof WaitEdge)) {
                    onlyWait = false;
                    break;
                }


                WaitEdge we = (WaitEdge)e;

                if (we.getEdgeType() != WaitEdge.CallType.WAIT) {
                    onlyWait = false;
                    break;
                }
            }


            if (onlyWait) {

                // take any incoming edge
                result.add(
                    inEdges.get(0).getTid()
                );
            }
        }


        return result;
    }
}

class AnalysisResult {

    public List<List<Integer>> cycles;
    public List<Integer> waitNodes;

    public AnalysisResult(
        List<List<Integer>> cycles,
        List<Integer> waitNodes
    ) {
        this.cycles = cycles;
        this.waitNodes = waitNodes;
    }
}