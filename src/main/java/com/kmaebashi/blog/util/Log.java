package com.kmaebashi.blog.util;
import com.kmaebashi.simplelogger.LogLevel;
import com.kmaebashi.simplelogger.Logger;

public class Log {
    private Log() {}

    private static Logger logger;

    public static void setLogger(Logger newLogger) {
        Log.logger = newLogger;
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }
    public static void error(String message) {
        logger.error(message);
    }

    public static void fatal(String message) {
        logger.fatal(message);
    }
}
