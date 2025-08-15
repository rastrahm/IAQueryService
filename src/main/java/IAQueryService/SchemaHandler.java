package IAQueryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SchemaHandler implements HttpHandler {
    private final DBMetadataReader db = new DBMetadataReader("jdbc:postgresql://localhost:5432/fluencyp_fluency?currentSchema=public", "postgres", "master");

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        try {
            String json = db.refreshAndGetSchema();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            byte[] resp = json.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, resp.length);
            ex.getResponseBody().write(resp);
            ex.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();
            ex.sendResponseHeaders(500, -1);
        }
    }

}
