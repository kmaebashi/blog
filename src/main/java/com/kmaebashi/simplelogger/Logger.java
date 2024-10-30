package com.kmaebashi.simplelogger;
import com.kmaebashi.simpleloggerimpl.FileLogger;
import java.io.IOException;

public interface Logger {
    void debug(String message, int callDepth);
    void info(String message, int callDepth);
    void warn(String message, int callDepth);
    void error(String message, int callDepth);
    void fatal(String message, int callDepth);
    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message);
    void fatal(String message);
    void setLogLevel(LogLevel logLevel);

    public static Logger createFileLogger(String dir, String filePrefix)  throws IOException  {
        return new FileLogger(dir, filePrefix);
    }
}
