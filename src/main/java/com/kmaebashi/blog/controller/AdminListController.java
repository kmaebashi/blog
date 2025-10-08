package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.common.SessionKey;
import com.kmaebashi.blog.service.AdminListService;
import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.util.CsrfUtil;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public class AdminListController {
    private AdminListController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, Map<String, Object> params,
                                         String currentUserId) {
        return invoker.invoke((context) -> {
            String blogId = (String) params.get("blog_id");
            int page = 1;
            String pageStr = context.getServletRequest().getParameter("page");
            if (pageStr != null) {
                try {
                    page = Integer.valueOf(Integer.parseInt(pageStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("ページ番号が不正です(" + pageStr + ")");
                }
            }
            DocumentResult result
                    = AdminListService.showPage(context.getServiceInvoker(), blogId, page, currentUserId);

            HttpSession session = context.getServletRequest().getSession(false);
            if (session != null) {
                String csrfToken = (String) session.getAttribute(SessionKey.CSRF_TOKEN);
                CsrfUtil.addCsrfToken(result, csrfToken);
            }

            return result;
        });
    }
}
