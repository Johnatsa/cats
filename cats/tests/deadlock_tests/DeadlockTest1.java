package deadlock_tests;

public class DeadlockTest1 {

    // The two resources our threads will fight over
    static final Object lockA = new Object();
    static final Object lockB = new Object();

    public static void main(String[] args) {
        System.out.println("Starting TestDeadlock1...");

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                synchronized (lockA) {
                    System.out.println("Thread 1: Holding Lock A...");
                    
                    // We sleep to guarantee Thread 2 has time to grab Lock B
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    
                    System.out.println("Thread 1: Waiting for Lock B...");
                    synchronized (lockB) {
                        System.out.println("Thread 1: Acquired Lock A and Lock B!");
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                synchronized (lockB) {
                    System.out.println("Thread 2: Holding Lock B...");
                    
                    // We sleep to guarantee Thread 1 has time to grab Lock A
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    
                    System.out.println("Thread 2: Waiting for Lock A...");
                    synchronized (lockA) {
                        System.out.println("Thread 2: Acquired Lock B and Lock A!");
                    }
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If a deadlock occurs, the program will freeze before ever reaching this line.
        System.out.println("Final value: Success! (If you see this, there was no deadlock)");
    }
}