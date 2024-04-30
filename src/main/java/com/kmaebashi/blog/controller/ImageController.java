package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.ImageService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.ImageFileResult;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImageController {
    private ImageController() {}

    public static RoutingResult postImages(ControllerInvoker invoker, String currentUserId, String blogId,
                                           Path originalImageRoot, Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            Collection<Part> parts = request.getParts();
            String section = null;
            List<Part> filePartList = new ArrayList<>();
            for (Part part: parts) {
                context.getLogger().info("part.name.." + part.getName());
                if (part.getName().equals("section")) {
                    section = request.getParameter("section");
                    context.getLogger().info("section.." + section);
                } else if (part.getName().startsWith("file")) {
                    filePartList.add(part);
                }
            }
            int sectionSeq = Integer.parseInt(section);
            JsonResult result = ImageService.saveImages(context.getServiceInvoker(), currentUserId, blogId,
                                                        sectionSeq, filePartList,
                                                        originalImageRoot, resizedImageRoot);

            return result;
        });
    }

    public static RoutingResult getImageAdmin(ControllerInvoker invoker, int photoId, String blogId,
                                              Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            return ImageService.getImageAdmin(context.getServiceInvoker(), photoId, blogId, resizedImageRoot);
        });
    }

    public static RoutingResult getImage(ControllerInvoker invoker, int photoId, String blogId, int blogPostId,
                                              Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            return ImageService.getImage(context.getServiceInvoker(), photoId, blogId, blogPostId, resizedImageRoot);
        });
    }
}
