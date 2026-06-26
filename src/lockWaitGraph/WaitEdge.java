package lockWaitGraph;

public class WaitEdge extends Edge {
    public enum CallType {
        WAIT,
        NOTIFY
    }

    private int cond_after;

    private CallType type;

    public WaitEdge(int tid, Node end, Node start, int cond_after, CallType type) {
        super(tid, end, start);
        this.type = type;
        this.cond_after = cond_after;
    }

    public WaitEdge(int tid, Node end, Node start, CallType type) {
        super(tid, end, start);
        this.type = type;
        this.cond_after = -1;
    }

    public int getCondValueAfterEdge() {
        return cond_after;
    }

    public CallType getEdgeType() {
        return type;
    }
}
