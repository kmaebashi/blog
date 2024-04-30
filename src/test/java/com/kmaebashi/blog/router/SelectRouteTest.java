package com.kmaebashi.blog.router;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SelectRouteTest {
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
        assertEquals(Route.BLOG_TOP, route);
        assertEquals("kmaebashi", params.get("blog_id"));
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
    void selectTest005() throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select("kmaebashi/admin/123", params);
        assertEquals(Route.EDIT_POST, route);
        assertEquals("kmaebashi", params.get("blog_id"));
        assertEquals(123, params.get("blog_post_id"));
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

}