package com.kmaebashi.blog.controller;

import com.kmaebashi.blog.service.AdminService;
import com.kmaebashi.blog.service.ImageService;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.RoutingResult;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ImageController {
    private ImageController() {}

    public static RoutingResult postImages(ControllerInvoker invoker, Path imageRoot) {
        return invoker.invoke((context) -> {
            HttpServletRequest request = context.getServletRequest();
            Collection<Part> parts = request.getParts();
            String section = null;
            String blogId = null;
            List<Part> filePartList = new ArrayList<>();
            for (Part part: parts) {
                context.getLogger().info("part.name.." + part.getName());
                if (part.getName().equals("section")) {
                    section = request.getParameter("section");
                    context.getLogger().info("section.." + section);
                } else if (part.getName().equals("blogId")) {
                    blogId = request.getParameter("blogId");
                    context.getLogger().info("blogId.." + blogId);
                } else if (part.getName().startsWith("file")) {
                    filePartList.add(part);
                }
            }
            int sectionSeq = Integer.parseInt(section);
            ImageService.saveImages(context.getServiceInvoker(), blogId, sectionSeq, filePartList, imageRoot);

            return null;
        });
    }
}
