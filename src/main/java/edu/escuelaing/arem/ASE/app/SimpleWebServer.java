package edu.escuelaing.arem.ASE.app;


import edu.escuelaing.arem.ASE.app.service.RESTService;
import edu.escuelaing.arem.ASE.app.service.UserService;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;



public class SimpleWebServer {
    private static final int PORT = 8080; //corre por este puerto
    public static final String WEB_ROOT = "src/main/webroot"; // Donde colocar los archivos del servidor
    public static final Map<String, RESTService> services = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10); // Crea un "Pool de hilos" de hilos a procesor
        ServerSocket serverSocket = new ServerSocket(PORT); // crea un SererSocket con el puerto
        addServices();
        while (true) {
            Socket clientSocket = serverSocket.accept(); // Espera el llamado de alguna peticion cliente
            threadPool.submit(new ClientHandler(clientSocket)); // AL cliente le asigna el socketCLiente y lo pone a correr
        }
    }

    private static void addServices() {
        services.put("User",new UserService());
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // Saca caracteres
             BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) return;
            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];

            printRequestHeader(requestLine, in);

            if (method.equals("GET")) {
                if (fileRequested.startsWith("/api/user")){
                    if(fileRequested.equals("/api/user")){
                        handleGetAllUsersRequest(out, dataOut);
                    }else {
                        handleGetUserRequest(fileRequested, out, dataOut);
                    }
                }else {
                    if (fileRequested.endsWith("/")) {
                        fileRequested += "index.html";
                    }
                    handleGetRequest(fileRequested, out, dataOut);
                }
            }else if (method.equals("POST")) {
                handlePostRequest(fileRequested, out, dataOut);
            } else {
                sendErrorResponse(out, 501, "Not Implemented");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void printRequestHeader(String requestLine, BufferedReader in) throws IOException {
        System.out.println("Request Line: " + requestLine);
        String inputLine = "";
        while ((inputLine = in.readLine()) != null) {
            if( !in.ready()) {
                break;
            }
            System.out.println("Header: " + inputLine);
        }
    }


    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        File file = new File(SimpleWebServer.WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (file.exists()) {
            byte[] fileData = readFileData(file, fileLength);
            sendOkResponse(out,content,fileLength);
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        } else {
            sendErrorResponse(out,404,"Not Found");
        }
    }

    private void handleGetAllUsersRequest(PrintWriter out, BufferedOutputStream dataOut) throws IOException{
        String response = SimpleWebServer.services.get("User").response("allUsers","");
        if (!response.equals("USER NOT FOUND")) {
            // Send JSON response
            sendOkResponse(out,"application/json",response.length());
            dataOut.write(response.getBytes());
            dataOut.flush();
        } else {
            sendErrorResponse(out, 404, response);
        }
    }

    private void handleGetUserRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException{
        String query = fileRequested.split("\\?")[1];
        String[] params = query.split("&");
        String nameValue = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("name")) {
                nameValue = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        String response = SimpleWebServer.services.get("User").response("getUser",nameValue);
        if (!response.equals("USER NOT FOUND")) {
            // Send JSON response
            sendOkResponse(out,"application/json",response.length());
            dataOut.write(response.getBytes());
            dataOut.flush();
        } else {
            sendErrorResponse(out, 404, response);
        }
    }

    private void handlePostRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        String query = fileRequested.split("\\?")[1];
        String[] params = query.split("&");
        String nameValue = null;
        String emailValue = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("name")) {
                nameValue = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
            if (keyValue[0].equals("email")) {
                emailValue = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        String response = SimpleWebServer.services.get("User").response("saveUser",nameValue,emailValue);
        if (response.equals("Success")){
            // Send JSON response
            sendOkResponse(out,"text/plain",response.length());
            dataOut.write(response.getBytes());
            dataOut.flush();
        }else {
            sendErrorResponse(out,404,response);
        }
    }

    private void sendOkResponse(PrintWriter out,String content,int fileLength){
        out.println("HTTP/1.1 200 OK");
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();
    }


    private void sendErrorResponse(PrintWriter out,int statusCode, String statusText){
        out.println("HTTP/1.1 "+statusCode+" "+statusText);
        out.println("Content-type: text/html");
        out.println();
        out.flush();
        out.println("<html><body><h1>"+statusCode+" "+statusText+"</h1></body></html>");
        out.flush();
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        else if (fileRequested.endsWith(".css")) return "text/css";
        else if (fileRequested.endsWith(".js")) return "application/javascript";
        else if (fileRequested.endsWith(".png")) return "image/png";
        else if (fileRequested.endsWith(".jpg")) return "image/jpeg";
        return "text/plain";
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) fileIn.close();
        }
        return fileData;
    }
}