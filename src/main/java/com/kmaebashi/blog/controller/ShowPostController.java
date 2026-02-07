package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.common.SessionKey;
import com.kmaebashi.blog.controller.data.CommentData;
import com.kmaebashi.blog.service.CommentService;
import com.kmaebashi.blog.service.ShowPostService;
import com.kmaebashi.blog.service.Util;
import com.kmaebashi.blog.util.CsrfUtil;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class ShowPostController {
    private ShowPostController() {}

    public static RoutingResult showPostsByBlogId(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            DocumentResult result
                    = ShowPostService.showPostsByBlogId(context.getServiceInvoker(), blogId, page);

            return result;
        });
    }

    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static DateTimeFormatter monthlyDispDateFormat = DateTimeFormatter.ofPattern("yyyy/MM");

    public static RoutingResult showPostsMonthly(ControllerInvoker invoker, String blogId, String monthStr) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            LocalDate fromDate;
            try {
                fromDate = LocalDate.parse(monthStr + "01", dateFormat);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("日付フォーマットが不正です(" + monthStr + ")");
            }
            LocalDate toDate = fromDate.plusMonths(1);
            String dispDateStr = fromDate.format(monthlyDispDateFormat);

            DocumentResult result
                    = ShowPostService.showPostsDateRange(context.getServiceInvoker(), blogId,
                                                         fromDate, toDate, dispDateStr, page);

            return result;
        });
    }

    private static DateTimeFormatter dailyDispDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static RoutingResult showPostsDaily(ControllerInvoker invoker, String blogId, String dateStr) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            LocalDate fromDate;
            try {
                fromDate = LocalDate.parse(dateStr, dateFormat);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("日付フォーマットが不正です(" + dateStr + ")");
            }
            LocalDate toDate = fromDate.plusDays(1);
            String dispDateStr = fromDate.format(dailyDispDateFormat);

            DocumentResult result
                    = ShowPostService.showPostsDateRange(context.getServiceInvoker(), blogId,
                                                         fromDate, toDate, dispDateStr, page);

            return result;
        });
    }

    public static RoutingResult showPostByPostId(ControllerInvoker invoker, String blogId, int blogPostId,
                                                 String currentUserId, boolean isPreview) {
        return invoker.invoke((context) -> {
            String url = context.getServletRequest().getRequestURL().toString();
            context.getLogger().info("url.." + url);
            url = url.replaceFirst("\\.do$", "");
            context.getLogger().info("url2.." + url);
            DocumentResult result
                    = ShowPostService.showPostByPostId(context.getServiceInvoker(),
                                                       blogId, blogPostId, currentUserId, url, isPreview);

            HttpSession session = context.getServletRequest().getSession(false);
            if (session != null) {
                String csrfToken = (String) session.getAttribute(SessionKey.CSRF_TOKEN);
                CsrfUtil.addCsrfToken(result, csrfToken);
            }
            return result;
        });
    }

    public static RoutingResult showTitleList(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            DocumentResult result
                    = ShowPostService.showTitleList(context.getServiceInvoker(), blogId, page);

            return result;
        });
    }

    public static RoutingResult showCommentList(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            DocumentResult result
                    = ShowPostService.showCommentList(context.getServiceInvoker(), blogId, page);

            return result;
        });
    }

    public static RoutingResult showSearchList(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            String keywordsOrg = context.getServletRequest().getParameter("q");
            List<String> keywords = Util.splitQueryKeywords(keywordsOrg);
            if (keywords.size() == 0) {
                throw new BadRequestException("検索キーワードがありません。");
            }
            String modeStr = context.getServletRequest().getParameter("mode");
            boolean titleSearch;
            boolean contentSearch;
            if ("both".equals(modeStr)) {
                titleSearch = contentSearch = true;
            } else if ("title".equals(modeStr)) {
                titleSearch = true;
                contentSearch = false;
            } else if ("content".equals(modeStr)) {
                titleSearch = false;
                contentSearch = true;
            } else {
                titleSearch = contentSearch = true;
            }

            DocumentResult result
                    = ShowPostService.showSearchList(context.getServiceInvoker(), blogId, page,
                                                     keywords, titleSearch, contentSearch);

            return result;
        });
    }

    public static RoutingResult getPostCountEachDay(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String monthStr = context.getServletRequest().getParameter("month");
            if (monthStr == null) {
                throw new BadRequestException("対象月が指定されていません。");
            }
            LocalDate month;
            try {
                month = LocalDate.parse(monthStr + "01", dateFormat);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("日付フォーマットが不正です(" + monthStr + ")");
            }
            JsonResult result
                    = ShowPostService.getPostCountEachDay(context.getServiceInvoker(), blogId, month);

            return result;
        });

    }

    public static RoutingResult postComment(ControllerInvoker invoker, String currentUserId, String blogId) {
        return invoker.invoke((context) -> {
            JsonResult result = null;

            if (!CsrfUtil.checkCsrfToken(context.getServletRequest(), false)) {
                throw new BadRequestException("CSRFトークン不正");
            }
            try (JsonParser jsonParser = JsonParser.newInstance(context.getServletRequest().getReader())) {
                JsonElement elem = jsonParser.parse();
                CommentData comment = ClassMapper.toObject(elem, CommentData.class);
                if (!blogId.equals(comment.blogId)) {
                    throw new BadRequestException("URLのブログID(" + blogId
                                                    + ")とPOSTされたブログID(" + comment.blogId + ")が一致しません。");
                }
                result = CommentService.postComment(context.getServiceInvoker(),
                                                    currentUserId, blogId, comment);
            }
            return result;
        });
    }
}
