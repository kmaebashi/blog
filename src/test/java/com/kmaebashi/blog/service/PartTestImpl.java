package com.kmaebashi.blog.service;

import com.kmaebashi.simplelogger.Logger;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

class PartTestImpl implements Part {
    private Logger logger;
    private String fileName;
    private Path srcFilePath;

    @Override
    public void delete() {}
    @Override
    public String getContentType() {
        return null;
    }
    @Override
    public String getHeader(String name) {
        return null;
    }
    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }
    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }
    @Override
    public InputStream getInputStream() {
        return null;
    }
    @Override
    public String getName() {
        return null;
    }
    @Override
    public long getSize() {
        return 0;
    }
    @Override
    public String getSubmittedFileName() {
        return this.fileName;
    }
    @Override
    public void write(String fileName) throws IOException {
        this.logger.info("fileName.." + fileName);
        Files.copy(srcFilePath, Paths.get(fileName));
    }

    public PartTestImpl(Logger logger, String fileName, Path srcFilePath) {
        this.logger = logger;
        this.fileName = fileName;
        this.srcFilePath = srcFilePath;
    }
}
