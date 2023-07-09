package com.example.sheets.expression.parser.ast;

import com.example.sheets.expression.parser.lexer.Lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

import static com.example.sheets.expression.parser.ast.Bodies.*;

public class AstParser {

    public static AstNode parse(List<Lexer.Token> tokens) throws ParseException {
        Stack<Lexer.Token> delayed = new Stack<>();
        List<Lexer.Token> rpn = new ArrayList<>();
        int n = tokens.size();
        for (int i = 0; i < n; i++) {
            switch (tokens.get(i)) {
                case Lexer.Literal x -> rpn.add(x);
                case Lexer.CellRef x -> rpn.add(x);
                case Lexer.Ident x -> delayed.push(x);
                case Lexer.Fixed x -> {
                    switch (x.getType()) {
                        case LeftBracket -> delayed.push(x);
                        case RightBracket -> {
                            while (!delayed.empty() && delayed.peek().getType() != Lexer.TokenType.LeftBracket)
                                rpn.add(delayed.pop());
                            if (delayed.empty() || delayed.peek().getType() != Lexer.TokenType.LeftBracket)
                                throw new ParseException("Unmatched right bracket");
                            delayed.pop();
                            if (!delayed.empty() && delayed.peek().getType() == Lexer.TokenType.Ident)
                                rpn.add(delayed.pop());
                        }
                        case Comma -> {
                            while (!delayed.empty() &&
                                delayed.peek().getType() != Lexer.TokenType.LeftBracket && delayed.peek().getType() != Lexer.TokenType.Comma) {
                                rpn.add(delayed.pop());
                            }
                        }
                        case Plus, Minus, Asterisk, Slash -> {
                            if (x.getType() == Lexer.TokenType.Minus && (i == 0 || !isOperand(tokens.get(i - 1).getType())))
                                x.setUnary(true);
                            while (!delayed.empty() &&
                                delayed.peek().getType() != Lexer.TokenType.LeftBracket &&
                                delayed.peek().getPrecedence() >= x.getPrecedence()) {
                                rpn.add(delayed.pop());
                            }
                            delayed.push(x);
                        }
                        case default -> throw new RuntimeException("Unexpected TokenType for Fixed lexeme");
                    }
                }
            }
        }

        while (!delayed.empty()) {
            if (delayed.peek().getType() == Lexer.TokenType.LeftBracket)
                throw new ParseException("Unmatched left bracket");
            rpn.add(delayed.pop());
        }

        Stack<AstNode> nodes = new Stack<>();
        for (var token : rpn) {
            switch (token) {
                case Lexer.StrLiteral x -> nodes.push(new AstNode.StrLiteral(x.getValue()));
                case Lexer.NumberLiteral x -> nodes.push(new AstNode.NumberLiteral(x.getValue()));
                case Lexer.CellRef x -> nodes.push(
                    new AstNode.Reference(new AstNode.Reference.Address(x))
                );
                case Lexer.Fixed x -> {
                    switch (x.getType()) {
                        case Plus -> {
                            var right = popOrThrow(nodes);
                            var left = popOrThrow(nodes);
                            nodes.push(new AstNode.BinaryOp(left, right, PLUS_BODY));
                        }
                        case Minus -> {
                            if (x.isUnary()) {
                                var operand = popOrThrow(nodes);
                                nodes.push(new AstNode.UnaryOp(operand, UMINUS_BODY));
                            } else {
                                var right = popOrThrow(nodes);
                                var left = popOrThrow(nodes);
                                nodes.push(new AstNode.BinaryOp(left, right, MINUS_BODY));
                            }
                        }
                        case Asterisk -> {
                            var right = popOrThrow(nodes);
                            var left = popOrThrow(nodes);
                            nodes.push(new AstNode.BinaryOp(left, right, ASTERISK_BODY));
                        }
                        case Slash -> {
                            var right = popOrThrow(nodes);
                            var left = popOrThrow(nodes);
                            nodes.push(new AstNode.BinaryOp(left, right, SLASH_BODY));
                        }
                        case default -> throw new RuntimeException("Unexpected TokenType for Fixed lexeme in rpn");
                    }
                }
                case Lexer.Ident x -> {
                    if (!builtIns.containsKey(x.getValue()))
                        throw new ParseException("Unknown symbol %s".formatted(x.getValue()));
                    var functionDecl = Bodies.builtIns.get(x.getValue());
                    var args = new ArrayList<AstNode>();
                    for (int i = 0; i < functionDecl.arity(); i++)
                        args.add(popOrThrow(nodes));
                    Collections.reverse(args);
                    nodes.push(new AstNode.FunctionCall(x.getValue(), args, functionDecl.body()));
                }
            }
        }


        if (nodes.empty())
            return new AstNode.StrLiteral("");
        else if (nodes.size() > 1) {
            throw new ParseException("Multiple expressions found");
        } else
            return nodes.pop();
    }

    private static AstNode popOrThrow(Stack<AstNode> stack) throws ParseException {
        if (stack.empty())
            throw new ParseException("Unexpected end of rpn");
        return stack.pop();
    }

    private static boolean isOperand(Lexer.TokenType type) {
        return switch (type) {
            case CellRef, Literal, RightBracket -> true;
            case default -> false;
        };
    }


}
