package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

public class BlogPostSectionDto {
    @TableColumn("BLOG_POST_ID")
    public int blogPostId;

    @TableColumn("SECTION_SEQ")
    public int sectionSeq;

    @TableColumn("BODY")
    public String body;
}
