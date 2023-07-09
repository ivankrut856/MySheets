package com.example.sheets.expression.parser.lexer;

public class InvalidCellReference extends LexerException {
    public InvalidCellReference(String message) {
        super(message);
    }
}
