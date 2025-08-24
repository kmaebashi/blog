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

    // orientation...1
    @Test
    void convertImageTest003() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/photo1.jpg");
        Path destImage = Paths.get("test_output/resized_image/photo1.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    // orientation...6
    @Test
    void convertImageTest004() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/photo2.jpg");
        Path destImage = Paths.get("test_output/resized_image/photo2.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    // orientation...3
    @Test
    void convertImageTest005() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/photo3.jpg");
        Path destImage = Paths.get("test_output/resized_image/photo3.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    // orientation...8
    @Test
    void convertImageTest006() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/photo4.jpg");
        Path destImage = Paths.get("test_output/resized_image/photo4.jpg");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    @Test
    void convertImageTest007() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/iphone_h.JPG");
        Path destImage = Paths.get("test_output/resized_image/iphone_h.JPG");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }

    @Test
    void convertImageTest008() throws Exception {
        Path srcImage = Paths.get("test_input/test_image/iphone_v.JPG");
        Path destImage = Paths.get("test_output/resized_image/iphone_v.JPG");

        ImageUtil.convertImage(srcImage, destImage, 500, "jpg");
    }
}