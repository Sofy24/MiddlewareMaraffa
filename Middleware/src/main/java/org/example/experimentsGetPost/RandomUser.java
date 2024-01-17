package org.example.experimentsGetPost;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RandomUser {
    public static void main(String[] args) {
        try {
            // Define the URL for the GET request
            String apiUrl = "https://randomuser.me/api/";

            // Create a URL object
            URL url = new URL(apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response body
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Print the response body
            System.out.println("Response Body:\n" + response);

            // Close the connection
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
