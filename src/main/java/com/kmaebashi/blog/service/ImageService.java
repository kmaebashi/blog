package com.kmaebashi.blog.service;

import com.kmaebashi.blog.dbaccess.BlogDbAccess;
import com.kmaebashi.blog.dbaccess.BlogPostDbAccess;
import com.kmaebashi.blog.dbaccess.ImageDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.dto.BlogDto;
import com.kmaebashi.blog.dto.BlogPostDto;
import com.kmaebashi.blog.dto.PhotoDto;
import com.kmaebashi.blog.dto.ProfileDto;
import com.kmaebashi.jsonparser.JsonArray;
import com.kmaebashi.jsonparser.JsonElement;
import com.kmaebashi.jsonparser.JsonObject;
import com.kmaebashi.jsonparser.JsonValue;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.DocumentResult;
import com.kmaebashi.nctfw.ImageFileResult;
import com.kmaebashi.nctfw.InvokerOption;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceInvoker;
import jakarta.servlet.http.Part;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageService {
    private ImageService() {}
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static JsonResult saveImages(ServiceInvoker invoker, String currentUserId,
                                        String blogId, int sectionNumber, List<Part> partList,
                                        Path originalImageRoot, Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            BlogDto blogDto = BlogDbAccess.getBlog(context.getDbAccessInvoker(), blogId);
            if (blogDto == null && blogDto.ownerUser.equals(currentUserId)) {
                throw new BadRequestException("ブログ" + blogId + "が不正です");
            }
            LocalDateTime nowDate = LocalDateTime.now();
            Path originalImageDir = createImageDirectory(originalImageRoot, blogId, nowDate);
            Path resizedImageDir = createImageDirectory(resizedImageRoot, blogId, nowDate);

            int displayOrder = 1;
            List<JsonElement> imageList = new ArrayList<>();
            for (Part part : partList) {
                String srcFileName = part.getSubmittedFileName();
                if (srcFileName == null) {
                    throw new BadRequestException("画像のファイル名がありません");
                }
                String srcSuffix = Util.getSuffix(srcFileName).toLowerCase();
                String destSuffix;
                if (srcSuffix.equals("jpg") || srcSuffix.equals("jpeg")) {
                    destSuffix = "jpg";
                } else if (srcSuffix.equals("png")) {
                    destSuffix = "png";
                } else {
                    throw new BadRequestException("画像の拡張子が不正です(" + srcSuffix + ")");
                }
                int photoSequence = ImageDbAccess.getPhotoSequence(context.getDbAccessInvoker());
                String imageFileName = String.format("P%09d", photoSequence) + "." + destSuffix;
                Path orgImagePath = originalImageDir.resolve(imageFileName);
                part.write(orgImagePath.toString());

                ImageUtil.convertImage(orgImagePath, resizedImageDir.resolve(imageFileName), 500, destSuffix);

                int pathCount = orgImagePath.getNameCount();
                Path dbPath = orgImagePath.subpath(pathCount - 3, pathCount);
                int ret = ImageDbAccess.insertPhoto(context.getDbAccessInvoker(),
                                    photoSequence, blogId, sectionNumber, dbPath.toString(), displayOrder);
                displayOrder++;

                HashMap<String, JsonElement> imageMap = new HashMap<>();
                imageMap.put("id", JsonValue.createIntValue(photoSequence));
                imageList.add(JsonObject.newInstance(imageMap));
            }
            JsonArray array = JsonArray.newInstance(imageList);
            return new JsonResult(array.stringify());
        }, InvokerOption.TRANSACTIONAL);
    }

    private static Path createImageDirectory(Path rootDir, String blogId, LocalDateTime nowDate)
            throws IOException, BadRequestException {
        Path blogDir = rootDir.resolve(blogId);
        if (!Files.exists(blogDir)) {
            Files.createDirectory(blogDir);
        }
        String todayDate = nowDate.format(dateFormatter);
        Path dateDir = blogDir.resolve(todayDate);
        if (!Files.exists(dateDir)) {
            Files.createDirectory(dateDir);
        }
        return dateDir;
    }

    public static ImageFileResult getImageAdmin(ServiceInvoker invoker, int photoId, String blogId,
                                                Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            PhotoDto dto = ImageDbAccess.getPhotoAdmin(context.getDbAccessInvoker(), photoId, blogId);
            Path photoPath = resizedImageRoot.resolve(dto.path);

            return new ImageFileResult(photoPath);
        });
    }

    public static ImageFileResult getImage(ServiceInvoker invoker, int photoId, String blogId, int blogPostId,
                                                Path resizedImageRoot) {
        return invoker.invoke((context) -> {
            PhotoDto dto = ImageDbAccess.getPhoto(context.getDbAccessInvoker(), photoId, blogId, blogPostId);
            Path photoPath = resizedImageRoot.resolve(dto.path);

            return new ImageFileResult(photoPath);
        });
    }

    public static ImageFileResult getProfileImage(ServiceInvoker invoker, String blogId,
                                                  Path resizedProfileImageRoot) {
        return invoker.invoke((context) -> {
            ProfileDto dto = ProfileDbAccess.getProfileImageByBlogId(context.getDbAccessInvoker(), blogId);
            Path imagePath = resizedProfileImageRoot.resolve(dto.imagePath);

            return new ImageFileResult(imagePath);
        });
    }
}

