package com.kmaebashi.blog.service;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {
    private Util() {}

    static String hashPassword(String src) {
        return BCrypt.hashpw(src, BCrypt.gensalt());
    }

    static boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }

    public static String getSuffix(String fileName) {
        int pointIndex = fileName.lastIndexOf(".");
        if (pointIndex != -1) {
            return fileName.substring(pointIndex + 1);
        } else {
            return null;
        }
    }
}
