package org.example;

import java.util.*;
import java.io.*;

public class SubstitutionWithKeywordSolver {

    // Adjustable parameters
    private static final int ATTEMPTS_LIMIT = -1; // Attempts limit (-1 for unlimited)
    private static final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 10000000;
    private static final String NGRAM_FILES_DIR = "src/main/resources/"; // Directory containing n-gram frequency files
    private static final String[] NGRAM_FILES = {
            "english_monograms.txt",
            "english_bigrams.txt",
            "english_trigrams.txt",
            "english_quadgrams.txt",
            "english_quintgrams.txt"
    };

    // Letters to be mapped ('A' to 'S')
    private static final Set<Character> MAPPABLE_LETTERS = new HashSet<>();
    static {
        for (char ch = 'A'; ch <= 'S'; ch++) {
            MAPPABLE_LETTERS.add(ch);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Load n-gram frequencies
        NGramScorer ngramScorer;
        try {
            ngramScorer = new NGramScorer(NGRAM_FILES_DIR, NGRAM_FILES);
        } catch (IOException e) {
            System.err.println("Error loading n-gram frequencies: " + e.getMessage());
            return;
        }

        System.out.println("Enter the ciphertext:");
        String ciphertext = scanner.nextLine().toUpperCase().replaceAll("[^A-Z]", "");

        // Initial mapping based on letter frequencies
        Map<Character, Character> initialMapping = getInitialMapping(ciphertext);

        // Use hill-climbing algorithm to optimize the mapping
        Map<Character, Character> bestMapping = hillClimbing(ciphertext, initialMapping, ngramScorer);

        // Decrypt the ciphertext with the best mapping
        String decryptedText = applyMapping(ciphertext, bestMapping);

        System.out.println("\nBest Mapping Found:");
        for (Map.Entry<Character, Character> entry : bestMapping.entrySet()) {
            System.out.println("Cipher Letter: " + entry.getKey() + " -> Plain Letter: " + entry.getValue());
        }

        System.out.println("\nDecrypted Text:");
        System.out.println(formatOutput(decryptedText));

        scanner.close();
    }

    // Generate initial mapping based on letter frequencies
    private static Map<Character, Character> getInitialMapping(String ciphertext) {
        Map<Character, Integer> cipherFreq = new HashMap<>();
        for (char ch : ciphertext.toCharArray()) {
            cipherFreq.put(ch, cipherFreq.getOrDefault(ch, 0) + 1);
        }

        // Sort cipher letters by frequency
        List<Character> cipherLetters = new ArrayList<>(cipherFreq.keySet());
        // Only consider mappable letters
        cipherLetters.removeIf(ch -> !MAPPABLE_LETTERS.contains(ch));
        cipherLetters.sort((a, b) -> cipherFreq.get(b) - cipherFreq.get(a));

        // Standard English letter frequencies (only for letters 'A' to 'S')
        List<Character> englishLetters = Arrays.asList(
                'E', 'T', 'A', 'O', 'I', 'N', 'S', 'H', 'R', 'D',
                'L', 'C', 'U', 'M', 'W', 'F', 'G', 'Y', 'P', 'B',
                'Q', 'J', 'K', 'V', 'X', 'Z'
        );
        // Only consider letters 'A' to 'S' in englishLetters
        List<Character> mappableEnglishLetters = new ArrayList<>();
        for (char ch : englishLetters) {
            if (ch >= 'A' && ch <= 'S') {
                mappableEnglishLetters.add(ch);
            }
        }

        Map<Character, Character> mapping = new HashMap<>();
        // Initialize mapping for letters 'T' to 'Z' to map to themselves
        for (char ch = 'T'; ch <= 'Z'; ch++) {
            mapping.put(ch, ch);
        }
        int len = Math.min(cipherLetters.size(), mappableEnglishLetters.size());
        for (int i = 0; i < len; i++) {
            mapping.put(cipherLetters.get(i), mappableEnglishLetters.get(i));
        }

        return mapping;
    }

    // Hill-climbing algorithm to optimize the mapping
    private static Map<Character, Character> hillClimbing(String ciphertext, Map<Character, Character> initialMapping, NGramScorer ngramScorer) {
        Map<Character, Character> currentMapping = new HashMap<>(initialMapping);
        String currentDecryption = applyMapping(ciphertext, currentMapping);
        double currentScore = ngramScorer.score(currentDecryption);

        int iterationsWithoutImprovement = 0;
        int totalIterations = 0;

        // Only consider mappable letters for swapping
        List<Character> letters = new ArrayList<>(currentMapping.keySet());
        letters.removeIf(ch -> !MAPPABLE_LETTERS.contains(ch));

        Random random = new Random();

        // Keep track of top 5 results
        PriorityQueue<Result> topResults = new PriorityQueue<>(Comparator.comparingDouble(r -> r.score));

        System.out.println("\nStarting hill-climbing algorithm...");
        System.out.println("Initial Score: " + currentScore);

        while (iterationsWithoutImprovement < MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
            totalIterations++;

            // Generate a neighbor mapping by swapping two letters
            Map<Character, Character> neighborMapping = new HashMap<>(currentMapping);

            // Randomly select two letters to swap in the plaintext mapping
            int idx1 = random.nextInt(letters.size());
            int idx2 = random.nextInt(letters.size());
            while (idx1 == idx2) {
                idx2 = random.nextInt(letters.size());
            }

            char letter1 = letters.get(idx1);
            char letter2 = letters.get(idx2);

            // Swap the mappings
            char temp = neighborMapping.get(letter1);
            neighborMapping.put(letter1, neighborMapping.get(letter2));
            neighborMapping.put(letter2, temp);

            // Apply the neighbor mapping and score the result
            String neighborDecryption = applyMapping(ciphertext, neighborMapping);
            double neighborScore = ngramScorer.score(neighborDecryption);

            // Logging the attempt
            if (totalIterations % 10000 == 0) {
                System.out.println("Iteration " + totalIterations + ": Swapped '" + neighborMapping.get(letter1) + "' and '" + neighborMapping.get(letter2) + "' -> Score: " + neighborScore);
            }

            // Keep track of top results
            if (topResults.size() < 5) {
                topResults.add(new Result(neighborScore, neighborMapping, neighborDecryption));
            } else if (neighborScore > topResults.peek().score) {
                topResults.poll();
                topResults.add(new Result(neighborScore, neighborMapping, neighborDecryption));
            }

            // If the neighbor score is better, adopt it
            if (neighborScore > currentScore) {
                currentMapping = neighborMapping;
                currentScore = neighborScore;
                iterationsWithoutImprovement = 0;

                System.out.println("Accepted new mapping with improved score: " + currentScore);
            } else {
                iterationsWithoutImprovement++;
            }

            // Check attempts limit
            if (ATTEMPTS_LIMIT != -1 && totalIterations >= ATTEMPTS_LIMIT) {
                System.out.println("\nReached attempts limit of " + ATTEMPTS_LIMIT + ".");
                break;
            }
        }

        // Output top 5 results
        System.out.println("\nTop 5 Results:");
        List<Result> topResultsList = new ArrayList<>(topResults);
        topResultsList.sort((r1, r2) -> Double.compare(r2.score, r1.score)); // Sort in descending order

        for (int i = 0; i < topResultsList.size(); i++) {
            Result result = topResultsList.get(i);
            System.out.println("\nResult " + (i + 1) + ": Score = " + result.score);
            System.out.println("Mapping:");
            for (Map.Entry<Character, Character> entry : result.mapping.entrySet()) {
                System.out.print(entry.getKey() + "->" + entry.getValue() + " ");
            }
            System.out.println("\nDecrypted Text:");
            System.out.println(formatOutput(result.decryption));
        }

        return currentMapping;
    }

    // Apply the mapping to the ciphertext
    private static String applyMapping(String text, Map<Character, Character> mapping) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            char mappedChar = mapping.getOrDefault(ch, ch);
            sb.append(mappedChar);
        }
        return sb.toString();
    }

    private static String formatOutput(String text) {
        // Break text into blocks of 60 characters for readability
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            sb.append(text, index, Math.min(index + 5, text.length()));
            sb.append(' ');
            index += 5;
        }
        return sb.toString();
    }

    // Class to store results
    private static class Result {
        double score;
        Map<Character, Character> mapping;
        String decryption;

        Result(double score, Map<Character, Character> mapping, String decryption) {
            this.score = score;
            this.mapping = new HashMap<>(mapping);
            this.decryption = decryption;
        }
    }
}
