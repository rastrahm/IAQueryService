package IAQueryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OllamaClient {
    private final String apiUrl;

    public OllamaClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String generateSQL(String schemaJson, String promptText) throws IOException, ParseException {
        //String full = "Usa este esquema JSON para SQL sin explicaciones:\n" + schemaJson + "\nPregunta: " + promptText + "\nSQL:";
    	//String full = "Usa exclusivamente este esque JSON para genera UNA sentenccia SQL válida y completa, sin cometanrios ni explicaciones. Solo quiero el SQL:\n" + schemaJson + "\nPregunta: " + promptText + "\nSQL:";
    	//String full = "Quiero obtener una consulta SQL, la cual vas a responder con SQL: utilizando esta estructura: " + schemaJson + " para responder: " + promptText + "\nSQL:";
    	/*String full = 
    			  "Genera solo una consulta SQL válida sin explicaciones. Usa exclusivamente las tablas y columnas que figuran en este esquema JSON: " 
    			  + schemaJson 
    			  + " No inventes tablas ni campos. La consulta debe responder a esta pregunta: " 
    			  + promptText 
    			  + ", Devuelve solo el SQL:";*/
    	String full = """
    			Eres un experto en base de datos que escribe sencillo, para una base de datos PostgreSQL. A continuación, tienes el esquema:

    			%s

    			Devuelve sólo la consulta SQL sin explicaciones, incluyendo el esquema y la tabla, sin envolverla en texto adicional. Pregunta:
    			%s

    			
    			""".formatted(schemaJson, promptText);
    	System.out.println(full);
        ObjectMapper m = new ObjectMapper();
        ObjectNode o = m.createObjectNode();
		o.put("model", "codellama:latest");
        o.put("prompt", full);
        o.put("stream", false);
        o.putArray("stop").add(";");
        String body = m.writeValueAsString(o);

        HttpPost p = new HttpPost(apiUrl + "/api/generate");
        p.setHeader("Content-Type", "application/json");
        p.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        
        RequestConfig config = RequestConfig.custom()
        	    .setConnectTimeout(6000, TimeUnit.SECONDS)  // conexión
        	    .setResponseTimeout(7200, TimeUnit.SECONDS) // lectura
        	    .build();
        
        try (
        	CloseableHttpClient c = HttpClients.custom().setDefaultRequestConfig(config).build();
            ClassicHttpResponse r = (ClassicHttpResponse) c.execute(p)) {
            String rb = EntityUtils.toString(r.getEntity());
            System.out.println("Respuesta cruda de Ollama:\n" + rb);
            JsonNode root = m.readTree(rb);
            String sql = root.path("response").asText();
            return sql.replaceAll("\\\\n", " ").replaceAll("\\s+", " ").trim();
            /*int s = rb.indexOf("\"response\":\"");
            if (s>=0) {
                int f = s + 12;
                int t = rb.indexOf('"', f);
                return rb.substring(f, t).replaceAll("\\n", " ").trim();
            }
            throw new IOException("Invalid response from Ollama: "+rb);*/
        }
    }
}
