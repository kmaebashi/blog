package com.kmaebashi.blog.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void splitQueryKeywordsTest001() {
        List<String> ret = Util.splitQueryKeywords("あいうえお かきくけこ");
        assertEquals("あいうえお", ret.get(0));
        assertEquals("かきくけこ", ret.get(1));
    }

    @Test
    void splitQueryKeywordsTest002() {
        List<String> ret = Util.splitQueryKeywords(" あいうえお 　かきくけこ　");
        assertEquals("あいうえお", ret.get(0));
        assertEquals("かきくけこ", ret.get(1));
    }

    @Test
    void boldifyHitStringTest001() {
        String src = "奇妙奇天烈摩訶不思議奇想天外四捨五入出前迅速落書無用";
        List<String> keywords = new ArrayList<>();
        keywords.add("奇");
        String ret = Util.boldifyHitString(src, keywords);
        assertEquals("<b>奇</b>妙<b>奇</b>天烈摩訶不思議<b>奇</b>想天外四捨五入出前迅速落書無用", ret);
    }

    @Test
    void boldifyHitStringTest002() {
        String src = "奇妙奇天烈摩訶不思議奇想天外四捨五入出前迅速落書無用";
        List<String> keywords = new ArrayList<>();
        keywords.add("奇");
        keywords.add("奇妙");
        String ret = Util.boldifyHitString(src, keywords);
        assertEquals("<b><b>奇</b>妙</b><b>奇</b>天烈摩訶不思議<b>奇</b>想天外四捨五入出前迅速落書無用", ret);
    }

    @Test
    void boldifyHitStringTest003() {
        String src = "奇妙奇天烈<b>摩訶不思議</b>奇想天外四捨五入出前迅速落書無用";
        List<String> keywords = new ArrayList<>();
        keywords.add("<");
        keywords.add(">");
        String ret = Util.boldifyHitString(src, keywords);
        assertEquals("奇妙奇天烈<b>&lt;</b>b<b>&gt;</b>摩訶不思議<b>&lt;</b>/b<b>&gt;</b>奇想天外四捨五入出前迅速落書無用", ret);
    }

    @Test
    void boldifyHitStringTest004() {
        String src = "奇妙奇天烈摩訶不思議\r\n奇想天外四捨五入出前迅速落書無用";
        List<String> keywords = new ArrayList<>();
        keywords.add("<");
        keywords.add(">");
        String ret = Util.boldifyHitString(src, keywords);
        assertEquals("奇妙奇天烈摩訶不思議<br>奇想天外四捨五入出前迅速落書無用", ret);
    }

    @Test
    void getKeywordNeighborhoodTest001() {
        String src = "一二三四五六七八九〇一二三四五六七キーワード1八九〇一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇"
                + "一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇キーワード2一二三四五六七八九〇一二三四五六七八九〇";
        List<String> keywords = new ArrayList<>();
        keywords.add("キーワード1");
        keywords.add("キーワード2");

        String ret = Util.getKeywordNeighborhood(src, keywords);
        assertEquals(100, ret.length());
        assertTrue(ret.startsWith("八九〇一二三"));
    }

    @Test
    void getKeywordNeighborhoodTest002() {
        String src = "一二三四五六七八九〇一二三四五六七キーワード1八九〇";
        List<String> keywords = new ArrayList<>();
        keywords.add("キーワード1");

        String ret = Util.getKeywordNeighborhood(src, keywords);
        assertEquals(26, ret.length());
        assertTrue(ret.startsWith("一二三四五六七八九〇"));
    }

    @Test
    void getKeywordNeighborhoodTest003() {
        String src = "一二三四五六七八九〇一二三四五六七キーワード1八九〇一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇"
                + "一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇キーワード2一二三四五六七八九〇一二三四五六七八九〇";
        List<String> keywords = new ArrayList<>();
        keywords.add("存在しない");

        String ret = Util.getKeywordNeighborhood(src, keywords);
        assertEquals(100, ret.length());
        assertTrue(src.startsWith(ret));
    }

    @Test
    void getKeywordNeighborhoodTest004() {
        String src = "一二三四五六七八九〇一二三四五六七キーワード1八九〇一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇"
                + "一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇キーワード2一二三四五六七八九〇一二三四五六七八九〇";
        List<String> keywords = new ArrayList<>();
        keywords.add("キーワード2");
        keywords.add("存在しない");

        String ret = Util.getKeywordNeighborhood(src, keywords);
        assertEquals(36, ret.length());
        assertTrue(ret.startsWith("一二三四五六七八九〇キーワード2"));
    }
}