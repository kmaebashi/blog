package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.util.HashMap;

public class CommentDbAccess {
    public static int insertComment(DbAccessInvoker invoker, int blogPostId,
                                  String posterUserId, String posterName, String message) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO BLOG_POST_COMMENTS (
                      BLOG_POST_ID,
                      COMMENT_ID,
                      POSTER_ID,
                      POSTER_NAME,
                      MESSAGE,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :BLOG_POST_ID,
                      COALESCE((SELECT MAX(COMMENT_ID) FROM BLOG_POST_COMMENTS
                               WHERE BLOG_POST_ID = :BLOG_POST_ID) + 1,
                               1) ,
                      :POSTER_ID,
                      :POSTER_NAME,
                      :MESSAGE,
                      now(),
                      now()
                    )
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("BLOG_POST_ID", blogPostId);
            params.put("POSTER_ID", posterUserId);
            params.put("POSTER_NAME", posterName);
            params.put("MESSAGE", message);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }
}
