package com.kmaebashi.simpleloggerimpl;
import com.kmaebashi.simplelogger.LogLevel;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simplelogger.SimpleLoggerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Logger {
    LogLevel currentLogLevel = LogLevel.DEBUG;
    BufferedWriter writer;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
    DateTimeFormatter suffixDatePartFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String logDir;
    String filePrefix;
    String currentDatePart;

    public FileLogger(String dir, String filePrefix) throws IOException {
        this.currentDatePart = LocalDateTime.now().format(suffixDatePartFormatter);
        this.writer = openWriter(dir, filePrefix, this.currentDatePart);

        this.logDir = dir;
        this.filePrefix = filePrefix;
    }

    private static BufferedWriter openWriter(String dir, String filePrefix, String datePart) throws IOException {
        long pid = ProcessHandle.current().pid();
        Path logFilePath = Paths.get(dir, filePrefix + "_" + datePart + "_" + pid + ".log");
        return Files.newBufferedWriter(logFilePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    }

    public synchronized void debug(String message) {
        debug(message, 1);
    }
    public synchronized void info(String message) {
        info(message, 1);
    }
    public synchronized void warn(String message) {
        warn(message, 1);
    }
    public synchronized void error(String message) {
        error(message, 1);
    }
    public synchronized void fatal(String message) {
        fatal(message, 1);
    }

    public synchronized void debug(String message, int callDepth) {
        write(LogLevel.DEBUG, message, callDepth);
    }
    public synchronized void info(String message, int callDepth) {
        write(LogLevel.INFO, message, callDepth);
    }
    public synchronized void warn(String message, int callDepth) {
        write(LogLevel.WARN, message, callDepth);
    }
    public synchronized void error(String message, int callDepth) {
        write(LogLevel.ERROR, message, callDepth);
    }
    public synchronized void fatal(String message, int callDepth) {
        write(LogLevel.FATAL, message, callDepth);
    }
    public synchronized void setLogLevel(LogLevel logLevel) {
        this.currentLogLevel = logLevel;
    }

    private void write(LogLevel level, String message, int callDepth) {
        final int DEFAULT_CALL_DEPTH = 3;
        if (level.compareTo(this.currentLogLevel) < 0) {
            return;
        }
        try {
            int addedCallDepth = DEFAULT_CALL_DEPTH + callDepth;
            logRotate();
            LocalDateTime now = LocalDateTime.now();
            this.writer.append(now.format(this.dateTimeFormatter) + ",");

            this.writer.append(level.toString() + ",");

            this.writer.append("" + Thread.currentThread().threadId() + ",");

            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            this.writer.append(ste[addedCallDepth].getClassName() + ".");
            this.writer.append(ste[addedCallDepth].getMethodName());
            if (ste[2].getFileName() != null) {
                this.writer.append(" in " + ste[addedCallDepth].getFileName());
            }
            if (ste[2].getLineNumber() >= 0) {
                this.writer.append(" at " + ste[addedCallDepth].getLineNumber());
            }
            this.writer.append(",");
            this.writer.append("\"" + message.replace("\"", "\"\"") + "\"");
            this.writer.newLine();
            this.writer.flush();
        } catch (IOException ex) {
            throw new SimpleLoggerException("ログ出力時にエラーが発生しました。", ex);
        }
    }

    private void logRotate() throws IOException {
        String nextDatePart = LocalDateTime.now().format(suffixDatePartFormatter);
        if (!this.currentDatePart.equals(nextDatePart)) {
            this.currentDatePart = nextDatePart;
            this.writer.close();
            this.writer = openWriter(this.logDir, this.filePrefix, this.currentDatePart);
        }
    }
}
