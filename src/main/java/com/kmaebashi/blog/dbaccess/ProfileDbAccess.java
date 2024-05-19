package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.ProfileDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.ResultSet;
import java.util.HashMap;

public class ProfileDbAccess {
    private ProfileDbAccess() {}

    public static ProfileDto getProfileImageByBlogId(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      PROFILES.IMAGE_PATH
                    FROM PROFILES
                    INNER JOIN BLOGS
                      ON BLOGS.OWNER_USER = PROFILES.USER_ID
                    WHERE BLOGS.BLOG_ID = :BLOG_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            ProfileDto dto = ResultSetMapper.toDto(rs, ProfileDto.class);
            return dto;
        });
    }

    public static ProfileDto getProfileByUserId(DbAccessInvoker invoker, String userId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM PROFILES
                    WHERE USER_ID = :USER_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("USER_ID", userId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            ProfileDto dto = ResultSetMapper.toDto(rs, ProfileDto.class);
            return dto;
        });
    }
}
