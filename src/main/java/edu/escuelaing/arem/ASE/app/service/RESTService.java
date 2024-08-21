package edu.escuelaing.arem.ASE.app.service;

public interface RESTService {
    public String response (String request);

    String response(String request, String name);

    String response(String request, String name, String email);
}
