package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDate;

public class BlogPostCountEachDayDto {
    @TableColumn("GROUP_DATE")
    public LocalDate postedDate;

    @TableColumn("NUM_OF_POSTS")
    public int numOfPosts;
}
