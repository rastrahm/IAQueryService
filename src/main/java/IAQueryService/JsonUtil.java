package IAQueryService;

import java.util.List;
import java.util.Map;
import java.io.IOException;

public class JsonUtil {
	public static String toJson(List<Map<String,Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i=0;i<data.size();i++){
            Map<String,Object> row = data.get(i);
            sb.append('{');
            int j=0;
            for (Map.Entry<String,Object> e: row.entrySet()){
                sb.append('"').append(e.getKey()).append('"').append(':');
                Object v=e.getValue();
                if(v instanceof Number||v instanceof Boolean) sb.append(v);
                else sb.append('"').append(v).append('"');
                if(++j<row.size()) sb.append(',');
            }
            sb.append('}');
            if(i<data.size()-1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }
	
	public String extractSQLFromBackticks(String response) throws IOException {
	    //Tenemos que comentar al pasar de codellama a sqlcode
		String buscar = "```";
		String sql = "";
		int existe = response.split(buscar, -1).length - 1;
		if (existe > 1) {
			int start = response.indexOf("```");
			/*
			 * if (start == -1) { throw new
			 * IOException("No se encontró el delimitador de inicio ``` en la respuesta.");
			 * }
			 */
			
			int end = response.indexOf("```", start + 3);
			/*
			 * if (end == -1) { throw new
			 * IOException("No se encontró el delimitador de cierre ``` en la respuesta.");
			 * }
			 */
			sql = response.substring(start + 3, end).trim().replace("\\n", " ").replace("\n", " ").replace("sql", " ");	    	
		} else if (existe == 1) {
            int end = response.indexOf("```");
            sql = response.substring(0, end).trim().replace("\\n", " ").replace("\n", " ").replace("sql", " ");	    	
		} else {
			sql = response.trim().replace("\\n", " ").replace("\n", " ").replace("sql", " ");
		}
		//String sql = response.trim().replace("\\n", " ").replace("\n", " ").replace("sql", " ");
	    return sql;
	}
}
