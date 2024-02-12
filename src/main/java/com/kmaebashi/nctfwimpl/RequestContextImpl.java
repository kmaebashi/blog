package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.RequestContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestContextImpl implements RequestContext {
    ServiceInvoker serviceInvoker;
    HttpServletRequest request;
    HttpServletResponse response;
    Logger logger;

    public RequestContextImpl(ServiceInvoker serviceInvoker,
                              HttpServletRequest request, HttpServletResponse response,
                              Logger logger) {
        this.serviceInvoker = serviceInvoker;
        this.request = request;
        this.response = response;
        this.logger = logger;
    }

    @Override
    public ServiceInvoker getServiceInvoker() {
        return this.serviceInvoker;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        return this.request;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        return this.response;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
