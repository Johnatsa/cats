package fuzzer;

import lockWaitGraph.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Step 2 of the project: Will fill out the graph
// 1) Receive events from ObserverForActiveTesting 
// 2) Map tags/iid to the current runtime thread id
// 3) Fill out LockWaitGraph
public class GraphAnalysis implements Analysis {
    private LockWaitGraph graph;
    private Map<Integer, ArrayList<LockNode>> threadLockStack;
    private Map<Integer, WaitNode> waitNodeByLock;
    private LinkedHashSet<Integer> candidateIids;
    private Object myLock;
    private Map<Integer, LockNode> lockNodeByLock;



    public void initialize() {
        myLock = new Object(); // <-- Instantiate the lock here first!
        DeadLockOracle.startDeadlockMonitor();


        synchronized (myLock) {
            graph = new LockWaitGraph();
            threadLockStack = new HashMap<Integer, ArrayList<LockNode>>();
            waitNodeByLock = new HashMap<Integer, WaitNode>();
            candidateIids = new LinkedHashSet<Integer>();
            lockNodeByLock = new HashMap<Integer, LockNode>();
        }
    }

    // lockBefore and unlockAfter usually create or advance lock-related nodes.
    public void lockBefore(Integer iid, Integer thread, Integer lock){
        synchronized (myLock) {
            LockNode current = lockNodeByLock.get(lock);
            if (current == null) {
                current = new LockNode("lock-" + lock, lock);
                graph.addNode(current);
                lockNodeByLock.put(lock, current);
            }
            current.addLine(iid);
            
            graph.addNode(current);

            ArrayList<LockNode> stack = threadLockStack.get(thread);
            if (stack == null) {
                stack = new ArrayList<LockNode>();
                threadLockStack.put(thread, stack);
            }

            LockNode previous = null;
            if (!stack.isEmpty()) {
                previous = stack.get(stack.size() - 1);
            }

            if (previous != null) {
                graph.addEdge(previous, new Edge(thread, current, previous));
                candidateIids.add(Integer.valueOf(previous.getId()));
                candidateIids.add(iid);
            }

            stack.add(current);
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock){
        synchronized (myLock) {
            ArrayList<LockNode> stack = threadLockStack.get(thread);
            if (stack == null || stack.isEmpty()) {
                return;
            }

            if (stack.get(stack.size() - 1).getLockId() == lock) {
                stack.remove(stack.size() - 1);
                return;
            }

            for (int i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i).getLockId() == lock) {
                    stack.remove(i);
                    break;
                }
            }
        }
    }



    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked){ }
    public void methodEnterBefore(Integer iid){ }
    public void methodExitAfter(Integer iid){ }


    // startBefore and joinAfter add thread-relationship information if your graph tracks thread causality.
    public void startBefore(Integer iid, Integer parent, Integer child){ }
    public void joinAfter(Integer iid, Integer parent, Integer child){ }



    // waitAfter, notifyBefore, and notifyAllBefore usually create wait/notification edges.
    public void waitAfter(Integer iid, Integer thread, Integer lock){
        synchronized (myLock) {
            addWaitOrNotifyEdge(iid, thread, lock, WaitEdge.CallType.WAIT);
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock){
        synchronized (myLock) {
            addWaitOrNotifyEdge(iid, thread, lock, WaitEdge.CallType.NOTIFY);
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock){
        synchronized (myLock) {
            addWaitOrNotifyEdge(iid, thread, lock, WaitEdge.CallType.NOTIFY);
        }
    }





    // readBefore and writeBefore only matter if your graph also tracks memory-access relationships.
    public void readBefore(Integer iid, Integer thread, Long memory){
        // 4
    }

    public void writeBefore(Integer iid, Integer thread, Long memory){
        // 5
    }


    // finish() is where you usually run cycle detection and export the result for the fuzzer.
    public void finish() {
        synchronized (myLock) {
            graph.findAllCycles();
            writeCandidateIids("generated/graph_iids.txt"); // optional, only if fuzzer still needs iids

            System.out.println("*--> GraphAnalysis wrote graph_cycles.json");
            System.out.println("*--> GraphAnalysis wrote " + candidateIids.size()
                + " candidate iids to graph_iids.txt");
        }
    }



    private void addWaitOrNotifyEdge(Integer iid, Integer thread, Integer lock, WaitEdge.CallType type) {
        ArrayList<LockNode> stack = threadLockStack.get(thread);
        if (stack == null || stack.isEmpty()) {
            return;
        }

        WaitNode waitNode = waitNodeByLock.get(lock);
        if (waitNode == null) {
            waitNode = new WaitNode(lock, "lock-" + lock, iid, "iid-" + iid);
            graph.addNode(waitNode);
            waitNodeByLock.put(lock, waitNode);
        }

        waitNode.addLine(iid);
        candidateIids.add(iid);
        graph.addEdge(stack.get(stack.size() - 1), new WaitEdge(thread, waitNode, stack.get(stack.size() - 1), type));
    }

    private void writeCandidateIids(String fileName) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(fileName));
            for (Integer iid : candidateIids) {
                out.println(iid);
            }
        } catch (IOException e) {
            System.err.println("Could not write " + fileName + ": " + e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
