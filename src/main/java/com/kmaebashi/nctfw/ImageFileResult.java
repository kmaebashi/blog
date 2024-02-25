package com.kmaebashi.nctfw;

import java.nio.file.Path;

public class ImageFileResult implements RoutingResult {
    private Path imagePath;

    public ImageFileResult(Path imagePath) {
        this.imagePath = imagePath;
    }

    public Path getImagePath() {
        return this.imagePath;
    }
}
