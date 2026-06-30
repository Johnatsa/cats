package fuzzer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;



public class BugLogger{

    public static synchronized void log(String bugType, Integer threadNum, String iids){
        String filepath = "generated/bugs.txt";
        String entry = bugType + " " + threadNum + " " + iids;
        
        try (PrintWriter out = new PrintWriter(new FileWriter(filepath, true))) {
            out.println(entry);
            System.out.println("[LOGGED] " + entry);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not write to " + filepath);
        }
    }

}