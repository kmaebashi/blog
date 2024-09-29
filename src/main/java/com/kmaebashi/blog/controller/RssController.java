package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.RssService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;

public class RssController {
    private RssController() {}

    public static RoutingResult getRss(ControllerInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String url = context.getServletRequest().getRequestURL().toString();
            context.getLogger().info("url.." + url);
            url = url.replaceFirst("/\\w+/rss\\.do$", "");
            context.getLogger().info("url2.." + url);

            return RssService.createRss(context.getServiceInvoker(), blogId, url);
        });
    }
}
