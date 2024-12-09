package com.example;

import java.io.FileWriter;
import java.io.IOException;

/*
 * This class can just dump JSon data, was good for testing purposes
 * 
 */
public class JsonUtils {

    /**
     * Dumps the JSON response into a specified file.
     *
     * @param jsonResponse The JSON string to be written to the file.
     * @param filePath     The path to the file where the JSON will be saved.
     */
    public static void dumpJsonToFile(String jsonResponse, String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonResponse);
            System.out.println("JSON successfully written to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to write JSON to file: " + e.getMessage());
        }
    }
}
