package com.kmaebashi.blog.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomIdGenerator {
    private SecureRandom random;
    private static RandomIdGenerator randomIdGenerator;

    RandomIdGenerator() {
        this.random = new SecureRandom();
    }

    private String getId() {
        byte[] bytes = new byte[24];
        this.random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getEncoder();
        String ret = encoder.encodeToString(bytes);

        return ret;
    }

    public static String getRandomId() {
        if (randomIdGenerator == null) {
            randomIdGenerator = new RandomIdGenerator();
        }

        return randomIdGenerator.getId();
    }
}

