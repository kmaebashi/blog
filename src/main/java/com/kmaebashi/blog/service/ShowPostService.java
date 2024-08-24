package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.blog.controller.data.BlogPostCountEachDay;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogPostCountEachDayDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSummaryDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.blog.dto.BlogProfileDto;
import com.kmaebashi.blog.dto.CommentDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.ProfileDto;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfw.ServiceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowPostService {
    private ShowPostService() {
    }

    enum PathLevel {
        TOP,
        DATE,
        POST
    }

    public static DocumentResult showPostByPostId(ServiceInvoker invoker, String blogId, int blogPostId,
                                                  String currentUserId, String url) {
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
            ShowPostService.setProperties(doc, PathLevel.POST, blogPostDto.postedDate.toLocalDate());
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.POST);
            ShowPostService.renderHeadTitlePost(doc, blogDto, blogPostDto);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.POST);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.POST);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.POST);
            ShowPostService.renderBlogPost(context, doc, blogId, blogPostDto, url);
            ShowPostService.renderOlderNewerLink(context, doc, blogId, blogPostId);
            ShowPostService.renderCommentArea(context, doc, blogPostId, currentUserId);
            ShowPostService.renderForFacebook(doc, url);

            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showPostsByBlogId(ServiceInvoker invoker, String blogId, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            List<BlogPostSummaryDto> blogPostSummaryDtoList
                    = BlogPostDbAccess.getBlogPostSummaryList(context.getDbAccessInvoker(), blogId,
                                                              null, null,
                                                              (page - 1) * Constants.NUM_OF_BLOG_POSTS_PER_PAGE,
                                                              Constants.NUM_OF_BLOG_POSTS_PER_PAGE);
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/date/post_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.setProperties(doc, PathLevel.TOP, LocalDate.now());
            replacePathForBlogList(doc, true);
            ShowPostService.renderHeadTitleTop(doc, blogDto, page);
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.TOP);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.TOP);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.TOP);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.TOP);
            ShowPostService.renderBlogPostList(context, doc, blogId, blogPostSummaryDtoList, PathLevel.TOP);
            ShowPostService.renderListOlderNewerLink(context, doc, blogId, null, null, page);

            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showPostsDateRange(ServiceInvoker invoker, String blogId,
                                                    LocalDate startDate, LocalDate endDate, String dispDateStr, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            List<BlogPostSummaryDto> blogPostSummaryDtoList
                    = BlogPostDbAccess.getBlogPostSummaryList(context.getDbAccessInvoker(), blogId,
                    startDate, endDate,
                    (page - 1) * Constants.NUM_OF_BLOG_POSTS_PER_PAGE,
                    Constants.NUM_OF_BLOG_POSTS_PER_PAGE);
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/date/post_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            replacePathForBlogList(doc, false);
            ShowPostService.setProperties(doc, PathLevel.DATE, startDate);
            ShowPostService.renderHeadTitleList(doc, blogDto, dispDateStr, page);
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.DATE);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.DATE);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderBlogPostList(context, doc, blogId, blogPostSummaryDtoList, PathLevel.DATE);
            ShowPostService.renderListOlderNewerLink(context, doc, blogId, startDate, endDate, page);

            return new DocumentResult(doc);
        });
    }

    public static JsonResult getPostCountEachDay(ServiceInvoker invoker, String blogId, LocalDate month) {
        return invoker.invoke((context) -> {
            List<BlogPostCountEachDayDto> dtoList
                    = BlogPostDbAccess.getBlogPostCountByMonth(context.getDbAccessInvoker(), blogId, month);

            List<BlogPostCountEachDay> countList = new ArrayList<>();
            for (BlogPostCountEachDayDto dto : dtoList) {
                countList.add(new BlogPostCountEachDay(dto.postedDate.getDayOfMonth(), dto.numOfPosts));
            }
            String json = ClassMapper.toJson(countList);

            return new JsonResult(json);
        });
    }

    private static DateTimeFormatter yyyyMMddFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static void setProperties(Document doc, PathLevel pageType, LocalDate date) {
        Element propDivElem = doc.getElementById("properties");
        Map<String,String> dataset = propDivElem.dataset();

        dataset.put("page-type", pageType.toString());
        dataset.put("posted-date", yyyyMMddFormatter.format(date));
    }

    private static void renderHeadTitlePost(Document doc, BlogProfileDto blogDto, BlogPostDto blogPostDto) {
        Element headTitleElem = doc.getElementById("blog-head-title");
        headTitleElem.text("" + blogPostDto.title + " ―― " + blogDto.title);
    }

    private static void renderHeadTitleTop(Document doc, BlogProfileDto blogDto, int page) {
        Element headTitleElem = doc.getElementById("blog-head-title");

        headTitleElem.text(blogDto.title + ((page > 1) ? (" page " + page) : ""));
    }


    private static void renderHeadTitleList(Document doc, BlogProfileDto blogDto, String dispDateStr, int page) {
        Element headTitleElem = doc.getElementById("blog-head-title");
        headTitleElem.text(blogDto.title + " " + dispDateStr + " page " + page);
    }


    private static void renderBlogTitle(Document doc, BlogProfileDto blogDto, PathLevel pathLevel) {
        Element blogTitleElem = doc.getElementById("blog-title");
        String topUrl;
        if (pathLevel == PathLevel.TOP) {
            topUrl = "./" + blogDto.blogId;
        } else if (pathLevel == PathLevel.DATE) {
            topUrl = "../" + blogDto.blogId;
        } else {
            assert pathLevel == PathLevel.POST;
            topUrl = "../../" + blogDto.blogId;
        }
        blogTitleElem.attr("href", topUrl);
        blogTitleElem.text(blogDto.title);

        Element blogDescriptionAreaElem = doc.getElementById("blog-description-area");
        Element descriptionElem = blogDescriptionAreaElem.getElementsByClass("description").first();
        descriptionElem.html(Util.escapeHtml2(blogDto.description));
    }

    private static void renderProfile(Document doc, String blogId, BlogProfileDto blogDto, PathLevel pathLevel) {
        Element profileAreaElem = doc.getElementById("profile-area");
        Element imageElem = profileAreaElem.getElementsByTag("img").first();
        imageElem.attr("src", getBlogRoot(blogId, pathLevel) + "api/getprofileimage");
        Element handleElem = profileAreaElem.getElementsByClass("profile-handlename").first();
        handleElem.text(blogDto.nickname);
        Element aboutMeElem = profileAreaElem.getElementsByClass("about-me").first();
        aboutMeElem.html(Util.escapeHtml2(blogDto.aboutMe));
    }

    private static void renderRecentPosts(ServiceContext context, Document doc, String blogId, PathLevel pathLevel) {
        Element calendarElem = doc.getElementById("calendar-area");
        calendarElem.empty();

        List<BlogPostDto> blogPostList = BlogPostDbAccess.getBlogPostList(context.getDbAccessInvoker(), blogId, 0, 10);
        Element ulElem = doc.select("#recent-posts-area ul").first();
        ulElem.empty();

        for (BlogPostDto blogPostDto : blogPostList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", getBlogRoot(blogId, pathLevel) + "post/" + blogPostDto.blogPostId);
            aElem.text(blogPostDto.title);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            ulElem.appendChild(liElem);
        }
    }

    private static void renderRecentComments(ServiceContext context, Document doc, String blogId, PathLevel pathLevel) {
        List<CommentDto> commentList = BlogPostDbAccess.getCommentsByBlogId(context.getDbAccessInvoker(), blogId);

        Element ulElem = doc.select("#recent-comment-area ul").first();
        ulElem.empty();

        for (CommentDto commentDto : commentList) {
            Element aElem = doc.createElement("a");
            aElem.attr("href", getBlogRoot(blogId, pathLevel) + "post/" + commentDto.blogPostId
                        + "#comment" + commentDto.commentId);
            aElem.text(commentDto.blogPostTitle + " by " + commentDto.posterName);
            Element liElem = doc.createElement("li");
            liElem.appendChild(aElem);
            ulElem.appendChild(liElem);
        }
    }

    private static DateTimeFormatter postedDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static void renderBlogPost(ServiceContext context, Document doc, String blogId, BlogPostDto blogPostDto, String url) {
        ShowPostService.setMetaProperty(doc, "og:url", url);
        ShowPostService.setMetaProperty(doc, "og:title", blogPostDto.title);

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
        String firstPhotoPath = null;
        for (BlogPostSectionDto sectionDto : sectionList) {
            appendParagraph(doc, postBodyElem, sectionDto.body);
            List<PhotoDto> photoList
                    = BlogPostDbAccess.getBlogPostPhoto(context.getDbAccessInvoker(),
                    blogPostDto.blogPostId, sectionNumber);
            for (PhotoDto photoDto : photoList) {
                Element photoPElem = doc.createElement("p");
                photoPElem.attr("class", "photo");
                Element imgElem = doc.createElement("img");
                imgElem.attr("src",  getBlogRoot(blogId, PathLevel.POST) + "api/getimage/"
                        + photoDto.blogPostId + "/" + photoDto.photoId);
                if (firstPhotoPath == null) {
                    firstPhotoPath = "api/getimage/" + photoDto.blogPostId + "/" + photoDto.photoId;
                }
                photoPElem.appendChild(imgElem);
                postBodyElem.appendChild(photoPElem);
                appendParagraph(doc, postBodyElem, photoDto.caption);
            }
            sectionNumber++;
        }
        if (firstPhotoPath != null) {
            String photoUrl = url.replaceFirst("post/\\d+$", firstPhotoPath);
            ShowPostService.setMetaProperty(doc, "og:image", photoUrl);
        }
    }

    private static void renderBlogPostList(ServiceContext context, Document doc, String blogId,
                                           List<BlogPostSummaryDto> blogPostSummaryDtoList, PathLevel pathLevel) {
        Element firstBlogPostElem = doc.getElementsByClass("one-blog-post").first();
        Element containerMainElem = doc.getElementById("blog-post-list-container");
        Element[] items = containerMainElem.getElementsByClass("one-blog-post").toArray(new Element[0]);
        for (int i = 0; i < items.length; i++) {
            items[i].remove();
        }
        for (BlogPostSummaryDto postDto : blogPostSummaryDtoList) {
            Element oneBlogPostElem = firstBlogPostElem.clone();
            Element titleElem = JsoupUtil.getFirst(oneBlogPostElem.getElementsByClass("blog-post-title"));
            Element titleAnchor = titleElem.getElementsByTag("a").first();
            titleAnchor.text(postDto.title);
            String postPath = (pathLevel == PathLevel.TOP ? ("./" + blogId + "/post") : "./post") + "/" + postDto.blogPostId;
            titleAnchor.attr("href", postPath);
            Element postDateElem = oneBlogPostElem.getElementsByClass("post-date").first();
            postDateElem.text(postDto.postedDate.format(postedDateFormatter));
            Element postBodyElem = oneBlogPostElem.getElementsByClass("one-blog-post-text").first();
            postBodyElem.empty();
            Element photoPElem = oneBlogPostElem.getElementsByClass("photo").first();
            if (postDto.photoId == null) {
                photoPElem.remove();
            } else {
                Element imgElem = photoPElem.getElementsByTag("img").first();
                imgElem.attr("src", getBlogRoot(blogId, pathLevel) + "api/getimage/" + postDto.blogPostId + "/" + postDto.photoId);
            }
            appendParagraph(doc, postBodyElem, Util.cutString(postDto.sectionText, Constants.POST_LIST_TEXT_LENGTH));
            containerMainElem.appendChild(oneBlogPostElem);
        }
    }

    private static void replacePathForBlogList(Document doc, boolean isTop) {
        Elements cssLinks = doc.select("link[rel=\"stylesheet\"]");
        for (Element elem : cssLinks) {
            String oldLink = elem.attr("href");
            String newLink;
            if (isTop) {
                newLink = oldLink.replaceFirst("^\\.\\./\\.\\./", "");
            } else {
                newLink = oldLink.replaceFirst("^\\.\\./", "");
            }
            elem.attr("href", newLink);
        }
        Elements jsLinks = doc.getElementsByTag("script");
        for (Element elem : jsLinks) {
            String oldLink = elem.attr("src");
            String newLink;
            if (isTop) {
                newLink = oldLink.replaceFirst("^\\.\\./\\.\\./", "");
            } else {
                newLink = oldLink.replaceFirst("^\\.\\./", "");
            }
            elem.attr("src", newLink);
        }
    }

    private static String getBlogRoot(String blogId, PathLevel pathLevel) {
        switch (pathLevel) {
            case TOP:
                return (blogId + "/");
            case DATE:
                return "./";
            case POST:
                return "../";
            default:
                assert false : ("pathLevel.." + pathLevel);
        }
        return null; // make compiler happy
    }

    private static void appendParagraph(Document doc, Element parent, String text) {
        String[] lines = text.replace("\\r", "").split("\\n");
        for (String line : lines) {
            Element pElem = doc.createElement("p");
            String escaped = Util.escapeHtml(line);
            String html = Util.createLinkAnchor(escaped);
            pElem.html(html);
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

    private static void renderListOlderNewerLink(ServiceContext context, Document doc, String blogId,
                                                 LocalDate startDate, LocalDate endDate, int page)
    {
        int postCount = BlogPostDbAccess.getBlogPostCountByBlogId(context.getDbAccessInvoker(), blogId, startDate, endDate);

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

    private static void setMetaProperty(Document doc, String propertyName, String value) {
        Element metaElem = doc.select("meta[property=\"" + propertyName + "\"]").first();
        metaElem.attr("content", value);
    }

    private static void renderForFacebook(Document doc, String url) {
        Element shareButtonElem = doc.getElementById("facebook-share-button");
        shareButtonElem.dataset().put("href", url);
    }
}
