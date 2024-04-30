package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.service.BlogListService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;

import java.util.Map;

public class BlogListController {
    private BlogListController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, String userId) {
        return invoker.invoke((context) -> {
            RoutingResult result
                    = BlogListService.showPage(context.getServiceInvoker(), userId);
            return result;
        });
    }
}
