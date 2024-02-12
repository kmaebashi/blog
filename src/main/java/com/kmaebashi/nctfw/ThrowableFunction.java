package com.kmaebashi.nctfw;

@FunctionalInterface
public interface ThrowableFunction<T, R> {
    R apply(T t) throws Exception;
}
