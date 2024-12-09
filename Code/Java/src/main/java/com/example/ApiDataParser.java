package com.example;

// Utiltiy
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * This class is responsible for turning the Json information into something usable
 * 
 * Again, not main focus, so some additional help from Openai's Chat GPT
 */
public class ApiDataParser {

    // Storage structure for parsed data
    static class ParsedItem {
        String productId;
        double totalAmount;
        double avgPricePerUnit;
        String lastUpdated;

        //we set information here
        public ParsedItem(String productId, double totalAmount, double avgPricePerUnit, String lastUpdated) {
            this.productId = productId;
            this.totalAmount = totalAmount;
            this.avgPricePerUnit = avgPricePerUnit;
            this.lastUpdated = lastUpdated;
        }

        //Mostly for debugging
        @Override
        public String toString() {
            return "ParsedItem{" +
                    "productId='" + productId + '\'' +
                    ", totalAmount=" + totalAmount +
                    ", avgPricePerUnit=" + avgPricePerUnit +
                    ", lastUpdated='" + lastUpdated + '\'' +
                    '}';
        }
    }


    /*
     * Responsible for extracting the items we want in paricular, takes in the raw json
     * Returns: list of the desired items
     * This endpoint: skyblock/bazaar
     * 
     */
    protected static List<ParsedItem> parseRawData(String rawData) {

        // Define the list of product IDs to filter, I explain why I chose these elsewhere
        Set<String> filterItems = new HashSet<>(Arrays.asList(
            "BOOSTER_COOKIE", "GREEN_GIFT", "REVENANT_FLESH", "ENCHANTED_ROTTEN_FLESH", 
            "ENCHANTMENT_SMITE_7", "GRIFFIN_FEATHER", "ENCHANTED_GOLD_BLOCK", "KISMET_FEATHER", 
            "GIANT_FRAGMENT_LASER", "STOCK_OF_STONKS", "GLOSSY_GEMSTONE", "FLAWLESS_SAPPHIRE_GEM",
            "TITANIUM_ORE", "CONDENSED_FERMENTO", "ENCHANTMENT_TURBO_WHEAT_1", "WHEAT", 
            "MAGMA_FISH_SILVER", "LAVA_WATER_ORB", "JERRY_BOX_PURPLE", "PURPLE_CANDY", 
            "FLAWLESS_JASPER_GEM", "ESSENCE_DRAGON", "SOULFLOW", "RECOMBOBULATOR_3000"
        ));

        //Setup to work with the data
        List<ParsedItem> parsedItems = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(rawData);

        // Get the time provided
        long timestamp = jsonResponse.getLong("lastUpdated");
        String readableDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));

        // This is like a list of all the products, we loop through this later
        JSONObject products = jsonResponse.getJSONObject("products");

        //Now we want to filter out some of the objects based on what I had selected
        for (String key : products.keySet()) {
            JSONObject product = products.getJSONObject(key); //Here we can select the individual 'product' json portion

            String productId = product.getString("product_id"); //We want the name to check if its what we need

            // Skip when we have an item NOT in the filter list
            if (!filterItems.contains(productId)) {
                continue;
            }

            //Otherwise proceed, and get rest of information
            JSONArray sellSummary = product.getJSONArray("sell_summary");

            double totalAmount = 0;
            double totalPrice = 0;

            // I wanted average amount and price, the data originally had all the top 'buy orders' and 'sell orders' or 
            // player created listings with whatever price they had. This should make my data more general.
            for (int i = 0; i < sellSummary.length(); i++) {
                JSONObject order = sellSummary.getJSONObject(i);
                //Get specific information from listing
                double amount = order.getDouble("amount");
                double pricePerUnit = order.getDouble("pricePerUnit");

                //Sum to average later
                totalAmount += amount;
                totalPrice += pricePerUnit * amount;
            }

            // Prevent division by zero when getting the average price per unit
            double avgPricePerUnit = totalAmount > 0 ? totalPrice / totalAmount : 0;

            // Now with all the data, we create and add to the list.
            ParsedItem parsedItem = new ParsedItem(productId, totalAmount, avgPricePerUnit, readableDate);
            parsedItems.add(parsedItem);
        }

        return parsedItems;
    }

    /*
     * Similar to above, extracts playercount filtered from json
     * Returns the number of players
     * Expects the raw Json SPECIFIC to the one returned by counts endpoint
     * 
     */
    public static int getSkyblockPlayerCount(String jsonResponse) {
        try {
            //Create object with json to break apart
            JSONObject jsonObject = new JSONObject(jsonResponse);
    
            // Check if the response is successful
            if (jsonObject.getBoolean("success")) {
                // We need to filter by game type, since players on server may not be on skyblock
                JSONObject gamesData = jsonObject.getJSONObject("games");
                JSONObject gameTypeData = gamesData.getJSONObject("SKYBLOCK");
    
                // We only care about the players online.
                return gameTypeData.getInt("players");
            } else { //Was for weird debugging, after getting it to work unless there is misisng data should never reach here
                return -1; 
            }
        } catch (Exception e) { //Actual Error
            e.printStackTrace();
            return -1; 
        }
    }
    

    /*
     * Last method to get current mayor
     * Returns name of mayor
     * Expects json from this endpoint: resources/skyblock/election
     * 
     */
    public static String getSkyblockMayor(String jsonResponse) {
        String mayor = "Unknown"; // Shouldn't happen, but just in case

        // System.out.println("Having errors so printing Json: " + jsonResponse);
        
        try {
            // Create object to work with easier
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Check if the response indicates success
            if (jsonObject.optBoolean("success", false)) {
                // Get actual mayor
                JSONObject mayorObject = jsonObject.optJSONObject("mayor");
                if (mayorObject != null) {
                    mayor = mayorObject.optString("name", "Unknown"); //Already set, but if unexpected return
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log error if needed, keep the base case for mayor as empty
        }

        return mayor; // Name
    }

    /*
     * Now that the information has been converted, we can write it to the CSV
     * 
     */
    public static void writeDataToCSV(List<ParsedItem> items, String fileName, long time, int playerCount, String Mayor) {
        try (FileWriter writer = new FileWriter(fileName, true)) { // Create a file writer to interact with file
            //Since we will have this running and constantly adding, we have append mode on

            // Write header only if file is empty (first-time creation)
            if (new java.io.File(fileName).length() == 0) {
                writer.write("Timestamp,Player Count,Product ID,Total Amount,Average Price Per Unit, Mayor\n");
            }

            //Go through items and add the information as necessary
            for (ParsedItem item : items) {
                writer.write(String.format(
                    "%d,%d,%s,%.2f,%.2f,%s\n",
                    time, playerCount, item.productId, item.totalAmount, item.avgPricePerUnit, Mayor //Remember, we're collecting many items at same time
                ));
            }

            //Debugging
            System.out.println("Data appended to CSV file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage()); //If its moved, or broken
        }
    }



    // Test the parser, help from GPT
    public static void main(String[] args) {
        // Simulated raw JSON string returned by EntityUtils.toString(entity)
        String rawData = """
        {
            "success": true,
            "lastUpdated": 1731637750901,
            "products": {
                "INK_SACK:3": {
                    "product_id": "INK_SACK:3",
                    "sell_summary": [
                        {"amount": 17040, "pricePerUnit": 1.3, "orders": 1},
                        {"amount": 9938, "pricePerUnit": 1.2, "orders": 1},
                        {"amount": 48953, "pricePerUnit": 1.1, "orders": 1},
                        {"amount": 203227, "pricePerUnit": 1.0, "orders": 3}
                    ],
                    "buy_summary": [],
                    "quick_status": {
                        "productId": "INK_SACK:3",
                        "sellPrice": 1.1579710526662363,
                        "sellVolume": 2748035,
                        "sellMovingWeek": 67037760,
                        "sellOrders": 43,
                        "buyPrice": 3.961549793337337,
                        "buyVolume": 11187849,
                        "buyMovingWeek": 20115627,
                        "buyOrders": 237
                    }
                },
                "BOOSTER_COOKIE": {
                    "product_id": "BOOSTER_COOKIE",
                    "sell_summary": [
                        {"amount": 1000, "pricePerUnit": 3000000.0, "orders": 2},
                        {"amount": 500, "pricePerUnit": 2950000.0, "orders": 1}
                    ],
                    "buy_summary": [],
                    "quick_status": {
                        "productId": "BOOSTER_COOKIE",
                        "sellPrice": 2995000.0,
                        "sellVolume": 1500,
                        "sellMovingWeek": 500000,
                        "sellOrders": 5,
                        "buyPrice": 2900000.0,
                        "buyVolume": 10000,
                        "buyMovingWeek": 200000,
                        "buyOrders": 15
                    }
                }
            }
        }
        """;

        // Parse and filter data
        List<ParsedItem> parsedItems = parseRawData(rawData);

        // Write filtered data to a CSV file
        writeDataToCSV(parsedItems, "filtered_items.csv", System.currentTimeMillis(), 11111, "paul");
    }
}
