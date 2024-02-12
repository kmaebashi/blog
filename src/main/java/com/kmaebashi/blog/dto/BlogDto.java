package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

public class BlogDto {
    @TableColumn("BLOG_ID")
    public String blogId;

    @TableColumn("TITLE")
    public String title;

    @TableColumn("DESCRIPTION")
    public String description;

    @TableColumn("OWNER_USER")
    public String ownerUser;
}
