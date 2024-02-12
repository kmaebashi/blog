package com.kmaebashi.blog.router;

import com.kmaebashi.nctfw.BadRequestException;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SelectRoute {
    private SelectRoute() {}

    private static Pattern idPattern = Pattern.compile("^\\w+$");
    private static Pattern postPattern = Pattern.compile("^(\\w+)/post/(\\d+)$");
    private static Pattern adminPattern = Pattern.compile("^(\\w+)/admin$");
    private static Pattern adminPostPattern = Pattern.compile("(^\\w+)/admin/(\\d*)$");

    static Route select(String path, HashMap<String, Object> params) {
        try {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            Route route;

            if (idPattern.matcher(path).matches()) {
                params.put("blog_id", path);
                return Route.BLOG_TOP;
            }
            Matcher matcher = postPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.SHOW_POST;
            }
            matcher = adminPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                return Route.ADMIN;
            }
            matcher = adminPostPattern.matcher(path);
            if (matcher.matches()) {
                params.put("blog_id", matcher.group(1));
                String postIdStr = matcher.group(2);
                int postId = Integer.parseInt(postIdStr);
                params.put("blog_post_id", postId);
                return Route.ADMIN;
            }
            if (path.equals("api/dologin")) {
                return Route.DO_LOGIN;
            }
            if (path.equals("api/postimages")) {
                return Route.POST_IMAGES;
            }
        } catch (Exception ex) {
            throw new BadRequestException("クエリストリングが不正です。");
        }
        return Route.NO_ROUTE;
    }
}
