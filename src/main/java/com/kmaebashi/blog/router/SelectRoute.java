package com.kmaebashi.blog.router;

import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.blog.util.Log;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SelectRoute {
    private SelectRoute() {}

    private static Pattern blogTopPattern = Pattern.compile("^\\w+$");
    private static Pattern monthlyPattern = Pattern.compile("^(\\w+)/(\\d\\d\\d\\d\\d\\d)$");
    private static Pattern dailyPattern = Pattern.compile("^(\\w+)/(\\d\\d\\d\\d\\d\\d\\d\\d)$");
    private static Pattern postPattern = Pattern.compile("^(\\w+)/post/(\\d+)$");
    private static Pattern previewPostPattern = Pattern.compile("^(\\w+)/previewpost/(\\d+)$");
    private static Pattern getImagePattern = Pattern.compile("^(\\w+)/api/getimage/(\\d+)/(\\d+)$");
    private static Pattern getOriginalSizeImagePattern = Pattern.compile("^(\\w+)/api/getorgsizeimage/(\\d+)/(\\d+)$");
    private static Pattern getProfileImagePattern = Pattern.compile("^(\\w+)/api/getprofileimage$");
    private static Pattern adminPattern = Pattern.compile("^(\\w+)/admin$");
    private static Pattern getPostCountEachDayPattern = Pattern.compile("^(\\w+)/api/getpostcounteachday$");
    private static Pattern postImagesPattern = Pattern.compile("^(\\w+)/api/postimages$");
    private static Pattern getImageAdminPattern = Pattern.compile("^(\\w+)/api/getimageadmin/(\\d+)$");
    private static Pattern postArticlePattern = Pattern.compile("^(\\w+)/api/postarticle$");
    private static Pattern postCommentPattern = Pattern.compile("^(\\w+)/api/postcomment$");
    private static Pattern rssPattern = Pattern.compile("^(\\w+)/rss$");

    static Route select(String path, HashMap<String, Object> params) {
        try {
            Log.info("path.." + path);
            if (path.endsWith("/")) {
                return Route.REDIRECT_REMOVE_SLASH;
            }
            if (path.equals("login")) {
                return Route.LOGIN;
            }
            if (path.equals("blog_list")) {
                return Route.BLOG_LIST;
            }
            Matcher matcher = blogTopPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", path);
                return Route.BLOG_TOP;
            }
            matcher = monthlyPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                params.put("month", matcher.group(2));
                return Route.POST_LIST_MONTHLY;
            }
            matcher = dailyPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                params.put("date", matcher.group(2));
                return Route.POST_LIST_DAILY;
            }
            matcher = postPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.SHOW_POST;
            }
            matcher = previewPostPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.PREVIEW_POST;
            }            matcher = getImagePattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                String blogPostIdStr = matcher.group(2);
                int blogPostId = Integer.parseInt(blogPostIdStr);
                params.put("blog_post_id", blogPostId);
                String photoIdStr = matcher.group(3);
                int photoId = Integer.parseInt(photoIdStr);
                params.put("photo_id", photoId);
                return Route.GET_IMAGE;
            }
            matcher = getOriginalSizeImagePattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                String blogPostIdStr = matcher.group(2);
                int blogPostId = Integer.parseInt(blogPostIdStr);
                params.put("blog_post_id", blogPostId);
                String photoIdStr = matcher.group(3);
                int photoId = Integer.parseInt(photoIdStr);
                params.put("photo_id", photoId);
                return Route.GET_ORIGINAL_SIZE_IMAGE;
            }
            matcher = getProfileImagePattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.GET_PROFILE_IMAGE;
            }
            matcher = adminPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                return Route.ADMIN;
            }
            matcher = getPostCountEachDayPattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.GET_POST_COUNT_EACH_DAY;
            }
            matcher = postImagesPattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.POST_IMAGES;
            }
            matcher = getImageAdminPattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                String photoIdStr = matcher.group(2);
                int photoId = Integer.parseInt(photoIdStr);
                params.put("photo_id", photoId);
                return Route.GET_IMAGE_ADMIN;
            }
            matcher = postArticlePattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.POST_ARTICLE;
            }
            matcher = postCommentPattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.POST_COMMENT;
            }
            if (path.equals("api/checkpassword")) {
                return Route.CHECK_PASSWORD;
            }
            if (path.equals("api/dologin")) {
                return Route.DO_LOGIN;
            }
            matcher = rssPattern.matcher(path);
            if (matcher.matches()) {
                String blogId = matcher.group(1);
                params.put("blog_id", blogId);
                return Route.RSS;
            }
        } catch (Exception ex) {
            throw new BadRequestException("クエリストリングが不正です。");
        }
        return Route.NO_ROUTE;
    }
}
