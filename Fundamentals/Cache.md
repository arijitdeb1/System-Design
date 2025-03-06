# Cache

## Cache Patterns 
**`Cache Aside Pattern`**: 
  - Application code first checks the cache. If the cache has the data, the application uses it. If the cache does not have the data, the application fetches the data from the data store, adds it to the cache, and then uses it.
  - Pros: 
    - Simple to implement
    - Cache is updated/invalidated from application code when data changes or TTL(Time To Live) expires.
    - Application code is responsible for cache consistency.
    - Application can still function if the cache is down with expected slow down.
  - Cons: 
    - Cache miss penalty: The application code has to fetch the data from the data store.
    - Cache stampede: When the cache expires, multiple requests hit the data store at the same time.
    - Cache inconsistency: Data in the cache and data store can be out of sync.

**`Cache Read Through Pattern`**: 
  - Application code reads data from the cache. If the cache does not have the data, the cache fetches the data from the data store, adds it to the cache, and then returns it to the application code.
  - Pros: 
    - Cache miss penalty is reduced
    - Cache stampede is reduced
    - Use for Read-heavy applications. Preload the cache with frequently accessed data.
      - Application doesn't talk to the data store directly.
  - Cons: 
    - Cache miss for first read

**`Cache Write Through Pattern`**: 
  - Application code writes data to the cache. The cache writes the data to the data store and then updates the cache.
  - Pros: 
    - Data consistency
    - Cache and data store are always in sync
  - Cons: 
    - Write latency
    - Write amplification

**`Cache Write Around Pattern`**: 
  - Application code writes data to the data store. The data store is updated and the cache is invalidated. Application code reads data from the cache.
  - Pros: 
    - Cache is not filled with data that is not frequently accessed
    - Useful for write-heavy applications
  - Cons: 
    - Cache miss penalty
    - Cache stampede

**`Cache Write Behind Pattern`**: 
  - Application code writes data to the cache. The cache writes the data to the data store asynchronously.
  - Pros: 
    - Write latency is reduced
    - Write amplification is reduced
  - Cons: 
    - Data consistency
    - Data loss if the cache goes down before writing to the data store


## Memcached
- How data is organized? (avoid freuqent Fragmentation, slabs of variable size in Pages)
- How fast processing is handled? (Multi threading, Non blocking I/O)
- How cache eviction is handled? (LRU)
- How data access/race condition is handled? (LRU locking/slab level locking)
- How fast Read is handled? (Hastable based index for keys)
- How fast Write is handled? (Index the key using Hash, use chain/linked list if key exists)
- Distributed cache - (in Memcached , communication between memcached servers are not managed by Memcached, rather any client(e.g NodJS) which is connecting to Memcached clusters has to manage it)