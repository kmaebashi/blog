package com.kmaebashi.blog.servlet;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class StaticResourceFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.length() == 0 || path.equals("/")) {
            request.getRequestDispatcher("/index.html").forward(request, response);
        } else if (path.endsWith(".css") || path.endsWith(".jpg")) {
            chain.doFilter(request, response);
        } else {
            request.getRequestDispatcher(path + ".do").forward(request, response);
        }
    }
}
