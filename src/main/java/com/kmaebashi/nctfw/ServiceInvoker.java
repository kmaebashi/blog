package com.kmaebashi.nctfw;

import java.util.function.Consumer;

public interface ServiceInvoker {
    <R> R invoke(ThrowableFunction<ServiceContext, R> logic, InvokerOption ... options);
}
