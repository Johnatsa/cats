package fuzzer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class DeadLockOracle {
    //Its a daemon running and just checking for deadlocks every 500ms

    public static void startDeadlockMonitor() {
        Thread monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                while(true) {
                    try {
                        long[] deadlockedThreadIDs = bean.findDeadlockedThreads();
                        if(deadlockedThreadIDs != null) {
                            //Deadlock found
                            StringBuilder threads = new StringBuilder("[");
                            for(int i = 0;i < deadlockedThreadIDs.length; i++){
                                threads.append(deadlockedThreadIDs[i]);
                                if (i < deadlockedThreadIDs.length - 1) {
                                    threads.append(", ");
                                }
                            }
                            threads.append("]");

                            BugLogger.log("Deadlock", deadlockedThreadIDs.length, "ThreadIDs: " + threads.toString());

                            System.out.println("Deadlock found");
                            System.exit(4243); //Code for deadlock
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}