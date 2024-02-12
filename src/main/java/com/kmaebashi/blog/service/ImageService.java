package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import jakarta.servlet.http.Part;
import org.jsoup.nodes.Document;

import java.nio.file.Path;
import java.util.List;

public class ImageService {
    private ImageService() {}

    public static JsonResult saveImages(ServiceInvoker invoker, String blogId, int sectionSeq, List<Part> partList,
                                        Path imageRoot) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            if (blogDto == null) {
                throw new BadRequestException("ブログ" + blogId + "は存在しません");
            }


            return new JsonResult(null);
        });
    }

}

