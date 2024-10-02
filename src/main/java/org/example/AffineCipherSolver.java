package org.example;

import java.util.*;

public class AffineCipherSolver {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Read the ciphertext
        System.out.println("Enter the ciphertext:");
        String ciphertext = scanner.nextLine().toUpperCase().replaceAll("[^A-Z]", "");

        // Step 2: Perform frequency analysis on the ciphertext
        Map<Character, Integer> freqMap = frequencyAnalysis(ciphertext);

        // Step 3: Estimate possible mappings
        // Common letters in English (you can adjust for Czech if needed)
        char[] commonPlainLetters = {'E', 'T', 'A', 'O', 'I', 'N', 'S', 'H', 'R', 'D'};

        // Get the most frequent letters in the ciphertext
        List<Character> commonCipherLetters = getMostFrequentLetters(freqMap);

        // Step 4 & 5: Try different pairs of mappings to solve for A and B
        boolean foundSolution = false;
        int[] keys = new int[2]; // keys[0] = A, keys[1] = B

        outerLoop:
        for (int i = 0; i < Math.min(5, commonPlainLetters.length); i++) {
            for (int j = i + 1; j < Math.min(5, commonPlainLetters.length); j++) {
                char p1 = commonPlainLetters[i];
                char p2 = commonPlainLetters[j];

                for (int k = 0; k < Math.min(5, commonCipherLetters.size()); k++) {
                    for (int l = k + 1; l < Math.min(5, commonCipherLetters.size()); l++) {
                        char c1 = commonCipherLetters.get(k);
                        char c2 = commonCipherLetters.get(l);

                        // Convert letters to numbers (A=0, B=1, ..., Z=25)
                        int p1Val = p1 - 'A';
                        int p2Val = p2 - 'A';
                        int c1Val = c1 - 'A';
                        int c2Val = c2 - 'A';

                        // Solve for A and B
                        int[] possibleKeys = solveForKeys(p1Val, p2Val, c1Val, c2Val);
                        if (possibleKeys != null) {
                            keys = possibleKeys;
                            foundSolution = true;
                            System.out.println("Possible mapping found:");
                            System.out.println(p1 + " -> " + c1);
                            System.out.println(p2 + " -> " + c2);
                            System.out.println("Keys: A = " + keys[0] + ", B = " + keys[1]);
                            break outerLoop;
                        }
                    }
                }
            }
        }

        if (foundSolution) {
            // Step 6: Decrypt the ciphertext using the found keys
            String plaintext = decryptAffine(ciphertext, keys[0], keys[1]);
            System.out.println("\nDecrypted Text:");
            System.out.println(formatOutput(plaintext));
        } else {
            System.out.println("No solution found with the estimated mappings.");
        }

        scanner.close();
    }

    // Perform frequency analysis on the ciphertext
    private static Map<Character, Integer> frequencyAnalysis(String text) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char ch : text.toCharArray()) {
            freqMap.put(ch, freqMap.getOrDefault(ch, 0) + 1);
        }
        return freqMap;
    }

    // Get the most frequent letters in the ciphertext
    private static List<Character> getMostFrequentLetters(Map<Character, Integer> freqMap) {
        List<Character> letters = new ArrayList<>(freqMap.keySet());
        letters.sort((a, b) -> freqMap.get(b) - freqMap.get(a));
        return letters;
    }

    // Solve for keys A and B given two plaintext-ciphertext letter pairs
    private static int[] solveForKeys(int p1, int p2, int c1, int c2) {
        int m = 26; // Size of the alphabet

        // Set up the equations:
        // c1 = (A * p1 + B) mod 26
        // c2 = (A * p2 + B) mod 26

        // Compute differences to eliminate B:
        // (c1 - c2) mod 26 = A * (p1 - p2) mod 26

        int pDiff = (p1 - p2 + m) % m;
        int cDiff = (c1 - c2 + m) % m;

        // Compute modular inverse of pDiff modulo m
        int pDiffInv = modInverse(pDiff, m);
        if (pDiffInv == -1) {
            return null; // No inverse exists, cannot solve for A
        }

        int A = (cDiff * pDiffInv) % m;
        if (A <= 0 || gcd(A, m) != 1) {
            return null; // A must be coprime with m
        }

        // Solve for B using one of the equations
        int B = (c1 - A * p1) % m;
        B = (B + m) % m; // Ensure B is positive

        return new int[]{A, B};
    }

    // Decrypt the ciphertext using the affine cipher formula
    private static String decryptAffine(String ciphertext, int A, int B) {
        int m = 26;
        int A_inv = modInverse(A, m);
        if (A_inv == -1) {
            throw new IllegalArgumentException("Multiplicative inverse of A does not exist.");
        }

        StringBuilder plaintext = new StringBuilder();
        for (char ch : ciphertext.toCharArray()) {
            int c = ch - 'A';
            int p = (A_inv * (c - B + m)) % m;
            plaintext.append((char) (p + 'A'));
        }
        return plaintext.toString();
    }

    // Compute the modular inverse of a modulo m
    private static int modInverse(int a, int m) {
        int[] res = extendedGCD(a, m);
        int gcd = res[0];
        int x = res[1];
        if (gcd != 1) {
            return -1; // Inverse does not exist
        } else {
            return (x % m + m) % m;
        }
    }

    // Extended Euclidean Algorithm
    private static int[] extendedGCD(int a, int b) {
        if (b == 0) {
            return new int[]{a, 1, 0};
        } else {
            int[] res = extendedGCD(b, a % b);
            int d = res[0];
            int x1 = res[2];
            int y1 = res[1] - (a / b) * res[2];
            return new int[]{d, x1, y1};
        }
    }

    // Compute the greatest common divisor
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // Format output text into readable blocks
    private static String formatOutput(String text) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int lineLength = 60;
        while (index < text.length()) {
            sb.append(text, index, Math.min(index + lineLength, text.length()));
            sb.append('\n');
            index += lineLength;
        }
        return sb.toString();
    }
}
