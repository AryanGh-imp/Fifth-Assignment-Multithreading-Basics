### 1. `start()` vs `run()`

```java  
public class StartVsRun {    
    static class MyRunnable implements Runnable {    
        public void run() {    
            System.out.println("Running in: " + Thread.currentThread().getName()); 
        }    
    }    
    public static void main(String[] args) throws InterruptedException {    
        Thread t1 = new Thread(new MyRunnable(), "Thread-1");    
        System.out.println("Calling run()");    
        t1.run();    
        Thread.sleep(100);    
    
        Thread t2 = new Thread(new MyRunnable(), "Thread-2");    
        System.out.println("Calling start()");    
        t2.start();    
    }  
}  
```  

**Questions:**

- What output do you get from the program? Why?

- What’s the difference in behavior between calling `start()` and `run()`?

---  
**Answers:**

1 : `t1.run()` executes the `run()` method directly in the main thread, printing `"Running in: main"`.
`t2.start()` creates a new thread (Thread-2) that executes the `run()` method, printing `"Running in: Thread-2"`.
`Thread.sleep(100)` ensures t1’s output appears before t2’s, though Thread-2’s output timing depends on JVM scheduling.

**Output:** 
```
Calling run()
Running in: main
Calling start()
Running in: Thread-2
```

2 : `run()` is a regular method call in the current thread; `start()` initiates a new thread for concurrent execution.

---

### 2. Daemon Threads

```java  
public class DaemonExample {    
    static class DaemonRunnable implements Runnable {    
        public void run() {    
            for(int i = 0; i < 20; i++) {    
                System.out.println("Daemon thread running...");    
                try {    
                    Thread.sleep(500);    
                } catch (InterruptedException e) {    
                 //[Handling Exception...]  
                }            
            }    
        }    
    }    
    public static void main(String[] args) {    
        Thread thread = new Thread(new DaemonRunnable());    
        thread.setDaemon(true);    
        thread.start();    
        System.out.println("Main thread ends.");    
    }  
}  
```  

**Questions:**
- What output do you get from the program? Why?

- What happens if you remove `thread.setDaemon(true)`?

- What are some real-life use cases of daemon threads?
---
**Answers:**

1 : **Output:** `"Main thread ends."` followed by a few (or none) `"Daemon thread running..."` messages.

```
Main thread ends.
Daemon thread running...
```
The thread is a daemon thread, so the JVM exits when the main thread ends, terminating the daemon thread early, even though its loop (20 iterations) hasn’t completed.

2 : **Output:** `"Main thread ends."` followed by `"Daemon thread running..."` printed 20 times over ~10 seconds.

Without `setDaemon(true)`, the thread is a user thread, so the JVM waits for it to finish all 20 iterations before exiting.

3 : Garbage collection, logging, IDE background tasks, etc.

They handle non-critical background tasks that automatically stop when the main application ends, avoiding manual cleanup.

---
### 3. A shorter way to create threads

```java  
public class ThreadDemo {  
    public static void main(String[] args) {  
        Thread thread = new Thread(() -> {  
            System.out.println("Thread is running using a ...!");  
        });  
  
        thread.start();  
    }  
}   
```  

**Questions:**
- What output do you get from the program?

- What is the `() -> { ... }` syntax called?

- How is this code different from creating a class that extends `Thread` or implements `Runnable`?

---

**Answers:**

1 : `Thread is running using a ...!`

2 : It’s called a **lambda expression**.

3 : 
- **Lambda**: Short and inline, good for simple tasks, but not reusable.
-  **Implements Runnable**: More code, reusable, better for complex tasks.
-  **Extends Thread**: More code, less flexible due to inheritance, not usually recommended.