package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.common.SessionKey;
import com.kmaebashi.blog.controller.data.CommentData;
import com.kmaebashi.blog.service.CommentService;
import com.kmaebashi.blog.service.ShowPostService;
import com.kmaebashi.blog.util.CsrfUtil;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.HttpSession;

public class ShowPostController {
    private ShowPostController() {}

    public static RoutingResult showPostByPostId(ControllerInvoker invoker, String blogId, int blogPostId,
                                                 String currentUserId) {
        return invoker.invoke((context) -> {
            DocumentResult result
                    = ShowPostService.showPostByPostId(context.getServiceInvoker(), blogId, blogPostId, currentUserId);

            HttpSession session = context.getServletRequest().getSession(false);
            if (session != null) {
                String csrfToken = (String) session.getAttribute(SessionKey.CSRF_TOKEN);
                CsrfUtil.addCsrfToken(result, csrfToken);
            }
            return result;
        });
    }

    public static RoutingResult postComment(ControllerInvoker invoker, String currentUserId, String blogId) {
        return invoker.invoke((context) -> {
            JsonResult result = null;

            if (!CsrfUtil.checkCsrfToken(context.getServletRequest(), false)) {
                throw new BadRequestException("CSRFトークン不正");
            }
            try (JsonParser jsonParser = JsonParser.newInstance(context.getServletRequest().getReader())) {
                JsonElement elem = jsonParser.parse();
                CommentData comment = ClassMapper.toObject(elem, CommentData.class);
                if (!blogId.equals(comment.blogId)) {
                    throw new BadRequestException("URLのブログID(" + blogId
                                                    + ")とPOSTされたブログID(" + comment.blogId + ")が一致しません。");
                }
                result = CommentService.postComment(context.getServiceInvoker(),
                                                    currentUserId, blogId, comment);
            }
            return result;
        });
    }
}
