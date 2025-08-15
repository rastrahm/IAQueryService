# IAQueryService
Consulting IA for database querys in my own computer

## Idea
In a business environment, it is essential to preserve the privacy of both data and business logic. This leads to the use of local AI solutions, which in this case involves using **Ollama** (<https://ollama.com>) running locally, along with a **Java** service developed in a simplified way without using any frameworks, to reduce memory usage and development time.

## Installation
1. Download Ollama from <https://ollama.com>, install it according to your operating system, and run:
   ```bash
   ollama pull codellama:latest
2. Clone the IAQueryService repository and open it in your Java IDE (e.g., Eclipse 2025-09 with Java 17). In this particular case, java-17-openjdk-amd64 is used. Run the program; it is configured to listen on port 8100.

3. Start Ollama:
   ```bash
   ollama run codellama:latest
4. From your browser, access: http:\\localhost:8100

## Español
Consultoría de IA para consultas a bases de datos en mi propio ordenador

## La idea
En un ambiente empresarial, es fundamental la pribacidad de los datos de la empresa y su logica de negocio, eso hace pensar en el uso de IAs de forma local, lo que implica en este caso el uso de Ollama [https://ollama.com] que se ejecuta de forma local, y un servicio, en este caso en Java, simplificado, no utilizando ningún framework para ahorrar memoria y tiempo de desarrollo.

## Instalación
1. Se debe bajar el Ollama de https://ollama.com, instalar el mismo según el SO, y bajar a traves de: ollama pull codellama:latest.
   ```bash
   ollama pull codellama:latest
   
2. Clonar el repositorio de IAQueryService y abrir el mismo en su editor de java, en mi caso Eclipse 2025-09 con Java 17, en este caso particular utilizo java-17-openjdk-amd64, ejecutar el programa, el mismo esta configurado para oir el puerto 8100

3. Ejecutar el Ollama con: ollama run codellama:latest
   ```bash
   ollama run codellama:latest
4. Acceder desde el navegador a: http:\\localhost:8100

## Uso
Para su uso, la IA necesita la estructura de nuestra base de datos, incluyendo los Foreingkey, para este primer paso pulse sobre el botón refrezcar, esto mostrara en formato JSON la estructura de la base de datos.
Una ves terminado podemos comenzar a utilizar las preguntas, la misma retornara un SQL, el cual permite verificar la respuesta de la IA y ejecutar la consulta, esto retornara la consulta, los valores resultantes y un grafico de los datos.