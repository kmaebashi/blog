package com.kmaebashi.blog.controller.data;

public class BlogPostCountEachDay {
    public int day;
    public int numOfPosts;

    public BlogPostCountEachDay(int day, int numOfPosts) {
        this.day = day;
        this.numOfPosts = numOfPosts;
    }
}
