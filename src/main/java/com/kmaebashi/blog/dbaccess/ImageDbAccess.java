package com.kmaebashi.blog.dbaccess;

import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.dbutil.ResultSetMapper;
import com.kmaebashi.nctfw.DbAccessInvoker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class ImageDbAccess {
    private ImageDbAccess() {
    }

    public static int getPhotoSequence(DbAccessInvoker invoker) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT NEXTVAL('PHOTO_SEQUENCE')
                    """;
            PreparedStatement ps = context.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int nextVal = rs.getInt("NEXTVAL");

            return nextVal;
        });
    }

    public static int insertPhoto(DbAccessInvoker invoker, int photoId, String blogId, int sectionNumber,
                                  String path, int displayOrder) {
        return invoker.invoke((context) -> {
            String sql = """
                    INSERT INTO PHOTOS (
                      PHOTO_ID,
                      BLOG_ID,
                      SECTION_NUMBER,
                      PATH,
                      DISPLAY_ORDER,
                      CREATED_AT,
                      UPDATED_AT
                    ) VALUES (
                      :PHOTO_ID,
                      :BLOG_ID,
                      :SECTION_NUMBER,
                      :PATH,
                      :DISPLAY_ORDER,
                      now(),
                      now()
                    )
                    """;

            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("PHOTO_ID", photoId);
            params.put("BLOG_ID", blogId);
            params.put("SECTION_NUMBER", sectionNumber);
            params.put("PATH", path);
            params.put("DISPLAY_ORDER", displayOrder);

            npps.setParameters(params);
            int result = npps.getPreparedStatement().executeUpdate();

            return result;
        });
    }

    public static PhotoDto getPhotoAdmin(DbAccessInvoker invoker, int photoId, String blogId) {
        return invoker.invoke((context) -> {
            String sql = """
                    SELECT * FROM PHOTOS
                    WHERE PHOTO_ID = :PHOTO_ID
                    AND BLOG_ID = :BLOG_ID
                    """;
            NamedParameterPreparedStatement npps
                    = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
            var params = new HashMap<String, Object>();
            params.put("PHOTO_ID", photoId);
            params.put("BLOG_ID", blogId);
            npps.setParameters(params);
            ResultSet rs = npps.getPreparedStatement().executeQuery();
            PhotoDto dto = ResultSetMapper.toDto(rs, PhotoDto.class);
            return dto;
        });
    }
}