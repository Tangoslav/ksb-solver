package org.example;

import java.util.*;

public class PlayfairCipher {

    private final char[][] cipherSquare = new char[5][5];
    private final Map<Character, Integer> charToRow = new HashMap<>();
    private final Map<Character, Integer> charToCol = new HashMap<>();

    public static void main(String[] args) {
        String encryptedText = "LRIXH FESUI PDGKO PDKOQ EFTZD KHTIQ AIGCB XHMUA GKSEO FRHDP DLCXO QSYFR QOTFB HQIYA\n" +
                "GHPDM XCFDG KIMZM DZGEF HTYMW GHQGC"; // The plaintext message to encode


        PlayfairCipher playfair = new PlayfairCipher();
        playfair.createCipherSquare("JAHODY"); // Create the cipher square with the keyword

        String decryptedText = playfair.decrypt(encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);
    }

    /**
     * Creates the Playfair cipher square using the provided keyword.
     *
     * @param keyword the keyword used to generate the cipher square
     */
    public void createCipherSquare(String keyword) {
        String processedKey = processKeyword(keyword);
        fillCipherSquare(processedKey);
        buildCharPositionMaps();
        printCipherSquare();
    }

    /**
     * Processes the keyword by removing duplicates and non-letter characters,
     * converting to uppercase, and merging 'I' and 'J'.
     *
     * @param keyword the raw keyword input
     * @return the processed keyword
     */
    private String processKeyword(String keyword) {
        keyword = keyword.toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");

        // Use a LinkedHashSet to maintain insertion order and remove duplicates
        Set<Character> keyChars = new LinkedHashSet<>();
        for (char c : keyword.toCharArray()) {
            keyChars.add(c);
        }

        StringBuilder processedKey = new StringBuilder();
        for (char c : keyChars) {
            processedKey.append(c);
        }

        return processedKey.toString();
    }

    /**
     * Fills the cipher square with the processed keyword followed by the remaining letters.
     *
     * @param processedKey the processed keyword
     */
    private void fillCipherSquare(String processedKey) {
        Set<Character> usedChars = new LinkedHashSet<>();
        for (char c : processedKey.toCharArray()) {
            usedChars.add(c);
        }

        // Add remaining letters to the set (excluding 'J' because it's merged with 'I')
        for (char c = 'A'; c <= 'Z'; c++) {
            if (c == 'J') {
                continue; // Skip 'J' as it's merged with 'I'
            }
            usedChars.add(c);
        }

        // Fill the cipher square
        int index = 0;
        for (Character c : usedChars) {
            cipherSquare[index / 5][index % 5] = c;
            index++;
        }
    }

    /**
     * Builds maps to quickly find the row and column of each character in the cipher square.
     */
    private void buildCharPositionMaps() {
        for (int row = 0; row < cipherSquare.length; row++) {
            for (int col = 0; col < cipherSquare[row].length; col++) {
                char c = cipherSquare[row][col];
                charToRow.put(c, row);
                charToCol.put(c, col);
            }
        }
    }

    /**
     * Decrypts the ciphertext using the Playfair cipher rules.
     *
     * @param ciphertext the encrypted ciphertext
     * @return the decrypted plaintext
     */
    public String decrypt(String ciphertext) {
        // Preprocess the ciphertext
        ciphertext = ciphertext.toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");

        // Ensure the ciphertext length is even
        if (ciphertext.length() % 2 != 0) {
            System.out.println("Ciphertext length is odd, adding 'X' to make it even.");
            ciphertext += "X";
        }

        StringBuilder plaintext = new StringBuilder();

        for (int i = 0; i < ciphertext.length(); i += 2) {
            char first = ciphertext.charAt(i);
            char second = ciphertext.charAt(i + 1);

            // Check if characters are in the cipher square
            if (!charToRow.containsKey(first) || !charToRow.containsKey(second)) {
                System.out.println("Error: Character '" + first + "' or '" + second + "' not in cipher square.");
                continue; // Skip this pair
            }

            int row1 = charToRow.get(first);
            int col1 = charToCol.get(first);
            int row2 = charToRow.get(second);
            int col2 = charToCol.get(second);

            if (row1 == row2) {
                // Same row - replace with letters to the left
                col1 = (col1 + 4) % 5; // Equivalent to (col1 - 1 + 5) % 5
                col2 = (col2 + 4) % 5;
            } else if (col1 == col2) {
                // Same column - replace with letters above
                row1 = (row1 + 4) % 5; // Equivalent to (row1 - 1 + 5) % 5
                row2 = (row2 + 4) % 5;
            } else {
                // Rectangle - swap columns
                int temp = col1;
                col1 = col2;
                col2 = temp;
            }

            char decFirst = cipherSquare[row1][col1];
            char decSecond = cipherSquare[row2][col2];
            plaintext.append(decFirst).append(decSecond);
        }

        return plaintext.toString();
    }


    /**
     * Prints the cipher square.
     */
    public void printCipherSquare() {
        System.out.println("Playfair Cipher Square:");
        for (char[] chars : cipherSquare) {
            for (char aChar : chars) {
                System.out.print(aChar + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
