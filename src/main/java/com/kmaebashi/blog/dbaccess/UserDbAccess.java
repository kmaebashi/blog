package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.UserDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class UserDbAccess {
    private UserDbAccess() {}

    public static UserDto getUser(DbAccessInvoker invoker, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM USERS
                    WHERE USER_ID = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            UserDto dto = ResultSetMapper.toDto(rs, UserDto.class);
            return dto;
        });
    }
}
