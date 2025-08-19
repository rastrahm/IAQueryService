package IAQueryService;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

public class IAQueryService {

	public static void main(String[] args) {
        // Modo de prueba: leer y guardar esquema en JSON
		/*        */
        int port = 8101;
        HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", new StaticFileHandler());
			server.createContext("/query", new QueryHandler());
			server.createContext("/queryexec", new QueryExec());
			server.createContext("/refreshSchema", new SchemaHandler());
	        server.setExecutor(null);
	        System.out.println("Server started on port " + port);
	        server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
