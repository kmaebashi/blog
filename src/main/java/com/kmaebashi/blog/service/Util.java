package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.nctfw.DocumentResult;
import org.jsoup.nodes.Element;
import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.util.*;

import org.jsoup.nodes.Document;

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

    static String escapeHtml(String src) {
        return src.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }

    static String escapeHtml2(String src) {
        String escaped = Util.escapeHtml(src);
        String linkCreated = Util.createLinkAnchor(escaped);
        return nl2Br(linkCreated);
    }

    static String escapeHtmlChar(char src) {
        return src == '&' ? "&amp;"
                : src == '"' ? "&quot;"
                : src == '<' ? "&lt;"
                : src == '>' ? "&gt;"
                : src == '\'' ? "&#39;"
                : Character.toString(src);
    }

    static String nl2Br(String str) {
        str = str.replaceAll("\r\n", "<br>");
        str = str.replaceAll("\n", "<br>");

        return str;
    }

    static String cutString(String src, int len) {
        if (src.codePointCount(0, src.length()) <= len) {
            return src;
        }
        int charIdx = src.offsetByCodePoints(0, len);
        return src.substring(0, charIdx) + "…";
    }

    public static String createLinkAnchor(String src) {
        return  src.replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+\\@]+",
                "<a href=\"$0\">$0</a>");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    // pageCountBuf[0]..ページ数
    static int calcPagenationStart(int itemCount, int page, int numOfPerPage, int[] pageCountBuf) {
        int pageCount = (itemCount + numOfPerPage - 1) / numOfPerPage;
        int adjustedPage = page;
        if (page < 1) {
            adjustedPage = 1;
        } else if (page > pageCount) {
            adjustedPage = pageCount;
        }
        int startIdx;
        if (adjustedPage <= Constants.NUM_OF_PAGENATION / 2) {
            startIdx = 1;
        } else if (adjustedPage > (pageCount - Constants.NUM_OF_PAGENATION / 2)) {
            startIdx = pageCount - Constants.NUM_OF_PAGENATION + 1;
        } else {
            startIdx = adjustedPage - (Constants.NUM_OF_PAGENATION / 2) + 1;
        }
        pageCountBuf[0] = pageCount;
        return startIdx;
    }

    public static List<String> splitQueryKeywords(String src) {
        List<String> keywords = Arrays.asList(src.replaceAll("^[ 　]+|[ 　]+$", "")
                                              .split("[ 　]+"));
        return keywords;
    }

    public static String boldifyHitString(String src, List<String> keywords) {
        final String BOLD_START = "[錦禍斑慟冥翠骸雹燭鵬]";
        final String BOLD_END = "[/錦禍斑慟冥翠骸雹燭鵬]";

        List<String> sortedKeywords = new ArrayList();
        sortedKeywords.addAll(keywords);
        sortedKeywords.sort(Comparator.comparingInt(String::length).reversed());

        String boldify = src;
        for (String keyword : sortedKeywords) {
            boldify = boldify.replace(keyword, BOLD_START + keyword + BOLD_END);
        }
        String htmlEscaped = escapeHtml(boldify);
        String boldTagged = htmlEscaped.replace(BOLD_START, "<b>").replace(BOLD_END, "</b>");

        return nl2Br(boldTagged);
    }

    public static String getKeywordNeighborhood(String src, List<String> keywords) {
        int firstKeywordIndex = Integer.MAX_VALUE;
        for (String keyword : keywords) {
            int idx = src.indexOf(keyword);
            if (idx < firstKeywordIndex && idx >= 0) {
                firstKeywordIndex = idx;
            }
        }
        if (firstKeywordIndex == Integer.MAX_VALUE) {
            return src.substring(0, Math.min(Constants.SEARCH_CONTENT_LENGTH, src.length()));
        }
        int startIndex;
        if (src.length() < Constants.SEARCH_CONTENT_LENGTH) {
            startIndex = 0;
        } else {
            startIndex = Math.max(firstKeywordIndex - Constants.BEFORE_SEARCH_KEYWORD_LENGTH, 0);
        }
        return src.substring(startIndex, Math.min(startIndex + Constants.SEARCH_CONTENT_LENGTH, src.length()));
    }
}
