package com.kmaebashi.blog.common;

public enum BlogPostStatus {
    DRAFT(1),
    PUBLISHED(2)
    ;

    private final int status;

    private BlogPostStatus(final int status) {
        this.status = status;
    }

    public int intValue() {
        return this.status;
    }
}
