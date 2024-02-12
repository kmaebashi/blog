package com.kmaebashi.simpleloggerimpl;
import com.kmaebashi.simplelogger.LogLevel;
import com.kmaebashi.simplelogger.Logger;
import com.kmaebashi.simplelogger.SimpleLoggerException;

import java.io.BufferedWriter;
import java.io.File;
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
    DateTimeFormatter suffixFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String logDir;
    String filePrefix;
    String currentSuffix;

    public FileLogger(String dir, String filePrefix) throws IOException {
        this.currentSuffix = LocalDateTime.now().format(suffixFormatter);
        this.writer = openWriter(dir, filePrefix, this.currentSuffix);

        this.logDir = dir;
        this.filePrefix = filePrefix;
    }

    private static BufferedWriter openWriter(String dir, String filePrefix, String suffix) throws IOException {
        Path logFilePath = Paths.get(dir, filePrefix + "_" + suffix + ".log");
        return Files.newBufferedWriter(logFilePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    }

    public synchronized void debug(String message) {
        write(LogLevel.DEBUG, message);
    }
    public synchronized void info(String message) {
        write(LogLevel.INFO, message);
    }
    public synchronized void warn(String message) {
        write(LogLevel.WARN, message);
    }
    public synchronized void error(String message) {
        write(LogLevel.ERROR, message);
    }
    public synchronized void fatal(String message) {
        write(LogLevel.FATAL, message);
    }
    public synchronized void setLogLevel(LogLevel logLevel) {
        this.currentLogLevel = logLevel;
    }

    private void write(LogLevel level, String message) {
        if (level.compareTo(this.currentLogLevel) < 0) {
            return;
        }
        try {
            logRotate();
            LocalDateTime now = LocalDateTime.now();
            this.writer.append(now.format(this.dateTimeFormatter) + ",");

            this.writer.append(level.toString() + ",");

            this.writer.append("" + Thread.currentThread().getId() + ",");

            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            this.writer.append(ste[3].getClassName() + ".");
            this.writer.append(ste[3].getMethodName());
            if (ste[2].getFileName() != null) {
                this.writer.append(" in " + ste[3].getFileName());
            }
            if (ste[2].getLineNumber() >= 0) {
                this.writer.append(" at " + ste[3].getLineNumber());
            }
            this.writer.append(",");
            this.writer.append(message);
            this.writer.newLine();
            this.writer.flush();
        } catch (IOException ex) {
            throw new SimpleLoggerException("ログ出力時にエラーが発生しました。", ex);
        }
    }

    private void logRotate() throws IOException {
        String nextSuffix = LocalDateTime.now().format(suffixFormatter);
        if (!this.currentSuffix.equals(nextSuffix)) {
            this.currentSuffix = nextSuffix;
            this.writer.close();
            this.writer = openWriter(this.logDir, this.filePrefix, this.currentSuffix);
        }
    }
}
