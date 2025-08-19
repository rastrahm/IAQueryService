package IAQueryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class QueryHandler implements HttpHandler {
    private final DBMetadataReader metadataReader = new DBMetadataReader("jdbc:postgresql://localhost:5432/fluencyp_fluency?currentSchema=public", "postgres", "master");
    private final OllamaClient ollama = new OllamaClient("http://localhost:11434");

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        JsonUtil jsonUtil = new JsonUtil();
        String prompt = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            String schemaJson = new String(Files.readAllBytes(Paths.get("esquema.json")), StandardCharsets.UTF_8);
            String sql = ollama.generateSQL(prompt);
            //String consul = jsonUtil.extractSQLFromBackticks(sql);
            //List<Map<String,Object>> data = metadataReader.executeQuery(consul);
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("sql", sql);
            String json = new ObjectMapper().writeValueAsString(respuesta);
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
