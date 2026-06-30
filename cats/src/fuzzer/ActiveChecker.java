package fuzzer;

import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ActiveChecker {

    static private Map<Integer, Integer> fuzzerTape = new HashMap<>();

    static { //Runs once when the program starts
        loadTape();
    }

    private static void loadTape(){
        String tape = null;
        try{
          tape = new String(Files.readAllBytes(Paths.get("generated/tape.txt")));
        } catch(Exception e){
            System.out.println("tape creation" + e);
        }

        //Tape format = {"45": 10, "90": 0} (iid : delay)
        tape = tape.replaceAll("[{}\"\\s]", "");
        if(tape != null){
            String[] pairs = tape.split(",");
            for (String pair : pairs){
                String[] curr = pair.split(":");
                fuzzerTape.put(Integer.parseInt(curr[0]), Integer.parseInt(curr[1]));
            }
        }
    }


    //RacerD must add a hook to the original code calling this function
    final public static void fuzzDelay(Integer iid){
        Integer delay = fuzzerTape.get(iid);
        
        // Athena: What if delay is null
        // It will never be null or negative. Because it gets initialized when running the fuzzer b4 running the program
        try{
            Thread.sleep(delay);
        } catch(Exception e){
            System.out.println("Thread sleep" + e);
        }
    }
}
