# IAQueryService
Servicio de consultas de IA para bases de datos ejecutándose localmente

## 📋 Descripción
En un ambiente empresarial, es fundamental la privacidad de los datos de la empresa y su lógica de negocio. Este proyecto utiliza **Ollama** ejecutándose localmente junto con un servicio Java desarrollado de forma simplificada sin frameworks, para reducir el uso de memoria y tiempo de desarrollo.

## 🎯 Características
- ✅ Servicio REST en Java puro (sin frameworks)
- ✅ Integración con Ollama para IA local
- ✅ Soporte para bases de datos PostgreSQL y Oracle
- ✅ Filtrado inteligente de esquemas por palabras clave
- ✅ Interfaz web incluida
- ✅ Tests unitarios con JUnit 5
- ✅ Manejo de metadatos de base de datos

## 🚀 Instalación y Configuración

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- Ollama instalado y ejecutándose
- Base de datos PostgreSQL/Oracle (opcional para pruebas)

### 1. Instalar Ollama
```bash
# Descargar e instalar Ollama desde https://ollama.com
# Luego descargar el modelo CodeLlama
ollama pull codellama:latest
```

### 2. Configurar el Proyecto
```bash
# Clonar el repositorio
git clone <repository-url>
cd IAQueryService

# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test
```

### 3. Ejecutar el Servicio
```bash
# Iniciar Ollama
ollama run codellama:latest

# En otra terminal, ejecutar el servicio Java
mvn exec:java
```

El servidor estará disponible en: `http://localhost:8101`

## 🔧 Configuración Técnica

### Puerto del Servidor
- **Puerto configurado**: 8101 (modificable en `IAQueryService.java`)
- **URL Ollama**: `http://localhost:11434` (por defecto)

### Base de Datos
El servicio está configurado para trabajar con:
- **PostgreSQL**: `jdbc:postgresql://localhost:5432/database`
- **Oracle**: Soporte incluido con esquemas específicos

### Estructura de Esquemas
El sistema soporta filtrado inteligente por secciones:
- **ASIS**: Para consultas de asistencia (`SIGTH_ASIS`)
- **ACAD**: Para consultas académicas (`SIGTH_ACAD`)  
- **DNARH**: Para consultas de recursos humanos (por defecto)

## 📁 Estructura del Proyecto
```
IAQueryService/
├── src/main/java/IAQueryService/
│   ├── IAQueryService.java          # Servidor HTTP principal
│   ├── OllamaClient.java           # Cliente para integración con Ollama
│   ├── DBMetadataReader.java       # Lector de metadatos de BD
│   ├── QueryHandler.java           # Manejador de consultas
│   ├── QueryExec.java              # Ejecutor de consultas
│   ├── SchemaHandler.java          # Manejador de esquemas
│   ├── StaticFileHandler.java      # Servidor de archivos estáticos
│   └── JsonUtil.java               # Utilidades JSON
├── src/test/java/IAQueryService/
│   ├── OllamaClientTest.java       # Tests del cliente Ollama
│   └── DBMetadataReaderTest.java   # Tests del lector de BD
├── web/
│   └── index.html                  # Interfaz web
├── esquema.json                    # Esquema de base de datos
├── esquema_test.json              # Esquemas para testing
└── pom.xml                        # Configuración Maven
```

## 🧪 Testing
El proyecto incluye tests unitarios completos:

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=OllamaClientTest
mvn test -Dtest=DBMetadataReaderTest
```

### Cobertura de Tests
- ✅ **OllamaClientTest**: 3 tests
  - Detección de palabras clave ASIS
  - Fallback a DNARH por defecto
  - Validación de instrucciones
- ✅ **DBMetadataReaderTest**: 1 test
  - Test básico de funcionalidad

## 🔄 Cambios Recientes (Verificación 2025)

### ✅ Problemas Corregidos
1. **Dependencias de Testing**
   - ➕ Agregadas dependencias JUnit 5 (Jupiter) al `pom.xml`
   - ➕ Plugin Surefire configurado para JUnit 5

2. **Configuración Maven**
   - 🔧 Corregida clase principal: `IAQueryService.IAQueryService`
   - 🔧 Puerto actualizado a 8101

3. **Funcionalidad de Tests**
   - 🔧 Implementado filtrado por secciones en `OllamaClient`
   - 🔧 Creado `esquema_test.json` para tests estructurados
   - ✅ Todos los tests ahora pasan correctamente

4. **Calidad de Código**
   - 🧹 Eliminados imports no utilizados
   - 🔧 Corregidas APIs deprecadas en HttpClient
   - 🔧 Mejorado manejo de recursos con try-with-resources

### 📊 Estado Actual
- **Tests**: 4/4 pasando ✅
- **Compilación**: Sin errores ✅
- **Warnings**: Minimizados ✅
- **Funcionalidad**: Completamente operativa ✅

## 🌐 API Endpoints

### Principales
- `GET /` - Interfaz web principal
- `POST /query` - Generar consulta SQL desde texto natural
- `POST /queryexec` - Ejecutar consulta SQL
- `GET /refreshSchema` - Actualizar esquema de base de datos

### Ejemplo de Uso
```javascript
// Generar SQL desde texto natural
fetch('/query', {
    method: 'POST',
    body: 'Mostrar empleados del área de sistemas'
})

// Ejecutar consulta SQL
fetch('/queryexec', {
    method: 'POST',
    body: 'SELECT * FROM empleados WHERE area = "sistemas"'
})
```

## 📝 Uso del Sistema

### 1. Preparar el Esquema
1. Acceder a `http://localhost:8101`
2. Hacer clic en "Refrescar Esquema"
3. Verificar que se muestre la estructura JSON de la base de datos

### 2. Realizar Consultas
1. Escribir la pregunta en lenguaje natural
2. El sistema detectará automáticamente la sección apropiada:
   - Palabras con "ASIS" → Esquema de asistencia
   - Palabras con "ACAD" → Esquema académico  
   - Por defecto → Esquema DNARH
3. Obtener el SQL generado
4. Ejecutar y visualizar resultados

## 🛠️ Desarrollo

### Compilación
```bash
mvn clean compile
```

### Ejecución en Desarrollo
```bash
mvn exec:java
```

### Packaging
```bash
mvn clean package
java -jar target/IAQueryService-0.0.1-SNAPSHOT.jar
```

## 📋 Dependencias Principales
- **Java**: 17+
- **Apache HttpClient 5**: 5.2.1
- **Jackson**: 2.15.2 (JSON processing)
- **PostgreSQL JDBC**: 42.6.0
- **SLF4J**: 2.0.7 (logging)
- **JUnit 5**: 5.9.3 (testing)

## 🔒 Seguridad y Privacidad
- ✅ Procesamiento completamente local
- ✅ No envío de datos a servicios externos
- ✅ Control total sobre la información empresarial
- ✅ Ollama ejecutándose en el mismo equipo

## 📞 Soporte
Para problemas o consultas:
1. Verificar que Ollama esté ejecutándose: `ollama list`
2. Comprobar logs del servidor Java
3. Ejecutar tests: `mvn test`
4. Verificar conectividad: `curl http://localhost:8101`

---

**Última verificación**: 19 de Agosto de 2025  
**Estado**: ✅ Completamente funcional y testeado