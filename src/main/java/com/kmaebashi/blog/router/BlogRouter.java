package com.kmaebashi.blog.router;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.kmaebashi.blog.common.SessionKey;
import com.kmaebashi.blog.controller.BlogListController;
import com.kmaebashi.blog.controller.ImageController;
import com.kmaebashi.blog.controller.LoginController;
import com.kmaebashi.blog.controller.AdminController;
import com.kmaebashi.blog.controller.ShowPostController;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RedirectResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.kmaebashi.nctfw.Router;
import com.kmaebashi.simplelogger.Logger;

public class BlogRouter extends Router {
    private ServletContext servletContext;
    private Logger logger;
    private ResourceBundle resourceBundle;
    private Path originalImageRoot;
    private Path resizedImageRoot;
    private Path originalProfileImageRoot;
    private Path resizedProfileImageRoot;

    public BlogRouter(ServletContext servletContext, Logger logger, ResourceBundle rb) {
        this.servletContext = servletContext;
        this.logger = logger;
        this.resourceBundle = rb;
        this.originalImageRoot = Paths.get(rb.getString("blog.original-image-directory"));
        this.resizedImageRoot = Paths.get(rb.getString("blog.resized-image-directory"));
        this.originalProfileImageRoot = Paths.get(rb.getString("blog.original-profile-image-directory"));
        this.resizedProfileImageRoot = Paths.get(rb.getString("blog.resized-profile-image-directory"));
    }

    @Override
    public RoutingResult doRouting(String path, ControllerInvoker invoker, HttpServletRequest request) {
        this.logger.info("HttpServletRequest test start.");
        this.logger.info("getQueryString.." + request.getQueryString());
        this.logger.info("getRequestURI.." + request.getRequestURI());
        this.logger.info("getRequestURL.." + request.getRequestURL());
        this.logger.info("getServletPath.." + request.getServletPath());
        this.logger.info("getContextPath.." + request.getContextPath());
        this.logger.info("getPathInfo.." + request.getPathInfo());
        this.logger.info("getPathTranslated.." + request.getPathTranslated());

        this.logger.info("HttpServletRequest test end.");
        this.logger.info("doRouting start. path.." + path);
        this.logger.info("method.." + request.getMethod());

        RoutingResult result = null;

        HttpSession session = request.getSession();
        String currentUserId = (String)session.getAttribute(SessionKey.CURRENT_USER_ID);

        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select(path, params);
        if (route == Route.NO_ROUTE) {
            throw new BadRequestException("URLが不正です。");
        }
        this.logger.info("currentUserId.." + currentUserId);
        this.logger.info("route.." + route);

        if (request.getMethod().equals("GET")) {
            if (route == Route.BLOG_TOP) {
                String blogId = (String) params.get("blog_id");
                return ShowPostController.showPostsByBlogId(invoker, blogId);
            } else if (route == Route.POST_LIST_MONTHLY) {
                String blogId = (String) params.get("blog_id");
                String monthStr = (String) params.get("month");
                return ShowPostController.showPostsMonthly(invoker, blogId, monthStr);

            } else if (route == Route.POST_LIST_DAILY) {
                String blogId = (String) params.get("blog_id");
                String dateStr = (String) params.get("date");
                return ShowPostController.showPostsDaily(invoker, blogId, dateStr);

            } else if (route == Route.SHOW_POST) {
                String blogId = (String) params.get("blog_id");
                int blogPostId = (int) params.get("blog_post_id");
                return ShowPostController.showPostByPostId(invoker, blogId, blogPostId, currentUserId);
            } else if (route == Route.GET_IMAGE) {
                int photoId = (int)params.get("photo_id");
                String blogId = (String)params.get("blog_id");
                int blogPostId = (int)params.get("blog_post_id");

                return ImageController.getImage(invoker, photoId, blogId, blogPostId, this.resizedImageRoot);

            } else if (route == Route.GET_PROFILE_IMAGE) {
                String blogId = (String)params.get("blog_id");

                return ImageController.getProfileImage(invoker, blogId, this.resizedProfileImageRoot);
            } else if (route == Route.LOGIN) {
                result = LoginController.showPage(invoker, path);
            } else if (route == Route.GET_POST_COUNT_EACH_DAY) {
                logger.info("route == Route.GET_POST_COUNT_EACH_DAY");
                String blogId = (String)params.get("blog_id");
                result = ShowPostController.getPostCountEachDay(invoker, blogId);
            } else if (route == Route.BLOG_LIST) {
                if (currentUserId == null) {
                    session.setAttribute(SessionKey.RETURN_URL, createReturnPath(request, path));
                    String loginPath = request.getContextPath() + "/login";
                    this.logger.info("loginPath.." + loginPath);
                    return new RedirectResult(loginPath);
                } else {
                    result = BlogListController.showPage(invoker, currentUserId);
                }
            } else if (route == Route.ADMIN) {
                if (currentUserId == null) {
                    session.setAttribute(SessionKey.RETURN_URL, createReturnPath(request, path));
                    String loginPath = request.getContextPath() + "/login";
                    this.logger.info("loginPath.." + loginPath);
                    return new RedirectResult(loginPath);
                } else {
                    result = AdminController.showPage(invoker, params);
                }
            } else if (route == Route.GET_IMAGE_ADMIN && currentUserId != null) {
                int photoId = (int)params.get("photo_id");
                String blogId = (String)params.get("blog_id");
                result = ImageController.getImageAdmin(invoker, photoId, blogId, this.resizedImageRoot);
            }
        } else if (request.getMethod().equals("POST")) {
            if (route == Route.CHECK_PASSWORD) {
                result = LoginController.checkPassword(invoker);
            } else if (route == Route.DO_LOGIN) {
                result = LoginController.doLogin(invoker);
            } else if (route == Route.POST_IMAGES && currentUserId != null) {
                result = ImageController.postImages(invoker, currentUserId, (String)params.get("blog_id"),
                                                    this.originalImageRoot, this.resizedImageRoot);
            } else if (route == Route.POST_ARTICLE && currentUserId != null) {
                result = AdminController.postArticle(invoker, currentUserId, (String)params.get("blog_id"));
            } else if (route == Route.POST_COMMENT) {
                result = ShowPostController.postComment(invoker, currentUserId, (String)params.get("blog_id"));
            }
        }
        this.logger.info("doRouting end.");

        return result;
    }

    private static String createReturnPath(HttpServletRequest request, String path)  {
        String returnUrl;
        if (request.getQueryString() == null) {
            returnUrl = path;
        } else {
            returnUrl = path + "?" + request.getQueryString();
        }
        return returnUrl;
    }

    @Override
    public Connection getConnection() throws Exception {
        Context context = new InitialContext();
        DataSource ds = (DataSource)context.lookup("java:comp/env/jdbc/blog");
        Connection conn = ds.getConnection();

        return  conn;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Path getHtmlTemplateDirectory() {
        return Paths.get(this.servletContext.getRealPath("WEB-INF/htmltemplate"));
    }
}
