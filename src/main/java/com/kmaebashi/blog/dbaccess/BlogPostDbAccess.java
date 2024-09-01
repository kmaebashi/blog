package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.common.BlogPostStatus;
import com.kmaebashi.blog.dto.BlogPostCountEachDayDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.BlogPostSectionDto;
import com.kmaebashi.blog.dto.BlogPostSummaryDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.CommentDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class BlogPostDbAccess {
    private BlogPostDbAccess() {
    }

    public static List<BlogPostDto> getBlogPostList(DbAccessInvoker invoker, String blogId, int offset, int limit) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      *
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND STATUS = 2
                    ORDER BY POSTED_DATE DESC
                    OFFSET :OFFSET
                    LIMIT :LIMIT
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            params.put("OFFSET", offset);
            params.put("LIMIT", limit);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<BlogPostDto> dtoList = ResultSetMapper.toDtoList(rs, BlogPostDto.class);
            return dtoList;
        });
    }

    public static List<BlogPostSummaryDto> getBlogPostSummaryList(DbAccessInvoker invoker, String blogId,
                                                         LocalDate fromDate, LocalDate toDate, int offset, int limit) {
        return invoker.invoke((context) -> {
            String sql1 = """
                    SELECT
                      POST.BLOG_POST_ID,
                      POST.TITLE,
                      POST.POSTED_DATE,
                      SEC.BODY AS SECTION_TEXT,
                      PHOTOS.PHOTO_ID
                    FROM BLOG_POSTS POST
                    LEFT OUTER JOIN BLOG_POST_SECTIONS SEC
                      ON POST.BLOG_POST_ID = SEC.BLOG_POST_ID
                    LEFT OUTER JOIN PHOTOS
                      ON PHOTOS.PHOTO_ID = (
                        SELECT PHOTO_ID FROM PHOTOS
                        WHERE PHOTOS.BLOG_POST_ID = POST.BLOG_POST_ID
                        ORDER BY SECTION_NUMBER, DISPLAY_ORDER
                        LIMIT 1
                      )
                    WHERE
                      POST.BLOG_ID = :BLOG_ID
                      AND SEC.SECTION_SEQ = 0
                      AND STATUS = 2
                      """;
            String rangeSql = """
                      AND POST.POSTED_DATE BETWEEN :FROM_DATE AND :TO_DATE
                    """;
            String sql2 = """
                    ORDER BY POST.POSTED_DATE DESC
                    OFFSET :OFFSET
                    LIMIT :LIMIT
                    """;
            String sql;
            var params = new HashMap<String, Object>();
            if (fromDate != null) {
                sql = sql1 + rangeSql + sql2;
                params.put("FROM_DATE", fromDate);
                params.put("TO_DATE", toDate);
            } else {
                sql = sql1 + sql2;
            }
            params.put("BLOG_ID", blogId);
            params.put("OFFSET", offset);
            params.put("LIMIT", limit);

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            npps.setParameters(params);

            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<BlogPostSummaryDto> dtoList = ResultSetMapper.toDtoList(rs, BlogPostSummaryDto.class);
            return dtoList;
        });
    }

    public static int getBlogPostCountByBlogId(DbAccessInvoker invoker, String blogId, LocalDate startDate, LocalDate endDate) {
        return invoker.invoke((context) -> {
            String sql1 = """
                    SELECT
                      COUNT(*)
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND STATUS = 2
                    """;
            String rangeSql = """
                      AND BLOG_POSTS.POSTED_DATE BETWEEN :FROM_DATE AND :TO_DATE 
                    """;
            String sql;
            if (startDate == null) {
                sql = sql1;
            } else {
                sql = sql1 + rangeSql;
            }

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            if (startDate != null) {
                params.put("FROM_DATE", startDate);
                params.put("TO_DATE", endDate);
            }
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            rs.next();
            int count = rs.getInt("COUNT");

            return count;
        });
    }

    public static List<BlogPostCountEachDayDto> getBlogPostCountByMonth(DbAccessInvoker invoker, String blogId, LocalDate month) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      CAST(POSTED_DATE AS DATE) AS GROUP_DATE,
                      COUNT(*) AS NUM_OF_POSTS
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND POSTED_DATE BETWEEN :FROM_DATE AND :TO_DATE
                      AND STATUS = 2
                    GROUP BY GROUP_DATE
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            params.put("FROM_DATE", month);
            params.put("TO_DATE", month.plusMonths(1));
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();

            List<BlogPostCountEachDayDto> dtoList = ResultSetMapper.toDtoList(rs, BlogPostCountEachDayDto.class);
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
                                          int photoId, String blogId, int blogPostId, int displayOrder,
                                          String caption) {
        return invoker.invoke((context) -> {
            String sql = """
                    UPDATE PHOTOS SET
                      BLOG_POST_ID = :BLOG_POST_ID,
                      DISPLAY_ORDER = :DISPLAY_ORDER,
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
            params.put("DISPLAY_ORDER", displayOrder);
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

    public static BlogPostDto getOlderBlogPost(DbAccessInvoker invoker, String blogId, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      BLOG_POST_ID,
                      TITLE
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND BLOG_POST_ID < :BLOG_POST_ID
                      AND STATUS = 2
                    ORDER BY BLOG_POST_ID DESC
                    LIMIT 1
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            BlogPostDto blogPostDto = ResultSetMapper.toDto(rs, BlogPostDto.class);

            return blogPostDto;
        });
    }

    public static BlogPostDto getNewerBlogPost(DbAccessInvoker invoker, String blogId, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      BLOG_POST_ID,
                      TITLE
                    FROM BLOG_POSTS
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND BLOG_POST_ID > :BLOG_POST_ID
                      AND STATUS = 2
                    ORDER BY BLOG_POST_ID ASC
                    LIMIT 1
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            BlogPostDto blogPostDto = ResultSetMapper.toDto(rs, BlogPostDto.class);

            return blogPostDto;
        });
    }

    public static List<CommentDto> getCommentsByBlogId(DbAccessInvoker invoker, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      BLOG_POST_COMMENTS.BLOG_POST_ID,
                      COMMENT_ID,
                      POSTER_ID,
                      POSTER_NAME,
                      MESSAGE,
                      BLOG_POSTS.TITLE BLOG_POST_TITLE
                    FROM BLOG_POST_COMMENTS
                    INNER JOIN BLOG_POSTS
                      ON BLOG_POSTS.BLOG_POST_ID = BLOG_POST_COMMENTS.BLOG_POST_ID
                    WHERE BLOG_POSTS.BLOG_ID = :BLOG_ID
                    ORDER BY BLOG_POST_COMMENTS.CREATED_AT DESC
                    LIMIT 10
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<CommentDto> dtoList = ResultSetMapper.toDtoList(rs, CommentDto.class);

            return dtoList;
        });
    }

    public static List<CommentDto> getCommentsByBlogPostId(DbAccessInvoker invoker, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT
                      COMMENT_ID,
                      POSTER_ID,
                      POSTER_NAME,
                      MESSAGE,
                      CREATED_AT
                    FROM BLOG_POST_COMMENTS
                    WHERE BLOG_POST_ID = :BLOG_POST_ID
                    ORDER BY CREATED_AT ASC
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            List<CommentDto> dtoList = ResultSetMapper.toDtoList(rs, CommentDto.class);

            return dtoList;
        });
    }

    public static int updateBlogPost(DbAccessInvoker invoker, int blogPostId, String blogId, String title,
                                     LocalDateTime postedDate, BlogPostStatus status) {
        return invoker.invoke((context) -> {
            String sql = """
                    UPDATE BLOG_POSTS SET
                      TITLE = :TITLE,
                      POSTED_DATE = :POSTED_DATE,
                      STATUS = :STATUS,
                      UPDATED_AT = now()
                    WHERE
                      BLOG_ID = :BLOG_ID
                      AND BLOG_POST_ID = :BLOG_POST_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("TITLE", title);
            params.put("POSTED_DATE", postedDate);
            params.put("STATUS", status.intValue());
            params.put("BLOG_POST_ID", blogPostId);
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);

            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static int deleteAllSections(DbAccessInvoker invoker, int blogPostId) {
        return invoker.invoke((context) -> {
            String sql = """
                    DELETE FROM BLOG_POST_SECTIONS
                    WHERE BLOG_POST_ID = :BLOG_POST_ID                     
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            npps.setParameters(params);

            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

}