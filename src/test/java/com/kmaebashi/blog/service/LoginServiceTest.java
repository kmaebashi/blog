package com.kmaebashi.blog.service;

import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import com.kmaebashi.blog.BlogTestUtil;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        LoginServiceTest.conn = BlogTestUtil.getConnection();
        LoginServiceTest.logger = new FileLogger("./log", "LoginServiceTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void showPageTest001() throws Exception {
        //LoginService.showPage();
    }

    @Test
    void checkPasswordTest001() throws Exception {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker, null, logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        boolean result = LoginService.checkPassword(si, "aa", "bb");
        assertEquals(false, result);
    }

    @Test
    void checkPasswordTest002() throws Exception {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker, null, logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        boolean result = LoginService.checkPassword(si, "kmaebashi", "testpass/123");
        assertEquals(true, result);
    }

    @Test
    void doLogin() {
    }
}