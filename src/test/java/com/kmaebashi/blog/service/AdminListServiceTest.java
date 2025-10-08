package com.kmaebashi.blog.service;

import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.nctfw.*;
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

import static org.junit.jupiter.api.Assertions.*;

class AdminListServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        AdminListServiceTest.conn = BlogTestUtil.getConnection();
        AdminListServiceTest.logger = new FileLogger("./log", "AdminListServiceTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void showPageTest001() throws Exception {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        DocumentResult dr = AdminListService.showPage(si, "kmaebashiblog", 1, "kmaebashi");
        String html = dr.getDocument().html();
    }
}