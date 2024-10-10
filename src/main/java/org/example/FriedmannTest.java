package org.example;

import java.util.HashMap;
import java.util.Map;

import static org.example.Utils.getInputAndProcess;

public class FriedmannTest {

    // Method to calculate the Index of Coincidence (IC) for a given text
    public static double calculateIC(String text) {
        int n = text.length();
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

        System.out.println("IC: " + icSum / (n * (n - 1)));
        return icSum / (n * (n - 1));
    }

    // Method to estimate the keyword length using the corrected formula
    public static double estimateKeywordLength(String ciphertext) {
        int n = ciphertext.length();
        double ic = calculateIC(ciphertext);

        // Apply the corrected formula to estimate the keyword length
        double numerator = 0.027 * n;
        double denominator = ((n - 1) * ic) - (0.038 * n) + 0.065;

        // Ensure the denominator is not zero or negative
        if (denominator <= 0) {
            throw new IllegalArgumentException("Invalid IC value or text length to calculate keyword length.");
        }

        return numerator / denominator;
    }

    // Main method for testing
    public static void main(String[] args) {
        String ciphertext = getInputAndProcess();

        try {
            double estimatedKeywordLength = estimateKeywordLength(ciphertext);
            System.out.printf("Estimated Keyword Length: %.2f%n", estimatedKeywordLength);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}
