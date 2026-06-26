package javato.activetesting;

import javato.activetesting.analysis.CheckerAnalysisImpl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class DeadlockFuzzerAnalysis extends CheckerAnalysisImpl {
    
    //The list of the ids. Result of the graph
    private HashSet<Integer> targetLockIDs = new HashSet<>();
    
    //THe paused threads waiting after their lock
    private HashSet<Integer> parkedIids = new HashSet<>();

    //lock used to freeze the threads
    private final Object coordinator = new Object();


    public void initialize() {
        //Starts the daemon locating the deadlocks
        DeadLockOracle.startDeadlockMonitor();

        
        //!If not from a filepath then we need to figure out where will the graph put its results
        loadDeadLockTargets("filepath");
    }

    private void loadDeadLockTargets(String filepath){
        try{
            String content = new String(Files.readAllBytes(Paths.get(filepath))).trim();
            if(!content.isEmpty()){
                String[] numbers = content.split(",");
                for (String num : numbers) {
                    targetLockIDs.add(Integer.parseInt(num.trim()));
                }

            }
        } catch(Exception e){
            System.out.println("Deadlock thread ids file" + e);
        }
    }

    private void trapThread(Integer iid, Integer thread, String actionType){
        if(targetLockIDs.contains(iid)){

            synchronized(coordinator){
                parkedIids.add(iid);

                if(parkedIids.containsAll(targetLockIDs)){
                    coordinator.notifyAll();;
                } else{
                    try{
                        coordinator.wait(5000);
                    } catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                }

                parkedIids.remove(iid);
            }
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock) {
        trapThread(iid, thread, "Lock");
    }

    public void waitBefore(Integer iid, Integer thread, Integer lock) {
        trapThread(iid, thread, "Wait");
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        trapThread(iid, thread, "Notify");
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        trapThread(iid, thread, "Notify");
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

    public void notifyAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyAllAfter(Integer iid, Integer thread, Integer lock) {
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
