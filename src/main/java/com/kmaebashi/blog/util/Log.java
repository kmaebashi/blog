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
        logger.debug(message, 1);
    }

    public static void info(String message) {
        logger.info(message, 1);
    }

    public static void warn(String message) {
        logger.warn(message, 1);
    }
    public static void error(String message) {
        logger.error(message, 1);
    }

    public static void fatal(String message) {
        logger.fatal(message, 1);
    }
}
