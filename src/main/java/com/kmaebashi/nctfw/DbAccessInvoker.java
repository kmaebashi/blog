package com.kmaebashi.nctfw;

import java.util.function.Consumer;

public interface DbAccessInvoker {
    <R> R invoke(ThrowableFunction<DbAccessContext, R> logic);
}
