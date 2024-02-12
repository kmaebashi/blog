package com.kmaebashi.blog.service;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class JsoupUtil {
    private JsoupUtil() {}

    static Element getFirst(Elements elements) {
        return elements.toArray(new Element[0])[0];
    }
}
