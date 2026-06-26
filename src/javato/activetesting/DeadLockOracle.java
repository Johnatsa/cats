package javato.activetesting;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


public class DeadLockOracle{
    //Its a daemon running and just checking for deadlocks every 500ms

    public static void startDeadlockMonitor(){
        Thread monitor = new Thread(()->{
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            while(true){
                try{
                    long[] deadlockedThreadIDs = bean.findDeadlockedThreads();
                    if(deadlockedThreadIDs != null){
                        //Deadlock found
                        System.out.println("Deadlock found");
                        System.exit(4243); //Code for deadlock
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e){break;}
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}
