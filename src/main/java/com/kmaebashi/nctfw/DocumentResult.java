package com.kmaebashi.nctfw;

import org.jsoup.nodes.Document;

public class DocumentResult implements RoutingResult {
    private Document jsoupDocument;

    public DocumentResult(Document document) {
        this.jsoupDocument = document;
    }

    public Document getDocument() {
        return this.jsoupDocument;
    }
}
