package com.kmaebashi.blog.router;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;

import com.kmaebashi.blog.controller.LoginController;
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

    public BlogRouter(ServletContext servletContext, Logger logger) {
        this.servletContext = servletContext;
        this.logger = logger;
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
                }
            }
        } else if (request.getMethod().equals("POST")) {
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
