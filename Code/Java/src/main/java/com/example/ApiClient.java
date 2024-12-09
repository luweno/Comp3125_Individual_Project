package com.example;

//Allow for api calls
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/*
 * Responsible for managing API_KEY and fetching raw results from Hypixel API.
 * You can do whatever you want with this; my program will just parse everything important into a pretty CSV (hopefully)
 */
public class ApiClient {
    private final String API_KEY;

    //Create object with api key, quick calls
    public ApiClient(String API_KEY) { 
        this.API_KEY = API_KEY; 
    }

    // Base URL for Hypixel API
    private static final String BASE_URL = "https://api.hypixel.net/v2/";

    // This fetches information from any endpoint specified, in form of x(/x) as needed
    public String fetchData(String endpoint) {
        //First we form the request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = BASE_URL + endpoint + "?key=" + API_KEY;
            HttpGet request = new HttpGet(url);
            //Here we try to launch it
            try (CloseableHttpResponse response = httpClient.execute(request)) { // This is now deprecated, consider updating in future
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity); // Return the response as a string
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; //Sort of drop it if its not what we wanted
        }
    }

    // Specific count call, can do with other API, however, I like freedom of default method
    public String getCounts() {
        // Call fetchdata method with specific argument
        return fetchData("counts");
    }

    //This can test everything, GPT made this completely (I need to write more tests on my own)
    public static void main(String[] args) {
        String apiKey = "Insert your api key here (this is a test method)";
        ApiClient apiClient = new ApiClient(apiKey);

        // Fetch data from the /v2/counts endpoint
        String countsData = apiClient.getCounts();

        // Print the raw data or process it
        if (countsData != null) {
            System.out.println(countsData); // Output raw JSON response
        } else {
            System.out.println("Error fetching data.");
        }
    }
}
