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
                "エスケープ<p>&;";
        String converted = converter.convert(src);
        String footnote = converter.getFootnoteStr();
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

}