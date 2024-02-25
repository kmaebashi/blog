package com.kmaebashi.blog.router;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.kmaebashi.blog.controller.ImageController;
import com.kmaebashi.blog.controller.LoginController;
import com.kmaebashi.blog.controller.AdminController;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
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

    public BlogRouter(ServletContext servletContext, Logger logger, ResourceBundle rb) {
        this.servletContext = servletContext;
        this.logger = logger;
        this.resourceBundle = rb;
        this.originalImageRoot = Paths.get(rb.getString("blog.original-image-directory"));
        this.resizedImageRoot = Paths.get(rb.getString("blog.resized-image-directory"));
    }

    @Override
    public RoutingResult doRouting(String path, ControllerInvoker invoker, HttpServletRequest request) {
        this.logger.info("doRouting start. path.." + path);
        this.logger.info("method.." + request.getMethod());

        RoutingResult result = null;

        HttpSession session = request.getSession();
        String currentUserId = (String)session.getAttribute("current_user_id");

        HashMap<String, Object> params = new HashMap<>();
        Route route = SelectRoute.select(path, params);
        if (route == Route.NO_ROUTE) {
            throw new BadRequestException("URLが不正です。");
        }
        this.logger.info("currentUserId.." + currentUserId);
        this.logger.info("route.." + route);
        if (request.getMethod().equals("GET")) {
            if (route == Route.ADMIN) {
                if (currentUserId == null) {
                    result = LoginController.showPage(invoker, path);
                } else {
                    result = AdminController.showPage(invoker, params);
                }
            } else if (route == Route.GET_IMAGE_ADMIN && currentUserId != null) {
                int photoId = (int)params.get("photo_id");
                String blogId = (String)params.get("blog_id");
                result = ImageController.getImageAdmin(invoker, photoId, blogId, this.resizedImageRoot);
            }
        } else if (request.getMethod().equals("POST")) {
            if (route == Route.DO_LOGIN) {
                result = LoginController.doLogin(invoker);
            } else if (route == Route.POST_IMAGES && currentUserId != null) {
                result = ImageController.postImages(invoker, currentUserId, (String)params.get("blog_id"),
                                                    this.originalImageRoot, this.resizedImageRoot);
            }
        }
        this.logger.info("doRouting end.");

        return result;
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
