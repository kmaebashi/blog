package com.kmaebashi.nctfw;

public class JsonResult implements RoutingResult {
    private String json;

    public JsonResult(String json) {
        this.json = json;
    }

    public String getJson() {
        return this.json;
    }
}
