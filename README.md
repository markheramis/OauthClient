# OAuth Client (java)

This project is a demonstrator on how to Authenticate to an OAuth2 server using Authorization Code Grant with Proof Key for Code Exchange (PKCE)

Use case

```java
OAuthClient client = new OAuthClient(
        ENDPOINT,
        CLIENT_ID,
        TOKEN_SCOPE
);
client.authenticate();
System.out.println("Access Token: " + client.getAccessToken());
System.out.println("Refresh Token: " + client.getRefreshToken());
```