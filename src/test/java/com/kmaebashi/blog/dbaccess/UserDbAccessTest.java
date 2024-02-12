package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.blog.dto.UserDto;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class UserDbAccessTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        UserDbAccessTest.conn = BlogTestUtil.getConnection();
        UserDbAccessTest.logger = new FileLogger("./log", "UserDbAccessTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void getUserTest001() {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        UserDto userDto = UserDbAccess.getUser(invoker, "kmaebashi");
        logger.info("userDto.password.." + userDto.password);
        assertEquals("kmaebashi", userDto.userId);
        assertEquals("$2a$10$mGsGdB63iwr6aFJ3e5LB.ua/LTkx5zJ/DTMFSBw.kTkhxzswuJJBy", userDto.password);
        assertEquals("PXU00211@nifty.ne.jp", userDto.mailAddress);
    }
}