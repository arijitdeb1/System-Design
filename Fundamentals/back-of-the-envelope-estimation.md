
## Back of the Envelope Estimation

## Cheat Sheet
| Zeros | Traffic (requests/sec) | Storage |
|-------|------------------------|---------|
| 10^3  | thousand               | KB      |
| 10^6  | million                | MB      |
| 10^9  | billion                | GB      |
| 10^12 | trillion               | TB      |
| 10^15 | quadrillion            | PB      |

-----------------------------------------

* char - 1 byte(ASCII), 2 byte(UNICODE)
* int32 - 4 byte, int64 - 8 byte
* float - 4 byte, double - 8 byte
* long - 8 byte
* image - 300 kilobyte (standard for consideration while calculations)

-----------------------------------------
### Calculate Storage Requirement for below scenarios:
* 10 million(10^7) user each need 1 MB(10^6) of storage = 10^13 = 10 TB

-----------------------------------------
### Scenario :
 * Total User - 1 billion(10^9)
 * DAU(Daily Active User) - 25% of Total User = 250 million
 * Every user is doing 5 Read and 2 Write operations per day = 7 queries/day
 * Every user is doing 2 posts having 250 characters each = 500 characters/day
 * 10% user uploading 1 image of 300 KB
 * System is storing last 5 posts of each user in cache(RAM)
 * One system can store 50GB of data in RAM 

### Traffic Estimation:
 * Total Queries/sec = 250 million * 7 / 24*60*60 = 25*10^7 * 7 / 10K(round off) = 17K queries/sec

### Storage Estimation:
 * 2 posts = 250 characters * 2 bytes = 500 bytes * 2 posts = 1 KB per user/day
 * Total Posts by DAU = 250 million * 1 KB = 250 GB/day
 * 10% of DAU user uploading 1 image of 300 KB = 25 million * 300 KB = 7500 GB/day = 7.5 TB/day = 8 TB/day(round off)
 * Total Storage for 5 years or 2000 days approx.
       posts = 250 GB/day * 2000 = 500 TB
       images = 8 TB/day * 2000 = 16 PB

### RAM Estimation:
 * RAM required for 1 user = 5 posts * 250 characters = 1250 bytes = 1.25 KB = 2 KB(round off)
 * RAM required for DAU = 250 million * 2 KB = 500 GB
 * Total number of systems/servers required = 500 GB / 50 GB = 10 systems

### Latency
 * Latency = 95% percentile 500 ms(_assumed_)
 * 1 server have 50 threads
 * 1 thread can serve 2 requests/sec ( considering 500 ms for each request)
 * 1 server can serve 100 requests/sec ( 50 threads * 2 requests/sec)
 * Total servers required = 17K / 100 = 170 servers

### Trade Off (CAP Theorem)
 * Consistency - All nodes see the same data at the same time
 * Availability - Every request receives a response about whether it was successful or failed
 * Partition Tolerance - The system continues to operate despite arbitrary message loss or failure of part of the system
 * In a distributed system, we can only achieve two of the three.
    







-----------------------------------------
* 1 million requests/day = 12 requests/sec (approx)
* 1 billion requests/day = 11574 requests/sec (approx)
-----------------------------------------
* 10% of 1 million = 100,000 requests/hour = 28 requests/sec (approx) = 30 requests/sec (round off)
* Usual Inflow = 1 million requests/day = 12 requests/sec (approx)
* Peak Inflow = 10% peak for 1 hour = 100,000 requests/hour = 28 requests/sec (approx) = 30 requests/sec (round off)
* Peak Inflow = 30% peak for 1 hour = 300,000 requests/hour = 83 requests/sec (approx) = 90 requests/sec (round off)
* Peak Inflow = 30% peak for 3 hour = 100,000 requests/hour = 28 requests/sec (approx) = 30 requests/sec (round off)
* Assuming a user requires 50 transactions per user session i.e 
 1 million requests/day = 12 requests/sec (approx) = 12x50 = 600 transactions/sec (approx) = 50 million transactions/day
-------------------------------------------



