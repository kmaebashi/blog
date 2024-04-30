package com.kmaebashi.blog.router;

import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.simplelogger.Logger;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SelectRoute {
    private SelectRoute() {}

    private static Pattern idPattern = Pattern.compile("^\\w+$");
    private static Pattern postPattern = Pattern.compile("^(\\w+)/post/(\\d+)$");
    private static Pattern getImagePattern = Pattern.compile("^(\\w+)/api/getimage/(\\d+)/(\\d+)$");
    private static Pattern adminPattern = Pattern.compile("^(\\w+)/admin$");
    private static Pattern editPostPattern = Pattern.compile("^(\\w+)/admin\\?postid=(\\d+)$");
    private static Pattern postImagesPattern = Pattern.compile("^(\\w+)/api/postimages$");
    private static Pattern getImageAdminPattern = Pattern.compile("^(\\w+)/api/getimageadmin/(\\d+)$");
    private static Pattern postArticlePattern = Pattern.compile("^(\\w+)/api/postarticle$");

    static Route select(String path, HashMap<String, Object> params) {
        try {
            System.out.println("path.." + path);
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }



            System.out.println("path2.." + path);
            if (path.equals("login")) {
                return Route.LOGIN;
            }
            if (path.equals("blog_list")) {
                return Route.BLOG_LIST;
            }
            System.out.println("pass1");
            if (idPattern.matcher(path).matches()) {
                params.put("blog_id", path);
                return Route.BLOG_TOP;
            }
            System.out.println("pass2");
            Matcher matcher = postPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.SHOW_POST;
            }
            matcher = getImagePattern.matcher(path);
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
            matcher = adminPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                return Route.ADMIN;
            }
            matcher = editPostPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.EDIT_POST;
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
            if (path.equals("api/checkpassword")) {
                return Route.CHECK_PASSWORD;
            }
            if (path.equals("api/dologin")) {
                return Route.DO_LOGIN;
            }
        } catch (Exception ex) {
            throw new BadRequestException("クエリストリングが不正です。");
        }
        return Route.NO_ROUTE;
    }
}
