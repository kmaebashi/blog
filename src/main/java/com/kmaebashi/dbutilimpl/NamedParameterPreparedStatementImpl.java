package com.kmaebashi.dbutilimpl;

import java.sql.*;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ParameterValueNotFoundException;
import com.kmaebashi.dbutil.SqlParseException;
import com.kmaebashi.dbutil.UnsupportedTypeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class NamedParameterPreparedStatementImpl extends NamedParameterPreparedStatement {
    private PreparedStatement preparedStatement;
    private String[] paramNames;

    public NamedParameterPreparedStatementImpl(Connection conn, String sql)
            throws SQLException, SqlParseException {
        SqlAndParams sqlAndParams = parseSql(sql);
        this.paramNames = sqlAndParams.paramNames;
        this.preparedStatement = conn.prepareStatement(sqlAndParams.sql);
    }

    @Override
    public void setParameters(Map<String, Object> params)
        throws SQLException, UnsupportedTypeException, ParameterValueNotFoundException {
        setParametersImpl(this.preparedStatement, this.paramNames, params);
    }

    @Override
    public PreparedStatement getPreparedStatement() {
        return this.preparedStatement;
    }

    private enum State {
        INITIAL,
        COLON,
        IN_PARAMETER,
        COMMENT_START,
        IN_COMMENT,
        C_STYLE_COMMENT_START,
        IN_C_STYLE_COMMENT,
        C_STYLE_COMMENT_END,
        IN_STRING,
        STRING_END
    }

    static SqlAndParams parseSql(String srcSql) throws SqlParseException {
        State state = State.INITIAL;
        StringBuilder sqlSB = new StringBuilder();
        StringBuilder param = null;
        ArrayList<String> paramList = new ArrayList<String>();

        for (int i = 0; i < srcSql.length(); i++) {
            char ch = srcSql.charAt(i);

            switch (state) {
                case INITIAL:
                    if (ch == ':') {
                        state = State.COLON;
                    } else if (ch == '-') {
                        state = State.COMMENT_START;
                        sqlSB.append(ch);
                    } else if (ch == '/') {
                        state = State.C_STYLE_COMMENT_START;
                        sqlSB.append(ch);
                    } else if (ch == '\'') {
                        state = State.IN_STRING;
                        sqlSB.append(ch);
                    } else {
                        sqlSB.append(ch);
                    }
                    break;
                case COLON:
                    if (Character.isJavaIdentifierStart(ch)) {
                        param = new StringBuilder();
                        param.append(ch);
                        state = State.IN_PARAMETER;
                    } else {
                        throw new SqlParseException(":の後ろに識別子がありません。");
                    }
                    break;
                case IN_PARAMETER:
                    if (Character.isJavaIdentifierPart(ch)) {
                        param.append(ch);
                    } else {
                        paramList.add(param.toString());
                        sqlSB.append('?');
                        sqlSB.append(ch);
                        state = State.INITIAL;
                    }
                    break;
                case COMMENT_START:
                    if (ch == '-') {
                        state = State.IN_COMMENT;
                    } else {
                        state = State.INITIAL;
                    }
                    sqlSB.append(ch);
                    break;
                case IN_COMMENT:
                    if (ch == '\n') {
                        state = State.INITIAL;
                    }
                    sqlSB.append(ch);
                    break;
                case C_STYLE_COMMENT_START:
                    if (ch == '*') {
                        state = State.IN_C_STYLE_COMMENT;
                    } else {
                        state = State.INITIAL;
                    }
                    sqlSB.append(ch);
                    break;
                case IN_C_STYLE_COMMENT:
                    if (ch == '*') {
                        state = State.C_STYLE_COMMENT_END;
                    }
                    sqlSB.append(ch);
                    break;
                case C_STYLE_COMMENT_END:
                    if (ch == '/') {
                        state = State.INITIAL;
                    } else {
                        state = State.IN_COMMENT;
                    }
                    sqlSB.append(ch);
                    break;
                case IN_STRING:
                    if (ch == '\'') {
                        state = State.STRING_END;
                    }
                    sqlSB.append(ch);
                    break;
                case STRING_END:
                    if (ch == '\'') {
                        state = State.IN_STRING;
                        sqlSB.append(ch);
                    } else {
                        if (ch == ':') {
                            state = State.COLON;
                        } else if (ch == '-') {
                            state = State.COMMENT_START;
                            sqlSB.append(ch);
                        } else if (ch == '/') {
                            state = State.C_STYLE_COMMENT_START;
                            sqlSB.append(ch);
                        } else {
                            sqlSB.append(ch);
                        }
                    }
            }
        }
        return new SqlAndParams(sqlSB.toString(), paramList.toArray(new String[0]));
    }

    static void setParametersImpl(PreparedStatement ps, String[] paramNames, Map<String, Object> paramValues)
        throws SQLException, UnsupportedTypeException, ParameterValueNotFoundException {
        for (int i = 0; i < paramNames.length; i++) {
            if (!paramValues.containsKey(paramNames[i])) {
                throw new ParameterValueNotFoundException("パラメタ" + paramNames[i] + "の値が見つかりません。");
            }
            Object value = paramValues.get(paramNames[i]);
            if (value instanceof Integer intValue) {
                ps.setInt(i + 1, intValue.intValue());
            } else if (value instanceof Double doubleValue) {
                ps.setDouble(i + 1, doubleValue);
            } else if (value instanceof Boolean boolValue) {
                ps.setBoolean(i + 1, boolValue);
            } else if (value instanceof String strValue) {
                ps.setString(i + 1, strValue);
            } else if (value instanceof java.sql.Date dateValue) {
                ps.setDate(i + 1, dateValue);
            } else if (value instanceof LocalDate dateValue) {
                ps.setDate(i + 1, java.sql.Date.valueOf(dateValue));
            } else if (value instanceof java.sql.Timestamp timestampValue) {
                ps.setTimestamp(i + 1, timestampValue);
            } else if (value instanceof LocalDateTime dateTimeValue) {
                ps.setTimestamp(i + 1, java.sql.Timestamp.valueOf(dateTimeValue));
            } else if (value == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                throw new UnsupportedTypeException("型"+ value.getClass().getName() + "はサポートしていません。"
                        + "必要に応じて書き足してください。");
            }
        }
    }
}
