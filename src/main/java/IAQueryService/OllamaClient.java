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

    String buildPrompt(String promptText) throws IOException {
        String upper = promptText.toUpperCase();
        String schemaJsonDNARH = new String(Files.readAllBytes(Paths.get("esquema.json")), StandardCharsets.UTF_8);
        esquemaSecciones.append("\t\t\t    DNARH:\n").append(schemaJsonDNARH).append("\n");
        String full = """
				Eres un generador de SQL para una base de datos Oracle. Utilizando Oracle SQL.
				
				INSTRUCCIONES ESTRICTAS:
			    1. Usa EXCLUSIVAMENTE los nombres de tablas y columnas que aparecen en el esquema JSON.
			    2. Usa los nombres EXACTOS, respetando mayúsculas y minúsculas tal como están en el JSON.
			    3. En TODOS los identificadores, usa el schema real del JSON como prefijo exacto: schema.tabla.columna (por ejemplo: SIGTH_ASIS.MI_TABLA.MI_COLUMNA).
			    4. Para hacer JOIN, usa ÚNICAMENTE las relaciones que aparecen en `foreign_keys` y `exported_keys`.
			    5. No inventes tablas ni columnas que no estén en el esquema.
			    6. Devuelve SOLO el SQL, sin comentarios, sin explicaciones y sin texto adicional.
			    7. No agregues comentarios ni explicaciones.
			    8. No incluyas punto y coma al final.
			    9. Si no es posible construir la consulta con la información disponible, devuelve exactamente: -- Consulta no posible
			    10. Ignora los encabezados de sección (ASIS, ACAD, etc.); usa siempre el valor del campo "schema" del JSON como nombre de esquema en el SQL.
			    
			    ESQUEMA DE LA BASE DE DATOS (solo los relevantes según la consulta):
				""" + esquemaSecciones.toString() + "\n\nPREGUNTA:\n" + promptText + "\n\nSQL:\n";
        return full;
    }

    public String generateSQL(String promptText) throws IOException, ParseException {
        String full = buildPrompt(promptText);
        System.out.println(full);
        ObjectMapper m = new ObjectMapper();
        ObjectNode o = m.createObjectNode();
		o.put("model", "codellama:latest");
        o.put("prompt", full);
        o.put("stream", false);
        ObjectNode options = o.putObject("options");
        options.put("temperature", 0.1);
        options.put("num_ctx", 12288);
        o.putArray("stop").add(";").add("```\n").add("\n\n");
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
        }
    }
}
