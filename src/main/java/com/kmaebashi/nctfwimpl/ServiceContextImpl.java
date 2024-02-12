package com.kmaebashi.nctfwimpl;
import com.kmaebashi.nctfw.DbAccessContext;
import com.kmaebashi.nctfw.DbAccessInvoker;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.simplelogger.Logger;

import java.nio.file.Path;

public class ServiceContextImpl implements ServiceContext {
    private DbAccessInvoker dbAccessInvoker;
    private Path htmlTemplateDirectory;
    private Logger logger;

    public ServiceContextImpl(DbAccessInvoker dbAccessInvoker, Path htmlTemplateDirectory, Logger logger) {
        this.dbAccessInvoker = dbAccessInvoker;
        this.htmlTemplateDirectory = htmlTemplateDirectory;
        this.logger = logger;
    }

    @Override
    public DbAccessInvoker getDbAccessInvoker() {
        return this.dbAccessInvoker;
    };

    @Override
    public Path getHtmlTemplateDirectory() {
        return this.htmlTemplateDirectory;
    };

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
