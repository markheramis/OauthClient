package com.markheramis.oauth.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Mark
 *
 * @note before testing this, you will need to create an Authorization Code
 * Grant for the user that will be using this.
 */
public class Main {

    /**
     * The URL of the OAuth2 Server
     */
    private static final String ENDPOINT = "http://localhost:8000";
    /**
     * The Authorization Code Grant Client ID
     */
    private static final String CLIENT_ID = "10";
    /**
     * The Scope you need to authorize
     */
    private static final String TOKEN_SCOPE = "user.index user.show user.store";

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExecutionException, NoSuchAlgorithmException {
        OAuthClient client = new OAuthClient(
                ENDPOINT,
                CLIENT_ID,
                TOKEN_SCOPE
        );
        client.authenticate();
        System.out.println("Access Token: " + client.getAccessToken());
        System.out.println("Refresh Token: " + client.getRefreshToken());
    }
}
