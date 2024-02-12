package com.kmaebashi.nctfw;

import com.kmaebashi.simplelogger.Logger;

import java.nio.file.Path;

public interface ServiceContext {
    DbAccessInvoker getDbAccessInvoker();
    Path getHtmlTemplateDirectory();
    Logger getLogger();
}
