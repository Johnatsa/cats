package race_tests;

public class TestRace1 {
    
    // The shared memory location
    public static int x = 0;

    public static void main(String[] args) {
        System.out.println("Starting TestRace1...");

        // Thread 1 will try to increment x by 1
        Thread t1 = new Thread(new Worker1());
        
        // Thread 2 will try to increment x by 2
        Thread t2 = new Thread(new Worker2());

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final value of x: " + x);
    }

    static class Worker1 implements Runnable {
        public void run() {
            // RACE CONDITION HERE: Read, then Write
            int temp = x;
            // A delay here makes the race highly likely even without the fuzzer, 
            // but your fuzzer will pause it exactly here using ActiveChecker!
            x = temp + 1;
        }
    }

    static class Worker2 implements Runnable {
        public void run() {
            // RACE CONDITION HERE: Read, then Write
            int temp = x;
            x = temp + 2;
        }
    }
}