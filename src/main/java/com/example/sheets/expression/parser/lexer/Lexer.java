package com.example.sheets.expression.parser.lexer;

import com.example.sheets.expression.parser.LetterIndexUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.sheets.expression.parser.lexer.Lexer.TokenType.*;

public final class Lexer {

    private String s;
    private ArrayList<Token> tokens;
    private int start;
    private int current;

    public Lexer(String s) {
        this.s = s;
    }

    public List<Token> toTokens() throws LexerException {
        start = 0;
        current = 0;
        tokens = new ArrayList<>();

        if (!s.startsWith("=")) {
            try {
                return Collections.singletonList(new NumberLiteral(Double.parseDouble(s)));
            } catch (java.lang.NumberFormatException ignored) {
                return Collections.singletonList(new StrLiteral(s));
            }
        }

        s = s.substring(1);

        while (!isEndOfString()) {
            start = current;
            char c = next();
            if (c == '+') {
                addFixedToken(Plus);
            } else if (c == '-') {
                addFixedToken(Minus);
            } else if (c == '/') {
                addFixedToken(Slash);
            } else if (c == '*') {
                addFixedToken(Asterisk);
            } else if (c == '(') {
                addFixedToken(LeftBracket);
            } else if (c == ')') {
                addFixedToken(RightBracket);
            } else if (c == ',') {
                addFixedToken(Comma);
            } else if (c == ' ' || c == '\t' || c == '\n') {
                continue;
            } else if (c == '"') {
                var strLiteralValue = parseString();
                tokens.add(new StrLiteral(strLiteralValue));
            } else if (isArabicNumeral(c)) {
                var numberLiteralValue = parseNumber();
                tokens.add(new NumberLiteral(numberLiteralValue));
            } else if (Character.isLetter(c)) {
                if ('A' <= c && c <= 'Z') {
                    tokens.add(parseCellRef());
                } else {
                    var lexeme = parseIdent();
                    tokens.add(new Ident(lexeme));
                }
            } else {
                throw new InvalidSymbol("Invalid symbol %c at position %d".formatted(c, start));
            }
        }
        return tokens;
    }

    private boolean isEndOfString() {
        return current >= s.length();
    }

    private boolean isArabicNumeral(char c) {
        return '0' <= c && c <= '9';
    }

    private char next() {
        return s.charAt(current++);
    }

    private char getNext() {
        if (isEndOfString()) return '\0';
        return s.charAt(current);
    }

    private void addFixedToken(TokenType type) {
        tokens.add(new Fixed(type));
    }

    private String parseString() throws LexerException {
        while (!isEndOfString()
            && (getNext() != '"' || current > start + 1 && s.charAt(current - 1) == '\\')) {
            next();
        }
        if (isEndOfString())
            throw new NonTerminatedString("Non-terminated string found starting from position %d".formatted(start));

        next();
        return s.substring(start + 1, current - 1).replace("\\\"", "\"");
    }

    private static final int ROW_INDEX_LENGTH_LIMIT = 8;
    private static final int COLUMN_INDEX_LENGTH_LIMIT = 6;

    private Token parseCellRef() throws LexerException {
        int rowIdx = 0;
        current--;

        var sb = new StringBuilder();
        for (int sectionLength = 0;
             sectionLength < COLUMN_INDEX_LENGTH_LIMIT && !isEndOfString() && 'A' <= getNext() && getNext() <= 'Z';
             sectionLength++) {
            sb.append(next());
        }

        for (int sectionLength = 0;
             sectionLength < ROW_INDEX_LENGTH_LIMIT && !isEndOfString() && isArabicNumeral(getNext());
             sectionLength++) {
            int i = next() - '0';
            rowIdx *= 10;
            rowIdx += i;
        }

        if (!isEndOfString() && Character.isLetterOrDigit(getNext()))
            throw new InvalidCellReference("Unexpected symbol %s at position %d".formatted(getNext(), current));
        return new CellRef(rowIdx - 1, LetterIndexUtil.toNumberIndex(sb.toString()) - 1);
    }

    private String parseIdent() {
        var sb = new StringBuilder();
        current--;
        while (!isEndOfString() && Character.isLetter(getNext())) {
            sb.append(next());
        }
        return sb.toString();
    }

    private double parseNumber() throws LexerException {
        var sb = new StringBuilder();
        current--;
        while (!isEndOfString() && isArabicNumeral(getNext())) {
            sb.append(next());
        }

        if (isEndOfString() || getNext() != '.')
            return toDoubleSafe(sb.toString());

        sb.append(next());
        while (!isEndOfString() && isArabicNumeral(getNext())) {
            sb.append(next());
        }

        return toDoubleSafe(sb.toString());
    }

    private double toDoubleSafe(String s) throws LexerException {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            var wrapper = new NumberFormatException("Unable to parse %s as number".formatted(s));
            wrapper.addSuppressed(e);
            throw wrapper;
        }
    }

    public abstract static sealed class Token permits Fixed, Ident, CellRef, Literal {
        protected TokenType type;

        Token(TokenType type) {
            this.type = type;
        }

        public TokenType getType() {
            return type;
        }

        public int getPrecedence() {
            return 0;
        }
    }

    public enum TokenType {
        Plus, Minus, Slash, Asterisk, LeftBracket, RightBracket, Comma, Ident, CellRef, Literal
    }

    public static final class Fixed extends Token {

        private boolean unary = false;

        public Fixed(TokenType type) {
            super(type);
        }

        @Override
        public String toString() {
            return "Fixed{" +
                "type=" + type +
                '}';
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Fixed that && that.type.equals(type);
        }

        public void setUnary(boolean unary) {
            this.unary = unary;
        }

        public boolean isUnary() {
            return unary;
        }

        @Override
        public int getPrecedence() {
            return switch (type) {
                case Plus, Minus -> unary ? 8 : 4;
                case Asterisk, Slash -> 6;
                case default -> 0;
            };
        }
    }

    public final static class Ident extends Token {
        private final String value;

        public Ident(String value) {
            super(Ident);
            this.value = value;
        }

        @Override
        public String toString() {
            return "Ident{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Ident that && that.type.equals(type) && that.value.equals(value);
        }

        @Override
        public int getPrecedence() {
            return 10;
        }

        public String getValue() {
            return value;
        }
    }

    public final static class CellRef extends Token {
        private int rowIdx, columnIdx;

        public CellRef(int rowIdx, int columnIdx) {
            super(CellRef);
            this.rowIdx = rowIdx;
            this.columnIdx = columnIdx;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CellRef that && that.type.equals(type) && that.rowIdx == rowIdx && that.columnIdx == columnIdx;
        }

        @Override
        public String toString() {
            return "CellRef{" +
                "rowIdx=" + rowIdx +
                ", columnIdx=" + columnIdx +
                ", type=" + type +
                '}';
        }

        public int getRowIdx() {
            return rowIdx;
        }

        public int getColumnIdx() {
            return columnIdx;
        }
    }

    public sealed static abstract class Literal extends Token permits StrLiteral, NumberLiteral {
        Literal() {
            super(Literal);
        }
    }

    public final static class StrLiteral extends Literal {
        private String value;

        public StrLiteral(String value) {
            super();
            this.value = value;
        }

        @Override
        public String toString() {
            return "StrLiteral{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StrLiteral that && that.type.equals(type) && that.value.equals(value);
        }

        public String getValue() {
            return value;
        }
    }

    public final static class NumberLiteral extends Literal {
        private double value;

        public NumberLiteral(double value) {
            super();
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NumberLiteral that && that.type.equals(type) && that.value == value;
        }

        @Override
        public String toString() {
            return "NumberLiteral{" +
                "value=" + value +
                ", type=" + type +
                '}';
        }

        public double getValue() {
            return value;
        }
    }
}
