package com.kmaebashi.nctfw;
import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RequestContext {
    ServiceInvoker getServiceInvoker();
    HttpServletRequest getServletRequest();
    HttpServletResponse getServletResponse();
    Logger getLogger();
}

