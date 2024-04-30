package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;
import java.util.List;

public class BlogListService {
    private BlogListService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String userId) {
        return invoker.invoke((context) -> {
            List<BlogDto> blogDtoList = BlogDbAccess.getBlogsByUser(context.getDbAccessInvoker(), userId);

            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blog_list.html");
            Document doc = BlogListService.render(htmlPath, blogDtoList);
            return new DocumentResult(doc);
        });
    }

    private static Document render(Path htmlPath, List<BlogDto> blogDtoList) throws Exception {
        Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

        Element blogListElem = doc.getElementById("blog-list");
        Element[] items = blogListElem.getElementsByTag("li").toArray(new Element[0]);
        for (int i = 0; i < items.length; i++) {
            items[i].remove();
        }
        for (BlogDto blogDto : blogDtoList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", blogDto.blogId + "/admin");
            aElem.text(blogDto.title);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            blogListElem.appendChild(liElem);
        }

        return doc;
    }
}
