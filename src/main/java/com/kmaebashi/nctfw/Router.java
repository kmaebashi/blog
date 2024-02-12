package com.kmaebashi.nctfw;
import com.kmaebashi.nctfwimpl.ControllerInvokerImpl;
import com.kmaebashi.nctfwimpl.DbAccessContextImpl;
import com.kmaebashi.nctfwimpl.DbAccessInvokerImpl;
import com.kmaebashi.nctfwimpl.RequestContextImpl;
import com.kmaebashi.nctfwimpl.ServiceContextImpl;
import com.kmaebashi.nctfwimpl.ServiceInvokerImpl;
import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.Connection;

public abstract class Router {
    public void execute(HttpServletRequest request, HttpServletResponse response) {
        try (Connection conn = this.getConnection()) {
            Logger logger = this.getLogger();
            ControllerInvoker ci = this.createControllerInvoker(request, response, conn);
            String path = request.getRequestURI();
            logger.info("Router:path.." + path);
            String shortPath = path.replaceFirst("^/\\w+/", "").replaceFirst("\\.do$", "");
            logger.info("Router:shortPath.." + shortPath);
            RoutingResult result = this.doRouting(shortPath, ci, request);
            if (result == null) {
                this.getLogger().info("404 Not Found. path.." + path);
                showErrorPage(HttpServletResponse.SC_NOT_FOUND,
                              "ファイルが見つかりません(" + path.replaceFirst("\\.do$", "") + ")。", response);
            } else if (result instanceof DocumentResult doc) {
                String html = doc.getDocument().html();
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println(html);
                out.close();
            } else if (result instanceof RedirectResult redirect) {
                response.sendRedirect(redirect.getRedirectUrl());
            } else if (result instanceof PlainTextResult plainText) {
                response.setContentType(plainText.getContentType());
                if (plainText.getDownloadFilename() != null) {
                    response.setHeader("Content-Disposition", "attachment; filename=\""
                            + plainText.getDownloadFilename() + "\"");
                }
                PrintWriter out = response.getWriter();
                out.print(plainText.getText());
                out.close();
            }
        } catch (BadRequestException ex) {
            this.getLogger().info(ex.getMessage());
            showErrorPage(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), response);
        } catch (Exception ex) {
            this.getLogger().error("内部エラー。" + ex.toString());
            ex.printStackTrace();
            throw new InternalException("内部エラーです。", ex);
        }
    }

    private void showErrorPage(int statusCode, String message, HttpServletResponse response) {
        try {
            String htmlFile = null;
            if (statusCode == HttpServletResponse.SC_BAD_REQUEST) {
                htmlFile = "400error.html";
            } else if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
                htmlFile = "404error.html";
            }
            Path htmlPath = this.getHtmlTemplateDirectory().resolve(htmlFile);
            Document doc = Jsoup.parse(htmlPath.toFile(), "UTF-8");
            Element elem = doc.getElementById("message");
            elem.text(message);

            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(doc.html());
            out.close();
        } catch (Exception ex) {
            throw new InternalException("エラーページの出力に失敗。", ex);
        }
    }

    private ControllerInvoker createControllerInvoker(HttpServletRequest request, HttpServletResponse response,
                                                      Connection conn) {
        Logger logger = this.getLogger();
        DbAccessContext dac = new DbAccessContextImpl(conn, logger);
        DbAccessInvoker dai = new DbAccessInvokerImpl(dac);
        ServiceContext sc = new ServiceContextImpl(dai, this.getHtmlTemplateDirectory(), logger);
        ServiceInvoker si = new ServiceInvokerImpl(sc);
        RequestContext rc = new RequestContextImpl(si, request, response, logger);
        ControllerInvoker ci = new ControllerInvokerImpl(rc);

        return ci;
    }

    public abstract RoutingResult doRouting(String path, ControllerInvoker invoker, HttpServletRequest request);
    public abstract Connection getConnection() throws Exception;
    public abstract Logger getLogger();
    public abstract Path getHtmlTemplateDirectory();
}