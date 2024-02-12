package com.kmaebashi.dbutilimpl;

class SqlAndParams {
    String sql;
    String[] paramNames;

    SqlAndParams(String sql, String[] paramNames) {
        this.sql = sql;
        this.paramNames = paramNames;
    }
}
