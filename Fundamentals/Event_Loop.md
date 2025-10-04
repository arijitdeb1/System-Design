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





