package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfw.ServiceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kmaebashi.blog.dbaccess.BlogPostDbAccess.getBlogPostSection;

public class ShowPostService {
    private ShowPostService() {}

    public static DocumentResult showPostByPostId(ServiceInvoker invoker, String blogId, int blogPostId) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            BlogPostDto blogPostDto
                    = BlogPostDbAccess.getBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
            if (blogDto == null || blogPostDto == null
                || blogPostDto.status != BlogPostStatus.PUBLISHED.intValue()) {
                throw new NotFoundException("ブログ記事がありません");
            }
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/post/post.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.renderBlogTitle(doc, blogDto, blogPostDto);
            ShowPostService.renderRecentPosts(context, doc, blogId);
            ShowPostService.renderBlogPost(context, doc, blogPostDto);

            return new DocumentResult(doc);
        });
    }

    private static void renderBlogTitle(Document doc, BlogDto blogDto, BlogPostDto blogPostDto) {
        Element headTitleElem = doc.getElementById("blog-head-title");
        headTitleElem.text("" + blogPostDto.title + " ―― " + blogDto.title);

        Element blogTitleElem = doc.getElementById("blog-title");
        blogTitleElem.text(blogDto.title);
    }

    private static void renderRecentPosts(ServiceContext context, Document doc, String blogId) {
        List<BlogPostDto> blogPostList = BlogPostDbAccess.getBlogPostList(context.getDbAccessInvoker(), blogId);
        Element ulElem = JsoupUtil.getFirst(doc.select("#recent-posts-area ul"));
        ulElem.empty();

        for (BlogPostDto blogPostDto : blogPostList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", "./" + blogPostDto.blogPostId);
            aElem.text(blogPostDto.title);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            ulElem.appendChild(liElem);
        }
    }

    private static DateTimeFormatter postedDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static void renderBlogPost(ServiceContext context, Document doc, BlogPostDto blogPostDto) {
        Element oneBlogPostElem = JsoupUtil.getFirst(doc.getElementsByClass("one-blog-post"));

        Element titleElem = JsoupUtil.getFirst(oneBlogPostElem.getElementsByClass("blog-post-title"));
        titleElem.text(blogPostDto.title);

        Element postDateElem = JsoupUtil.getFirst(oneBlogPostElem.getElementsByClass("post-date"));
        postDateElem.text(blogPostDto.postedDate.format(postedDateFormatter));

        Element postBodyElem = JsoupUtil.getFirst(oneBlogPostElem.getElementsByClass("blog-post-body"));
        postBodyElem.empty();
        List<BlogPostSectionDto> sectionList
                = BlogPostDbAccess.getBlogPostSection(context.getDbAccessInvoker(), blogPostDto.blogPostId);
        int sectionNumber = 1;
        for (BlogPostSectionDto sectionDto : sectionList) {
            appendParagraphs(doc, postBodyElem, sectionDto.body);
            List<PhotoDto> photoList
                    = BlogPostDbAccess.getBlogPostPhoto(context.getDbAccessInvoker(),
                                                        blogPostDto.blogPostId, sectionNumber);
            for (PhotoDto photoDto : photoList) {
                Element photoPElem = doc.createElement("p");
                photoPElem.attr("class", "photo");
                Element imgElem = doc.createElement("img");
                imgElem.attr("src", "../api/getimage/" + photoDto.blogPostId + "/" + photoDto.photoId);
                photoPElem.appendChild(imgElem);
                postBodyElem.appendChild(photoPElem);
                appendParagraphs(doc, postBodyElem, photoDto.caption);
            }
            sectionNumber++;
        }
    }
    private static void appendParagraphs(Document doc, Element parent, String text) {
        String[] lines = text.replace("\\r", "").split("\\n");
        for (String line : lines) {
            Element pElem = doc.createElement("p");
            pElem.text(line);
            parent.appendChild(pElem);
        }
    }
}
