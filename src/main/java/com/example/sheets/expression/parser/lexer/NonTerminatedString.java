package com.example.sheets.expression.parser.lexer;

public final class NonTerminatedString extends LexerException {
    public NonTerminatedString(String message) {
        super(message);
    }
}
