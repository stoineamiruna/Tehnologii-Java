package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ServletClient {

    public static void main(String[] args) {
        try {
            // Parametrul pe care vrem sa-l trimitem
            String paramValue = "cat";

            // URL-ul servletului (localhost, port 8080)
            String urlString = "http://localhost:8080/Lab1_war_exploded/ControllerServlet";

            // Construim URL-ul cu query param
            String query = "page=" + URLEncoder.encode(paramValue, "UTF-8");

            URL url = new URL(urlString + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setăm metoda GET
            connection.setRequestMethod("GET");

            // Setăm ca vrem să primim plain text
            connection.setRequestProperty("Accept", "text/plain");

            // Verificăm codul de răspuns
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Citim răspunsul
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Afișăm răspunsul în consolă
            System.out.println("Response from servlet: " + response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
