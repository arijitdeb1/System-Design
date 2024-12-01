package com.arijit.redis.controller;

import com.arijit.redis.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class URLShortenerController {

    @Autowired
    private CounterService counterService;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // Convert a counter to Base62
    private String toBase62(long counter) {
        StringBuilder base62 = new StringBuilder();
        StringBuilder shortURL = new StringBuilder();
        while (shortURL.length() < 7) {
            shortURL.append(CHARSET.charAt((int) (counter % 62)));
            counter /= 62;
        }
        return shortURL.reverse().toString();
    }

    @PostMapping("/shorten")
    public String shortenUrl(@RequestBody String longUrl) {
        long counter = counterService.getCounter(); // Fetch a counter
        //By adding a salt and/or using hashing, you can make the short URL less predictable and prevent people from easily guessing subsequent URLs.
        long saltedCounter = counterService.addSaltToCounter(counter);
        String shortUrl = toBase62(saltedCounter); // Convert to Base62
        return "http://short.url/" + shortUrl;
    }
}
