package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.LoginService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class LoginController {
    private LoginController() {};

    public static RoutingResult showPage(ControllerInvoker invoker, String path) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("return_url", path);
            RoutingResult result
                    = LoginService.showPage(context.getServiceInvoker());
            return result;
        });
    }

    public static RoutingResult doLogin(ControllerInvoker invoker) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            RoutingResult result
                    = LoginService.doLogin(context.getServiceInvoker(),
                                           request.getParameter("userid"),
                                           request.getParameter("password"));
            if (result == null) {
                String returnUrl = null;
                HttpSession session = request.getSession(true);
                session.setAttribute("current_user_id", request.getParameter("userid").trim());
                returnUrl = (String)session.getAttribute("return_url");
                return new RedirectResult("/blog/" +returnUrl);
            } else {
                return result;
            }
        });
    }
}
