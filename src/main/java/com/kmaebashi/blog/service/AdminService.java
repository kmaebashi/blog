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
import java.util.List;

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
        newSectionElem.removeAttr("style");
        newSectionElem.attr("id", "section-box1");
        Element fileInputElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("image-file-input"));
        fileInputElem.attr("data-section", "1");
        sectionContainerElem.appendChild(newSectionElem);
        Element blogPostTitleElem = doc.getElementById("blog-post-title");
        blogPostTitleElem.attr("value", "");
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
            for (PhotoDto photoDto : photoDtoList) {
                Element divElem = doc.createElement("div");
                divElem.attr("class", "one-photo");
                Element pElem = doc.createElement("p");
                Element imgElem = doc.createElement("img");
                imgElem.attr("src", "./api/getimageadmin/" + photoDto.photoId);
                pElem.appendChild(imgElem);
                divElem.appendChild(pElem);

                Element captionElem = doc.createElement("textarea");
                captionElem.attr("id", "photo-caption-" + photoDto.photoId);
                captionElem.attr("class", "photo-caption");
                captionElem.text(photoDto.caption);
                divElem.appendChild(captionElem);

                photoAreaElem.appendChild(divElem);
            }
            Element fileInputElem = JsoupUtil.getFirst(newSectionElem.getElementsByClass("image-file-input"));
            fileInputElem.attr("data-section", "" + sectionNumber);
            sectionContainerElem.appendChild(newSectionElem);


            sectionNumber++;
        }
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
            for (int secIdx = 0; secIdx < article.sectionArray.length; secIdx++) {
                BlogPostDbAccess.insertSection(context.getDbAccessInvoker(), blogPostId, secIdx,
                                                article.sectionArray[secIdx].body);
                for (ArticlePhoto photo : article.sectionArray[secIdx].photos) {
                    BlogPostDbAccess.linkPhotoToBlogPost(context.getDbAccessInvoker(),
                            photo.id, blogId, blogPostId, photo.caption);
                }
            }
            PostArticleStatus status = new PostArticleStatus(ApiStatus.SUCCESS, "投稿成功", blogPostId);
            String json = ClassMapper.toJson(status);

            return new JsonResult(json);
        }, InvokerOption.TRANSACTIONAL);
    }

}
