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
    public void refreshSchemasSeparados(List<String> schemas) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();

            for (String schemaName : schemas) {
                List<Map<String, Object>> schemaList = new ArrayList<>();
                System.out.println("Procesando esquema: " + schemaName);
                ResultSet tables = meta.getTables(null, schemaName.toUpperCase(), "%", new String[]{"TABLE"});

                while (tables.next()) {
                	System.out.println("Procesando tabla: " + tables.getString("TABLE_NAME"));
                    String tableName = tables.getString("TABLE_NAME");

                    // Leer columnas
                    List<Map<String, Object>> cols = new ArrayList<>();
                    try (ResultSet columns = meta.getColumns(null, schemaName.toUpperCase(), tableName, "%")) {
                        while (columns.next()) {
                            Map<String, Object> col = new LinkedHashMap<>();
                            col.put("name", columns.getString("COLUMN_NAME"));
                            col.put("type", columns.getString("TYPE_NAME"));
                            cols.add(col);
                        }
                    }

                    // FK importadas
                    List<Map<String, String>> foreignKeys = new ArrayList<>();
                    try (ResultSet fks = meta.getImportedKeys(null, schemaName.toUpperCase(), tableName)) {
                        while (fks.next()) {
                        	System.out.println("Procesando clave foránea: " + fks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("fk_column", fks.getString("FKCOLUMN_NAME"));
                            fk.put("pk_schema", fks.getString("PKTABLE_SCHEM"));
                            fk.put("pk_table", fks.getString("PKTABLE_NAME"));
                            fk.put("pk_column", fks.getString("PKCOLUMN_NAME"));
                            foreignKeys.add(fk);
                        }
                    }

                    // FK exportadas
                    List<Map<String, String>> exportedKeys = new ArrayList<>();
                    try (ResultSet efks = meta.getExportedKeys(null, schemaName.toUpperCase(), tableName)) {
                        while (efks.next()) {
                        	System.out.println("Procesando clave foránea exportada: " + efks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("pk_column", efks.getString("PKCOLUMN_NAME"));
                            fk.put("fk_schema", efks.getString("FKTABLE_SCHEM"));
                            fk.put("fk_table", efks.getString("FKTABLE_NAME"));
                            fk.put("fk_column", efks.getString("FKCOLUMN_NAME"));
                            exportedKeys.add(fk);
                        }
                    }

                    Map<String, Object> tbl = new LinkedHashMap<>();
                    tbl.put("schema", schemaName.toUpperCase());
                    tbl.put("table", tableName);
                    tbl.put("columns", cols);
                    tbl.put("foreign_keys", foreignKeys);
                    tbl.put("exported_keys", exportedKeys);
                    schemaList.add(tbl);
                }

                // Guardar JSON separado
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaList);
                Files.write(Paths.get("esquema_" + schemaName.toLowerCase() + ".json"), json.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /*public String refreshAndGetSchema() throws SQLException, IOException {
        List<Map<String, Object>> schemaList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, "DNARH", "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Procesando tabla: " + tableName);

                // Leer columnas
                List<Map<String, Object>> cols = new ArrayList<>();
                ResultSet columns = meta.getColumns(null, user.toUpperCase(), tableName, "%");
                while (columns.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    String colName = columns.getString("COLUMN_NAME");
                    col.put("name", colName);
                    col.put("type", columns.getString("TYPE_NAME"));
                    // Intentar obtener catálogo si parece tener pocos valores distintos
                    System.out.println("Procesando columna: " + colName);
                    Map<String, String> valuesMap = detectColumnValues(conn, tableName, colName);
                    if (!valuesMap.isEmpty()) {
                        col.put("values", valuesMap);
                    }
                    cols.add(col);
                }

                // Foreign Keys
                List<Map<String, String>> foreignKeys = new ArrayList<>();
                try (ResultSet fks = meta.getImportedKeys(null, user.toUpperCase(), tableName)) {
                    while (fks.next()) {
                    	System.out.println("Procesando clave foránea: " + fks.getString("FKCOLUMN_NAME"));
                        Map<String, String> fk = new LinkedHashMap<>();
                        fk.put("fk_column", fks.getString("FKCOLUMN_NAME"));
                        fk.put("pk_table", fks.getString("PKTABLE_NAME"));
                        fk.put("pk_column", fks.getString("PKCOLUMN_NAME"));
                        foreignKeys.add(fk);
                    }
                }

                // Exported Keys
                List<Map<String, String>> exportedKeys = new ArrayList<>();
                try (ResultSet efks = meta.getExportedKeys(null, user.toUpperCase(), tableName)) {
                    while (efks.next()) {
                    	System.out.println("Procesando clave foránea exportada: " + efks.getString("FKCOLUMN_NAME"));
                        Map<String, String> fk = new LinkedHashMap<>();
                        fk.put("pk_column", efks.getString("PKCOLUMN_NAME"));
                        fk.put("fk_table", efks.getString("FKTABLE_NAME"));
                        fk.put("fk_column", efks.getString("FKCOLUMN_NAME"));
                        exportedKeys.add(fk);
                    }
                }

                Map<String, Object> tbl = new LinkedHashMap<>();
                tbl.put("schema", user.toUpperCase());
                tbl.put("table", tableName);
                tbl.put("columns", cols);
                tbl.put("foreign_keys", foreignKeys);
                tbl.put("exported_keys", exportedKeys);

                schemaList.add(tbl);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaList);
        Files.write(Paths.get("esquema.json"), json.getBytes(StandardCharsets.UTF_8));
        return json;
    }*/

    /**
     * Detecta valores distintos de una columna si el total es pequeño (catálogo).
     */
    /*private Map<String, String> detectColumnValues(Connection conn, String tableName, String colName) {
        Map<String, String> values = new LinkedHashMap<>();
        String sql = String.format("SELECT DISTINCT %s FROM %s WHERE %s IS NOT NULL FETCH FIRST 20 ROWS ONLY", colName, tableName, colName);
        System.out.println("Ejecutando SQL para detectar valores: " + sql);
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String val = rs.getString(1);
                if (val != null) {
                    values.put(val, val); // Aquí podrías mapear a descripción real si existe
                }
            }
        } catch (SQLException e) {
            // ignorar si no se puede leer
        }
        return values;
    }*/
    
    public String refreshAndGetSchema() throws SQLException, IOException {
        List<Map<String, Object>> schemaList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            String schema = "DNARH";
            try (ResultSet tables = meta.getTables(null, schema, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("Procesando tabla: " + tableName);
                    
                    // Leer columnas
                    List<Map<String, String>> cols = new ArrayList<>();
                    try (ResultSet columns = meta.getColumns(null, schema, tableName, "%")) {
                        while (columns.next()) {
                            Map<String, String> col = new LinkedHashMap<>();
                            col.put("name", columns.getString("COLUMN_NAME"));
                            col.put("type", columns.getString("TYPE_NAME"));
                            cols.add(col);
                        }
                    }

                    // Leer claves foráneas (importadas)
                    List<Map<String, String>> foreignKeys = new ArrayList<>();
                    try (ResultSet fks = meta.getImportedKeys(null, schema, tableName)) {
                        while (fks.next()) {
                        	System.out.println("Procesando clave foránea: " + fks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("fk_column", fks.getString("FKCOLUMN_NAME"));
                            fk.put("pk_schema", fks.getString("PKTABLE_SCHEM"));
                            fk.put("pk_table", fks.getString("PKTABLE_NAME"));
                            fk.put("pk_column", fks.getString("PKCOLUMN_NAME"));
                            foreignKeys.add(fk);
                        }
                    }

                    // Leer claves foráneas exportadas
                    List<Map<String, String>> exportedKeys = new ArrayList<>();
                    try (ResultSet efks = meta.getExportedKeys(null, schema, tableName)) {
                        while (efks.next()) {
                        	System.out.println("Procesando clave foránea exportada: " + efks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("pk_column", efks.getString("PKCOLUMN_NAME"));
                            fk.put("fk_schema", efks.getString("FKTABLE_SCHEM"));
                            fk.put("fk_table", efks.getString("FKTABLE_NAME"));
                            fk.put("fk_column", efks.getString("FKCOLUMN_NAME"));
                            exportedKeys.add(fk);
                        }
                    }

                    // Construir estructura de la tabla (mantener formato consistente: schema y table por separado)
                    Map<String, Object> tbl = new LinkedHashMap<>();
                    tbl.put("schema", "DNARH");
                    tbl.put("table", tableName);
                    tbl.put("columns", cols);
                    tbl.put("foreign_keys", foreignKeys);
                    tbl.put("exported_keys", exportedKeys);

                    schemaList.add(tbl);
                }
            }
            /*try (ResultSet tables = meta.getTables(null, "SIGTH_ASIS", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    
                    System.out.println("Procesando tabla: " + tableName);
                    
                    // Leer columnas
                    List<Map<String, String>> cols = new ArrayList<>();
                    try (ResultSet columns = meta.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            Map<String, String> col = new LinkedHashMap<>();
                            col.put("name", columns.getString("COLUMN_NAME"));
                            col.put("type", columns.getString("TYPE_NAME"));
                            cols.add(col);
                        }
                    }

                    // Leer claves foráneas (importadas)
                    List<Map<String, String>> foreignKeys = new ArrayList<>();
                    try (ResultSet fks = meta.getImportedKeys(null, null, tableName)) {
                        while (fks.next()) {
                        	System.out.println("Procesando clave foránea: " + fks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("fk_column", fks.getString("FKCOLUMN_NAME"));
                            fk.put("pk_table", fks.getString("PKTABLE_NAME"));
                            fk.put("pk_column", fks.getString("PKCOLUMN_NAME"));
                            foreignKeys.add(fk);
                        }
                    }

                    // Leer claves foráneas exportadas
                    List<Map<String, String>> exportedKeys = new ArrayList<>();
                    try (ResultSet efks = meta.getExportedKeys(null, null, tableName)) {
                        while (efks.next()) {
                        	System.out.println("Procesando clave foránea exportada: " + efks.getString("FKCOLUMN_NAME"));
                            Map<String, String> fk = new LinkedHashMap<>();
                            fk.put("pk_column", efks.getString("PKCOLUMN_NAME"));
                            fk.put("fk_table", efks.getString("FKTABLE_NAME"));
                            fk.put("fk_column", efks.getString("FKCOLUMN_NAME"));
                            exportedKeys.add(fk);
                        }
                    }

                    // Construir estructura de la tabla
                    Map<String, Object> tbl = new LinkedHashMap<>();
                    tbl.put("schema", "SIGTH_ASIS");
                    tbl.put("table", tableName);
                    tbl.put("columns", cols);
                    tbl.put("foreign_keys", foreignKeys);
                    tbl.put("exported_keys", exportedKeys);

                    schemaList.add(tbl);
                }
            }*/
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(schemaList);
        Files.write(Paths.get("esquema.json"), json.getBytes(StandardCharsets.UTF_8));
        return json;
    }

    /**
     * public String refreshAndGetSchema() throws SQLException, IOException {
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
     */
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
