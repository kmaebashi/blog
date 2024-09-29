package com.kmaebashi.blog.service;

import static org.junit.jupiter.api.Assertions.*;
import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.PlainTextResult;
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

class RssServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        RssServiceTest.conn = BlogTestUtil.getConnection();
        RssServiceTest.logger = new FileLogger("./log", "RssServiceTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void createRssTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        PlainTextResult ptr = RssService.createRss(si, "kmaebashiblog", "http://localhost:8080/blog");
        String xml = ptr.getText();
    }
}