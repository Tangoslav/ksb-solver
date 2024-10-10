package org.example;

public class VigenereVisualiser {

    public static void main(String[] args) {
        String key = "MOUSE"; // You can change this to any key you like
        visualizeVigenereDecoding(key);
    }

    public static void visualizeVigenereDecoding(String key) {
        key = key.toUpperCase().replaceAll("[^A-Z]", ""); // Clean the key
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        int letterWidth = 2; // Width of each letter column

        // Print the header alphabet with fixed spacing
        System.out.printf("%-15s", "Plain text:");
        for (char c : alphabet) {
            System.out.printf("%-" + letterWidth + "s", c);
        }
        System.out.println();

        // For each character in the key, print the shifted alphabet
        for (char keyChar : key.toCharArray()) {
            int shift = keyChar - 'A'; // Calculate shift amount

            // Prepare the label for the line with fixed width
            String label = String.format("Shift '%c' %d:", keyChar, (26-shift)%26);
            System.out.printf("%-15s", label);

            for (char c : alphabet) {
                int shiftedIndex = (c - 'A' + shift) % 26; // For encoding
                if (shiftedIndex < 0) {
                    shiftedIndex += 26;
                }
                System.out.printf("%-" + letterWidth + "s", alphabet[shiftedIndex]);
            }
            System.out.println();
        }
    }
}
