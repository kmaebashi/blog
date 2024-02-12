package com.kmaebashi.nctfw;

public interface ControllerInvoker {
    RoutingResult invoke(ThrowableFunction<RequestContext, RoutingResult> logic);
}
