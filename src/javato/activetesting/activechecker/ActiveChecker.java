package javato.activetesting.activechecker;

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
        String tape = new String(Files.readAllBytes(Paths.get("/tmp/tape.txt")));

        //Tape format = {"45": 10, "90": 0} (iid : delay)
        tape = tape.replaceAll("[{}\"\\s]", "");
        if(!tape.isEmpty()){
            String[] pairs = tape.split(",");
            for (String pair : pairs){
                String[] curr = pair.split(":");
                fuzzerTape.put(Integer.parseInt(curr[0]), Integer.parseInt(curr[1]))
            }
        }
    }


    //RacerD must add a hook to the original code calling this function
    final public static void fuzzDelay(Integer iid){
        Integer delay = fuzzerTape.get(iid);
        
        Thread.sleep(delay);
    }
}
