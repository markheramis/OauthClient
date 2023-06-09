package com.markheramis.oauth.client;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Mark
 */
public class OAuthClient {

    private final int PORT = 8080;
    private final String CALLBACK_URL = "http://localhost:" + PORT + "/callback";

    private final String endPoint;
    private final String clientId;
    private final OAuth20Service service;

    private String codeVerifier;
    private String codeChallenge;
    private static final CountDownLatch latch = new CountDownLatch(1);
    private OAuth2AccessToken oauthToken = null;

    public OAuthClient(
            String endPoint,
            String clientId,
            String tokenScope
    ) {
        this.endPoint = endPoint;
        this.clientId = clientId;

        this.service = new ServiceBuilder(clientId)
                .defaultScope(tokenScope)
                .callback(CALLBACK_URL)
                .build(PassportApi.instance());
    }

    public void authenticate() throws NoSuchAlgorithmException, URISyntaxException, IOException, InterruptedException, ExecutionException {

        codeVerifier = generateCodeVerifier();
        codeChallenge = generateCodeChallenge(codeVerifier);
        String authorizationUrl = getAuthorizationUrl();
        Desktop.getDesktop().browse(new URI(authorizationUrl));
        String code = getCode();
        oauthToken = getAccessToken(code);
    }

    private String getCode() throws IOException, InterruptedException {
        // Start a temporary local web server to receive the callback
        SimpleWebServer server;
        server = new SimpleWebServer(
                PORT,
                // Consumer Callback called inside SimpleWebServer.class
                (Boolean response) -> {

                    // Stop waiting for callback
                    latch.countDown();
                }
        );
        server.start();
        // Wait for the callback to be received
        latch.await();
        // Shutdown the temporary local web server
        server.stop();
        return server.getAuthorizationCode();
    }

    private String getAuthorizationUrl() {
        OAuthRequest request = new OAuthRequest(Verb.GET, service.getAuthorizationUrl());
        request.addParameter("code_challenge", codeChallenge);
        request.addParameter("code_challenge_method", "S256");
        return request.getCompleteUrl();
    }

    private OAuth2AccessToken getAccessToken(String code) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest accessTokenRequest = new OAuthRequest(Verb.POST, endPoint + "/oauth/token");
        accessTokenRequest.addBodyParameter("grant_type", "authorization_code");
        accessTokenRequest.addBodyParameter("client_id", clientId);
        accessTokenRequest.addBodyParameter("redirect_uri", CALLBACK_URL);
        accessTokenRequest.addBodyParameter("code", code);
        accessTokenRequest.addBodyParameter("code_verifier", codeVerifier);
        Response response = service.execute(accessTokenRequest);
        OAuth2AccessTokenJsonExtractor extractor = OAuth2AccessTokenJsonExtractor.instance();
        return extractor.extract(response);
    }

    private String generateCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        sr.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public String getAccessToken() {
        if (oauthToken != null) {
            return oauthToken.getAccessToken();
        }
        return null;
    }

    public String getRefreshToken() {
        if (oauthToken != null) {
            return oauthToken.getRefreshToken();
        }
        return null;
    }
}
