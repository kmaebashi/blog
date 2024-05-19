package com.kmaebashi.blog.util;

import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.blog.common.SessionKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CsrfUtil {
    public static void addCsrfToken(DocumentResult dr, String token) {
        Document doc = dr.getDocument();
        Element metaElem = doc.createElement("meta");
        metaElem.attr("name", SessionKey.CSRF_TOKEN);
        metaElem.attr("content", token);
        doc.head().appendChild(metaElem);
    }

    public static boolean checkCsrfToken(HttpServletRequest request, boolean needLogin) {
        Log.info("checkCsrfToken start. needToken.." + needLogin);
        HttpSession session = request.getSession(false);
        if (session == null) {
            Log.info("checkCsrfToken session == null");
            return !needLogin;
        }

        String sessionToken = (String)session.getAttribute(SessionKey.CSRF_TOKEN);
        Log.info("sessionToken.." + sessionToken);
        if (sessionToken == null) {
            return !needLogin;
        }
        String headerToken = request.getHeader("X-Csrf-Token");
        Log.info("headerToken.." + headerToken);
        if (headerToken != null && headerToken.equals(sessionToken)) {
            Log.info("checkCsrfToken return true.");
            return true;
        }
        Log.info("checkCsrfToken return false.");
        return false;
    }
}
