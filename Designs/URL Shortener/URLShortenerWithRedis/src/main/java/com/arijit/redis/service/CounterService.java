package com.arijit.redis.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class CounterService {

    private static final int COUNTER_BATCH_SIZE = 10;
    private static final String REDIS_COUNTER_KEY = "url_counter";

    private final ConcurrentHashMap<Long, Boolean> counters = new ConcurrentHashMap<>();
    private final AtomicInteger counterThreshold = new AtomicInteger(0); // Tracks remaining counters
    private final String salt = "redis";

    @Autowired
    private Jedis jedis;

    // Load initial counters at startup
    @PostConstruct
    public void initializeCounters() {
        loadCountersFromRedis();
    }

    // Fetch a counter and mark it as used
    public synchronized long getCounter() {
        if (counterThreshold.get() <= COUNTER_BATCH_SIZE / 2) { // If counters are near depletion
            new Thread(this::loadCountersFromRedis).start(); // Fetch the next batch asynchronously
        }

        for (long counter : counters.keySet()) {
            if (counters.remove(counter) != null) { // Remove and use the counter
                counterThreshold.decrementAndGet();
                return counter;
            }
        }
        throw new IllegalStateException("No counters available!");
    }

    // Fetch counters from Redis and store them in the ConcurrentHashMap
    private synchronized void loadCountersFromRedis() {
        long start = jedis.incrBy(REDIS_COUNTER_KEY, COUNTER_BATCH_SIZE) - COUNTER_BATCH_SIZE + 1;
        for (long i = start; i < start + COUNTER_BATCH_SIZE; i++) {
            counters.put((long) i, true); // Store counters in memory
            System.out.println("--------Loaded Counter---------"+i);
        }
        counterThreshold.set(COUNTER_BATCH_SIZE);
    }

    public long addSaltToCounter(long counter) {
        String combined = counter + salt;
        return Integer.toUnsignedLong(combined.hashCode()); // ensure that the value is always positive
    }
}
