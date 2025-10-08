package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.common.Constants;
import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminListService {
    public static DocumentResult showPage(ServiceInvoker invoker, String blogId, int page,
                                          String currentUserId) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            if (blogDto == null
                || !blogDto.ownerUser.equals(currentUserId)) {
                throw new BadRequestException("ブログの所有者ではありません。");
            }
            List<BlogPostDto> blogPostList
                    = BlogPostDbAccess.getBlogPostForAdmin(context.getDbAccessInvoker(), blogId,
                                        (page - 1) * Constants.NUM_OF_POST_LIST_PER_PAGE_ADMIN,
                                        Constants.NUM_OF_POST_LIST_PER_PAGE_ADMIN);
            int postCount = BlogPostDbAccess.getBlogPostCountForAdmin(context.getDbAccessInvoker(), blogId);

            Path htmlPath = context.getHtmlTemplateDirectory().resolve("blogid/blog_admin_list.html");
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");

            AdminService.renderAdminHeader(doc, blogDto);
            AdminListService.renderPostList(doc, blogPostList);
            AdminListService.renderPagenation(doc, page, postCount);

            return new DocumentResult(doc);
        });
    }

    private static DateTimeFormatter postedDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static void renderPostList(Document doc, List<BlogPostDto> blogPostList) {
        Element tableElem = doc.getElementById("article-list-table");
        Element bodyElem = tableElem.getElementsByTag("tbody").first();
        Element templateTr = bodyElem.getElementsByTag("tr").first();
        bodyElem.empty();

        for (BlogPostDto dto : blogPostList) {
            Element cloneTr = templateTr.clone();
            Elements tdList = cloneTr.getElementsByTag("td");
            tdList.get(0).text(postedDateFormatter.format(dto.postedDate));
            Element titleA = tdList.get(1).getElementsByTag("a").first();
            titleA.attr("href", "./admin?postid=" + dto.blogPostId);
            titleA.text(dto.title);
            tdList.get(2).text(dto.status == BlogPostStatus.PUBLISHED.intValue() ? "公開" : "下書き");
            Element linkA = tdList.get(3).getElementsByTag("a").first();
            linkA.attr("href", "./previewpost/" + dto.blogPostId);
            bodyElem.appendChild(cloneTr);
        }

    }

    private static void renderPagenation(Document doc, int page, int totalCount) {
        int[] pageCountBuf = new int[1];
        int startPage = Util.calcPagenationStart(totalCount, page, Constants.NUM_OF_POST_LIST_PER_PAGE_ADMIN, pageCountBuf);
        int pageCount = pageCountBuf[0];

        Element divElem = doc.getElementById("pagenation-area");
        divElem.empty();
        if (page > 1) {
            Element prevA = doc.createElement("a");
            prevA.attr("href", "./admin_list?page=" + (page - 1));
            prevA.text("≪");
            divElem.appendChild(prevA);
        }
        for (int i = 0; i < (pageCount - startPage + 1) && i < Constants.NUM_OF_PAGENATION; i++) {
            Element numElem;
            if (startPage + i == page) {
                numElem = doc.createElement("span");
            } else {
                numElem = doc.createElement("a");
                numElem.attr("href", "./admin_list?page=" + (startPage + i));
            }
            numElem.text(" " + (startPage + i));
            divElem.appendChild(numElem);
        }
        if (page < pageCount) {
            Element nextA = doc.createElement("a");
            nextA.attr("href", "./admin_list?page=" + (page + 1));
            nextA.text(" ≫");
            divElem.appendChild(nextA);
        }
    }

}
