package cats_src.lockWaitGraph;

import cats_src.*;
import java.util.*;

public class LockNode extends Node {
    private int lockId;
    private String id;

    public LockNode(String identifier, int lockId, int nid) {
        super(nid);
        this.id = identifier;
        this.lockId = lockId;
    }

    public LockNode(int nid) {
        super(nid);
        this.id = null;
        this.lockId = -1;
    }

    public int getLockId() {
        return lockId;
    }

    public String getId() {
        return id;
    }

    public void printContext() {
        LockNode tmp = this;
        while (tmp.getLockId() != -1) {
            tmp.printNode();
        }
    }

    public void printNode() {
        System.out.println("LockID " + lockId + " static identifier " + id);
    }
}