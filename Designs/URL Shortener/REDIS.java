import redis.clients.jedis.Jedis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class REDIS {

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE62_LENGTH = 62;
    private static final String REDIS_COUNTER_KEY = "url_counter";
    private static final long INITIAL_COUNTER = 1111111;  // Start counter from 7 digits
    private static String salt = "arijit";

    public static String generateShortURL(String longURL) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(longURL.getBytes());
            StringBuilder shortURL = new StringBuilder();
            long value = 0;
            //for (int i = 0; i < 7; i++) {//use this for only MD5 hashing
            for (int i = 0; i < 8; i++) {
                //shortURL.append(Integer.toHexString((hash[i] & 0xFF) % 16)); //use this for only MD5 hashing
                value = (value << 8) | (hash[i] & 0xFF);
            }
            //return shortURL.toString(); //use this for only MD5 hashing
            return encodeBase62(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private static String encodeBase62(long value) {
        StringBuilder shortURL = new StringBuilder();
        value = Math.abs(value); // Ensure positive value
        while (shortURL.length() < 7) {
            shortURL.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return shortURL.toString();
    }

    public static String generateShortURLOnlyBase62() {
        Random random = new Random();
        StringBuilder shortURL = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            int index = random.nextInt(BASE62_LENGTH);
            shortURL.append(BASE62.charAt(index));
        }
        return shortURL.toString();
    }

    public static String getShortURLWithREDIS() {
        try (Jedis jedis = new Jedis("localhost", 6379)) { // Connect to Redis
            // Set the initial counter if it doesn't exist
            if (jedis.get(REDIS_COUNTER_KEY) == null) {
                jedis.set(REDIS_COUNTER_KEY, String.valueOf(INITIAL_COUNTER));
            }
            long counter = jedis.incr(REDIS_COUNTER_KEY); // Increment the Redis counter
            System.out.println("REDIS generated counter ------------------ "+counter);
            return combineWithSaltForREDIS(counter, salt); // Convert counter to Base62
        }
    }

    private static String combineWithSaltForREDIS(long counter, String salt){
        String combined = counter + salt;
        long saltedCounter = Integer.toUnsignedLong(combined.hashCode()); //Use hash for randomness and only positive integers
        System.out.println("REDIS generated salted counter ------------------ "+saltedCounter);
        return encodeBase62ForREDIS(saltedCounter);

    }

    private static String encodeBase62ForREDIS(long value) {
        StringBuilder shortURL = new StringBuilder();
        while (shortURL.length() < 7) {
            shortURL.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return shortURL.reverse().toString(); // Reverse to get the correct Base62 order
    }
    public static void main(String[] args) {
        String longURL = "https://medium.com/javarevisited/how-to-implement-change-data-capture-cdc-with-kafka-connect-debezium-and-elasticsearch-03cc41454a0a";
        String shortURL = generateShortURL(longURL);
        String shortURL2 = generateShortURLOnlyBase62();
        String shortURL3 = getShortURLWithREDIS();
        System.out.println("Short URL with MD5: " + shortURL);
        System.out.println("Short URL with Base62: " + shortURL2);
        System.out.println("Short URL with REDIS: " + shortURL3);
    }

}
