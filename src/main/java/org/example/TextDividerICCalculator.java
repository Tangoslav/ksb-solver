package org.example;

import java.util.*;

import static org.example.Utils.getInputAndProcess;

public class TextDividerICCalculator {

    public static void main(String[] args) {
        String ciphertext = getInputAndProcess();
        int keywordLength = 5;

        // Step 3: Divide the text into multiple strings based on keyword length
        List<String> dividedStrings = divideTextByKeywordLength(ciphertext, keywordLength);

        // Step 4: Calculate and print the Index of Coincidence (IC) for each divided string
        for (int i = 0; i < dividedStrings.size(); i++) {
            String dividedString = dividedStrings.get(i);
            double ic = calculateIC(dividedString);
            System.out.printf("String %d: %s%n", i + 1, dividedString);
            System.out.printf("Index of Coincidence for String %d: %.4f%n%n", i + 1, ic);
        }
    }

    // Method to divide the text into multiple strings based on keyword length
    public static List<String> divideTextByKeywordLength(String text, int keywordLength) {
        List<StringBuilder> dividedBuilders = new ArrayList<>();

        // Initialize StringBuilder objects for each divided string
        for (int i = 0; i < keywordLength; i++) {
            dividedBuilders.add(new StringBuilder());
        }

        // Distribute characters to corresponding StringBuilder based on position
        for (int i = 0; i < text.length(); i++) {
            int index = i % keywordLength;
            dividedBuilders.get(index).append(text.charAt(i));
        }

        // Convert StringBuilders to Strings
        List<String> dividedStrings = new ArrayList<>();
        for (StringBuilder sb : dividedBuilders) {
            dividedStrings.add(sb.toString());
        }

        return dividedStrings;
    }

    // Method to calculate the Index of Coincidence (IC) for a given text
    public static double calculateIC(String text) {
        int n = text.length();
        if (n <= 1) {
            return 0.0; // Avoid division by zero
        }

        Map<Character, Integer> frequencyMap = new HashMap<>();

        // Calculate frequency of each character
        for (char ch : text.toCharArray()) {
            frequencyMap.put(ch, frequencyMap.getOrDefault(ch, 0) + 1);
        }

        // Calculate the IC using the formula IC = (Σ f_i * (f_i - 1)) / (n * (n - 1))
        double icSum = 0.0;
        for (int frequency : frequencyMap.values()) {
            icSum += frequency * (frequency - 1);
        }

        return icSum / (n * (n - 1));
    }
}
