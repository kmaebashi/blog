package com.kmaebashi.blog;

import java.sql.DriverManager;
import java.sql.Connection;

public class BlogTestUtil {
    private BlogTestUtil() {}

    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/blogdb?currentSchema=blog",
                "bloguser", "XXXXXX");
        return conn;
    }
}
