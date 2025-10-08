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

    @Test
    void createLinkAnchor001() {
        String src = "なんとか https://kmaebashi.com かんとか";
        String result = Util.createLinkAnchor(src);
        assertEquals("なんとか <a href=\"https://kmaebashi.com\">https://kmaebashi.com</a> かんとか", result);
    }

    @Test
    void createLinkAnchor002() {
        String src = "なんとか https://www.youtube.com/@someone かんとか";
        String result = Util.createLinkAnchor(src);
        assertEquals("なんとか <a href=\"https://www.youtube.com/@someone\">https://www.youtube.com/@someone</a> かんとか", result);
    }

    @Test
    void escapeHtmlCharTest001() {
        assertEquals("&amp;", Util.escapeHtmlChar('&'));
        assertEquals("&quot;", Util.escapeHtmlChar('"'));
        assertEquals("&lt;", Util.escapeHtmlChar('<'));
        assertEquals("&gt;", Util.escapeHtmlChar('>'));
        assertEquals("&#39;", Util.escapeHtmlChar('\''));
        assertEquals("a", Util.escapeHtmlChar('a'));
    }

    @Test
    void calcPagenationStartTest001() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(0, 1, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(0, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest002() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(1, 1, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(1, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest003() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(50, 1, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(1, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest004() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(51, 1, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(2, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest005() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(501, 2, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest006() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(501, 5, 50, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest007() {
        int[] dispPageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(501, 6, 50, dispPageCountBuf);
        assertEquals(2, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }

}