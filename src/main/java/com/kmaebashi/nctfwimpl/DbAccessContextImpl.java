package com.kmaebashi.nctfwimpl;
import java.sql.Connection;
import com.kmaebashi.nctfw.*;
import com.kmaebashi.simplelogger.Logger;

public class DbAccessContextImpl implements DbAccessContext {
    private Connection conn;
    private Logger logger;

    public DbAccessContextImpl(Connection conn, Logger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    @Override
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
