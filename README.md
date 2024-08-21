# Taller 1 - Simple Web Server

Este proyecto consiste en la creación de un servidor web REST sencillo  que puede manejar peticiones HTTP GET y POST, devolviendo respuestas de archivos locales en el servidor y contenido JSON, y permitiendo la gestión de usuarios con funcionalidades como guardar y listar usuarios.

## Getting Started

Estas instrucciones te ayudarán a obtener una copia del proyecto en funcionamiento en tu máquina local para fines de desarrollo y pruebas.

### Prerequisites

Necesitarás instalar las siguientes herramientas para ejecutar el proyecto:

- **Java JDK 8 o superior**
- **Maven**
- **Un IDE** como IntelliJ IDEA, Eclipse o similar

### Installing

Una serie de pasos para poner en marcha el entorno de desarrollo:

1. **Clonar el repositorio** desde GitHub:
```
git clone https://github.com/SebSanUwU/taller1-simple-webserver.git
```
2. **Navegar al directorio del proyecto**:
```
cd taller1-simple-webserver
```
3. **Construir el proyecto** usando Maven:
```
mvn clean install
```
3. **Ejecucion del proyecto** ejecuta la clase SimpleWebServer que contiene el metodo main.
4. **[URL](http://localhost:8080)** El servidor esta listo y corriendo en tu direccion local por el puerto 8080.

## Running the tests

Para ejecutar las pruebas automatizadas en el proyecto:
1. Asegúrate de el proyecto no este en ejecucion.
2. Ejecuta los tests usando el siguiente comando:
```
mvn test
```
### Break down into end to end tests

Las pruebas de este sistema comprueban que las peticiones HTTP GET y POST funcionan correctamente, así como la creación y listado de usuarios en formato JSON. Tambien se agregaron pruebas de errores y excepciones.

Ejemplo:

```
@Test
    public void testGetUser() throws IOException, InterruptedException, JSONException {
        // Crear un cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crear la solicitud GET para el usuario "John"
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/user?name=John"))
                .GET()
                .build();

        // Enviar la solicitud y recibir la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Comprobar que el código de estado HTTP sea 200
        assertEquals("Expected HTTP status 404",200, response.statusCode());

        // Comprobar que el Content-Type sea application/json
        assertEquals("Expected Content-Type application/json", response.headers().firstValue("Content-Type").orElse(""), "application/json");

        // Parsear el cuerpo de la respuesta como JSON
        String responseBody = response.body();
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Comprobar que el nombre y el correo son correctos
        assertEquals("Expected name 'John' in the JSON response", jsonResponse.getString("name"), "John");
        assertEquals("Expected email 'john_wick@example.com' in the JSON response", jsonResponse.getString("email"), "john_wick@example.com");
    }
```
## DETAILS

EL modelo del Usuario es simple. Tiene nombre, id y correo electronico. El nombre es un tipo de dato unico, por lo que no existen usuarios con el mismo nombre.
## URI - Uniform Resource Identifier

* GET—http://localhost:8080/api/user/: Retorna la lista de todos los usuarios guardados.
* GET—http://localhost:8080/api/user?name=NAME: Retorna la informacion del usuario de nombre NAME.
* POST—http://localhost:8080/api/user?name=NAME&email=EMAIL: Crea un nuevo usuario con nombre NAME y email EMAIL.

## Built With

* [Maven](https://maven.apache.org/) - Gestión de dependencias
* [JUnit](https://junit.org/junit5/) - Framework de pruebas unitarias

## Authors

* **Juan Sebastian Camargo Sanchez** - *AREP* - [SebSanUwU](https://github.com/SebSanUwU)


