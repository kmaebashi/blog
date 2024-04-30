package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

public class PhotoDto {
    @TableColumn("PHOTO_ID")
    public int photoId;

    @TableColumn("BLOG_POST_ID")
    public Integer blogPostId;

    @TableColumn("SECTION_NUMBER")
    public int sectionNumber;

    @TableColumn("PATH")
    public String path;

    @TableColumn("DISPLAY_ORDER")
    public int displayOrder;

    @TableColumn("CAPTION")
    public String caption;
}
