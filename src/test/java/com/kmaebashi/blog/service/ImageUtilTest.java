package com.kmaebashi.blog.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    @Test
    void convertImageTest001() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/horizontal.jpg");
        Path destImage = Paths.get("test_output/resized_image/horizontal001.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    @Test
    void convertImageTest002() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/vertical.jpg");
        Path destImage = Paths.get("test_output/resized_image/vertical001.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }
}