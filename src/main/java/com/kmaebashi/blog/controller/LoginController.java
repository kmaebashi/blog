package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.LoginService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;

public class LoginController {
    private LoginController() {};

    public static RoutingResult showPage(ControllerInvoker invoker, String path) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            RoutingResult result
                    = LoginService.showPage(context.getServiceInvoker());
            return result;
        });
    }
}
