package com.kmaebashi.dbutil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultSetMapper {
    private ResultSetMapper() {
    }

    public static <T> T toDto(ResultSet rs, Class<T> dtoClass)
        throws SQLException, InstantiationException, IllegalAccessException, UnsupportedTypeException,
            MultipleMatchException, NoSuchMethodException, InvocationTargetException {
        List<T> list = toDtoList(rs, dtoClass);
        if (list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            throw new MultipleMatchException("" + list.size() + "件検索されました。");
        } else {
            return list.get(0);
        }
    }

    public static <T> List<T> toDtoList(ResultSet rs, Class<T> dtoClass)
        throws SQLException, InstantiationException, IllegalAccessException, UnsupportedTypeException,
            NoSuchMethodException, InvocationTargetException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();

        HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();
        Field[] fieldArray = dtoClass.getDeclaredFields();
        for (int i = 0; i < fieldArray.length; i++) {
            TableColumn tc = fieldArray[i].getAnnotation(TableColumn.class);
            if (tc != null)
            {
                nameToIndex.put(tc.value().toUpperCase(), i);
            }
        }

        List<T> list = new ArrayList<T>();

        while (rs.next()) {
            T dto = dtoClass.getDeclaredConstructor().newInstance();

            for (int i = 0; i < colCount; i++) {
                final int rsIdx = i + 1;
                String colName = rsmd.getColumnName(rsIdx).toUpperCase();
                if (!nameToIndex.containsKey(colName))
                    continue;

                int fieldIndex = nameToIndex.get(colName);

                int colType = rsmd.getColumnType(rsIdx);
                switch (colType) {
                    case Types.INTEGER:
                    case Types.BIGINT:
                        if (fieldArray[fieldIndex].getType() == Integer.TYPE) {
                            fieldArray[fieldIndex].setInt(dto, rs.getInt(rsIdx));
                        } else if (fieldArray[fieldIndex].getType() == Integer.class) {
                            int intValue = rs.getInt(rsIdx);
                            if (rs.wasNull()) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, intValue);
                            }
                        } else {
                            throw new UnsupportedTypeException("整数を型"
                                    + fieldArray[fieldIndex].getType().getTypeName()
                                    + "に変換できません(列:" + colName + ")。");
                        }
                        break;
                    case Types.REAL:
                        if (fieldArray[fieldIndex].getType() == Double.TYPE) {
                            fieldArray[fieldIndex].setDouble(dto, rs.getDouble(rsIdx));
                        } else if (fieldArray[fieldIndex].getType() == Double.class) {
                            double doubleValue = rs.getDouble(rsIdx);
                            if (rs.wasNull()) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, doubleValue);
                            }
                        } else {
                            throw new UnsupportedTypeException("実数を型"
                                    + fieldArray[fieldIndex].getType().getTypeName()
                                    + "に変換できません(列:" + colName + ")。");
                        }
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        if (fieldArray[fieldIndex].getType() == Boolean.TYPE) {
                            fieldArray[fieldIndex].setBoolean(dto, rs.getBoolean(rsIdx));
                        } else if (fieldArray[fieldIndex].getType() == Boolean.class) {
                            boolean boolValue = rs.getBoolean(rsIdx);
                            if (rs.wasNull()) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, boolValue);
                            }
                        } else {
                            throw new UnsupportedTypeException("ブーリアンを型"
                                    + fieldArray[fieldIndex].getType().getTypeName()
                                    + "に変換できません(列:" + colName + ")。");
                        }
                        break;
                    case Types.CHAR:
                        if (fieldArray[fieldIndex].getAnnotation(TableColumn.class).trim()) {
                            if (rs.getString(rsIdx) == null) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, rs.getString(rsIdx).trim());
                            }
                        } else {
                            fieldArray[fieldIndex].set(dto, rs.getString(rsIdx));
                        }
                        break;
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                        fieldArray[fieldIndex].set(dto, rs.getString(rsIdx));
                        break;
                    case Types.DATE:
                        java.sql.Date sqlDate = rs.getDate(rsIdx);
                        if (fieldArray[fieldIndex].getType() == java.util.Date.class) {
                            fieldArray[fieldIndex].set(dto, sqlDate);
                        } else if (fieldArray[fieldIndex].getType() == LocalDate.class) {
                            if (sqlDate == null) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, sqlDate.toLocalDate());
                            }
                        } else {
                            throw new UnsupportedTypeException("DATE型を型"
                                    + fieldArray[fieldIndex].getType().getTypeName()
                                    + "に変換できません(列:" + colName + ")。");
                        }
                        break;
                    case Types.TIMESTAMP:
                        java.sql.Timestamp sqlTimestamp = rs.getTimestamp(rsIdx);
                        if (fieldArray[fieldIndex].getType() == java.util.Date.class) {
                            fieldArray[fieldIndex].set(dto, sqlTimestamp);
                        } else if (fieldArray[fieldIndex].getType() == LocalDateTime.class) {
                            if (sqlTimestamp == null) {
                                fieldArray[fieldIndex].set(dto, null);
                            } else {
                                fieldArray[fieldIndex].set(dto, sqlTimestamp.toLocalDateTime());
                            }
                        } else {
                            throw new UnsupportedTypeException("TIMESTAMP型を型"
                                    + fieldArray[fieldIndex].getType().getTypeName()
                                    + "に変換できません(列:" + colName + ")。");
                        }
                        break;
                    default:
                        throw new UnsupportedTypeException("java.sql.Typesの" + colType + "は未対応です。");
                }
            }
            list.add(dto);
        }
        return list;
    }
}
