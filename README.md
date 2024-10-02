
# Cipher Solvers

This repository contains three Java classes designed to perform cryptographic analysis on ciphertexts. The goal is to decrypt text using methods such as affine cipher solving and substitution ciphers with keyword analysis. The following classes are included:

## 1. AffineCipherSolver

The `AffineCipherSolver` class solves affine ciphers by performing frequency analysis on ciphertexts and attempting to map common English letter pairs to corresponding cipher text. The affine cipher is solved by estimating keys A and B through letter mapping, and then decrypting the cipher text.

### Key Steps:
1. **N-Gram Loading**: The class loads frequency files containing common monograms, bigrams, trigrams, etc.
2. **Frequency Analysis**: The cipher text undergoes frequency analysis to identify the most common cipher letters.
3. **Key Estimation**: The solver uses frequency mapping to estimate the keys A and B by mapping plaintext-ciphertext letter pairs.
4. **Decryption**: The cipher text is decrypted using the affine decryption formula and scored using n-gram frequency analysis.
5. **Top Results**: The top 5 decrypted texts, along with their mappings and keys, are displayed.

### Example Input:
```plaintext
Enter the ciphertext:
GFSIGLTHYOZMGXREXOV
```

### Example Output:
```plaintext
Top 5 Most Probable Decryptions:
Result 1:
Score: -65.87
Mapping: E->H, T->S
Keys: A = 7, B = 18
Decrypted Text: EXAMPLERE...
```

## 2. NGramScorer

The `NGramScorer` class is responsible for scoring decrypted text based on n-gram frequency analysis. This is crucial in determining the likelihood that a decrypted text is accurate.

### How It Works:
1. **N-Gram Loading**: The class loads monogram, bigram, trigram, quadgram, and quintgram frequencies from text files.
2. **Log Probability Calculation**: It calculates the log probability of each n-gram in the text, summing them to generate a score.
3. **Scoring**: The class uses the loaded n-gram frequencies to calculate the likelihood of the decrypted text.

### N-Gram Files:
Ensure the following files are available in the `src/main/resources/` directory:
- `english_monograms.txt`
- `english_bigrams.txt`
- `english_trigrams.txt`
- `english_quadgrams.txt`
- `english_quintgrams.txt`

## 3. SubstitutionWithKeywordSolver

The `SubstitutionWithKeywordSolver` class attempts to solve substitution ciphers using random keyword generation and analysis based on n-gram frequencies.

### Key Features:
1. **Random Keyword Generation**: The solver generates random keywords of varying lengths and applies them to generate a cipher alphabet.
2. **Keyword Permutation**: For each keyword, the solver tries all possible permutations to determine the most likely decryption.
3. **N-Gram Scoring**: Like the AffineCipherSolver, this class scores decrypted text based on n-gram frequencies.
4. **Top Results**: The solver displays the top 5 decryption results, sorted by n-gram scores.

### Example Input:
```plaintext
Enter the ciphertext:
GFSIGLTHYOZMGXREXOV
```

### Example Output:
```plaintext
Top 5 Permutation Results:
Result 1: Score = -72.45
Keyword: NIGHT
Cipher Alphabet: [N, I, G, H, T, A, B, C...]
Decrypted Text: EXAMPLERE...
```

## Installation & Usage

1. **Clone the repository**.
2. **Place N-Gram Files**: Ensure that the following n-gram frequency files are available in the `src/main/resources/` directory:
    - `english_monograms.txt`
    - `english_bigrams.txt`
    - `english_trigrams.txt`
    - `english_quadgrams.txt`
    - `english_quintgrams.txt`
3. **Run the classes** in your favorite Java IDE (e.g., IntelliJ or Eclipse).
4. **Input your ciphertext** when prompted and view the decryption results.

## License
This project is open-source and free to use under the MIT License.

