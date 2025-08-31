package com.kmaebashi.blog.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkupConverterTest {

    @Test
    void convertTest001() {
        MarkupConverter converter = new MarkupConverter(false);

        String src = "*はじめに\n" +
                "はじめの説明をここに書く\n" +
                "*1章\n" +
                "**なんとかかんとか\n" +
                "なんとか改行\n" +
                "なんとか((なんとかの脚注です))。\n" +
                "***うんとかすんとか\n" +
                ">>\n" +
                "引用\n" +
                "引用の中の２行目\n" +
                "<<\n" +
                "+箇条書き1\n" +
                "1の続き" +
                "+箇条書き2\n" +
                "-箇条書き・\n" +
                "-箇条書き・\n" +
                "エスケープ<p>&;\n" +
                ">|\n" +
                "#include <stdio.h>\n" +
                "|<\n" +
                "https://kmaebashi.com\n" +
                "リンクは[https://kmaebashi.com:title=こちら]です。\n" +
                "リンクは[http://kmaebashi.com:title=こちら]です。\n" +
                "ここは[b]太字[/b]";
        String converted = converter.convert(src);
        String footnote = converter.getFootnoteStr();

        String expected = "<h2>はじめに</h2>\r\n" +
                "<p>はじめの説明をここに書く\r\n" +
                "</p>\r\n" +
                "<h2>1章</h2>\r\n" +
                "<h3>なんとかかんとか</h3>\r\n" +
                "<p>なんとか改行\r\n" +
                "<br>\r\n" +
                "なんとか<a href=\"#footnote1\">※1</a></sup>。\r\n" +
                "</p>\r\n" +
                "<h4>うんとかすんとか</h4>\r\n" +
                "<blockquote>\r\n" +
                "<p>引用\r\n" +
                "<br>\r\n" +
                "引用の中の２行目\r\n" +
                "</p>\r\n" +
                "</blockquote>\r\n" +
                "<ol>\r\n" +
                "<li>箇条書き1\r\n" +
                "</ol>\r\n" +
                "<p>1の続き+箇条書き2\r\n" +
                "</p>\r\n" +
                "<ul>\r\n" +
                "<li>箇条書き・\r\n" +
                "<li>箇条書き・\r\n" +
                "</ul>\r\n" +
                "<p>エスケープ&lt;p&gt;&amp;;\r\n" +
                "</p>\r\n" +
                "<pre>\r\n" +
                "<p>#include &lt;stdio.h&gt;\r\n" +
                "</p>\r\n" +
                "</pre>\r\n";
        String expectedFootnote = "\r\n" +
                "<hr>\r\n" +
                "<ul class=\"footnote\">\r\n" +
                "<li><a name=\"footnote1\">※1</a>なんとかの脚注です\r\n" +
                "</ul>\r\n";
        assertEquals(expected, converted);
        assertEquals(expectedFootnote, footnote);
    }

    @Test
    void convertTest001_2() {
        MarkupConverter converter = new MarkupConverter(true);

        String src = "*はじめに\n" +
                "はじめの説明をここに書く\n" +
                "*1章\n" +
                "**なんとかかんとか\n" +
                "なんとか改行\n" +
                "なんとか((なんとかの脚注です))。\n" +
                "***うんとかすんとか\n" +
                ">>\n" +
                "引用\n" +
                "引用の中の２行目\n" +
                "<<\n" +
                "+箇条書き1\n" +
                "1の続き" +
                "+箇条書き2\n" +
                "-箇条書き・\n" +
                "-箇条書き・\n" +
                "エスケープ<p>&;";
        String converted = converter.convert(src);
        String footnote = converter.getFootnoteStr();
    }

    @Test
    void convertTest002() {
        MarkupConverter converter = new MarkupConverter(false);

        String src =
                  "+OL1-1\n"
                + "+OL1-2\n"
                + "-UL1-1\n"
                + "-UL1-2\n"
                + "++OL2-1\n"
                + "++OL2-2\n"
                + "-UL1-3\n"
                + "中断\n"
                + "+OL1-2\n"
                + "++OL2-1\n"
                + "++OL2-2\n"
                + "+++OL3-1\n"
                + "+++OL3-2\n"
                + "中断\n"
                + "+OL1-2\n"
                + "--UL2-1\n"
                + "--UL2-2\n"
                + "---UL3-1\n"
                + "---UL3-2\n"
                ;
        String converted = converter.convert(src);
        assertEquals("<ol>\r\n" +
                "<li>OL1-1\r\n" +
                "<li>OL1-2\r\n" +
                "</ol>\r\n" +
                "<ul>\r\n" +
                "<li>UL1-1\r\n" +
                "<li>UL1-2\r\n" +
                "<ol>\r\n" +
                "<li>OL2-1\r\n" +
                "<li>OL2-2\r\n" +
                "</ol>\r\n" +
                "<li>UL1-3\r\n" +
                "</ul>\r\n" +
                "<p>中断\r\n" +
                "</p>\r\n" +
                "<ol>\r\n" +
                "<li>OL1-2\r\n" +
                "<ol>\r\n" +
                "<li>OL2-1\r\n" +
                "<li>OL2-2\r\n" +
                "<ol>\r\n" +
                "<li>OL3-1\r\n" +
                "<li>OL3-2\r\n" +
                "</ol>\r\n" +
                "</ol>\r\n" +
                "</ol>\r\n" +
                "<p>中断\r\n" +
                "</p>\r\n" +
                "<ol>\r\n" +
                "<li>OL1-2\r\n" +
                "<ul>\r\n" +
                "<li>UL2-1\r\n" +
                "<li>UL2-2\r\n" +
                "<ul>\r\n" +
                "<li>UL3-1\r\n" +
                "<li>UL3-2\r\n" +
                "</ul>\r\n" +
                "</ul>\r\n" +
                "</ol>\r\n", converted);
    }

    @Test
    void convertTest003() {
        MarkupConverter converter = new MarkupConverter(false);

        String src = "*<はじめに>\n" +
                "本文&本文\n" +
                "*1章\"なんとか\"\n" +
                "**なんとかかんとか'かんとか'\n" +
                "***うんとかすんとか\"すんとか\"\n" +
                ">>\n" +
                "<引用>\n" +
                "<<\n" +
                "+箇条<書き>1\n" +
                "1の'続き'" +
                "エスケープ<p>&;";
        String converted = converter.convert(src);
        assertEquals("<h2>&lt;はじめに&gt;</h2>\r\n" +
                "<p>本文&amp;本文\r\n" +
                "</p>\r\n" +
                "<h2>1章&quot;なんとか&quot;</h2>\r\n" +
                "<h3>なんとかかんとか&#39;かんとか&#39;</h3>\r\n" +
                "<h4>うんとかすんとか&quot;すんとか&quot;</h4>\r\n" +
                "<blockquote>\r\n" +
                "<p>&lt;引用&gt;\r\n" +
                "</p>\r\n" +
                "</blockquote>\r\n" +
                "<ol>\r\n" +
                "<li>箇条&lt;書き&gt;1\r\n" +
                "</ol>\r\n" +
                "<p>1の&#39;続き&#39;エスケープ&lt;p&gt;&amp;;", converted);
    }

    @Test
    void convertTest004() {
        MarkupConverter converter = new MarkupConverter(false);

        String src = ">>あい<<https://kmaebashi.com";
        String converted = converter.convert(src);
    }


    @Test
    void getLinkUrlTest001() {
        String src = "あいうえおhttps://kmaebashi.com?param=123なんとか";
        int[] linkLenBuf = new int[1];
        String ret = MarkupConverter.getLinkUrl(src, 5, linkLenBuf);
        assertEquals("https://kmaebashi.com?param=123なんとか", ret);
        assertEquals(35, linkLenBuf[0]);
    }

    @Test
    void getLinkUrlTest002() {
        String src = "あいうえおhttp://kmaebashi.com?param=123\r\nなんとか";
        int[] linkLenBuf = new int[1];
        String ret = MarkupConverter.getLinkUrl(src, 5, linkLenBuf);
        assertEquals("http://kmaebashi.com?param=123", ret);
        assertEquals(31, linkLenBuf[0]);
    }

    @Test
    void getLinkUrlTest003() {
        String src = "あいうえおhttps://kmaebashi.com?param=123 なんとか";
        int[] linkLenBuf = new int[1];
        String ret = MarkupConverter.getLinkUrl(src, 5, linkLenBuf);
        assertEquals("https://kmaebashi.com?param=123", ret);
        assertEquals(31, linkLenBuf[0]);
    }

    @Test
    void getLinkTitleTest001() {
        String src = "１２３[https://kmaebashi.com:title=K.Maebashi's home page]あ";
        int i = 25;
        int[] linkLenBuf = new int[1];
        String title = MarkupConverter.getLinkTitle(src, 25, linkLenBuf);
        i += linkLenBuf[0];
        assertEquals("K.Maebashi's home page", title);
        assertEquals('あ', src.charAt(i));
    }

    @Test
    void getLinkTitleTest002() {
        String src = "１２３[https://kmaebashi.com:xxx=K.Maebashi's home page]あ";
        int i = 25;
        int[] linkLenBuf = new int[1];
        String title = MarkupConverter.getLinkTitle(src, 25, linkLenBuf);
        i += linkLenBuf[0];
        assertNull(title);
        assertEquals('あ', src.charAt(i));
    }

}