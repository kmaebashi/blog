package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.service.LoginService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public class AdminController {
    private AdminController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, Map<String, Object> params) {
        return invoker.invoke((context) -> {
            String blogId = (String)params.get("blog_id");
            Integer blogPostId = (Integer)params.get("blog_post_id");
            context.getLogger().info("blogId.." + blogId);
            context.getLogger().info("blogPostId.." + blogPostId);
            RoutingResult result
                    = AdminService.showPage(context.getServiceInvoker(), blogId, blogPostId);
            return result;
        });
    }

    public static RoutingResult postArticle(ControllerInvoker invoker, String currentUserId, String blogId) {
        return invoker.invoke((context) -> {
            return null;
        });
    }
}
