package IAQueryService;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class OllamaClientTest {

    @Test
    void testDetectsASISByKeyword() throws Exception {
        OllamaClient client = new OllamaClient("http://localhost:11434");
        String prompt = "Quiero ver asistencia de personal en SIGTH_ASIS";
        String full = client.buildPrompt(prompt);
        assertTrue(full.contains("ASIS:"), "Debe incluir sección ASIS");
        assertFalse(full.contains("ACAD:"), "No debe incluir ACAD innecesariamente");
    }

    @Test
    void testDefaultsToDNARHWhenNoMatch() throws Exception {
        OllamaClient client = new OllamaClient("http://localhost:11434");
        String prompt = "Cantidad de empleados por área";
        String full = client.buildPrompt(prompt);
        assertTrue(full.contains("DNARH:"), "Debe incluir DNARH por defecto");
    }

    @Test
    void testInstructionsPresent() throws Exception {
        OllamaClient client = new OllamaClient("http://localhost:11434");
        String prompt = "reporte académico";
        String full = client.buildPrompt(prompt);
        assertTrue(full.contains("schema.tabla.columna"));
        assertTrue(full.contains("SQL:\n"));
    }
}
