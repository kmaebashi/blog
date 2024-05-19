package com.kmaebashi.blog.servlet;

import com.kmaebashi.blog.router.BlogRouter;
import com.kmaebashi.blog.util.Log;
import com.kmaebashi.nctfw.InternalException;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ResourceBundle;

@MultipartConfig
public class BlogServlet extends HttpServlet {
    private BlogRouter router;
    private Logger logger;
    public void init() {
        ResourceBundle rb = ResourceBundle.getBundle("application");
        String logDirectory = rb.getString("blog.log-directory");
        try {
            this.logger = new FileLogger(logDirectory, "BlogLog");
        } catch (IOException ex) {
            throw new InternalException("ログファイルの作成に失敗しました。", ex);
        }
        Log.setLogger(logger);
        this.router = new BlogRouter(this.getServletContext(), this.logger, rb);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Servlet.serice start." + request.getRequestURI());

        this.router.execute(request, response);

        logger.info("Servlet.serice end." + request.getRequestURI());
    }}
