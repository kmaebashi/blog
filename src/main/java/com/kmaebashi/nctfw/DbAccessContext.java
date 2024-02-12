package com.kmaebashi.nctfw;
import com.kmaebashi.simplelogger.Logger;

import java.sql.Connection;

public interface DbAccessContext {
    Connection getConnection();
    Logger getLogger();
}
