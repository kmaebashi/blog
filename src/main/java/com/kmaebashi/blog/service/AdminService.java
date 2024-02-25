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

public class AdminService {
    private AdminService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String blogId, Integer blogPostId) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            List<BlogPostDto> blogPostList = BlogPostDbAccess.getBlogPostForAdmin(context.getDbAccessInvoker(), blogId);

            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blog_admin.html");
            Document doc = AdminService.renderBlog(htmlPath, blogId, blogDto, blogPostList);
            if (blogPostId == null) {
                AdminService.renderNewPost(doc);
            }
            return new DocumentResult(doc);
        });
    }

    private static Document renderBlog(Path htmlPath, String blogId, BlogDto blogDto, List<BlogPostDto> blogPostList)
            throws Exception {
        Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

        Element blogHeadTitleElem = doc.getElementById("blog-head-title");
        blogHeadTitleElem.text(blogDto.title + " 管理画面");
        Element blogTitleElem = doc.getElementById("blog-title");
        blogTitleElem.text(blogDto.title + " 管理画面");

        Element sidebarRecentArticles = doc.getElementById("sidebar-recent-articles");
        Element[] items = sidebarRecentArticles.getElementsByTag("li").toArray(new Element[0]);
        for (int i = 0; i < items.length; i++) {
            items[i].remove();
        }
        for (BlogPostDto blogPostDto : blogPostList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", "/blog/" + blogId + "/admin/" + blogPostDto.blogPostId);
            aElem.text(blogPostDto.title);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            sidebarRecentArticles.appendChild(liElem);
        }
        Element sectionContainerElem = doc.getElementById("section-container");
        Element[] sections = sectionContainerElem.getElementsByClass("section-box").toArray(new Element[0]);
        for (int i = 0; i < sections.length; i++) {
            sections[i].remove();
        }

        return doc;
    }

    private static void renderNewPost(Document doc) throws Exception {
        Element templateSectionElem = doc.getElementById("hidden-section-box");
        Element sectionContainerElem = doc.getElementById("section-container");
        Element newSectionElem = templateSectionElem.clone();
        newSectionElem.removeAttr("style");
        newSectionElem.attr("id", "section-box1");
        Element fileInputElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("image-file-input"));
        fileInputElem.attr("data-section", "1");
        sectionContainerElem.appendChild(newSectionElem);
        Element blogPostTitleElem = doc.getElementById("blog-post-title");
        blogPostTitleElem.attr("value", "");
    }
}
