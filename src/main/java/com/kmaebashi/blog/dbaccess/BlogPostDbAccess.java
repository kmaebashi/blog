package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class BlogPostDbAccess {
    private BlogPostDbAccess() {
    }

    public static List<BlogPostDto> getBlogPostForAdmin(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                    ORDER BY POSTED_DATE DESC
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<BlogPostDto> dtoList = ResultSetMapper.toDtoList(rs, BlogPostDto.class);
            return dtoList;
        });
   }
}