package com.kmaebashi.blog.dto;

import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDateTime;

public class BlogPostSummaryDto {
    @TableColumn("BLOG_POST_ID")
    public int blogPostId;

    @TableColumn("TITLE")
    public String title;

    @TableColumn("POSTED_DATE")
    public LocalDateTime postedDate;

    @TableColumn("SECTION_TEXT")
    public String sectionText;

    @TableColumn("PHOTO_ID")
    public Integer photoId;
}
