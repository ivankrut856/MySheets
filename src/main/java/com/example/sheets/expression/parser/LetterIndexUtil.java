package com.example.sheets.expression.parser;

public class LetterIndexUtil {

    public static String toLetterIndex(int numberIndex) {
        var sb = new StringBuilder();
        while (numberIndex > 0) {
            int mod = (numberIndex - 1) % 26;
            sb.append((char)(mod + 'A'));
            numberIndex = (numberIndex - mod) / 26;
        }
        return sb.reverse().toString();
    }

    public static int toNumberIndex(String letterIndex) {
        int sum = 0;
        for (char c : letterIndex.toCharArray()) {
            sum *= 26;
            sum += (c - 'A' + 1);
        }
        return sum;
    }
}
