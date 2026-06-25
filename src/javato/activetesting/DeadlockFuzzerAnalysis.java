package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;
import javato.activetesting.igoodlock.DeadlockCycleInfo;
import javato.activetesting.igoodlock.Node;

import java.util.List;
import java.util.HashSet;

public class DeadlockFuzzerAnalysis extends CheckerAnalysisImpl {
    
    private HashSet<Integer> targetLockIDs = new HashSet<>();


    public void initialize() {
        DeadLockOracle.startDeadlockMonitor();

        //Gets the iids of the threads in the deadlockcycle
        if(Parameters.errorId >= 0){
            DeadlockCycleInfo cycles = DeadlockCycleInfo.read();
            List<Node> deadlockingCycle = cycles.getCycles().get(Parameters.errorId - 1);


            for (Node node : deadlockingCycle) {
                targetLockIDs.addAll(node.getContext());
            }
        }
    }


    public void lockBefore(Integer iid, Integer thread, Integer lock) {
        if(targetLockIDs.contains(iid)){
            ActiveChecker.fuzzDelay(iid);
        }
    }

    public void waitBefore(Integer iid, Integer thread, Integer lock) {

    }

    public void notifyAfter(Integer iid, Integer thread, Integer lock) {

    }

    public void notifyAllAfter(Integer iid, Integer thread, Integer lock) {
        
    }

    //Ignore
    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid) {
    }

    public void methodExitAfter(Integer iid) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
    }

    public void finish() {
    }
}
