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

    @Test
    void calcPagenationStartTest001() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(0, 1, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(0, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest002() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(1, 1, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(1, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest003() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(50, 1, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(1, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest004() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(51, 1, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(2, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest005() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(501, 2, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest006() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(501, 5, dispPageCountBuf);
        assertEquals(1, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }

    @Test
    void calcPagenationStartTest007() {
        int[] dispPageCountBuf = new int[1];
        int startPage = ShowPostService.calcPagenationStart(501, 6, dispPageCountBuf);
        assertEquals(2, startPage);
        assertEquals(11, dispPageCountBuf[0]);
    }
}