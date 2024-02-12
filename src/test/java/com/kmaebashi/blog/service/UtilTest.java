package com.kmaebashi.blog.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void hashPasswordTest001() throws Exception {
        String hashed = Util.hashPassword("testpass/123");
        assertTrue(Util.checkPassword("testpass/123", hashed));
        assertFalse(Util.checkPassword("testpass/234", hashed));
    }
}