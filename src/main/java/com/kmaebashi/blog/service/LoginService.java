package com.kmaebashi.blog.service;

import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.file.Path;

public class LoginService {
    private LoginService() {}

    public static DocumentResult showPage(ServiceInvoker invoker) {
        return invoker.invoke((context) -> {
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("login.html");
            Document doc = LoginService.render(htmlPath);
            return new DocumentResult(doc);
        });
    }

    static Document render(Path htmlPath)
            throws Exception {
        Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
        return doc;
    }

}
