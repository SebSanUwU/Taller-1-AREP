package edu.escuelaing.arem.ASE.app;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static junit.framework.Assert.assertEquals;


public class SimpleWebServerTest {
    private static final int PORT = 8080;

    @BeforeAll
    public static void setUp() throws IOException {
        // Inicia el servidor web en un hilo separado
        new Thread(() -> {
            try {
                SimpleWebServer.main(new String[]{});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Espera un poco para asegurarse de que el servidor se haya iniciado
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAllUsers() throws IOException, InterruptedException, JSONException {
        // Crear un cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crear la solicitud GET para obtener todos los usuarios
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/user"))  // Suponemos que la ruta es "/api/users"
                .GET()
                .build();

        // Enviar la solicitud y recibir la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Comprobar que el código de estado HTTP sea 200
        assertEquals("Expected HTTP status 200", response.statusCode(), 200);

        // Comprobar que el Content-Type sea application/json
        assertEquals("Expected Content-Type application/json", response.headers().firstValue("Content-Type").orElse(""), "application/json");

        // Parsear el cuerpo de la respuesta como JSON
        String responseBody = response.body();
        assert responseBody.contains("John") : "Expected one (John) user";
    }

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

    @Test
    public void testGetUserNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/user?name=NonExistentUser"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("Expected HTTP status 404", response.statusCode(), 404);
        String expectedErrorResponse = "<html><body><h1>404 USER NOT FOUND</h1></body></html>";
        assertEquals("Response should match the expected error message", response.body().trim(), expectedErrorResponse.trim());
    }

    @Test
    public void testPostUserAndVerifyNewUser() throws IOException, InterruptedException, JSONException {
        // Crear un cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Definir los parámetros de la solicitud POST
        String name = "Jane";
        String email = "jane_doe@example.com";
        String url = "http://localhost:" + PORT + "/api/user?name=" + name + "&email=" + email;

        // Crear la solicitud POST con los parámetros en la URL
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody()) // Como se usan parámetros en la URL, no se necesita un body.
                .build();

        // Enviar la solicitud y recibir la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verificar que el código de estado HTTP sea 200
        assertEquals("Expected HTTP status 200", response.statusCode(), 200);

        // Verificar que la respuesta sea "Success" (según la lógica en handlePostRequest)
        assertEquals("Expected response body to be 'Success'", response.body(), "Success");

        // Crear la solicitud GET para el usuario "John"
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/user?name="+name))
                .GET()
                .build();

        // Enviar la solicitud y recibir la respuesta
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /// Parsear el cuerpo de la respuesta como JSON
        String responseBody = response.body();
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Comprobar que el nombre y el correo son correctos
        assertEquals("Expected name "+name+" in the JSON response", jsonResponse.getString("name"), name);
        assertEquals("Expected email "+email+" in the JSON response", jsonResponse.getString("email"), email);
    }

    @Test
    public void testPostUserMustNotWork() throws IOException, InterruptedException {
        // Crear un cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Definir los parámetros de la solicitud POST
        String name = "John";
        String email = "john@example.com";
        String url = "http://localhost:" + PORT + "/api/user?name=" + name + "&email=" + email;

        // Crear la solicitud POST con los parámetros en la URL con un nombre ya existente
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody()) // Como se usan parámetros en la URL, no se necesita un body.
                .build();

        // Enviar la solicitud y recibir la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Expected HTTP status 404", response.statusCode(), 404);
        String expectedErrorResponse = "<html><body><h1>404 USER ALREADY EXIST (NAME)</h1></body></html>";
        assertEquals("Response should match the expected error message", response.body().trim(), expectedErrorResponse.trim());
    }

    @Test
    public void testErrors() throws IOException, InterruptedException {
        // Crear un cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crear la solicitud GET para una direccion no existente
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/documents"))
                .GET()
                .build();

        // Enviar la solicitud y recibir la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Expected HTTP status 404", response.statusCode(), 404);
        String expectedErrorResponse = "<html><body><h1>404 Not Found</h1></body></html>";
        assertEquals("Response should match the expected error message", response.body().trim(), expectedErrorResponse.trim());

        // Crear la solicitud DELETE no implementada
        request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/documents"))
                .DELETE()
                .build();
        // Enviar la solicitud y recibir la respuesta
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("Expected HTTP status 501", response.statusCode(), 501);
        expectedErrorResponse = "<html><body><h1>501 Not Implemented</h1></body></html>";
        assertEquals("Response should match the expected error message", response.body().trim(), expectedErrorResponse.trim());
    }
}

