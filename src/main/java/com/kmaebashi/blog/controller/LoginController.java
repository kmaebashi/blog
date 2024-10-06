package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.LoginService;
import com.kmaebashi.blog.service.Util;
import com.kmaebashi.blog.util.RandomIdGenerator;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.PlainTextResult;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import com.kmaebashi.blog.common.SessionKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class LoginController {
    private LoginController() {};

    public static RoutingResult showPage(ControllerInvoker invoker, String path) {
        return invoker.invoke((context) -> {
            RoutingResult result
                    = LoginService.showPage(context.getServiceInvoker());
            return result;
        });
    }

    public static RoutingResult checkPassword(ControllerInvoker invoker) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            boolean checkResult =LoginService.checkPassword(context.getServiceInvoker(),
                                                    request.getParameter("userid"),
                                                    request.getParameter("password"));
            return new PlainTextResult(checkResult ? "OK" : "NG");
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
                session.setAttribute(SessionKey.CURRENT_USER_ID, request.getParameter("userid").trim());
                session.setAttribute(SessionKey.CSRF_TOKEN, RandomIdGenerator.getRandomId());
                returnUrl = (String)session.getAttribute(SessionKey.RETURN_URL);
                session.removeAttribute(SessionKey.RETURN_URL);
                if (returnUrl == null) {
                    return new RedirectResult(request.getContextPath() + "/blog_list");
                } else {
                    return new RedirectResult(request.getContextPath() + "/" + returnUrl);
                }
            } else {
                return result;
            }
        });
    }
}
