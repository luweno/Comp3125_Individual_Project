package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


/*
 * Main Driver class, combines all the funcitonality of retriving and passing into CSV
 * Can be exited thanks to threads
 * 
 * 
 * NOTE: this is not the focus of my project, and thus I have been AI assisted! credits to 
 * OpenAI. (2024). ChatGPT [Large language model]. https://chatgpt.com
 */
public class SBDataCollector {
    private static volatile boolean exitRequested = false;  // Flag to signal exit

    public static void main(String[] args) {
        System.out.println("Welcome to a project I have for school");
        System.out.println("Press 0 for exit :>");
        //set HYPIXEL_API_KEY=your_actual_api_key_here call this in cmd or directly paste (AS long as its your computer and you do not post this anywhere else)
        String API_KEY = System.getenv("HYPIXEL_API_KEY");
        //ensure we have key before proceeding
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.out.println("API Key not set. Please set the HYPIXEL_API_KEY environment variable.");
            return; 
        }
        
        //To setup connection...
        ApiClient apiClient = new ApiClient(API_KEY);

        // In order to get the program to exit, we have it threaded, one to sleep, one to listen
        Thread inputThread = new Thread(SBDataCollector::handleUserInput);
        inputThread.start(); 

        // Main program logic
        try {
            while (!exitRequested) {
                // Fetch required information using ApiClient
                String countJson = apiClient.getCounts();  // Player counts
                String mayorJson = apiClient.fetchData("resources/skyblock/election");  // Mayor
                String bazaarJson = apiClient.fetchData("skyblock/bazaar");  // Bazaar / pricing

                // Parse individual Json responses into actual usable information
                List<ApiDataParser.ParsedItem> pBazaar = ApiDataParser.parseRawData(bazaarJson);
                int playerCount = ApiDataParser.getSkyblockPlayerCount(countJson);
                String mayor = ApiDataParser.getSkyblockMayor(mayorJson);

                // Now that we have the parsed data, write it to CSV
                ApiDataParser.writeDataToCSV(pBazaar, "Skyblock.csv", System.currentTimeMillis(), playerCount, mayor);

                // To not overload the API, we wait, you can adjust time as necessary
                System.out.print("Data Fetched... Sleeping");
                Thread.sleep(7200000);  // Sleep for 2 hours

                // And to end prematurely (for reseting api key and what not)
                if (exitRequested) {
                    break; 
                }
            }
            // Throw when we no longer want program to run. Java is nice so no cleanup either, though it uses a lot more ram that I would like.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Exiting program...");
    }

    // This is how to deal with the user input to have a more graceful closing.
    public static void handleUserInput() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!exitRequested) {
                String input = reader.readLine();
                if (input.trim().equals("0")) {
                    exitRequested = true;  // Set the exit flag when '0' is entered
                    System.exit(0); //just exit lol
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
