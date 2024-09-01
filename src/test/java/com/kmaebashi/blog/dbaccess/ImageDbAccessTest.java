package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.UserDto;
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

class ImageDbAccessTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        ImageDbAccessTest.conn = BlogTestUtil.getConnection();
        ImageDbAccessTest.logger = new FileLogger("./log", "ImageDbAccessTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void getPhotoSequenceTest001() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        int nextVal = ImageDbAccess.getPhotoSequence(invoker);
        logger.info("new photo sequence.." + nextVal);
    }

    @Test
    void insertPhotoTest001() throws Exception {
        DbAccessContext context = new DbAccessContextImpl(this.conn, this.logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(context);

        int photoId = ImageDbAccess.getPhotoSequence(invoker);
        int ret = ImageDbAccess.insertPhoto(invoker, photoId, "kmaebashiblog",
                                            1, "kmaebashiblog/20240224/P000000001.jpg");
        assertEquals(1, ret);

        PhotoDto dto = ImageDbAccess.getPhotoAdmin(invoker, photoId, "kmaebashiblog");
    }
}