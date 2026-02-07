package com.kmaebashi.blog.service;

import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.blog.controller.data.CommentData;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShowPostServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        ShowPostServiceTest.conn = BlogTestUtil.getConnection();
        ShowPostServiceTest.logger = new FileLogger("./log", "ShowPostServiceTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void showPostByPostIdTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DocumentResult dr = ShowPostService.showPostByPostId(si, "kmaebashiblog", Integer.valueOf(47), "kmaebashi",
                                                             "http://localhost:8080/blog/post/47", false);
        String html = dr.getDocument().html();
    }

    @Test
    void showPostByPostIdTest002() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DocumentResult dr = ShowPostService.showPostByPostId(si, "kmaebashiblog", Integer.valueOf(37), "kmaebashi",
                "http://localhost:8080/blog/post/37", false);
        String html = dr.getDocument().html();
    }

    @Test
    void showPostByPostIdTest003() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        try {
            DocumentResult dr = ShowPostService.showPostByPostId(si, "kmaebashiblog", Integer.valueOf(30), "kmaebashi",
                    "http://localhost:8080/blog/post/30", false);
            String html = dr.getDocument().html();
        } catch (Exception ex) {
            assertTrue(ex instanceof NotFoundException);
            return;
        }
        fail();
    }

    @Test
    void showPostByPostIdTest004() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DocumentResult dr = ShowPostService.showPostByPostId(si, "kmaebashiblog", Integer.valueOf(30), "kmaebashi",
                "http://localhost:8080/blog/post/30", true);
        String html = dr.getDocument().html();
    }

    @Test
    void showPostByPostIdTest005() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        try {
            DocumentResult dr = ShowPostService.showPostByPostId(si, "kmaebashiblog", Integer.valueOf(30), "kmaebashi2",
                    "http://localhost:8080/blog/post/30", true);
            String html = dr.getDocument().html();
        } catch (Exception ex) {
            assertTrue(ex instanceof BadRequestException);
            return;
        }
        fail();
    }

    @Test
    void showPostsByBlogIdTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DocumentResult dr = ShowPostService.showPostsByBlogId(si, "kmaebashiblog", 1);
        String html = dr.getDocument().html();
    }

    @Test
    void showPostsMonthlyTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DateTimeFormatter monthlyFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fromDate = LocalDate.parse("202407" + "01", monthlyFormat);
        LocalDate toDate = fromDate.plusMonths(1);
        DocumentResult dr = ShowPostService.showPostsDateRange(si, "kmaebashiblog", fromDate, toDate, "2024/07", 1);
        String html = dr.getDocument().html();
    }

    @Test
    void showPostsDailyTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DateTimeFormatter monthlyFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fromDate = LocalDate.parse("20240514", monthlyFormat);
        LocalDate toDate = fromDate.plusDays(1);
        DocumentResult dr = ShowPostService.showPostsDateRange(si, "kmaebashiblog", fromDate, toDate, "2024/05/14", 1);
        String html = dr.getDocument().html();
    }

    @Test
    void showSearchListTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        List<String> keywords = new ArrayList<>();
        keywords.add("鰯禄勾讐麓戚黴槌嶽"); // 絶対にヒットしない文字列で検索
        DocumentResult dr = ShowPostService.showSearchList(si, "kmaebashiblog", 1, keywords, true, false);
        String html = dr.getDocument().html();
    }

    @Test
    void showSearchListTest002() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        List<String> keywords = new ArrayList<>();
        keywords.add("鳳斎瑠");
        DocumentResult dr = ShowPostService.showSearchList(si, "kmaebashiblog", 1, keywords, true, false);
        String html = dr.getDocument().html();
    }

    @Test
    void showSearchListTest003() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        List<String> keywords = new ArrayList<>();
        keywords.add("㐀㑳㒼㓾㔿㕣");
        DocumentResult dr = ShowPostService.showSearchList(si, "kmaebashiblog", 1, keywords, true, true);
        String html = dr.getDocument().html();
    }

    @Test
    void getSearchStringTest001() throws Exception {
        List<String> keywords = new ArrayList<>();

        keywords.add("なんとか");
        keywords.add("かんとか");

        String ret = ShowPostService.getSearchString(keywords, true, false);
        assertEquals("&q=%E3%81%AA%E3%82%93%E3%81%A8%E3%81%8B+%E3%81%8B%E3%82%93%E3%81%A8%E3%81%8B&mode=title", ret);
    }

    @Test
    void getSearchStringTest002() throws Exception {
        List<String> keywords = new ArrayList<>();

        keywords.add("abc");
        keywords.add("def");

        String ret = ShowPostService.getSearchString(keywords, false, true);
        assertEquals("&q=abc+def&mode=content", ret);
    }

    @Test
    void getSearchStringTest003() throws Exception {
        List<String> keywords = new ArrayList<>();

        keywords.add("a&bc");
        keywords.add("def");

        String ret = ShowPostService.getSearchString(keywords, true, true);
        assertEquals("&q=a%26bc+def&mode=both", ret);
    }

    @Test
    void getPostCountEachDayTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DateTimeFormatter monthlyFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate month = LocalDate.parse("20240501", monthlyFormat);
        JsonResult jr = ShowPostService.getPostCountEachDay(si, "kmaebashiblog", month);
    }

    @Test
    void postCommentTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        CommentData data = new CommentData();
        data.blogId = "kmaebashiblog";
        data.blogPostId = 5;
        data.poster = "とおりすがり";
        data.message = "なんとかかんとか";
        JsonResult result = CommentService.postComment(si, null, "kmaebashiblog", data);
    }
}