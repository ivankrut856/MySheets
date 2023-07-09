package com.example.sheets.expression.parser.ast;

import com.example.sheets.expression.parser.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.example.sheets.expression.parser.ast.Bodies.*;
import static org.junit.jupiter.api.Assertions.*;

class AstParserTest {

    @Test
    void empty() throws ParseException {
        var ast = AstParser.parse(List.of());
        assertEquals(new AstNode.StrLiteral(""), ast);
    }

    @Test
    void sum() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(5)
        ));
        assertEquals(
            new AstNode.BinaryOp(new AstNode.NumberLiteral(4), new AstNode.NumberLiteral(5), PLUS_BODY),
            ast);

        assertEquals(new NodeValue.Number(9), ast.getValue());
    }

    @Test
    void sumRightAssoc() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.NumberLiteral(5),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(1),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(4),
                new AstNode.BinaryOp(
                    new AstNode.NumberLiteral(5),
                    new AstNode.NumberLiteral(1),
                    PLUS_BODY),
                PLUS_BODY),
            ast);

        assertEquals(new NodeValue.Number(10), ast.getValue());
    }

    @Test
    void sumLeftAssoc() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(5),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(1)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.BinaryOp(
                    new AstNode.NumberLiteral(4),
                    new AstNode.NumberLiteral(5),
                    PLUS_BODY),
                new AstNode.NumberLiteral(1),
                PLUS_BODY),
            ast);

        assertEquals(new NodeValue.Number(10), ast.getValue());
    }

    @Test
    void sinOfSum() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("sin"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.NumberLiteral(Math.PI),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(Math.PI),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.FunctionCall(
                "sin",
                List.of(
                    new AstNode.BinaryOp(
                        new AstNode.NumberLiteral(Math.PI),
                        new AstNode.NumberLiteral(Math.PI),
                        PLUS_BODY)),
                SIN_BODY),
            ast);

        assertEquals(0, ast.getValue().getDoubleValue().get(), 0.00001);
    }

    @Test
    void sinWithUnaryMinus() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("sin"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.Fixed(Lexer.TokenType.Minus),
            new Lexer.NumberLiteral(Math.PI / 2),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(Math.PI),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.FunctionCall(
                "sin",
                List.of(
                    new AstNode.BinaryOp(
                        new AstNode.UnaryOp(new AstNode.NumberLiteral(Math.PI / 2), UMINUS_BODY),
                        new AstNode.NumberLiteral(Math.PI),
                        PLUS_BODY)
                ),
                SIN_BODY),
            ast);

        assertEquals(1, ast.getValue().getDoubleValue().get(), 0.00001);
    }

    @Test
    void unaryMinusWithBrackets() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Fixed(Lexer.TokenType.Minus),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(5),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.UnaryOp(
                new AstNode.BinaryOp(
                    new AstNode.NumberLiteral(4),
                    new AstNode.NumberLiteral(5),
                    PLUS_BODY
                ),
                UMINUS_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(-9), ast.getValue());
    }

    @Test
    void minusNegatedNumber() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(5),
            new Lexer.Fixed(Lexer.TokenType.Minus),
            new Lexer.Fixed(Lexer.TokenType.Minus),
            new Lexer.NumberLiteral(4)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(5),
                new AstNode.UnaryOp(new AstNode.NumberLiteral(4), UMINUS_BODY),
                MINUS_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(9), ast.getValue());
    }

    @Test
    void division() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(22),
            new Lexer.Fixed(Lexer.TokenType.Slash),
            new Lexer.NumberLiteral(7)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(22),
                new AstNode.NumberLiteral(7),
                SLASH_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(22. / 7.), ast.getValue());
    }

    @Test
    void divisionByZero() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(22),
            new Lexer.Fixed(Lexer.TokenType.Slash),
            new Lexer.NumberLiteral(0)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(22),
                new AstNode.NumberLiteral(0),
                SLASH_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(22. / 0.), ast.getValue());
    }

    @Test
    void nanResult() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(0),
            new Lexer.Fixed(Lexer.TokenType.Slash),
            new Lexer.NumberLiteral(0)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(0),
                new AstNode.NumberLiteral(0),
                SLASH_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(Double.NaN), ast.getValue());
    }

    @Test
    void stringPlusNumber() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.StrLiteral("hello"),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(0)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.StrLiteral("hello"),
                new AstNode.NumberLiteral(0),
                PLUS_BODY
            ),
            ast);

        assertEquals("Error[message=Expected number, but got Str[value=hello]]", ast.getValue().toString());
    }

    @Test
    void omitAsteriskWhenMultiply() {
        assertThrows(ParseException.class, () -> AstParser.parse(List.of(
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.NumberLiteral(3),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(2),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        )));
    }

    @Test
    void mess() {
        //-1234sin(*7.7+)
        assertThrows(ParseException.class, () -> AstParser.parse(List.of(
            new Lexer.Fixed(Lexer.TokenType.Minus),
            new Lexer.NumberLiteral(1234),
            new Lexer.Ident("sin"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.Fixed(Lexer.TokenType.Asterisk),
            new Lexer.NumberLiteral(7.7),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        )));
    }

    @Test
    void substring() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("substr"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.StrLiteral("hello world!"),
            new Lexer.Fixed(Lexer.TokenType.Comma),
            new Lexer.NumberLiteral(3),
            new Lexer.Fixed(Lexer.TokenType.Comma),
            new Lexer.NumberLiteral(9),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.FunctionCall(
                "substr",
                List.of(
                    new AstNode.StrLiteral("hello world!"),
                    new AstNode.NumberLiteral(3),
                    new AstNode.NumberLiteral(9)
                ),
                SUBSTR_BODY
            ),
            ast);

        assertEquals(new NodeValue.Str("lo wor"), ast.getValue());
    }

    @Test
    void substringError() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("substr"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.StrLiteral("hello world!"),
            new Lexer.Fixed(Lexer.TokenType.Comma),
            new Lexer.NumberLiteral(3),
            new Lexer.Fixed(Lexer.TokenType.Comma),
            new Lexer.NumberLiteral(9),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.FunctionCall(
                "substr",
                List.of(
                    new AstNode.StrLiteral("hello world!"),
                    new AstNode.NumberLiteral(3),
                    new AstNode.NumberLiteral(9)
                ),
                SUBSTR_BODY
            ),
            ast);

        assertEquals(new NodeValue.Str("lo wor"), ast.getValue());
    }

    @Test
    void multiplyThenAdd() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(2),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(2),
            new Lexer.Fixed(Lexer.TokenType.Asterisk),
            new Lexer.NumberLiteral(2)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.NumberLiteral(2),
                new AstNode.BinaryOp(
                    new AstNode.NumberLiteral(2),
                    new AstNode.NumberLiteral(2),
                    ASTERISK_BODY
                ),
                PLUS_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(6), ast.getValue());
    }

    @Test
    void complexExpression() throws ParseException {
        //4*2.5 + 8.5+1.5 / 3.0 * (5.0005 + 0.0095)
        var ast = AstParser.parse(List.of(
            new Lexer.NumberLiteral(4),
            new Lexer.Fixed(Lexer.TokenType.Asterisk),
            new Lexer.NumberLiteral(2.5),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(8.5),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(1.5),
            new Lexer.Fixed(Lexer.TokenType.Slash),
            new Lexer.NumberLiteral(3),
            new Lexer.Fixed(Lexer.TokenType.Asterisk),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.NumberLiteral(5.0005),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(0.0095),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.BinaryOp(
                    new AstNode.BinaryOp(
                        new AstNode.NumberLiteral(4),
                        new AstNode.NumberLiteral(2.5),
                        ASTERISK_BODY
                    ),
                    new AstNode.NumberLiteral(8.5),
                    PLUS_BODY
                ),
                new AstNode.BinaryOp(
                    new AstNode.BinaryOp(
                        new AstNode.NumberLiteral(1.5),
                        new AstNode.NumberLiteral(3.0),
                        SLASH_BODY
                    ),
                    new AstNode.BinaryOp(
                        new AstNode.NumberLiteral(5.0005),
                        new AstNode.NumberLiteral(0.0095),
                        PLUS_BODY
                    ),
                    ASTERISK_BODY
                ),
                PLUS_BODY
            ),
            ast);

        assertEquals(new NodeValue.Number(21.005), ast.getValue());
    }

    @Test
    void sinOfPiOver2() throws ParseException {
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("sin"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.Ident("pi"),
            new Lexer.Fixed(Lexer.TokenType.LeftBracket),
            new Lexer.Fixed(Lexer.TokenType.RightBracket),
            new Lexer.Fixed(Lexer.TokenType.Slash),
            new Lexer.NumberLiteral(2),
            new Lexer.Fixed(Lexer.TokenType.RightBracket)
        ));
        assertEquals(
            new AstNode.FunctionCall(
                "sin",
                List.of(new AstNode.BinaryOp(
                    new AstNode.FunctionCall(
                        "pi",
                        List.of(),
                        PI_BODY
                    ),
                    new AstNode.NumberLiteral(2),
                    SLASH_BODY
                )),
                SIN_BODY
            ),
            ast);

        assertEquals(1, ast.getValue().getDoubleValue().get(), 0.00001);
    }

    @Test
    void functionWithoutBrackets() throws ParseException {
        // fun feature? sin PI + 5
        var ast = AstParser.parse(List.of(
            new Lexer.Ident("sin"),
            new Lexer.NumberLiteral(Math.PI),
            new Lexer.Fixed(Lexer.TokenType.Plus),
            new Lexer.NumberLiteral(5)
        ));
        assertEquals(
            new AstNode.BinaryOp(
                new AstNode.FunctionCall(
                    "sin",
                    List.of(new AstNode.NumberLiteral(Math.PI)),
                    SIN_BODY
                ),
                new AstNode.NumberLiteral(5),
                PLUS_BODY
            ),
            ast);

        assertEquals(5, ast.getValue().getDoubleValue().get(), 0.00001);
    }
}