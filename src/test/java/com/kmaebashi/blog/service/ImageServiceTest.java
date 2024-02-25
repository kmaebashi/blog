package com.kmaebashi.blog.service;

import com.kmaebashi.blog.BlogTestUtil;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {
    private static Connection conn;
    private static Logger logger;

    @BeforeAll
    static void connectDb() throws Exception {
        ImageServiceTest.conn = BlogTestUtil.getConnection();
        ImageServiceTest.logger = new FileLogger("./log", "ImageServiceTest");
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void saveImagesTest001() {
        DbAccessContext dc = new DbAccessContextImpl(this.conn, logger);
        DbAccessInvoker invoker = new DbAccessInvokerImpl(dc);
        ServiceContext sc = new ServiceContextImpl(invoker,
                Paths.get("./src/main/resources/htmltemplate"),
                logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        List<Part> partList = new ArrayList<>();
        Path p = Paths.get("test_input/test_image/horizontal.jpg");
        String absolutePath = p.toAbsolutePath().toString();
        partList.add(new PartTestImpl(logger, "horizontal.jpg", Paths.get("test_input/test_image/horizontal.jpg")));
        partList.add(new PartTestImpl(logger, "vertical.jpg", Paths.get("test_input/test_image/vertical.jpg")));
        JsonResult jr = ImageService.saveImages(si, "kmaebashi", "kmaebashiblog", 1, partList,
                        Paths.get("./OriginalImageRoot"), Paths.get("./ResizedImageRoot"));
        logger.info("JSON.." + jr.getJson());
    }
}