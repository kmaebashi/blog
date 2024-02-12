package com.kmaebashi.blog.dto;
import com.kmaebashi.dbutil.TableColumn;

public class UserDto {
    @TableColumn("USER_ID")
    public String userId;

    @TableColumn("PASSWORD")
    public String password;

    @TableColumn("MAIL_ADDRESS")
    public String mailAddress;
}
