package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * This was a testing method, I had stored the API requests to be able to test them locally without pulling for them every time.
 * 
 */
public class JsonFileReader {

    //Make the file into a string (store in memory)
    public static String readFileAsString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return new String(Files.readAllBytes(path)); // Read entire file
        } catch (IOException e) {
            e.printStackTrace();
            return null; //If there is some unexpected error
        }
    }

    //Testing method
    public static void main(String[] args) {
        String filePath1 = "bazaar.json";
        String filePath2 = "count.json"; //These were example calls

        // Convert to strings
        String json1 = readFileAsString(filePath1);
        String json2 = readFileAsString(filePath2);

        // Try to print them out
        System.out.println(json1);
        System.out.println(json2);

    }
}
