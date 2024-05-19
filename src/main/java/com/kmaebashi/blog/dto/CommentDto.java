package com.kmaebashi.blog.dto;

import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class CommentDto {
    @TableColumn("BLOG_POST_ID")
    public int blogPostId;

    @TableColumn("COMMENT_ID")
    public int commentId;

    @TableColumn("POSTER_ID")
    public String posterId;

    @TableColumn("POSTER_NAME")
    public String posterName;

    @TableColumn("MESSAGE")
    public String message;

    @TableColumn("CREATED_AT")
    public LocalDateTime createdAt;

    @TableColumn("BLOG_POST_TITLE")
    public String blogPostTitle;
}
