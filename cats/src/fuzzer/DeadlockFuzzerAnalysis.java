package fuzzer;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeadlockFuzzerAnalysis implements Analysis{
    
    //The list of the ids. Result of the graph
    private HashSet<Integer> targetLockIDs = new HashSet<>();
    
    //List of the cycles
    private List<HashSet<Integer>> allCycles = new ArrayList<>();

    //Set with the wait only nodes
    private HashSet<Integer> waitOnlyNodes = new HashSet<>();

    //THe paused threads waiting after their lock
    private HashSet<Integer> parkedIids = new HashSet<>();

    //lock used to freeze the threads
    private final Object coordinator = new Object();

    //Maps thread ids to iids
    public static final ConcurrentHashMap<Long, Integer> activeTraps = new ConcurrentHashMap<>();


    public void initialize() {
        //Starts the daemon locating the deadlocks
        DeadLockOracle.startDeadlockMonitor();

        
        //!If not from a filepath then we need to figure out where will the graph put its results
        loadDeadLockTargets("generated/graph_iids.txt");
    }

    private void loadDeadLockTargets(String filepath){
        try{
            String content = new String(Files.readAllBytes(Paths.get(filepath))).trim();
            if(!content.isEmpty()){
                
                //Parse the json file
                int waitIndex = content.indexOf("\"waitOnlyNodes\"");
                String cyclesSection = waitIndex != -1 ? content.substring(0, waitIndex) : content;
                String waitOnlySection = waitIndex != -1 ? content.substring(waitIndex) : "";

                Pattern arrayPattern = Pattern.compile("\\[([0-9,\\s]+)\\]");
                
                //Parse the cycle section
                Matcher cycleMatcher = arrayPattern.matcher(cyclesSection);
                while(cycleMatcher.find()){
                    HashSet<Integer> cycle = new HashSet<>();
                    String[] numbers = cycleMatcher.group(1).split(",");
                    for(String num : numbers){
                        if(!num.trim().isEmpty()){
                            int iid = Integer.parseInt(num.trim());
                            cycle.add(iid);
                            targetLockIDs.add(iid);
                        }
                    }
                    if(!cycle.isEmpty()) allCycles.add(cycle);
                }
            
                //Parse waitOnlyNodes section
                Matcher waitMatcher = arrayPattern.matcher(waitOnlySection);
                while(waitMatcher.find()){
                    String[] numbers = waitMatcher.group(1).split(",");
                    for(String num : numbers){
                        if(!num.trim().isEmpty()){
                            int iid = Integer.parseInt(num.trim());
                            waitOnlyNodes.add(iid);
                            targetLockIDs.add(iid);
                        }
                    }
                }

            }

        } catch(Exception e){
            System.out.println("Deadlock thread ids file" + e);
        }
    }

    private void trapThread(Integer iid, Integer thread, String actionType){
        if(targetLockIDs.contains(iid)){
            
            if(waitOnlyNodes.contains(iid)){
                BugLogger.log("WaitOnlyNode", 1, String.valueOf(iid));
                System.exit(4244); //error code for waitOnlyNode
            }

            synchronized(coordinator){
                parkedIids.add(iid);
                
                long currentThread = Thread.currentThread().getId();
                activeTraps.put(currentThread, iid);


                boolean cycleTriggered = false;
                for(HashSet<Integer> cycle : allCycles){
                    if(parkedIids.containsAll(cycle)){
                        cycleTriggered = true;
                        break;
                    }
                }

                if(cycleTriggered){
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
