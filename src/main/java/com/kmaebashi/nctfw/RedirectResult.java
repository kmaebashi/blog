package com.kmaebashi.nctfw;

public class RedirectResult implements RoutingResult {
    private String redirectUrl;

    public RedirectResult(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return this.redirectUrl;
    }
}
