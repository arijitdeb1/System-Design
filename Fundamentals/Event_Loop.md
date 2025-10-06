# Event Loop @ Nginx

<img width="2110" height="3491" alt="image" src="https://github.com/user-attachments/assets/b2fa63e9-90b8-4773-be98-e1069b06c79d" />

### Initial Events (Clients and OS Kernel)
(1).**Client 1 connects:** A client opens a TCP connection to Nginx. The operating system's kernel handles this and places a "new connection" event in the epoll **event queue** (EQ1).

(2).**Client 2 connects:** Another client does the same, creating another event in the queue (EQ2).

### Event loop processing (First cycle)

(1).**Wait for events:** The Nginx worker process calls epoll_wait(), essentially asking the kernel for a list of all connections that are ready for some kind of action (reading, writing, or new connection). The process then yields control to the kernel, waiting to be notified.

(2).**Kernel notification:** The kernel informs the worker that new connection events (EQ1 and EQ2) are ready.

(3).**Process events:** The worker gets the list of ready events and loops through them.

  (a).**Handle Client 1 (H1_READ):** The worker accepts the connection and reads the request headers from Client 1.                   The requestrequires data from a backend service.
  
  (b).**Proxy to Backend A (P1_PROXY):** The worker initiates a non-blocking connection to Backend A to get the data. It              sends the request and, critically, does not wait. It simply registers the new backend socket with the kernel for              monitoring and immediately moves to the next event in its batch.
  
  (c).**Handle Client 2 (H2_READ):** The worker moves on to Client 2's event. It reads the request headers. For this example,         let's say Client 2 is on a slow network and only sends part of its data. The worker reads what's available and                registers its interest in being notified when more data arrives, without waiting.
  
  (d).**Return to wait:** The worker has processed all currently ready events. It calls epoll_wait() again and goes back to           sleep.
    
### Event loop processing (Second and subsequent cycles)

  (1).**Backend response:** Sometime later, Backend A finishes its work and sends a response. The kernel sees this and adds a         "read ready" event for the backend socket to the epoll queue (EQ3).
  
  (2).**More Client 2 data:** At the same time, Client 2 finally sends the rest of its data. The kernel adds a "read ready"           event for Client 2's socket (EQ4).
  
  (3).**Process next events:** The worker wakes up and sees that events EQ3 and EQ4 are ready.
  
  (4).**Handle Backend A response (H3_READ):** The worker reads the response from Backend A.
  
  (5).**Write to Client 1 (P3_WRITE):** The worker begins writing the response back to Client 1. If Client 1 is slow to               receive the data, the worker sends what it can, marks the connection as needing more writing, and moves on without            waiting. It registers the client's socket for future write events.
  
   (6).**Handle Client 2 data (H4_READ):** The worker reads the rest of the data from Client 2, processes the full request,            and prepares the response.
   
   (7).**Respond to Client 2 (P4_FINISH):** The worker sends the complete response back to Client 2 and closes the connection.
  
   (8).**Finalize Client 1:** Eventually, after several more event loop cycles, the worker finishes writing the entire response to Client 1 (P5_FINISH) and closes the connection.

This flow demonstrates how the event loop allows the worker to efficiently juggle both requests, never blocking on a single slow operation. It can make progress on one request, handle another's event, and then return to the first once it becomes ready again.


# Event Loop @ Python Asyncio

Think of the event loop as an intelligent task manager. Instead of waiting idly when a task is blocked (like waiting for a network response), it switches to other tasks that are ready to run. This is what makes async code efficient.

Let's look at three tasks that simulate downloading files:
```
import asyncio
import time

async def download_file(name, duration):
    print(f"[{time.time():.2f}s] {name}: Started downloading")
    await asyncio.sleep(duration)  # Simulates download time
    print(f"[{time.time():.2f}s] {name}: Download complete!")
    return f"{name}_data"

async def main():
    results = await asyncio.gather(
        download_file("File_A", 3),
        download_file("File_B", 2),
        download_file("File_C", 1)
    )
    print(f"All downloads complete: {results}")

# Run the event loop
start = time.time()
asyncio.run(main())
print(f"Total time: {time.time() - start:.2f}s")
```

Expected Output:

```
[0.00s] File_A: Started downloading
[0.00s] File_B: Started downloading
[0.00s] File_C: Started downloading
[1.00s] File_C: Download complete!
[2.00s] File_B: Download complete!
[3.00s] File_A: Download complete!
All downloads complete: ['File_A_data', 'File_B_data', 'File_C_data']
Total time: 3.00s
```

Notice how all three "downloads" run concurrently, completing in 3 seconds instead of 6 seconds (1+2+3) if they ran sequentially.

**asyncio.run()** always creates a new event loop and closes it at the end of its execution. It is designed to be the main entry point for asyncio programs and should ideally be called only once. This behavior ensures that each call to asyncio.run() operates within a clean and isolated event loop environment, preventing potential conflicts or unexpected behavior that might arise from reusing or modifying an existing loop.

## How the Event Loop Manages Tasks

The event loop maintains two key data structures:

1. **Ready Queue:** Tasks that can run immediately
2. **Waiting Queue:** Tasks paused at await with their wake-up times

Let me walk you through exactly what happens:

**Time: 0.00s - Initialization**
```
Ready Queue: [File_A, File_B, File_C]
Waiting Queue: []

Action: Event loop created by asyncio.run()
```

**Time: 0.00s - File_A Starts**
```
Event Loop: "Let me run File_A"
- File_A prints "Started downloading"
- File_A hits: await asyncio.sleep(3)
- File_A: "I need to wait 3 seconds"

Ready Queue: [File_B, File_C]
Waiting Queue: [(3.0s, File_A)]

Action: File_A moved from Ready to Waiting
```

**Time: 0.00s - File_B Starts**
```
Event Loop: "File_A is waiting, let me run File_B"
- File_B prints "Started downloading"
- File_B hits: await asyncio.sleep(2)

Ready Queue: [File_C]
Waiting Queue: [(2.0s, File_B), (3.0s, File_A)]

Action: File_B moved to Waiting
```

**Time: 0.00s - File_C Starts**
```
Event Loop: "Let me run File_C"
- File_C prints "Started downloading"
- File_C hits: await asyncio.sleep(1)

Ready Queue: []
Waiting Queue: [(1.0s, File_C), (2.0s, File_B), (3.0s, File_A)]

Action: All tasks are waiting, event loop sleeps until 1.0s
```

**Time: 1.00s - File_C Resumes**
```
Event Loop: "File_C's timer expired, wake it up!"
- File_C moved from Waiting to Ready
- File_C resumes after await
- File_C prints "Download complete!"
- File_C returns result and exits

Ready Queue: []
Waiting Queue: [(2.0s, File_B), (3.0s, File_A)]

Action: File_C completed and removed from event loop
```

## Key Insights
**1. Non-Blocking Behavior**
When a task hits await, it doesn't block the entire program. The event loop immediately switches to another ready task.

**2. Efficient Task Management**
The event loop uses:

  **Ready Queue (FIFO):** Processes tasks in order
  **Waiting Queue (Priority Queue):** Sorted by wake-up time for efficient checking

**3. Automatic Context Switching**
You don't manually manage task switching - the event loop handles it automatically when it encounters await.  

**The Magic of await**
Every time you write await, you're essentially saying:

"I'm waiting for something. Event loop, please run other tasks and come back to me when I'm ready."


# Event Loop @ FastAPI

Instead of creating a new event loop for each request, here is how FastAPI handles requests correctly using its existing event loop:

**For async def endpoints:** When an asynchronous function is called, it becomes a coroutine. The event loop schedules this coroutine as a task to be run. When the task encounters an await statement (for example, await asyncio.sleep(1)), it yields control back to the event loop. The event loop then immediately switches to process the next ready task, which could be another incoming request.

**For def (synchronous) endpoints:** If you define a function with a standard def signature, FastAPI recognizes that it is blocking. To prevent it from freezing the main event loop, FastAPI automatically runs this function in a separate thread from an internal thread pool. The main event loop remains free to process other incoming requests, and once the synchronous task is complete, its result is returned. 

```
from fastapi import FastAPI
import asyncio

app = FastAPI()

async def some_long_async_operation():
    """Simulates a non-blocking I/O operation."""
    await asyncio.sleep(2)
    return "Operation complete!"

@app.get("/perform-async-task")
async def perform_async_task():
    # Correct: Schedule a task within the existing event loop
    result = await some_long_async_operation()
    return {"message": result}

```
In this correct example, if 10 requests hit the `/perform-async-task` endpoint at the same time:
- FastAPI schedules 10 coroutines to run on its single, shared event loop.
- The first request starts executing some_long_async_operation(). When it hits await asyncio.sleep(2), it pauses and gives control back to the event loop.
- The event loop immediately picks up the next task (the second request) and starts running it.
- This continues for all 10 requests. All 10 are "sleeping" concurrently, allowing the single event loop to efficiently manage their I/O waits without creating any new threads or event loops for the requests themselves.

## Handle millions request:
On a single core, a single event loop is extremely efficient for managing many I/O-bound tasks concurrently. However, several factors prevent it from handling millions of requests alone:
- **CPU-bound tasks:** A single CPU-bound operation, like a complex calculation or data processing, will block the event loop, freezing all other requests in that process until the task is complete.
- **System limitations:** A single process is limited by the server's available resources, like CPU and RAM. On multi-core machines, a single-process application would underutilize the hardware by only using one core.

### How to scale FastAPI to handle millions of requests
High-traffic FastAPI applications use multiple processes, distributed across multiple servers, to achieve massive scale. 

1. **Use multiple workers with a production ASGI server**
In production, you don't run a FastAPI app with a single uvicorn command. Instead, you use a process manager like `Gunicorn` to run multiple `Uvicorn` worker processes. 
`Gunicorn:` This orchestrator runs a master process that manages multiple worker processes.
`Uvicorn workers:` Gunicorn spawns a number of Uvicorn workers, with each worker running its own event loop and FastAPI application instance.
`Utilize CPU cores:` A common practice is to configure Gunicorn with `(2 * CPU_CORES) + 1` or similar worker counts to maximize hardware utilization.

2. **Scale horizontally with load balancing**
For even higher loads, you can run multiple instances of your Gunicorn-Uvicorn application and place them behind a load balancer, such as NGINX. 
`Load balancer:` This distributes incoming traffic across multiple servers or containers, ensuring no single instance is overwhelmed.
`Stateless applications:` To enable seamless horizontal scaling, your FastAPI application should be stateless. Store user sessions and other stateful data in a centralized, shared cache like Redis instead of in memory.


3. **Eliminate blocking operations**
Even with multiple workers, a single blocking call can still freeze an entire worker process. All requests handled by that worker would be stalled. 
`Async drivers:` Use async versions of database drivers and HTTP clients (e.g., `asyncpg` for PostgreSQL, databases for multiple databases, httpx for HTTP requests).
`Offload CPU-heavy tasks:` For CPU-intensive work, like image processing or AI model inference, move the task out of the request/response cycle. Use tools like FastAPI's `BackgroundTasks` or a separate job queue with `Celery`.

4. **Cache aggressively**
