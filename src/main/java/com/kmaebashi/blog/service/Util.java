package com.kmaebashi.blog.service;

import com.kmaebashi.nctfw.DocumentResult;
import org.jsoup.nodes.Element;
import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.util.Base64;
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
        return nl2Br(escaped);
    }

    static String nl2Br(String str) {
        str = str.replaceAll("\r\n", "<br>");
        str = str.replaceAll("\n", "<br>");

        return str;
    }
}
