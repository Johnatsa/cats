package cats_src.lockWaitGraph;

public class Node {
    private int nid;
    private int out_degree;

    public Node(int nid) {
        this.nid = nid;
        out_degree = 0;
    }

    public Node() {
        nid = -1;
        out_degree = 0;
    }

    public int getNid() {
        return nid;
    }

    public void addEdge() {
        out_degree++;
    }

    public int getOutDegree() {
        return out_degree;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }
}
