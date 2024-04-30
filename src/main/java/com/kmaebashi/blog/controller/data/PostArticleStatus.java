package com.kmaebashi.blog.controller.data;

import com.kmaebashi.blog.common.ApiStatus;

public class PostArticleStatus {
    public String status;
    public String message;
    public int blogPostId;

    public PostArticleStatus(ApiStatus status, String message, int blogPostId) {
        this.status = status.toString().toLowerCase();
        this.message = message;
        this.blogPostId = blogPostId;
    }
}
