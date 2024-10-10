package org.example;

import java.util.Scanner;

public class Utils {
    public static String getInputAndProcess() {
        Scanner scanner = new Scanner(System.in);
        StringBuilder input = new StringBuilder();
        String line;

        // Step 1: Read the ciphertext
        System.out.println("Enter the ciphertext:");
        while (true) {
            line = scanner.nextLine();  // Read a single line
            if (line.isEmpty()) {  // Stop when an empty line is entered
                break;
            }
            input.append(line).append(" ");  // Append line to input with a space between lines
        }
        String ciphertext = input.toString().toUpperCase().replaceAll("[^A-Z]", "");
        System.out.println("Processed Ciphertext: " + ciphertext);
        return ciphertext;
    }
}
