package com.example.sheets.expression.parser.lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.example.sheets.expression.parser.lexer.Lexer.*;

class LexerTest {

    @Test
    void emptyFormula() throws LexerException {
        var tokens = new Lexer("=").toTokens();
        assertEquals(List.of(), tokens);
    }

    @Test
    void loneString() throws LexerException {
        var tokens = new Lexer("=\"hello\"").toTokens();
        assertEquals(List.of(new StrLiteral("hello")), tokens);
    }

    @Test
    void stringPlusString() throws LexerException {
        var tokens = new Lexer("=\"hello\" + \"world\"").toTokens();
        assertEquals(List.of(
            new StrLiteral("hello"),
            new Fixed(TokenType.Plus),
            new StrLiteral("world")
        ), tokens);
    }

    @Test
    void quoteEscape() throws LexerException {
        var tokens = new Lexer("=\"hel\\\"lo\"").toTokens();
        assertEquals(List.of(
            new StrLiteral("hel\"lo")
        ), tokens);
    }

    @Test
    void unescapedFails() {
        assertThrows(NonTerminatedString.class, () -> new Lexer("=\"hel\"lo\"").toTokens());
    }

    @Test
    void functionCall() throws LexerException {
        var tokens = new Lexer("=length(\"hello\")").toTokens();
        assertEquals(List.of(
            new Ident("length"),
            new Fixed(TokenType.LeftBracket),
            new StrLiteral("hello"),
            new Fixed(TokenType.RightBracket)
        ), tokens);
    }

    @Test
    void functionCallOfRef() throws LexerException {
        var tokens = new Lexer("=getRawAddr(A4)").toTokens();
        assertEquals(List.of(
            new Ident("getRawAddr"),
            new Fixed(TokenType.LeftBracket),
            new CellRef(3, 0),
            new Fixed(TokenType.RightBracket)
        ), tokens);
    }

    @Test
    void barelyPassingCellRef() throws LexerException {
        var tokens = new Lexer("=ZZZZZZ99999999").toTokens();
        assertEquals(List.of(
            new CellRef(99999998, 321272405)
        ), tokens);
    }

    @Test
    void tooLongCellRef() {
        assertThrowsExactly(InvalidCellReference.class, () -> new Lexer("=AAAAAAAAAAAAAAAAA45").toTokens());
    }

    @Test
    void wholeNumber() throws LexerException {
        var tokens = new Lexer("=1234").toTokens();
        assertEquals(List.of(
            new NumberLiteral(1234)
        ), tokens);
    }

    @Test
    void number() throws LexerException {
        var tokens = new Lexer("=1234.1234").toTokens();
        assertEquals(List.of(
            new NumberLiteral(1234.1234)
        ), tokens);
    }

    @Test
    void longNumber() throws LexerException {
        var tokens = new Lexer("=1111111111111111111111234.1234").toTokens();
        assertEquals(List.of(
            new NumberLiteral(1.1111111111111111e24)
        ), tokens);
    }

    @Test
    void sumOfNumbers() throws LexerException {
        var tokens = new Lexer("=1234 + 1000").toTokens();
        assertEquals(List.of(
            new NumberLiteral(1234),
            new Fixed(TokenType.Plus),
            new NumberLiteral(1000)
        ), tokens);
    }

    @Test
    void parsableMess() throws LexerException {
        var tokens = new Lexer("=-1234sin(*7.7+)").toTokens();
        assertEquals(List.of(
            new Fixed(TokenType.Minus),
            new NumberLiteral(1234),
            new Ident("sin"),
            new Fixed(TokenType.LeftBracket),
            new Fixed(TokenType.Asterisk),
            new NumberLiteral(7.7),
            new Fixed(TokenType.Plus),
            new Fixed(TokenType.RightBracket)
        ), tokens);
    }

    @Test
    void cellRefNearIdent() {
        assertThrowsExactly(InvalidCellReference.class, () -> new Lexer("=A3sin").toTokens());
    }

    @Test
    void invalidSymbol() {
        assertThrowsExactly(InvalidSymbol.class, () -> new Lexer("=10% from 10").toTokens());
    }

    @Test
    void complexExpression() throws LexerException {
        var tokens = new Lexer("=4*2.5 + 8.5+1.5 / 3.0 * (5.0005 + 0.0095)").toTokens();
        assertEquals(List.of(
            new NumberLiteral(4),
            new Fixed(TokenType.Asterisk),
            new NumberLiteral(2.5),
            new Fixed(TokenType.Plus),
            new NumberLiteral(8.5),
            new Fixed(TokenType.Plus),
            new NumberLiteral(1.5),
            new Fixed(TokenType.Slash),
            new NumberLiteral(3),
            new Fixed(TokenType.Asterisk),
            new Fixed(TokenType.LeftBracket),
            new NumberLiteral(5.0005),
            new Fixed(TokenType.Plus),
            new NumberLiteral(0.0095),
            new Fixed(TokenType.RightBracket)
        ), tokens);
    }

}