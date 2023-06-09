package com.markheramis.oauth.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 *
 * @author Mark
 */
public class SimpleWebServer {

    private final HttpServer server;
    private String code;

    public SimpleWebServer(
            int port,
            Consumer<Boolean> callback
    ) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/callback", (HttpExchange exchange) -> {
            try (exchange) {
                String query = exchange.getRequestURI().getQuery();
                HashMap<String, String> params = parseQuery(query);
                code = params.get("code");
                if (code != null) {
                    sendResponse(exchange, "success.html", 200);
                    callback.accept(true);
                } else {
                    sendResponse(exchange, "failed.html", 401);
                    callback.accept(false);
                }
            } catch( Exception e) {
                System.err.println(e.getMessage());
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        });

    }

    private void sendResponse(
            HttpExchange exchange,
            String html,
            int code
    ) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String content = readHTMLFile(html);
        int length = content.getBytes(charset).length;
        // Set the response headers
        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html"
        );
        exchange.sendResponseHeaders(
                code,
                length
        );
        // Send the HTML content as the response body
        byte[] bytes = content.getBytes(charset);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(bytes);
        responseBody.close();

    }

    public HashMap<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<>();
        if (query != null && query.length() > 0) {
            String[] params = query.split("&");
            for (String param1 : params) {
                // starting from 1, not 0
                if (!"".equals(param1)) {
                    String param = param1;
                    String[] nameValue = param.split("=");
                    String name = URLDecoder.decode(nameValue[0], StandardCharsets.UTF_8.toString());
                    String value = URLDecoder.decode(nameValue[1], StandardCharsets.UTF_8.toString());
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    private String readHTMLFile(String filename) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public String getAuthorizationCode() {
        return code;
    }
}
