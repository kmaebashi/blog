package com.kmaebashi.blog.controller.data;

import com.kmaebashi.blog.common.ApiStatus;

public class PostCommentStatus {
    public String status;
    public String message;

    public PostCommentStatus(ApiStatus status, String message) {
        this.status = status.toString().toLowerCase();
        this.message = message;
    }
}
