package com.example.sheets.expression.parser.ast;

import com.example.sheets.expression.parser.lexer.Lexer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public sealed interface AstNode permits
    AstNode.BinaryOp,
    AstNode.Error,
    AstNode.FunctionCall,
    AstNode.NumberLiteral,
    AstNode.Reference,
    AstNode.StrLiteral,
    AstNode.UnaryOp {
    NodeValue getValue(Function<Reference.Address, NodeValue> memory);

    default NodeValue getValue() {
        return getValue((x) -> new NodeValue.Error("Incorrect reference %s".formatted(x.toString())));
    }

    Set<Reference.Address> getReferences();

    record UnaryOp(AstNode operand, UnaryOperator<NodeValue> operator) implements AstNode {

        @Override
        public NodeValue getValue(final Function<Reference.Address, NodeValue> memory) {
            return operator.apply(operand.getValue(memory));
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return operand.getReferences();
        }
    }

    record BinaryOp(AstNode leftOperand, AstNode rightOperand,
                    BinaryOperator<NodeValue> operator) implements AstNode {
        @Override
        public NodeValue getValue(Function<Reference.Address, NodeValue> memory) {
            return operator.apply(leftOperand.getValue(memory), rightOperand.getValue(memory));
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return Stream.concat(
                leftOperand.getReferences().stream(),
                rightOperand.getReferences().stream()
            ).collect(Collectors.toUnmodifiableSet());
        }
    }

    record FunctionCall(String functionName, List<AstNode> arguments,
                        Function<List<NodeValue>, NodeValue> functionBody) implements AstNode {

        @Override
        public NodeValue getValue(Function<Reference.Address, NodeValue> memory) {
            return functionBody.apply(arguments.stream().map(arg -> arg.getValue(memory)).collect(Collectors.toList()));
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return arguments.stream().flatMap(x -> x.getReferences().stream()).collect(Collectors.toUnmodifiableSet());
        }
    }

    record Reference(Address address) implements AstNode {

        @Override
        public NodeValue getValue(Function<Address, NodeValue> memory) {
            return memory.apply(address);
        }

        public record Address(int rowIndex, int columnIndex) {
            public Address(Lexer.CellRef cellRef) {
                this(cellRef.getRowIdx(), cellRef.getColumnIdx());
            }
        }

        @Override
        public Set<Address> getReferences() {
            return Set.of(address);
        }
    }

    record StrLiteral(String literal) implements AstNode {
        @Override
        public NodeValue getValue(Function<Reference.Address, NodeValue> memory) {
            return new NodeValue.Str(literal);
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return Collections.emptySet();
        }
    }

    record NumberLiteral(double literal) implements AstNode {

        @Override
        public NodeValue getValue(Function<Reference.Address, NodeValue> memory) {
            return new NodeValue.Number(literal);
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return Collections.emptySet();
        }
    }

    record Error(String message) implements AstNode {
        @Override
        public NodeValue getValue(Function<Reference.Address, NodeValue> memory) {
            return new NodeValue.Error(message);
        }

        @Override
        public Set<Reference.Address> getReferences() {
            return Collections.emptySet();
        }
    }
}








