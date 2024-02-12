package com.kmaebashi.dbutil;
import java.sql.*;
import java.util.Map;

import com.kmaebashi.dbutilimpl.NamedParameterPreparedStatementImpl;

public abstract class NamedParameterPreparedStatement {
    public static NamedParameterPreparedStatement newInstance(Connection conn, String sql)
            throws SQLException, SqlParseException  {
        return new NamedParameterPreparedStatementImpl(conn, sql);
    }

    abstract public void setParameters(Map<String, Object> params)
            throws SQLException, UnsupportedTypeException, ParameterValueNotFoundException;

    abstract public PreparedStatement getPreparedStatement();
}
