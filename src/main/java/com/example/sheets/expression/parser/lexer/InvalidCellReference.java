package com.example.sheets.expression.parser.lexer;

public final class InvalidCellReference extends LexerException {
    public InvalidCellReference(String message) {
        super(message);
    }
}
