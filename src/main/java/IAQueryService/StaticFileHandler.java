package IAQueryService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticFileHandler implements HttpHandler  {
	
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = "web/index.html"; // o donde tengas tu index.html

        File file = new File(filePath);
        if (!file.exists()) {
            String response = "404 - File not found";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

}
