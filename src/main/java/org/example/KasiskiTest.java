package org.example;

import java.util.*;

import static org.example.Utils.getInputAndProcess;

public class KasiskiTest {

    public static void main(String[] args) {
        String ciphertext = getInputAndProcess();
        // Map to store the occurrence count of each denominator
        Map<Integer, Integer> denominatorCountMap = new HashMap<>();

        // Process n-grams of lengths 2 to 6
        for (int ngramLength = 2; ngramLength <= 6; ngramLength++) {
            System.out.println("\n--- Processing " + ngramLength + "-grams ---");

            // Step 2: Find repeating n-grams
            Map<String, List<Integer>> ngramPositions = findRepeatingNGrams(ciphertext, ngramLength);

            // Step 3: Calculate distances between n-gram appearances
            Map<String, List<Integer>> ngramDistances = calculateNGramDistances(ngramPositions);

            // Step 4: Sort by count of occurrences and print results
            List<Map.Entry<String, List<Integer>>> sortedNGrams = sortByOccurrenceCount(ngramDistances);

            // Step 5: Find all possible denominators for the distances
            for (Map.Entry<String, List<Integer>> entry : sortedNGrams) {
                String ngram = entry.getKey();
                List<Integer> distances = entry.getValue();
                System.out.println("\nN-Gram: " + ngram + " | Occurrences: " + ngramPositions.get(ngram).size());
                System.out.println("Distances: " + distances);

                // Find and print possible denominators for distances
                List<Integer> commonDenominators = findCommonDenominators(distances);
                System.out.println("Possible denominators for distances: " + commonDenominators);

                // Update the count of each denominator in the map
                for (int denominator : commonDenominators) {
                    denominatorCountMap.put(denominator, denominatorCountMap.getOrDefault(denominator, 0) + 1);
                }
            }
        }


        // Print all the denominators and their occurrence counts
        System.out.println("\n--- Denominator Occurrence Count ---");
        denominatorCountMap.entrySet().stream()
                .filter(entry -> entry.getKey() > 1)
                .filter(entry -> entry.getValue() > 1)
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                .forEach(entry -> System.out.println("Denominator: " + entry.getKey() + " | Count: " + entry.getValue()));
    }

    // Method to find repeating n-grams in the text
    private static Map<String, List<Integer>> findRepeatingNGrams(String text, int n) {
        Map<String, List<Integer>> ngramPositions = new HashMap<>();

        for (int i = 0; i <= text.length() - n; i++) {
            String ngram = text.substring(i, i + n);
            ngramPositions.putIfAbsent(ngram, new ArrayList<>());
            ngramPositions.get(ngram).add(i);
        }

        // Filter n-grams that appear more than once
        ngramPositions.entrySet().removeIf(entry -> entry.getValue().size() < 2);

        return ngramPositions;
    }

    // Method to calculate distances between occurrences of n-grams
    private static Map<String, List<Integer>> calculateNGramDistances(Map<String, List<Integer>> ngramPositions) {
        Map<String, List<Integer>> ngramDistances = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : ngramPositions.entrySet()) {
            String ngram = entry.getKey();
            List<Integer> positions = entry.getValue();
            List<Integer> distances = new ArrayList<>();

            // Calculate distances between consecutive occurrences
            for (int i = 1; i < positions.size(); i++) {
                int distance = positions.get(i) - positions.get(i - 1);
                distances.add(distance);
            }

            ngramDistances.put(ngram, distances);
        }

        return ngramDistances;
    }

    // Method to sort n-grams by their count of occurrences (descending)
    private static List<Map.Entry<String, List<Integer>>> sortByOccurrenceCount(Map<String, List<Integer>> ngramDistances) {
        List<Map.Entry<String, List<Integer>>> sortedNGrams = new ArrayList<>(ngramDistances.entrySet());

        // Sort by the number of appearances (occurrence count)
        sortedNGrams.sort((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()));

        return sortedNGrams;
    }

    // Method to find common denominators for a list of distances
    private static List<Integer> findCommonDenominators(List<Integer> distances) {
        if (distances.isEmpty()) {
            return Collections.emptyList();
        }

        int gcdOfAllDistances = distances.get(0);

        // Calculate the GCD of all distances
        for (int i = 1; i < distances.size(); i++) {
            gcdOfAllDistances = gcd(gcdOfAllDistances, distances.get(i));
        }

        // Find all divisors of the GCD
        return findDivisors(gcdOfAllDistances);
    }

    // Method to compute the greatest common divisor (GCD)
    private static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // Method to find all divisors of a number
    private static List<Integer> findDivisors(int num) {
        List<Integer> divisors = new ArrayList<>();
        for (int i = 1; i <= num; i++) {
            if (num % i == 0) {
                divisors.add(i);
            }
        }
        return divisors;
    }
}
