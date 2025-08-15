package IAQueryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DBMetadataReader {
    private final String url;
    private final String user;
    private final String password;

    public DBMetadataReader(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Lee el esquema de la BD y lo guarda en esquema.json
     */
    public String refreshAndGetSchema() throws SQLException, IOException {
        List<Map<String,Object>> schemaList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
        	Statement stmt = conn.createStatement();
    		stmt.execute("SET search_path TO public");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableSchema = tables.getString("TABLE_SCHEM");
                String tableName = tables.getString("TABLE_NAME");
                List<Map<String,String>> cols = new ArrayList<>();
                ResultSet columns = meta.getColumns(null, tableSchema, tableName, "%");
                while (columns.next()) {
                    Map<String,String> col = new LinkedHashMap<>();
                    col.put("name", columns.getString("COLUMN_NAME"));
                    col.put("type", columns.getString("TYPE_NAME"));
                    cols.add(col);
                }
                Map<String,Object> tbl = new LinkedHashMap<>();
                tbl.put("schema", tableSchema);
                tbl.put("table", tableName);
                tbl.put("columns", cols);
                schemaList.add(tbl);
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(schemaList);
        Files.write(Paths.get("esquema.json"), json.getBytes(StandardCharsets.UTF_8));
        return json;
    }

    /**
     * Ejecuta SQL en la BD y devuelve lista de filas
     */
    public List<Map<String,Object>> executeQuery(String sql) throws SQLException {
        List<Map<String,Object>> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= count; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }
}
