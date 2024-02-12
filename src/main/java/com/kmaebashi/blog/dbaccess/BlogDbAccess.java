package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.UserDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class BlogDbAccess {
    private BlogDbAccess() {}

    public static BlogDto getBlog(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOGS
                    WHERE BLOG_ID = :BLOG_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            BlogDto dto = ResultSetMapper.toDto(rs, BlogDto.class);
            return dto;
        });
    }

}
