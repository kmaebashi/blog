package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;
import java.time.LocalDateTime;

public class BlogPostDto {
    @TableColumn("BLOG_POST_ID")
    public int blogPostId;

    @TableColumn("BLOG_ID")
    public String blogId;

    @TableColumn("TITLE")
    public String title;

    @TableColumn("POSTED_DATE")
    public LocalDateTime postedDate;

    @TableColumn("STATUS")
    public int status;
}
