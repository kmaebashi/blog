package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.blog.dto.BlogPostCountEachDayDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSearchDto;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlogPostDbAccessTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        BlogPostDbAccessTest.conn = BlogTestUtil.getConnection();
        BlogPostDbAccessTest.logger = new FileLogger("./log", "BlogPostDbAccessTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void getBlogPostCountByBlogId001() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        int count = BlogPostDbAccess.getBlogPostCountByBlogId(invoker, "kmaebashiblog", null, null);
        logger.info("blog post count.." + count);
    }

    @Test
    void getBlogPostCountByBlogId002() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fromDate = LocalDate.parse("20240501", dateFormat);
        LocalDate toDate = LocalDate.parse("20240601", dateFormat);
        int count = BlogPostDbAccess.getBlogPostCountByBlogId(invoker, "kmaebashiblog", fromDate, toDate);
        logger.info("blog post count.." + count);
    }

    @Test
    void getBlogPostCountByMonth001() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fromDate = LocalDate.parse("20240501", dateFormat);
        List<BlogPostCountEachDayDto> dtoList
                = BlogPostDbAccess.getBlogPostCountByMonth(invoker, "kmaebashiblog", fromDate);
    }

    @Test
    void searchBlogPostsByTitle001() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        List<String> keywordList = new ArrayList<>();
        keywordList.add("煌蓮玖");
        keywordList.add("翔禄冴");
        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPostsByTitle(invoker,"kmaebashiblog", keywordList, 0, 100);
        assertEquals(1, dtoList.size());
    }

    @Test
    void searchBlogPosts001() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        List<String> keywordList = new ArrayList<>();
        keywordList.add("鳳斎瑠");
        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPosts(invoker,"kmaebashiblog", keywordList, true, 0, 100);
        assertEquals(3, dtoList.size());
    }

    @Test
    void searchBlogPosts002() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        List<String> keywordList = new ArrayList<>();
        keywordList.add("鳳斎瑠");
        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPosts(invoker,"kmaebashiblog", keywordList, false, 0, 100);
        assertEquals(2, dtoList.size());
    }

    @Test
    void searchBlogPosts003() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        List<String> keywordList = new ArrayList<>();
        keywordList.add("煌蓮玖");
        keywordList.add("翔禄冴");

        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPosts(invoker,"kmaebashiblog", keywordList, true, 0, 100);
        assertEquals(1, dtoList.size());
    }

    @Test
    void searchBlogPosts004() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        List<String> keywordList = new ArrayList<>();
        keywordList.add("煌蓮玖");
        keywordList.add("翔禄冴");
        List<BlogPostSearchDto> dtoList
                = BlogPostDbAccess.searchBlogPosts(invoker,"kmaebashiblog", keywordList, false, 0, 100);
        assertEquals(1, dtoList.size());
    }
}