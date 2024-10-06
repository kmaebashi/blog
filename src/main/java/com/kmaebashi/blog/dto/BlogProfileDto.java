package com.kmaebashi.blog.dto;

import com.kmaebashi.dbutil.TableColumn;

public class BlogProfileDto {
    @TableColumn("BLOG_ID")
    public String blogId;

    @TableColumn("OWNER_USER")
    public String ownerUser;

    @TableColumn("TITLE")
    public String title;

    @TableColumn("DESCRIPTION")
    public String description;

    @TableColumn("NICKNAME")
    public String nickname;

    @TableColumn("IMAGE_PATH")
    public String imagePath;

    @TableColumn("ABOUT_ME")
    public String aboutMe;
}
