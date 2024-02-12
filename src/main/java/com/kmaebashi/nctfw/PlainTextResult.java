package com.kmaebashi.nctfw;

import org.jsoup.nodes.Document;

public class PlainTextResult implements RoutingResult {
    private String text;
    private String contentType = "text/plain";
    private String downloadFilename;

    public PlainTextResult(String text) {
        this.text = text;
    }

    public PlainTextResult(String text, String contentType, String downloadFilename) {
        this.text = text;
        this.contentType = contentType;
        this.downloadFilename = downloadFilename;
    }

    public String getText() {
        return this.text;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getDownloadFilename() {
        return this.downloadFilename;
    }
}
