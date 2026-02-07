package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.blog.controller.data.BlogPostCountEachDay;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.*;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.nctfw.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                                                  String currentUserId, String url, boolean isPreview) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            BlogPostDto blogPostDto
                    = BlogPostDbAccess.getBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
            if (blogDto == null || blogPostDto == null
                    || (!isPreview &&
                        blogPostDto.status != BlogPostStatus.PUBLISHED.intValue())) {
                throw new NotFoundException("ブログ記事がありません");
            }
            if (isPreview && !blogDto.ownerUser.equals(currentUserId)) {
                throw new BadRequestException("ブログの所有者ではありません。");
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
            ShowPostService.renderOlderNewerLink(context, doc, blogId, blogPostDto);
            ShowPostService.renderCommentArea(context, doc, blogPostId, currentUserId);
            if (isPreview) {
                Element snsAreaElem = doc.getElementById("sns-area");
                snsAreaElem.remove();
            } else {
                ShowPostService.renderForFacebook(doc, url);
            }
            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showPostsByBlogId(ServiceInvoker invoker, String blogId, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            if (blogDto == null) {
                throw new BadRequestException("ブログ" + blogId + "はありません。");
            }
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

    public static DocumentResult showTitleList(ServiceInvoker invoker, String blogId, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            if (blogDto == null) {
                throw new BadRequestException("ブログ" + blogId + "はありません。");
            }
            List<BlogPostDto> blogPostDtoList
                    = BlogPostDbAccess.getBlogPostList(context.getDbAccessInvoker(), blogId,
                    (page - 1) * Constants.NUM_OF_BLOG_TITLES_PER_PAGE,
                    Constants.NUM_OF_BLOG_TITLES_PER_PAGE);
            int postCount = BlogPostDbAccess.getBlogPostCountByBlogId(context.getDbAccessInvoker(), blogId, null, null);
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/title_list/title_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.setProperties(doc, PathLevel.DATE, LocalDate.now());
            replacePathForBlogList(doc, false);
            ShowPostService.renderHeadTitleTop(doc, blogDto, page);
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.DATE);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.DATE);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderTitleList(doc, blogId, blogPostDtoList, page, postCount);

            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showCommentList(ServiceInvoker invoker, String blogId, int page) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            if (blogDto == null) {
                throw new BadRequestException("ブログ" + blogId + "はありません。");
            }
            List<CommentDto> commentDtoList
                    = BlogPostDbAccess.getCommentsByBlogId(context.getDbAccessInvoker(), blogId,
                                (page - 1) * Constants.NUM_OF_BLOG_TITLES_PER_PAGE,
                                Constants.NUM_OF_BLOG_TITLES_PER_PAGE);
            int commentCount = BlogPostDbAccess.getCommentCountByBlogId(context.getDbAccessInvoker(), blogId);
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/comment_list/comment_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.setProperties(doc, PathLevel.DATE, LocalDate.now());
            replacePathForBlogList(doc, false);
            ShowPostService.renderHeadTitleTop(doc, blogDto, page);
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.DATE);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.DATE);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderCommentList(doc, blogId, commentDtoList, page, commentCount);

            return new DocumentResult(doc);
        });
    }

    public static DocumentResult showSearchList(ServiceInvoker invoker, String blogId, int page,
                                                List<String> keywords, boolean titleSearch, boolean contentSearch) {
        return invoker.invoke((context) -> {
            BlogProfileDto blogDto = BlogDbAccess.getBlogAndProfile(context.getDbAccessInvoker(), blogId);
            if (blogDto == null) {
                throw new BadRequestException("ブログ" + blogId + "はありません。");
            }
            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/search_list/search_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            ShowPostService.setProperties(doc, PathLevel.DATE, LocalDate.now());
            replacePathForBlogList(doc, false);
            ShowPostService.renderHeadTitleTop(doc, blogDto, page);
            ShowPostService.renderBlogTitle(doc, blogDto, PathLevel.DATE);
            ShowPostService.renderProfile(doc, blogId, blogDto, PathLevel.DATE);
            ShowPostService.renderRecentPosts(context, doc, blogId, PathLevel.DATE);
            ShowPostService.renderRecentComments(context, doc, blogId, PathLevel.DATE);
            renderSearchCondition(doc, keywords, titleSearch, contentSearch);
            if (titleSearch && !contentSearch) {
                ShowPostService.renderSearchTitleList(context, doc, blogId, keywords, page);
            } else if (contentSearch) {
                ShowPostService.renderSearchList(context, doc, blogId, keywords, page, titleSearch);
            } else {
                throw new InternalException("titleSearch.." + titleSearch + ", contentSearch.." + contentSearch);
            }

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

        Element rssAElem = doc.getElementById("rss-url");
        rssAElem.attr("href", topUrl + "/rss");
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

        Element seeMoreA = doc.select("#recent-posts-area .sidebar-see-more a").first();
        seeMoreA.attr("href", getBlogRoot(blogId, pathLevel) + "list");
    }

    private static void renderRecentComments(ServiceContext context, Document doc, String blogId, PathLevel pathLevel) {
        List<CommentDto> commentList = BlogPostDbAccess.getCommentsByBlogId(context.getDbAccessInvoker(), blogId, 0, 10);

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
        Element seeMoreA = doc.select("#recent-comment-area .sidebar-see-more a").first();
        seeMoreA.attr("href", getBlogRoot(blogId, pathLevel) + "commentlist");
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
        MarkupConverter converter = new MarkupConverter(MarkupConverterMode.NORMAL);
        List<BlogPostSectionDto> sectionList
                = BlogPostDbAccess.getBlogPostSection(context.getDbAccessInvoker(), blogPostDto.blogPostId);
        int sectionNumber = 1;
        String ogImagePhotoPath = null;
        for (BlogPostSectionDto sectionDto : sectionList) {
            if (sectionNumber == 1) {
                String summary = getSummary(sectionDto.body, MarkupConverterMode.SUMMARY_TEXT,
                                            Constants.OG_DESCRIPTION_LENGTH);
                ShowPostService.setMetaProperty(doc, "og:description", summary);
            }
            String converted = converter.convert(sectionDto.body);
            Element sectionBodyDiv = doc.createElement("div");
            sectionBodyDiv.html(converted);
            postBodyElem.appendChild(sectionBodyDiv);
            List<PhotoDto> photoList
                    = BlogPostDbAccess.getBlogPostPhoto(context.getDbAccessInvoker(),
                    blogPostDto.blogPostId, sectionNumber);
            for (PhotoDto photoDto : photoList) {
                Element captionContainerElem = null;
                if (!Util.isNullOrEmpty(photoDto.caption)) {
                    Element captionContainerBlockElem = doc.createElement("div");
                    captionContainerElem = doc.createElement("div");
                    captionContainerBlockElem.appendChild(captionContainerElem);
                    captionContainerElem.attr("class", "photo-container");
                    postBodyElem.appendChild(captionContainerBlockElem);
                }
                Element photoPElem = doc.createElement("p");
                photoPElem.attr("class", "photo");
                Element imgElem = doc.createElement("img");
                String photoPath = "api/getimage/" + photoDto.blogPostId + "/" + photoDto.photoId;
                imgElem.attr("src",  getBlogRoot(blogId, PathLevel.POST) + photoPath);
                if (ogImagePhotoPath == null || photoDto.isOgImage) {
                    ogImagePhotoPath = photoPath;
                }
                Element orgSizeAElem = doc.createElement("a");
                orgSizeAElem.appendChild(imgElem);
                orgSizeAElem.attr("href", getBlogRoot(blogId, PathLevel.POST) + "api/getorgsizeimage/"
                                          + photoDto.blogPostId + "/" + photoDto.photoId);
                orgSizeAElem.attr("target", "_blank");
                photoPElem.appendChild(orgSizeAElem);
                if (captionContainerElem != null) {
                    captionContainerElem.appendChild(photoPElem);
                    Element captionDiv = doc.createElement("div");
                    String convertedCaption = converter.convert(photoDto.caption);
                    captionDiv.html(convertedCaption);
                    captionContainerElem.appendChild(captionDiv);

                } else {
                    postBodyElem.appendChild(photoPElem);
                }
            }
            sectionNumber++;
        }
        if (ogImagePhotoPath != null) {
            String photoUrl = url.replaceFirst("post/\\d+$", ogImagePhotoPath);
            ShowPostService.setMetaProperty(doc, "og:image", photoUrl);
        }
        String footnoteHtml = converter.getFootnoteStr();
        if (footnoteHtml != null) {
            Element footnoteDivElem = doc.createElement("div");
            footnoteDivElem.html(footnoteHtml);
            postBodyElem.after(footnoteDivElem);
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
            appendSummary(doc, postBodyElem, postDto.sectionText);
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
        Element rssIconElem = doc.getElementById("rss-icon");
        String oldSrc = rssIconElem.attr("src");
        String newSrc;
        if (isTop) {
            newSrc = oldSrc.replaceFirst("^\\.\\./\\.\\./", "");
        } else {
            newSrc = oldSrc.replaceFirst("^\\.\\./", "");
        }
        rssIconElem.attr("src", newSrc);
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
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!isFirst) {
                sb.append("<br>");
            }
            isFirst = false;
            String escaped = Util.escapeHtml(line);
            String html = Util.createLinkAnchor(escaped);
            sb.append(html);
        }
        Element pElem = doc.createElement("p");
        pElem.html(sb.toString());
        parent.appendChild(pElem);
    }

    private static void appendSummary(Document doc, Element parent, String text) {
        String summary = getSummary(text, MarkupConverterMode.SUMMARY_TEXT, Constants.POST_LIST_TEXT_LENGTH);
        Element pElem = doc.createElement("p");
        pElem.text(summary);
        parent.appendChild(pElem);
    }

    private static String getSummary(String src, MarkupConverterMode mode, int length) {
        MarkupConverter converter = new MarkupConverter(mode);
        String str = converter.convert(src);
        String cutText = Util.cutString(str, length);

        return cutText;
    }

    private static void renderOlderNewerLink(ServiceContext context, Document doc, String blogId, BlogPostDto blogPostDto)
    {
        BlogPostDto olderPostDto
                = BlogPostDbAccess.getOlderBlogPost(context.getDbAccessInvoker(), blogId, blogPostDto.postedDate);
        BlogPostDto newerPostDto
                = BlogPostDbAccess.getNewerBlogPost(context.getDbAccessInvoker(), blogId, blogPostDto.postedDate);
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

    private static void renderTitleList(Document doc, String blogId, List<BlogPostDto> blogPostDtoList,
                                        int page, int postCount) {
        Element ulElem = doc.getElementById("blog-post-title-list");
        Element firstLi = ulElem.getElementsByTag("li").first();
        Element templateLi = firstLi.clone();
        ulElem.empty();
        for (BlogPostDto dto : blogPostDtoList) {
            Element newLi = templateLi.clone();
            Element aElem = newLi.getElementsByTag("a").first();
            aElem.attr("href", "post/" + dto.blogPostId);
            Element dateSpan = aElem.getElementsByClass("title-list-date").first();
            dateSpan.text(dto.postedDate.format(postedDateFormatter));
            Element titleSpan = aElem.getElementsByClass("title-list-title").first();
            titleSpan.text(dto.title);
            ulElem.appendChild(newLi);
        }
        renderPagenation(doc, "list", page, postCount, Constants.NUM_OF_BLOG_TITLES_PER_PAGE, null);
    }

    private static void renderCommentList(Document doc, String blogId, List<CommentDto> commentDtoList,
                                        int page, int commentCount) {
        Element ulElem = doc.getElementById("blog-comment-list");
        Element firstLi = ulElem.getElementsByTag("li").first();
        Element templateLi = firstLi.clone();
        ulElem.empty();
        for (CommentDto dto : commentDtoList) {
            Element newLi = templateLi.clone();
            Element aElem = newLi.getElementsByTag("a").first();
            aElem.attr("href", "post/" + dto.blogPostId + "#comment" + dto.commentId);
            Element dateSpan = aElem.getElementsByClass("comment-list-date").first();
            dateSpan.text(dto.createdAt.format(postedDateFormatter));
            Element titleSpan = aElem.getElementsByClass("comment-list-title").first();
            titleSpan.text(dto.blogPostTitle);
            Element posterSpan = aElem.getElementsByClass("comment-list-poster").first();
            posterSpan.text(dto.posterName);
            ulElem.appendChild(newLi);
        }
        renderPagenation(doc, "commentlist", page, commentCount, Constants.NUM_OF_BLOG_TITLES_PER_PAGE, null);
    }

    private static void renderSearchCondition(Document doc, List<String> keywords, boolean titleSearch, boolean contentSearch) {
        String keywordsStr = String.join(" ", keywords);
        Element spanElem = doc.getElementById("search-keyword");
        spanElem.text(keywordsStr);

        Element searchAreaDiv = doc.getElementById("main-search-area");
        Element textElem = doc.getElementsByClass("search-input").first();
        textElem.val(keywordsStr);
        Element titleCheckElem = doc.getElementsByClass("search-title-check").first();
        titleCheckElem.attr("checked", titleSearch);
        Element contentCheckElem = doc.getElementsByClass("search-content-check").first();
        contentCheckElem.attr("checked", contentSearch);
    }

    private static void renderSearchTitleList(ServiceContext context, Document doc, String blogId, List<String> keywords,
                                              int page) throws Exception {
        Element listElem = doc.getElementById("blog-search-list");
        Element firstItem = listElem.getElementsByClass("blog-search-item").first();
        Element templateItem = firstItem.clone();
        templateItem.getElementsByClass("blog-search-item-content").first().remove();
        listElem.empty();

        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPostsByTitle(context.getDbAccessInvoker(), blogId, keywords,
                                                    (page - 1) * Constants.NUM_OF_BLOG_SEARCH_PER_PAGE,
                                                          Constants.NUM_OF_BLOG_SEARCH_PER_PAGE);
        if (dtoList.size() == 0) {
            renderNoResult(doc, listElem);
            return;
        }
        int totalCount = dtoList.get(0).totalCount;
        for (BlogPostSearchDto dto : dtoList) {
            Element newItem = templateItem.clone();
            Element aElem = newItem.getElementsByTag("a").first();
            aElem.attr("href", "post/" + dto.blogPostId);
            Element dateSpan = aElem.getElementsByClass("search-list-date").first();
            dateSpan.text(dto.postedDate.format(postedDateFormatter));
            Element titleSpan = aElem.getElementsByClass("search-list-title").first();
            titleSpan.html(Util.boldifyHitString(dto.title, keywords));
            listElem.appendChild(newItem);
        }
        String searchStr = getSearchString(keywords, true, false);
        renderPagenation(doc, "searchlist", page, totalCount, Constants.NUM_OF_BLOG_SEARCH_PER_PAGE, searchStr);
    }

    private static void renderSearchList(ServiceContext context, Document doc, String blogId, List<String> keywords,
                                         int page, boolean titleSearch) throws Exception {
        Element listElem = doc.getElementById("blog-search-list");
        Element firstItem = listElem.getElementsByClass("blog-search-item").first();
        Element templateItem = firstItem.clone();
        listElem.empty();

        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPosts(context.getDbAccessInvoker(), blogId, keywords, titleSearch,
                (page - 1) * Constants.NUM_OF_BLOG_SEARCH_PER_PAGE,
                Constants.NUM_OF_BLOG_SEARCH_PER_PAGE);
        if (dtoList.size() == 0) {
            renderNoResult(doc, listElem);
            return;
        }
        int totalCount = dtoList.get(0).totalCount;
        for (BlogPostSearchDto dto : dtoList) {
            Element newItem = templateItem.clone();
            Element aElem = newItem.getElementsByTag("a").first();
            aElem.attr("href", "post/" + dto.blogPostId);
            Element dateSpan = aElem.getElementsByClass("search-list-date").first();
            dateSpan.text(dto.postedDate.format(postedDateFormatter));
            Element titleSpan = aElem.getElementsByClass("search-list-title").first();
            titleSpan.html(Util.boldifyHitString(dto.title, keywords));
            Element contentElem = newItem.getElementsByClass("blog-search-item-content").first();
            String summaryText = getSummary(dto.bodyConcat, MarkupConverterMode.SUMMARY_TEXT, Integer.MAX_VALUE);
            String neighborhood = Util.getKeywordNeighborhood(summaryText, keywords);
            contentElem.html(Util.boldifyHitString(neighborhood, keywords));

            listElem.appendChild(newItem);
        }
        String searchStr = getSearchString(keywords, titleSearch, true);
        renderPagenation(doc, "searchlist", page, totalCount, Constants.NUM_OF_BLOG_SEARCH_PER_PAGE, searchStr);
    }

    static String getSearchString(List<String> keywords, boolean titleSearch, boolean contentSearch) throws Exception {
        String joined = String.join(" ", keywords);
        String escaped = URLEncoder.encode(joined, StandardCharsets.UTF_8.toString());
        String mode;
        if (titleSearch && !contentSearch) {
            mode = "title";
        } else if (!titleSearch && contentSearch) {
            mode = "content";
        } else {
            mode = "both";
        }

        return "&q=" + escaped + "&mode=" + mode;
    }

    private static void renderNoResult(Document doc, Element parent) {
        Element pElem = doc.createElement("p");
        pElem.text("見つかりませんでした。");
        parent.appendChild(pElem);
        Element pagenationDiv = doc.getElementById("pagenation-area");
        pagenationDiv.remove();
    }

    private static void renderPagenation(Document doc, String mode, int page, int totalCount, int numOfPerPage,
                                         String searchStr) {
        int[] pageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(totalCount, page, numOfPerPage, pageCountBuf);
        int pageCount = pageCountBuf[0];

        Element divElem = doc.getElementById("pagenation-area");
        divElem.empty();
        if (page > 1) {
            Element prevA = doc.createElement("a");
            prevA.attr("href", mode + "?page=" + (page - 1) + (searchStr != null ? searchStr : ""));
            prevA.text("≪");
            divElem.appendChild(prevA);
        }
        for (int i = 0; i < (pageCount - startPage + 1) && i < Constants.NUM_OF_PAGENATION; i++) {
            Element numElem;
            if (startPage + i == page) {
                numElem = doc.createElement("span");
            } else {
                numElem = doc.createElement("a");
                numElem.attr("href", mode + "?page=" + (startPage + i) + (searchStr != null ? searchStr : ""));
            }
            numElem.text(" " + (startPage + i));
            divElem.appendChild(numElem);
        }
        if (page < pageCount) {
            Element nextA = doc.createElement("a");
            nextA.attr("href", mode + "?page=" + (page + 1) + (searchStr != null ? searchStr : ""));
            nextA.text(" ≫");
            divElem.appendChild(nextA);
        }
    }
}
