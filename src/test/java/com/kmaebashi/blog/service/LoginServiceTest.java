package com.kmaebashi.blog.service;

import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

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
    void doLogin() {
    }
}