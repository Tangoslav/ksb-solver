package org.example;

import java.io.*;
import java.util.*;

public class NGramScorer {
    private Map<Integer, Map<String, Double>> ngramMaps;
    private Map<Integer, Double> floorValues;

    public NGramScorer(String ngramDir, String[] ngramFiles) throws IOException {
        ngramMaps = new HashMap<>();
        floorValues = new HashMap<>();

        for (String filename : ngramFiles) {
            int ngramLength = getNgramLengthFromFilename(filename);
            Map<String, Double> ngrams = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(ngramDir + filename));
            String line;
            double total = 0.0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    String key = parts[0].toUpperCase();
                    int count = Integer.parseInt(parts[1]);
                    ngrams.put(key, (double) count);
                    total += count;
                }
            }
            br.close();

            if (ngrams.isEmpty()) {
                throw new IllegalArgumentException("N-gram file " + filename + " is empty.");
            }

            // Convert counts to log probabilities
            for (Map.Entry<String, Double> entry : ngrams.entrySet()) {
                double logProbability = Math.log10(entry.getValue() / total);
                entry.setValue(logProbability);
            }
            double floor = Math.log10(0.01 / total);

            ngramMaps.put(ngramLength, ngrams);
            floorValues.put(ngramLength, floor);

            System.out.println("Loaded " + ngrams.size() + " " + ngramLength + "-grams from " + filename);
        }
    }

    private int getNgramLengthFromFilename(String filename) {
        if (filename.contains("monograms")) {
            return 1;
        } else if (filename.contains("bigrams")) {
            return 2;
        } else if (filename.contains("trigrams")) {
            return 3;
        } else if (filename.contains("quadgrams")) {
            return 4;
        } else if (filename.contains("quintgrams")) {
            return 5;
        } else {
            throw new IllegalArgumentException("Invalid n-gram filename: " + filename);
        }
    }

    public double score(String text) {
        double score = 0.0;

        for (Map.Entry<Integer, Map<String, Double>> entry : ngramMaps.entrySet()) {
            int ngramLength = entry.getKey();
            Map<String, Double> ngrams = entry.getValue();
            double floor = floorValues.get(ngramLength);

            for (int i = 0; i <= text.length() - ngramLength; i++) {
                String ngram = text.substring(i, i + ngramLength);
                if (ngrams.containsKey(ngram)) {
                    score += ngrams.get(ngram);
                } else {
                    score += floor;
                }
            }
        }

        return score;
    }
}
