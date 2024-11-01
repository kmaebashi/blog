package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogProfileDto;
import com.kmaebashi.blog.dto.UserDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

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

    public static BlogProfileDto getBlogAndProfile(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      BLOGS.BLOG_ID,
                      BLOGS.OWNER_USER,
                      BLOGS.TITLE,
                      BLOGS.DESCRIPTION,
                      PROFILES.NICKNAME,
                      PROFILES.IMAGE_PATH,
                      PROFILES.ABOUT_ME
                    FROM BLOGS
                    LEFT OUTER JOIN PROFILES
                      ON BLOGS.OWNER_USER = PROFILES.USER_ID
                    WHERE BLOGS.BLOG_ID = :BLOG_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            BlogProfileDto dto = ResultSetMapper.toDto(rs, BlogProfileDto.class);
            return dto;
        });
    }

    public static List<BlogDto> getBlogsByUser(DbAccessInvoker invoker, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOGS
                    WHERE OWNER_USER = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<BlogDto> dtoList = ResultSetMapper.toDtoList(rs, BlogDto.class);

            return dtoList;
        });
    }

}
