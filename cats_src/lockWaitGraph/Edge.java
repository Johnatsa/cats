package cats_src.lockWaitGraph;

// Ordering of how a thread took resources
public class Edge {
    private int tid;
    private int eid;
    private Node start_node;
    private Node end_node;

    public Edge(int tid, Node end, Node start) {
        this.tid = tid;
        end_node = end;
        start_node = start;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public int getEid() {
        return eid;
    }

    public int getTid() {
        return tid;
    }

    public Node getStartNode() {
        return start_node;
    }

    public Node getEndNode() {
        return end_node;
    }
}