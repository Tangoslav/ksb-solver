package org.example;

import java.util.*;
import java.io.*;

public class SubstitutionWithKeywordSolver {

    private static final int KEYWORD_LENGTH = 5; // Adjust as needed
    private static final int NUM_ITERATIONS = 1000000; // Number of random keywords to try
    private static final String NGRAM_FILES_DIR = "src/main/resources/"; // Directory containing n-gram frequency files
    private static final String[] NGRAM_FILES = {
            "english_monograms.txt",
            "english_bigrams.txt",
            "english_trigrams.txt",
            "english_quadgrams.txt",
            "english_quintgrams.txt"
    };

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

        // Randomly generate and evaluate mappings
        List<Result> topResults = randomKeywordSearch(ciphertext, ngramScorer);

        // For each top result, generate all permutations of the keyword
        List<Result> permutationResults = new ArrayList<>();
        for (Result result : topResults) {
            List<String> permutations = generatePermutations(result.keyword);
            for (String permutedKeyword : permutations) {
                // Build the cipher alphabet with the permuted keyword
                List<Character> cipherAlphabet = buildCipherAlphabet(permutedKeyword);

                // Build the mapping from cipher alphabet
                Map<Character, Character> mapping = buildMappingFromCipherAlphabet(cipherAlphabet);

                // Apply the mapping to the ciphertext
                String decryption = applyMapping(ciphertext, mapping);

                // Compute the n-gram score
                double score = ngramScorer.score(decryption);

                // Store the result
                permutationResults.add(new Result(score, permutedKeyword, cipherAlphabet, decryption));
            }
        }

        // Sort permutation results by score in descending order
        permutationResults.sort(Comparator.comparingDouble(r -> -r.score));

        // Output all permutation results
        System.out.println("\nPermutation Results:");
        int count = 1;
        for (Result result : permutationResults) {
            System.out.println("\nResult " + count + ": Score = " + result.score);
            System.out.println("Keyword: " + result.keyword);
            System.out.println("Cipher Alphabet: " + result.cipherAlphabet);
            System.out.println("Decrypted Text:");
            System.out.println(formatOutput(result.decryption));
            count++;
        }

        scanner.close();
    }

    // Randomly generate keywords and evaluate mappings
    private static List<Result> randomKeywordSearch(String ciphertext, NGramScorer ngramScorer) {
        List<Result> topResults = new ArrayList<>();
        Random random = new Random();
        Set<String> triedKeywords = new HashSet<>();

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            // Generate a random keyword
            String keyword = generateRandomKeyword(random);

            // Skip if we've already tried this keyword
            if (triedKeywords.contains(keyword)) {
                continue;
            }
            triedKeywords.add(keyword);

            // Build the cipher alphabet
            List<Character> cipherAlphabet = buildCipherAlphabet(keyword);

            // Build the mapping from cipher alphabet
            Map<Character, Character> mapping = buildMappingFromCipherAlphabet(cipherAlphabet);

            // Apply the mapping to the ciphertext
            String decryption = applyMapping(ciphertext, mapping);

            // Compute the n-gram score
            double score = ngramScorer.score(decryption);

            // Store the result
            Result result = new Result(score, keyword, cipherAlphabet, decryption);

            // Keep top results
            if (topResults.size() < 5) {
                topResults.add(result);
                topResults.sort(Comparator.comparingDouble(r -> -r.score));
            } else if (score > topResults.get(topResults.size() - 1).score) {
                topResults.set(topResults.size() - 1, result);
                topResults.sort(Comparator.comparingDouble(r -> -r.score));
            }

            // Optionally, log progress
            if ((i + 1) % 10000 == 0) {
                System.out.println("Iterations completed: " + (i + 1));
            }
        }

        return topResults;
    }

    // Generate a random keyword of length KEYWORD_LENGTH
    private static String generateRandomKeyword(Random random) {
        List<Character> letters = new ArrayList<>();
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            letters.add(ch);
        }
        Collections.shuffle(letters, random);
        StringBuilder keywordBuilder = new StringBuilder();
        for (int i = 0; i < KEYWORD_LENGTH; i++) {
            keywordBuilder.append(letters.get(i));
        }
        return keywordBuilder.toString();
    }

    // Build the cipher alphabet from the keyword
    private static List<Character> buildCipherAlphabet(String keyword) {
        Set<Character> seen = new LinkedHashSet<>();
        for (char ch : keyword.toCharArray()) {
            seen.add(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            if (!seen.contains(ch)) {
                seen.add(ch);
            }
        }
        return new ArrayList<>(seen);
    }

    // Build the mapping from cipher alphabet
    private static Map<Character, Character> buildMappingFromCipherAlphabet(List<Character> cipherAlphabet) {
        Map<Character, Character> mapping = new HashMap<>();
        char plainChar = 'A';
        for (char cipherChar : cipherAlphabet) {
            mapping.put(cipherChar, plainChar);
            plainChar++;
        }
        return mapping;
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
        // Break text into blocks of 5 characters for readability
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            sb.append(text, index, Math.min(index + 5, text.length()));
            sb.append(' ');
            index += 5;
        }
        return sb.toString();
    }

    // Generate all permutations of a keyword
    private static List<String> generatePermutations(String keyword) {
        List<String> permutations = new ArrayList<>();
        permute(keyword.toCharArray(), 0, permutations);
        return permutations;
    }

    // Recursive helper method to generate permutations
    private static void permute(char[] arr, int k, List<String> permutations) {
        if (k == arr.length) {
            permutations.add(new String(arr));
        } else {
            for (int i = k; i < arr.length; i++) {
                swap(arr, k, i);
                permute(arr, k + 1, permutations);
                swap(arr, k, i); // backtrack
            }
        }
    }

    // Swap two characters in an array
    private static void swap(char[] arr, int i, int j) {
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // Class to store results
    private static class Result {
        double score;
        String keyword;
        List<Character> cipherAlphabet;
        String decryption;

        Result(double score, String keyword, List<Character> cipherAlphabet, String decryption) {
            this.score = score;
            this.keyword = keyword;
            this.cipherAlphabet = new ArrayList<>(cipherAlphabet);
            this.decryption = decryption;
        }
    }
}
