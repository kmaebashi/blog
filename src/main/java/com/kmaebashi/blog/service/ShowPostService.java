package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSummaryDto;
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
            ShowPostService.renderProfile(doc, blogId, blogDto, true);
            ShowPostService.renderRecentPosts(context, doc, blogId, true);
            ShowPostService.renderRecentComments(context, doc, blogId, true);
            ShowPostService.renderBlogPost(context, doc, blogId, blogPostDto);
            ShowPostService.renderOlderNewerLink(context, doc, blogId, blogPostId);
            ShowPostService.renderCommentArea(context, doc, blogPostId, currentUserId);

            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showPostsByBlogId(ServiceInvoker invoker, String blogId, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            List<BlogPostSummaryDto> blogPostSummaryDtoList
                    = BlogPostDbAccess.getBlogPostSummaryList(context.getDbAccessInvoker(), blogId,
                                                              (page - 1) * Constants.NUM_OF_BLOG_POSTS_PER_PAGE,
                                                              Constants.NUM_OF_BLOG_POSTS_PER_PAGE);
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/date/post_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            replacePathForBlogTop(doc);
            ShowPostService.renderProfile(doc, blogId, blogDto, false);
            ShowPostService.renderRecentPosts(context, doc, blogId, false);
            ShowPostService.renderRecentComments(context, doc, blogId, false);
            ShowPostService.renderBlogPostList(context, doc, blogId, blogPostSummaryDtoList);
            ShowPostService.renderListOlderNewerLink(context, doc, blogId, page);

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

    private static void renderProfile(Document doc, String blogId, BlogProfileDto blogDto, boolean isPostPage) {
        Element profileAreaElem = doc.getElementById("profile-area");
        Element imageElem = profileAreaElem.getElementsByTag("img").first();
        imageElem.attr("src", getBlogRoot(blogId, isPostPage) + "api/getprofileimage");
        Element handleElem = profileAreaElem.getElementsByClass("profile-handlename").first();
        handleElem.text(blogDto.nickname);
        Element aboutMeElem = profileAreaElem.getElementsByClass("about-me").first();
        aboutMeElem.html(Util.escapeHtml2(blogDto.aboutMe));
    }

    private static void renderRecentPosts(ServiceContext context, Document doc, String blogId, boolean isPostPage) {
        List<BlogPostDto> blogPostList = BlogPostDbAccess.getBlogPostList(context.getDbAccessInvoker(), blogId, 0, 10);
        Element ulElem = JsoupUtil.getFirst(doc.select("#recent-posts-area ul"));
        ulElem.empty();

        for (BlogPostDto blogPostDto : blogPostList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", getBlogRoot(blogId, isPostPage) + "post/" + blogPostDto.blogPostId);
            aElem.text(blogPostDto.title);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            ulElem.appendChild(liElem);
        }
    }

    private static void renderRecentComments(ServiceContext context, Document doc, String blogId, boolean isPostPage) {
        List<CommentDto> commentList = BlogPostDbAccess.getCommentsByBlogId(context.getDbAccessInvoker(), blogId);

        Element ulElem = doc.select("#recent-comment-area ul").first();
        ulElem.empty();

        for (CommentDto commentDto : commentList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", getBlogRoot(blogId, isPostPage) + "post/" + commentDto.blogPostId
                        + "#comment" + commentDto.commentId);
            aElem.text(commentDto.blogPostTitle + " by " + commentDto.posterName);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            ulElem.appendChild(liElem);
        }
    }

    private static DateTimeFormatter postedDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static void renderBlogPost(ServiceContext context, Document doc, String blogId, BlogPostDto blogPostDto) {
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
            appendParagraph(doc, postBodyElem, sectionDto.body);
            List<PhotoDto> photoList
                    = BlogPostDbAccess.getBlogPostPhoto(context.getDbAccessInvoker(),
                    blogPostDto.blogPostId, sectionNumber);
            for (PhotoDto photoDto : photoList) {
                Element photoPElem = doc.createElement("p");
                photoPElem.attr("class", "photo");
                Element imgElem = doc.createElement("img");
                imgElem.attr("src", getBlogRoot(blogId, true) + "api/getimage/" + photoDto.blogPostId + "/" + photoDto.photoId);
                photoPElem.appendChild(imgElem);
                postBodyElem.appendChild(photoPElem);
                appendParagraph(doc, postBodyElem, photoDto.caption);
            }
            sectionNumber++;
        }
    }

    private static void renderBlogPostList(ServiceContext context, Document doc, String blogId,
                                           List<BlogPostSummaryDto> blogPostSummaryDtoList) {
        Element firstBlogPostElem = doc.getElementsByClass("one-blog-post").first();
        Element containerMainElem = doc.getElementById("blog-post-list-container");
        Element[] items = containerMainElem.getElementsByClass("one-blog-post").toArray(new Element[0]);
        for (int i = 0; i < items.length; i++) {
            items[i].remove();
        }
        for (BlogPostSummaryDto postDto : blogPostSummaryDtoList) {
            Element oneBlogPostElem = firstBlogPostElem.clone();
            Element titleElem = JsoupUtil.getFirst(oneBlogPostElem.getElementsByClass("blog-post-title"));
            titleElem.text(postDto.title);
            Element postDateElem = oneBlogPostElem.getElementsByClass("post-date").first();
            postDateElem.text(postDto.postedDate.format(postedDateFormatter));
            Element postBodyElem = oneBlogPostElem.getElementsByClass("one-blog-post-text").first();
            postBodyElem.empty();
            Element photoPElem = oneBlogPostElem.getElementsByClass("photo").first();
            if (postDto.photoId == null) {
                photoPElem.remove();
            } else {
                Element imgElem = photoPElem.getElementsByTag("img").first();
                imgElem.attr("src", getBlogRoot(blogId, false) + "api/getimage/" + postDto.blogPostId + "/" + postDto.photoId);
            }
            appendParagraph(doc, postBodyElem, Util.cutString(postDto.sectionText, Constants.POST_LIST_TEXT_LENGTH));
            containerMainElem.appendChild(oneBlogPostElem);
        }
    }

    private static void replacePathForBlogTop(Document doc) {
        Elements cssLinks = doc.select("link[rel=\"stylesheet\"]");
        for (Element elem : cssLinks) {
            String oldLink = elem.attr("href");
            String newLink = oldLink.replaceFirst("^\\.\\./\\.\\./", "");
            elem.attr("href", newLink);
        }
        Elements jsLinks = doc.getElementsByTag("script");
        for (Element elem : jsLinks) {
            String oldLink = elem.attr("src");
            String newLink = oldLink.replaceFirst("^\\.\\./\\.\\./", "");
            elem.attr("src", newLink);
        }
    }

    private static String getBlogRoot(String blogId, boolean isPostPage) {
        return (isPostPage ? "../" : (blogId + "/"));
    }

    private static void appendParagraph(Document doc, Element parent, String text) {
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

    private static void renderListOlderNewerLink(ServiceContext context, Document doc, String blogId, int page)
    {
        int postCount = BlogPostDbAccess.getBlogPostCountByBlogId(context.getDbAccessInvoker(), blogId);

        Element newerOlderDiv = doc.getElementsByClass("newer-older-area").first();
        Element leftNaviElem = newerOlderDiv.getElementsByClass("left-navi").first();
        Element leftAnchor = leftNaviElem.getElementsByTag("a").first();
        if (page == 1) {
            leftAnchor.remove();
            leftNaviElem.text("　―　");
        } else {
            leftAnchor.attr("href", "?page=" + (page - 1));
        }
        Element rightNaviElem = newerOlderDiv.getElementsByClass("right-navi").first();
        Element rightAnchor = rightNaviElem.getElementsByTag("a").first();
        if (page >= (postCount + Constants.NUM_OF_BLOG_POSTS_PER_PAGE - 1) / Constants.NUM_OF_BLOG_POSTS_PER_PAGE) {
            rightAnchor.remove();
            rightNaviElem.text("　―　");
        } else {
            rightAnchor.attr("href", "?page=" + (page + 1));
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
