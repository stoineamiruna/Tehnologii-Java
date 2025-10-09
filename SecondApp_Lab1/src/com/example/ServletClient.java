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
            String paramValue = "cat";

            String urlString = "http://localhost:8080/Lab1_war_exploded/ControllerServlet";

            String query = "page=" + URLEncoder.encode(paramValue, "UTF-8");

            URL url = new URL(urlString + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Accept", "text/plain");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response from servlet: " + response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
