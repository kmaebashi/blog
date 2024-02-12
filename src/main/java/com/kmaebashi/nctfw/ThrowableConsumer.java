package com.kmaebashi.nctfw;

@FunctionalInterface
public interface ThrowableConsumer<T> {
    void accept(T t) throws Exception;
}
