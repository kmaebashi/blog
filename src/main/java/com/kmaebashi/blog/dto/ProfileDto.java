package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

public class ProfileDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("NICKNAME")
    public String nickname;

    @TableColumn("IMAGE_PATH")
    public String imagePath;

    @TableColumn("ABOUT_ME")
    public String aboutMe;
}
