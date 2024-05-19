package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.blog.dto.BlogProfileDto;
import com.kmaebashi.blog.dto.CommentDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.ProfileDto;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfw.ServiceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kmaebashi.blog.dbaccess.BlogPostDbAccess.getBlogPostSection;

public class ShowPostService {
    private ShowPostService() {
    }

    public static DocumentResult showPostByPostId(ServiceInvoker invoker, String blogId, int blogPostId,
                                                  String currentUserId) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            BlogPostDto blogPostDto
                    = BlogPostDbAccess.getBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
            if (blogDto == null || blogPostDto == null
                    || blogPostDto.status != BlogPostStatus.PUBLISHED.intValue()) {
                throw new NotFoundException("ブログ記事がありません");
            }
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/post/post.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.renderBlogTitle(doc, blogDto, blogPostDto);
            ShowPostService.renderProfile(doc, blogDto);
            ShowPostService.renderRecentPosts(context, doc, blogId);
            ShowPostService.renderRecentComments(context, doc, blogId);
            ShowPostService.renderBlogPost(context, doc, blogPostDto);
            ShowPostService.renderOlderNewerLink(context, doc, blogId, blogPostId);
            ShowPostService.renderCommentArea(context, doc, blogPostId, currentUserId);

            return new DocumentResult(doc);
        });
    }

    private static void renderBlogTitle(Document doc, BlogProfileDto blogDto, BlogPostDto blogPostDto) {
        Element headTitleElem = doc.getElementById("blog-head-title");
        headTitleElem.text("" + blogPostDto.title + " ―― " + blogDto.title);

        Element blogTitleElem = doc.getElementById("blog-title");
        blogTitleElem.text(blogDto.title);

        Element blogDescriptionAreaElem = doc.getElementById("blog-description-area");
        Element descriptionElem = blogDescriptionAreaElem.getElementsByClass("description").first();
        descriptionElem.html(Util.escapeHtml2(blogDto.description));
    }

    private static void renderProfile(Document doc, BlogProfileDto blogDto) {
        Element profileAreaElem = doc.getElementById("profile-area");
        Element imageElem = profileAreaElem.getElementsByTag("img").first();
        imageElem.attr("src", "../api/getprofileimage");
        Element handleElem = profileAreaElem.getElementsByClass("profile-handlename").first();
        handleElem.text(blogDto.nickname);
        Element aboutMeElem = profileAreaElem.getElementsByClass("about-me").first();
        aboutMeElem.html(Util.escapeHtml2(blogDto.aboutMe));
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

    private static void renderRecentComments(ServiceContext context, Document doc, String blogId) {
        List<CommentDto> commentList = BlogPostDbAccess.getCommentsByBlogId(context.getDbAccessInvoker(), blogId);

        Element ulElem = doc.select("#recent-comment-area ul").first();
        ulElem.empty();

        for (CommentDto commentDto : commentList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", "./" + commentDto.blogPostId + "#comment" + commentDto.commentId);
            aElem.text(commentDto.blogPostTitle + " by " + commentDto.posterName);
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

    private static void renderOlderNewerLink(ServiceContext context, Document doc, String blogId, int blogPostId)
    {
        BlogPostDto olderPostDto
                = BlogPostDbAccess.getOlderBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
        BlogPostDto newerPostDto
                = BlogPostDbAccess.getNewerBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
        Element[] divs = doc.select("div.newer-older-area div.content").toArray(new Element[0]);

        setOlderNewerLink(doc, divs[0], newerPostDto);
        setOlderNewerLink(doc, divs[1], olderPostDto);
    }

    private static void setOlderNewerLink(Document doc, Element div, BlogPostDto blogPostDto) {
        div.empty();
        if (blogPostDto == null) {
            div.text("　―　");
        } else {
            Element aElem = doc.createElement("a");
            aElem.attr("href", "./" + blogPostDto.blogPostId);
            aElem.text(blogPostDto.title);
            div.appendChild(aElem);
        }
    }

    private static DateTimeFormatter commentPostedDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static void renderCommentArea(ServiceContext context, Document doc, int blogPostId, String currentUserId) {
        List<CommentDto> commentList
                = BlogPostDbAccess.getCommentsByBlogPostId(context.getDbAccessInvoker(), blogPostId);
        Element commentContentsElem = doc.getElementById("comment-contents");
        Element oneCommentElem = doc.getElementsByClass("one-comment").first();
        commentContentsElem.empty();
        for (CommentDto commentDto : commentList) {
            Element newCommentElem = oneCommentElem.clone();
            Element messageElem = newCommentElem.getElementsByClass("message").first();
            messageElem.html(Util.escapeHtml2(commentDto.message));
            Element commentedByElem = newCommentElem.getElementsByClass("commented-by").first();
            commentedByElem.text("Posted by " + commentDto.posterName
                                 + " " + commentDto.createdAt.format(commentPostedDateFormatter));
            newCommentElem.attr("id", "comment" + commentDto.commentId);
            commentContentsElem.appendChild(newCommentElem);
        }
        if (currentUserId != null) {
            ProfileDto audienceProfile = ProfileDbAccess.getProfileByUserId(context.getDbAccessInvoker(), currentUserId);
            Element posterElem = doc.getElementById("comment-poster-input");
            posterElem.attr("value", audienceProfile.nickname);
            posterElem.attr("disabled", true);
        }
    }
}
