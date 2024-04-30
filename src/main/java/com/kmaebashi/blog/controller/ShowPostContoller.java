package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.service.ShowPostService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;

import java.util.Map;

public class ShowPostContoller {
    private ShowPostContoller() {}

    public static RoutingResult showPostByPostId(ControllerInvoker invoker, String blogId, int blogPostId) {
        return invoker.invoke((context) -> {
            RoutingResult result
                    = ShowPostService.showPostByPostId(context.getServiceInvoker(), blogId, blogPostId);
            return result;
        });
    }
}
