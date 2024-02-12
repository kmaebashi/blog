package com.kmaebashi.simplelogger;

public interface Logger {
    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message);
    void fatal(String message);
    void setLogLevel(LogLevel logLevel);
}
