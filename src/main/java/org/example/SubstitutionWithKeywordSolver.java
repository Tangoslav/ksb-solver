package org.example;

import java.util.*;
import java.io.*;

import static org.example.Utils.getInputAndProcess;

public class SubstitutionWithKeywordSolver {

    private static final String NGRAM_FILES_DIR = "src/main/resources/";
    private static final String[] NGRAM_FILES = {
            "english_monograms.txt",
            "english_bigrams.txt",
            "english_trigrams.txt",
            "english_quadgrams.txt",
            "english_quintgrams.txt"
    };

    private final NGramScorer ngramScorer;

    public SubstitutionWithKeywordSolver(NGramScorer ngramScorer) {
        this.ngramScorer = ngramScorer;
    }

    // Public method to execute the substitution solver logic
    public List<Result> solve(String ciphertext, int keywordLength, int numIterations, int topResultsLimit) {
        ciphertext = preprocessText(ciphertext);

        // Step 1: Generate random keywords and evaluate mappings
        List<Result> topResults = new ArrayList<>();
        for (int length = 1; length <= keywordLength; length++) {
            System.out.println("Processing keywords of length: " + length);
            List<Result> resultsForLength = randomKeywordSearch(ciphertext, length, numIterations, topResultsLimit);
            topResults.addAll(resultsForLength);
        }

        // Step 2: Evaluate permutations of top results and find the best ones
        System.out.println("Evaluating permutations of top results...");

        return evaluatePermutations(topResults, ciphertext, topResultsLimit);
    }

    // Preprocess the text by removing non-alphabetic characters and converting to uppercase
    private String preprocessText(String text) {
        return text.toUpperCase().replaceAll("[^A-Z]", "");
    }

    // Randomly generate keywords and evaluate mappings
    private List<Result> randomKeywordSearch(String ciphertext, int keywordLength, int numIterations, int topResultsLimit) {
        List<Result> topResults = new ArrayList<>();
        Random random = new Random();
        Set<String> triedKeywords = new HashSet<>();

        int iterations = numIterations / keywordLength;  // Adjust iterations per length

        for (int i = 0; i < iterations; i++) {
            String keyword = generateRandomKeyword(random, keywordLength);
            if (triedKeywords.contains(keyword)) {
                continue;
            }
            triedKeywords.add(keyword);

            List<Character> cipherAlphabet = buildCipherAlphabet(keyword);
            Map<Character, Character> mapping = buildMappingFromCipherAlphabet(cipherAlphabet);
            String decryption = applyMapping(ciphertext, mapping);

            double score = ngramScorer.score(decryption);

            Result result = new Result(score, keyword, cipherAlphabet, decryption);

            // Keep the top results
            if (topResults.size() < topResultsLimit) {
                topResults.add(result);
                topResults.sort(Comparator.comparingDouble(r -> -r.score));
            } else if (score > topResults.get(topResults.size() - 1).score) {
                topResults.set(topResults.size() - 1, result);
                topResults.sort(Comparator.comparingDouble(r -> -r.score));
            }

            // Print progress every 1000 iterations
            if ((i + 1) % 1000 == 0) {
                System.out.println("Iteration " + (i + 1) + " / " + iterations + " for keyword length " + keywordLength);
                System.out.println("Top result so far: " + topResults.get(0).keyword + " | Score: " + topResults.get(0).score);
            }
        }

        return topResults;
    }

    // Evaluate permutations of keywords and select the best results
    private List<Result> evaluatePermutations(List<Result> topResults, String ciphertext, int topResultsLimit) {
        PriorityQueue<Result> permutationResults = new PriorityQueue<>(topResultsLimit, Comparator.comparingDouble(r -> r.score));

        for (Result result : topResults) {
            List<String> permutations = generatePermutations(result.keyword);
            for (String permutedKeyword : permutations) {
                List<Character> cipherAlphabet = buildCipherAlphabet(permutedKeyword);
                Map<Character, Character> mapping = buildMappingFromCipherAlphabet(cipherAlphabet);
                String decryption = applyMapping(ciphertext, mapping);

                double score = ngramScorer.score(decryption);

                Result permutedResult = new Result(score, permutedKeyword, cipherAlphabet, decryption);

                if (permutationResults.size() < topResultsLimit) {
                    permutationResults.add(permutedResult);
                } else {
                    assert permutationResults.peek() != null;
                    if (score > permutationResults.peek().score) {
                        permutationResults.poll();
                        permutationResults.add(permutedResult);
                    }
                }
            }

            // Print progress after evaluating permutations for each keyword
            System.out.println("Evaluated permutations for keyword: " + result.keyword);
            assert permutationResults.peek() != null;
            System.out.println("Top permutation: " + permutationResults.peek().keyword + " | Score: " + permutationResults.peek().score);
        }

        List<Result> sortedResults = new ArrayList<>(permutationResults);
        sortedResults.sort(Comparator.comparingDouble(r -> -r.score));

        return sortedResults;
    }

    // Generate random keyword of a specified length
    private String generateRandomKeyword(Random random, int length) {
        List<Character> letters = new ArrayList<>();
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            letters.add(ch);
        }
        Collections.shuffle(letters, random);
        StringBuilder keyword = new StringBuilder();
        for (int i = 0; i < length; i++) {
            keyword.append(letters.get(i));
        }
        return keyword.toString();
    }

    // Build cipher alphabet from keyword
    private List<Character> buildCipherAlphabet(String keyword) {
        Set<Character> seen = new LinkedHashSet<>();
        for (char ch : keyword.toCharArray()) {
            seen.add(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            seen.add(ch);
        }
        return new ArrayList<>(seen);
    }

    // Build character mapping from cipher alphabet
    private Map<Character, Character> buildMappingFromCipherAlphabet(List<Character> cipherAlphabet) {
        Map<Character, Character> mapping = new HashMap<>();
        char plainChar = 'A';
        for (char cipherChar : cipherAlphabet) {
            mapping.put(cipherChar, plainChar);
            plainChar++;
        }
        return mapping;
    }

    // Apply character mapping to a text
    private String applyMapping(String text, Map<Character, Character> mapping) {
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            result.append(mapping.getOrDefault(ch, ch));
        }
        return result.toString();
    }

    // Generate all permutations of a keyword
    private List<String> generatePermutations(String keyword) {
        List<String> permutations = new ArrayList<>();
        permute(keyword.toCharArray(), 0, permutations);
        return permutations;
    }

    // Helper method for permutation generation
    private void permute(char[] arr, int k, List<String> permutations) {
        if (k == arr.length) {
            permutations.add(new String(arr));
        } else {
            for (int i = k; i < arr.length; i++) {
                swap(arr, k, i);
                permute(arr, k + 1, permutations);
                swap(arr, k, i);
            }
        }
    }

    // Swap two characters in an array
    private void swap(char[] arr, int i, int j) {
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // Helper class to store results
    public static class Result {
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

    // Static method to initialize the n-gram scorer
    public static NGramScorer loadNGramScorer() throws IOException {
        return new NGramScorer(NGRAM_FILES_DIR, NGRAM_FILES);
    }

    public static void main(String[] args) {
        try {
            // Load the N-gram scorer
            NGramScorer ngramScorer = SubstitutionWithKeywordSolver.loadNGramScorer();

            // Create the substitution solver with the loaded N-gram scorer
            SubstitutionWithKeywordSolver solver = new SubstitutionWithKeywordSolver(ngramScorer);

            // Get the ciphertext from the user
            String ciphertext = getInputAndProcess();

            int maxKeywordLength = 4;
            int numIterations = 100000000;
            int topResultsLimit = 10;

            // Solve for the keyword using the provided inputs
            List<SubstitutionWithKeywordSolver.Result> results = solver.solve(ciphertext, maxKeywordLength, numIterations, topResultsLimit);

            // Output the top results
            System.out.println("\nTop results:");
            for (int i = 0; i < results.size(); i++) {
                SubstitutionWithKeywordSolver.Result result = results.get(i);
                System.out.println("Result " + (i + 1) + ":");
                System.out.println("Keyword: " + result.keyword);
                System.out.println("Score: " + result.score);
                System.out.println("Decrypted text: " + result.decryption);
                System.out.println("--------------------------------");
            }
        } catch (IOException e) {
            System.err.println("Error loading N-gram scorer: " + e.getMessage());
        }
    }
}
