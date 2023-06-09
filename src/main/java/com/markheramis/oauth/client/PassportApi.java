package com.markheramis.oauth.client;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 *
 * @author Mark
 */
public class PassportApi extends DefaultApi20 {

    protected PassportApi() {
    }

    private static class InstanceHolder {

        private static final PassportApi INSTANCE = new PassportApi();
    }

    public static PassportApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "http://localhost:8000/oauth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "http://localhost:8000/oauth/authorize";
    }
}
