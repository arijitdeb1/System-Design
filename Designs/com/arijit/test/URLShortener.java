package com.arijit.test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class URLShortener {

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE62_LENGTH = 62;
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

        public static void main(String[] args) {
        String longURL = "https://medium.com/javarevisited/how-to-implement-change-data-capture-cdc-with-kafka-connect-debezium-and-elasticsearch-03cc41454a0a";
        String shortURL = generateShortURL(longURL);
        String shortURL2 = generateShortURLOnlyBase62();
        System.out.println("Short URL: " + shortURL);
        System.out.println("Short URL2: " + shortURL2);
    }
}
