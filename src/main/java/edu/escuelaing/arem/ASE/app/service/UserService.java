package edu.escuelaing.arem.ASE.app.service;

import com.nimbusds.jose.shaded.gson.Gson;
import edu.escuelaing.arem.ASE.app.model.User;

import java.util.*;

public class UserService implements RESTService{
    public static final Map<String, User> userDatabase = new HashMap<>();

    public UserService() {
        createUsers();
    }
    void createUsers(){
        userDatabase.put("John",new User(UUID.randomUUID().hashCode(),"John","john_wick@example.com"));
    }

    private String allUsers() {
        System.out.println("Finding all Users...");
        List<User> users = new ArrayList<>(userDatabase.values());
        // Convertir la lista de usuarios a JSON
        Gson gson = new Gson();
        return gson.toJson(users);
    }

    public String getUser(String nombre){
        System.out.println("Finding User...");
        User user = userDatabase.get(nombre);
        if (user != null) {
            // Convert user object to JSON
            return "{\n" +
                    "  \"name\": \"" + user.getNombre() + "\",\n" +
                    "  \"id\": " + user.getId() + ",\n" +
                    "  \"email\": \"" + user.getEmail() + "\"\n" +
                    "}";
        }
        return "USER NOT FOUND";
    }

    public boolean saveUser(String nombre,String email){
        System.out.println("Creating User...");
        if (getUser(nombre).equals("USER NOT FOUND")){
            userDatabase.put(nombre,new User(UUID.randomUUID().hashCode(),nombre,email));
            return true;
        }
        return false;
    }


    @Override
    public String response(String request) {
        return request;
    }

    @Override
    public String response(String request, String name) {
        if (request.equals("getUser")){
            return getUser(name);
        } else if (request.equals("allUsers")){
            return allUsers();
        }
        return response("BAD REQUEST SERVICE: "+request);
    }

    @Override
    public String response(String request, String name, String email){
        if (request.equals("saveUser")){
            if (saveUser(name,email)){
                return response("Success");
            }
            return response("USER ALREADY EXIST (NAME)");
        }
        return response("BAD REQUEST SERVICE: "+request);
    }
}
