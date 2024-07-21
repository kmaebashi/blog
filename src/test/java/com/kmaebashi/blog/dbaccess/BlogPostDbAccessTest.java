package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.BlogTestUtil;
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

import static org.junit.jupiter.api.Assertions.*;

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

        int count = BlogPostDbAccess.getBlogPostCountByBlogId(invoker, "kmaebashiblog");
        logger.info("blog post count.." + count);
    }
}