package javato.activetesting;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;

public class RaceOracle{
    //How many threads are in a specific area right now
    static ConcurrentHashMap<Long, Set<Long>> activeAccesses = new ConcurrentHashMap<>();

    //The last time each address was touched
    static ConcurrentHashMap<Long, Long> lastAccessTime = new ConcurrentHashMap<>();

    public static void checkCollision(Long memoryAddress, Integer iid){
        long currentTime = System.nanoTime();

        activeAccesses.putIfAbsent(memoryAddress, java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap()));
        Set<Long> threadsPresent = activeAccesses.get(memoryAddress);

        long myThreadId = Thread.currentThread().getId();
        threadsPresent.add(myThreadId);

        if(threadsPresent.size() > 1){
            //Win! Found a data race
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
            FileWriter fw = new FileWriter("/tmp/fuzzer_feedback.txt");
            fw.write(String.valueOf(distance));
            fw.close();
        } catch(Exception e){
            System.out.println("Filewriter" + e);
        }
    }
}
