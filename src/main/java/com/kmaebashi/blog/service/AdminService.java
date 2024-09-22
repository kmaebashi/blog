package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.ApiStatus;
import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.controller.data.ArticleData;
import com.kmaebashi.blog.controller.data.ArticlePhoto;
import com.kmaebashi.blog.controller.data.PostArticleStatus;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonArray;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonObject;
import com.kmaebashi.jsonparser.JsonValue;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.InvokerOption;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private AdminService() {}

    public static DocumentResult showPage(ServiceInvoker invoker, String blogId, Integer blogPostId) {
        return invoker.invoke((context) -> {
            context.getLogger().info("shoePage called. blogId.." + blogId + " blogPostId.." + blogPostId);
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            List<BlogPostDto> blogPostList = BlogPostDbAccess.getBlogPostForAdmin(context.getDbAccessInvoker(), blogId);

            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/blog_admin.html");
            Document doc = AdminService.renderBlog(htmlPath, blogId, blogDto, blogPostList);
            if (blogPostId == null) {
                AdminService.renderNewPost(doc);
            } else {
                AdminService.renderExistingPost(context, doc, blogId, blogPostId.intValue());
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
            aElem.attr("href", "/blog/" + blogId + "/admin?postid=" + blogPostDto.blogPostId);
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
        Element templatePhotoElem = newSectionElem.getElementsByClass("one-photo").first();
        templatePhotoElem.remove();
        newSectionElem.removeAttr("style");
        newSectionElem.attr("id", "section-box1");
        Element fileInputElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("image-file-input"));
        fileInputElem.attr("data-section", "1");
        sectionContainerElem.appendChild(newSectionElem);
        Element blogPostTitleElem = doc.getElementById("blog-post-title");
        blogPostTitleElem.attr("value", "");

        Map<String, JsonElement> jsonSectionList = new HashMap<>();
        jsonSectionList.put("section1", JsonArray.newInstance(new ArrayList<JsonElement>()));
        setScript(doc, JsonObject.newInstance(jsonSectionList));
    }

    private static void renderExistingPost(ServiceContext context, Document doc,
                                           String blogId, int blogPostId) throws Exception {
        BlogPostDto blogPostDto = BlogPostDbAccess.getBlogPost(context.getDbAccessInvoker(), blogId, blogPostId);
        Element blogPostTitleElem = doc.getElementById("blog-post-title");
        blogPostTitleElem.attr("value", blogPostDto.title);

        Element templateSectionElem = doc.getElementById("hidden-section-box");
        Element sectionContainerElem = doc.getElementById("section-container");
        List<BlogPostSectionDto> sectionList
                = BlogPostDbAccess.getBlogPostSection(context.getDbAccessInvoker(), blogPostId);
        int sectionNumber = 1;
        Map<String, JsonElement> jsonSectionList = new HashMap<>();

        for (BlogPostSectionDto sectionDto : sectionList) {
            Element newSectionElem = templateSectionElem.clone();
            newSectionElem.removeAttr("style");
            newSectionElem.attr("id", "section-box" + sectionNumber);
            Element sectionTitleElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("section-title"));
            sectionTitleElem.text("セクション" + sectionNumber);
            Element textAreaElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("section-text"));
            textAreaElem.text(sectionDto.body);

            Element photoAreaElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("photo-area"));
            List<PhotoDto> photoDtoList
                    = BlogPostDbAccess.getBlogPostPhoto(context.getDbAccessInvoker(), blogPostId, sectionNumber);
            Element templatePhotoElem = newSectionElem.getElementsByClass("one-photo").first();
            templatePhotoElem.remove();
            List<JsonElement> jsonPhotoList = new ArrayList<>();
            int photoIndex = 0;
            for (PhotoDto photoDto : photoDtoList) {
                Element newPhotoElem = templatePhotoElem.clone();
                photoIndex++;
                Element imgElem = newPhotoElem.getElementsByClass("photo").first();
                imgElem.attr("src", "./api/getimageadmin/" + photoDto.photoId);
                Element captionElem = newPhotoElem.getElementsByClass("photo-caption").first();
                captionElem.attr("id", "photo-caption-" + photoDto.photoId);
                captionElem.text(photoDto.caption);

                photoAreaElem.appendChild(newPhotoElem);
                Map<String, JsonElement> jsonPhotoMap = new HashMap<>();
                jsonPhotoMap.put("id", JsonValue.createIntValue(photoDto.photoId));
                jsonPhotoMap.put("caption", JsonValue.createStringValue(photoDto.caption));
                jsonPhotoList.add(JsonObject.newInstance(jsonPhotoMap));
            }
            Element fileInputElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("image-file-input"));
            fileInputElem.attr("data-section", "" + sectionNumber);
            sectionContainerElem.appendChild(newSectionElem);
            jsonSectionList.put("section"+ sectionNumber, JsonArray.newInstance(jsonPhotoList));

            sectionNumber++;
        }
        setScript(doc, JsonObject.newInstance(jsonSectionList));
    }

    private static void setScript(Document doc, JsonElement json) {
        Element scriptElem = doc.getElementById("server-side-include-script");
        scriptElem.text("  photosInThisPage = " + json.stringify() + ";");
    }

    public static JsonResult postArticle(ServiceInvoker invoker, String userId, String blogId, ArticleData article) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            if (!blogDto.ownerUser.equals(userId)) {
                throw new BadRequestException("ブログのオーナー以外からのポストです。");
            }
            int blogPostId;
            if (article.blogPostId == null) {
                blogPostId = BlogPostDbAccess.getBlogPostSequence(context.getDbAccessInvoker());
                BlogPostDbAccess.insertBlogPost(context.getDbAccessInvoker(),
                        blogPostId, blogId, article.title, LocalDateTime.now(),
                        article.publishFlag ? BlogPostStatus.PUBLISHED : BlogPostStatus.DRAFT);
            } else {
                blogPostId = article.blogPostId;
                BlogPostDbAccess.updateBlogPost(context.getDbAccessInvoker(),
                        blogPostId, blogId, article.title, LocalDateTime.now(),
                        article.publishFlag ? BlogPostStatus.PUBLISHED : BlogPostStatus.DRAFT);
            }

            if (article.blogPostId != null) {
                BlogPostDbAccess.deleteAllSections(context.getDbAccessInvoker(), blogPostId);
            }
            BlogPostDbAccess.unlinkPhotoFromBlogPost(context.getDbAccessInvoker(),
                                                     blogId, blogPostId);
            for (int secIdx = 0; secIdx < article.sectionArray.length; secIdx++) {
                BlogPostDbAccess.insertSection(context.getDbAccessInvoker(), blogPostId, secIdx,
                                                article.sectionArray[secIdx].body);
                int displayOrder = 1;
                for (ArticlePhoto photo : article.sectionArray[secIdx].photos) {
                    BlogPostDbAccess.linkPhotoToBlogPost(context.getDbAccessInvoker(),
                            photo.id, blogId, blogPostId, secIdx + 1, displayOrder, photo.caption);
                    displayOrder++;
                }
            }
            PostArticleStatus status = new PostArticleStatus(ApiStatus.SUCCESS, "投稿成功", blogPostId);
            String json = ClassMapper.toJson(status);

            return new JsonResult(json);
        }, InvokerOption.TRANSACTIONAL);
    }

}
