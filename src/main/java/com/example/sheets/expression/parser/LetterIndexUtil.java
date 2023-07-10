package com.example.sheets.expression.parser;

public final class LetterIndexUtil {
    private static final int LETTERS_COUNT = 26;
    public static String toLetterIndex(int numberIndex) {
        var sb = new StringBuilder();
        while (numberIndex > 0) {
            int mod = (numberIndex - 1) % LETTERS_COUNT;
            sb.append((char) (mod + 'A'));
            numberIndex = (numberIndex - mod) / LETTERS_COUNT;
        }
        return sb.reverse().toString();
    }

    public static int toNumberIndex(String letterIndex) {
        int sum = 0;
        for (char c : letterIndex.toCharArray()) {
            sum *= LETTERS_COUNT;
            sum += (c - 'A' + 1);
        }
        return sum;
    }

    private LetterIndexUtil() {
    }
}
