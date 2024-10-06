package com.kmaebashi.blog.router;

import com.kmaebashi.blog.util.Log;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SelectRouteTest {
    @BeforeAll
    static void createLogger() throws Exception {
       Log.setLogger(new FileLogger("./log", "SelectRouteTest"));
    }

    @Test
    void selectTest001() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi", params);
        assertEquals(Route.BLOG_TOP, route);
        assertEquals("kmaebashi", params.get("blog_id"));
    }

    @Test
    void selectTest002() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/", params);
        assertEquals(Route.REDIRECT_REMOVE_SLASH, route);
    }

    @Test
    void selectTest003() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/post/123", params);
        assertEquals(Route.SHOW_POST, route);
        assertEquals("kmaebashi", params.get("blog_id"));
        assertEquals(123, params.get("blog_post_id"));
    }

    @Test
    void selectTest004() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/admin", params);
        assertEquals(Route.ADMIN, route);
        assertEquals("kmaebashi", params.get("blog_id"));
    }

    @Test
    void selectTest006() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("api/dologin", params);
        assertEquals(Route.DO_LOGIN, route);
    }

    @Test
    void selectTest007() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/api/postimages", params);
        assertEquals(Route.POST_IMAGES, route);
        assertEquals("kmaebashi", params.get("blog_id"));
    }

    @Test
    void selectTest008() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/api/getimageadmin/28", params);
        assertEquals(Route.GET_IMAGE_ADMIN, route);
        assertEquals("kmaebashi", params.get("blog_id"));
        int photoId = (int)params.get("photo_id");
        assertEquals(28, photoId);
    }
    @Test
    void selectTest009() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("login", params);
        assertEquals(Route.LOGIN, route);
    }

    @Test
    void selectTest010() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/202407", params);
        assertEquals(Route.POST_LIST_MONTHLY, route);
        String blogId = (String)params.get("blog_id");
        assertEquals("kmaebashi", blogId);
        String month = (String)params.get("month");
        assertEquals("202407", month);
    }

    @Test
    void selectTest011() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/20240721", params);
        assertEquals(Route.POST_LIST_DAILY, route);
        String blogId = (String)params.get("blog_id");
        assertEquals("kmaebashi", blogId);
        String date = (String)params.get("date");
        assertEquals("20240721", date);
    }

    @Test
    void selectTest012() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/api/getpostcounteachday", params);
        assertEquals(Route.GET_POST_COUNT_EACH_DAY, route);
        String blogId = (String)params.get("blog_id");
        assertEquals("kmaebashi", blogId);
    }
}