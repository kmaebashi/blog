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

    @Test
    void cutStringTest001() {
        assertEquals("ab…", Util.cutString("abc", 2));
        assertEquals("ab𩸽…", Util.cutString("ab𩸽𠮟", 3));
        assertEquals("ab🍰…", Util.cutString("ab🍰🍺", 3));
        assertEquals("ab🍰🍺", Util.cutString("ab🍰🍺", 4));
        assertEquals("ab🍰🍺", Util.cutString("ab🍰🍺", 5));
    }
}