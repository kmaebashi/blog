package com.kmaebashi.blog.dto;

import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class BlogPostSearchDto {
    @TableColumn("BLOG_POST_ID")
    public int blogPostId;

    @TableColumn("TITLE")
    public String title;

    @TableColumn("POSTED_DATE")
    public LocalDateTime postedDate;

    @TableColumn("BODY_CONCAT")
    public String bodyConcat;

    @TableColumn("TOTAL_COUNT")
    public int totalCount;
}
