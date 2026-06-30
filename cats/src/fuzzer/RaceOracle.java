package fuzzer;

import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;

public class RaceOracle{
    //How many threads are in a specific area right now
    static ConcurrentHashMap<Long, ConcurrentHashMap<Long, Integer>> activeAccesses = new ConcurrentHashMap<>();

    //The last time each address was touched
    static ConcurrentHashMap<Long, Long> lastAccessTime = new ConcurrentHashMap<>();


    public static void checkCollision(Long memoryAddress, Integer iid){
        System.out.println("Oracle checking");
        long currentTime = System.nanoTime();

        activeAccesses.putIfAbsent(memoryAddress, new java.util.concurrent.ConcurrentHashMap<Long, Integer>());
        ConcurrentHashMap<Long, Integer> threadsPresent = activeAccesses.get(memoryAddress);

        long myThreadId = Thread.currentThread().getId(); 
        threadsPresent.put(myThreadId, iid);

        java.util.Map<Long, Integer> snapshot = new java.util.HashMap<>(threadsPresent);

        if(snapshot.size() > 1){
            //Win! Found a data race
            StringBuilder iids = new StringBuilder("[");
            boolean first = true;
            for(long eiid : snapshot.values()){
                if(!first)
                    iids.append(", ");
                iids.append(eiid);
                first = false;
            }
            iids.append("]");
            BugLogger.log("RaceCondition", threadsPresent.size(), iids.toString());
            System.exit(4242); //Code for our fuzzer to know that it exited from a date race
        }

        //Calculate how far apart were the thread accesses to a specific memory address in order to guide the mutations
        Long previousTime = lastAccessTime.put(memoryAddress, currentTime); //Returns the previous value
        if (previousTime != null){
            long distance = Math.abs(currentTime - previousTime);
            writeFeedback(distance);
        }

        threadsPresent.remove(myThreadId);
    }

    public static synchronized void writeFeedback(long distance){
        try{
            FileWriter fw = new FileWriter("generated/fuzzer_feedback.txt");
            fw.write(String.valueOf(distance));
            fw.close();
        } catch(Exception e){
            System.out.println("Filewriter" + e);
        }
    }
}
