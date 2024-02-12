package com.kmaebashi.blog.service;

import org.mindrot.jbcrypt.BCrypt;

public class Util {
    private Util() {}

    static String hashPassword(String src) {
        return BCrypt.hashpw(src, BCrypt.gensalt());
    }

    static boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }
}
