package cats_src.lockWaitGraph;

import java.util.*;

// ( object, cond )
// obj will be used to call notify like cond or lock in java
// cond is the bool in the while loop where wait lives
// lines is the code lines where the object is modified
public class WaitNode extends Node {
    private int obj_id;
    private String obj_identifier;
    private List<Integer> obj_lines;
    
    private int cond_var_id;
    private String cond_identifier;
    private List<Integer> cond_lines;

    public WaitNode(int obj_id, String obj_identifier, int cond_var_id, String cond_identifier) {
        super();
        this.obj_id = obj_id;
        this.obj_identifier = obj_identifier;
        this.cond_var_id = cond_var_id;
        this.cond_identifier = cond_identifier;
        obj_lines = new ArrayList<>();
        cond_lines = new ArrayList<>();
    }

    public int getObjId() {
        return obj_id;
    }

    public String getObjName() {
        return obj_identifier;
    }

    
    public int getCondId() {
        return cond_var_id;
    }

    public String getCondName() {
        return cond_identifier;
    }

    public void addObjLine(int line) {
        obj_lines.add(line);
    }

    public void addLine(int line) {
        cond_lines.add(line);
    }

    public void printContext() {
        WaitNode tmp = this;
        while (tmp.getNid() != -1) {
            tmp.printNode();
        }
    }

    public void printNode() {
        System.out.println("Wait " + getObjId() + " static identifier " + getObjName());
        System.out.println("Cond " + getCondId() + " static identifier " + getCondName());
    }
}
