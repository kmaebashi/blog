package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.common.SessionKey;
import com.kmaebashi.blog.controller.data.ArticleData;
import com.kmaebashi.blog.controller.data.ArticlePhoto;
import com.kmaebashi.blog.controller.data.ArticleSection;
import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.service.LoginService;
import com.kmaebashi.blog.service.Util;
import com.kmaebashi.blog.util.CsrfUtil;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonParser;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.RoutingResult;
import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public class AdminController {
    private AdminController() {}

    public static RoutingResult showPage(ControllerInvoker invoker, Map<String, Object> params) {
        return invoker.invoke((context) -> {
            String blogId = (String)params.get("blog_id");
            Integer blogPostId = null;
            String postIdStr = context.getServletRequest().getParameter("postid");
            if (postIdStr != null) {
                try {
                    blogPostId = Integer.valueOf(Integer.parseInt(postIdStr));
                } catch (NumberFormatException ex) {
                    throw new BadRequestException("postidが不正です。");
                }
            }
            context.getLogger().info("blogId.." + blogId);
            context.getLogger().info("blogPostId.." + blogPostId);
            DocumentResult result
                    = AdminService.showPage(context.getServiceInvoker(), blogId, blogPostId);

            HttpSession session = context.getServletRequest().getSession(false);
            if (session != null) {
                String csrfToken = (String) session.getAttribute(SessionKey.CSRF_TOKEN);
                CsrfUtil.addCsrfToken(result, csrfToken);
            }

            return result;
        });
    }

    public static RoutingResult postArticle(ControllerInvoker invoker, String currentUserId, String blogId) {
        return invoker.invoke((context) -> {
            JsonResult result = null;
            if (!CsrfUtil.checkCsrfToken(context.getServletRequest(), true)) {
                throw new BadRequestException("CSRFトークン不正");
            }
            try (JsonParser jsonParser = JsonParser.newInstance(context.getServletRequest().getReader())) {
                JsonElement elem = jsonParser.parse();
                ArticleData article = ClassMapper.toObject(elem, ArticleData.class);
                result = AdminService.postArticle(context.getServiceInvoker(),
                                    currentUserId, blogId, article);

                Logger logger = context.getLogger();
                logger.info("article.title.." + article.title);
                logger.info("article.publishFlag.." + article.publishFlag);
                ArticleSection[] sections = article.sectionArray;
                logger.info("article.section count.." + article.sectionArray.length);
                for (int i = 0; i < sections.length; i++) {
                    logger.info("sections[" + i + "].id.." + sections[i].id);
                    logger.info("sections[" + i + "].body.." + sections[i].body);
                    ArticlePhoto[] photos = sections[i].photos;
                    logger.info("sections[" + i + "].photos.length.." + sections[i].photos.length);
                    for (int photoIdx = 0; photoIdx < sections[i].photos.length; photoIdx++) {
                        logger.info("photo[" + photoIdx + "].id.." + sections[i].photos[photoIdx].id);
                        logger.info("photo[" + photoIdx + "].caption.." + sections[i].photos[photoIdx].caption);
                    }
                }
            }
            return result;
        });
    }
}
