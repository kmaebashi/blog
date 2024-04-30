package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class BlogPostDbAccess {
    private BlogPostDbAccess() {
    }

    public static List<BlogPostDto> getBlogPostList(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                    ORDER BY POSTED_DATE DESC
                    LIMIT 10
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

    public static int getBlogPostSequence(DbAccessInvoker invoker) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT NEXTVAL('BLOG_POST_SEQUENCE')
                    """;
            PreparedStatement ps = context.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int nextVal = rs.getInt("NEXTVAL");

            return nextVal;
        });
    }

    public static int insertBlogPost(DbAccessInvoker invoker, int blogPostId, String blogId, String title,
                                     LocalDateTime postedDate, BlogPostStatus status) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO BLOG_POSTS (
                      BLOG_POST_ID,
                      BLOG_ID,
                      TITLE,
                      POSTED_DATE,
                      STATUS,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :BLOG_POST_ID,
                      :BLOG_ID,
                      :TITLE,
                      :POSTED_DATE,
                      :STATUS,
                      now(),
                      now()
                    )                
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            params.put("BLOG_ID", blogId);
            params.put("TITLE", title);
            params.put("POSTED_DATE", postedDate);
            params.put("STATUS", status.intValue());
            npps.setParameters(params);

            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int insertSection(DbAccessInvoker invoker, int blogPostId, int sectionSeq, String body) {
        return invoker.invoke((context) -> {
            String sql = """
                     INSERT INTO BLOG_POST_SECTIONS (
                       BLOG_POST_ID,
                       SECTION_SEQ,
                       BODY,
                       CREATED_AT,
                       UPDATED_AT
                     ) VALUES (
                       :BLOG_POST_ID,
                       :SECTION_SEQ,
                       :BODY,
                       now(),
                       now()
                     )            
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            params.put("SECTION_SEQ", sectionSeq);
            params.put("BODY", body);
            npps.setParameters(params);

            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int linkPhotoToBlogPost(DbAccessInvoker invoker,
                                          int photoId, String blogId, int blogPostId, String caption) {
        return invoker.invoke((context) -> {
            String sql = """
                    UPDATE PHOTOS SET
                      BLOG_POST_ID = :BLOG_POST_ID,
                      CAPTION = :CAPTION,
                      UPDATED_AT = now()
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND PHOTO_ID = :PHOTO_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            params.put("CAPTION", caption);
            params.put("BLOG_ID", blogId);
            params.put("PHOTO_ID", photoId);
            npps.setParameters(params);

            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
   }

    public static BlogPostDto getBlogPost(DbAccessInvoker invoker, String blogId, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND BLOG_POST_ID = :BLOG_POST_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            BlogPostDto dto = ResultSetMapper.toDto(rs, BlogPostDto.class);

            return dto;
        });
    }

    public static List<BlogPostSectionDto> getBlogPostSection(DbAccessInvoker invoker, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOG_POST_SECTIONS
                    WHERE
                      BLOG_POST_ID = :BLOG_POST_ID
                    ORDER BY SECTION_SEQ
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<BlogPostSectionDto> dtoList = ResultSetMapper.toDtoList(rs, BlogPostSectionDto.class);

            return dtoList;
        });
    }

    public static List<PhotoDto> getBlogPostPhoto(DbAccessInvoker invoker, int blogPostId, int sectionNumber) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM PHOTOS
                    WHERE
                      BLOG_POST_ID = :BLOG_POST_ID
                      AND SECTION_NUMBER = :SECTION_NUMBER
                    ORDER BY DISPLAY_ORDER
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            params.put("SECTION_NUMBER", sectionNumber);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<PhotoDto> dtoList = ResultSetMapper.toDtoList(rs, PhotoDto.class);

            return dtoList;
        });
    }

}