package com.example.sheets.expression.parser.ast;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Bodies {

    public record FunctionDecl(int arity, Function<List<NodeValue>, NodeValue> body) {
    }

    public static final BinaryOperator<NodeValue> PLUS_BODY =
        (NodeValue left, NodeValue right) -> numberBinaryOperator(left, right, Double::sum);

    public static final BinaryOperator<NodeValue> MINUS_BODY =
        (NodeValue left, NodeValue right) -> numberBinaryOperator(left, right, (a, b) -> a - b);

    public static final BinaryOperator<NodeValue> ASTERISK_BODY =
        (NodeValue left, NodeValue right) -> numberBinaryOperator(left, right, (a, b) -> a * b);

    public static final BinaryOperator<NodeValue> SLASH_BODY =
        (NodeValue left, NodeValue right) -> numberBinaryOperator(left, right, (a, b) -> a / b);

    public static final UnaryOperator<NodeValue> UMINUS_BODY =
        (NodeValue x) -> numberUnaryOperator(x, (a) -> -a);

    public static final Function<List<NodeValue>, NodeValue> SIN_BODY =
        (List<NodeValue> args) -> numberUnaryOperator(args.get(0), Math::sin);

    public static final Function<List<NodeValue>, NodeValue> MAX_BODY =
        (List<NodeValue> args) -> numberBinaryOperator(args.get(0), args.get(1), Math::max);

    public static final Function<List<NodeValue>, NodeValue> SUBSTR_BODY =
        (List<NodeValue> args) -> substringBody(args.get(0), args.get(1), args.get(2));

    public static final Function<List<NodeValue>, NodeValue> PI_BODY =
        (List<NodeValue> args) -> new NodeValue.Number(Math.PI);

    public static final Function<List<NodeValue>, NodeValue> POW_BODY =
        (List<NodeValue> args) -> numberBinaryOperator(args.get(0), args.get(1), Math::pow);

    public static final Map<String, FunctionDecl> builtIns = Map.ofEntries(
        Map.entry("sin", new FunctionDecl(1, SIN_BODY)),
        Map.entry("max", new FunctionDecl(2, MAX_BODY)),
        Map.entry("substr", new FunctionDecl(3, SUBSTR_BODY)),
        Map.entry("pi", new FunctionDecl(0, PI_BODY)),
        Map.entry("pow", new FunctionDecl(2, POW_BODY))
    );

    public static NodeValue numberUnaryOperator(NodeValue x, UnaryOperator<Double> op) {
        var value = x.getDoubleValue();
        if (value.isEmpty())
            return unexpectedValueError("number", x);
        return new NodeValue.Number(op.apply(value.get()));
    }

    public static NodeValue numberBinaryOperator(NodeValue left, NodeValue right, BinaryOperator<Double> op) {
        var leftValue = left.getDoubleValue();
        var rightValue = right.getDoubleValue();
        if (leftValue.isEmpty())
            return unexpectedValueError("number", left);
        if (rightValue.isEmpty())
            return unexpectedValueError("number", right);

        return new NodeValue.Number(op.apply(leftValue.get(), rightValue.get()));
    }

    public static NodeValue substringBody(NodeValue s, NodeValue start, NodeValue end) {
        var sValue = s.getStrValue();
        var startValue = start.getDoubleValue();
        var endValue = end.getDoubleValue();
        if (sValue.isEmpty())
            return unexpectedValueError("a string", s);
        if (startValue.isEmpty() || startValue.get().intValue() != startValue.get())
            return unexpectedValueError("whole number", start);
        if (endValue.isEmpty() || endValue.get().intValue() != endValue.get())
            return unexpectedValueError("whole number", end);

        try {
            return new NodeValue.Str(sValue.get().substring(startValue.get().intValue(), endValue.get().intValue()));
        } catch (IndexOutOfBoundsException e) {
            return new NodeValue.Error(e.getMessage());
        }
    }

    private static NodeValue unexpectedValueError(String expectedType, NodeValue x) {
        var actualValueRepr = switch (x) {
            case NodeValue.Error e -> "Error";
            case default -> x.toString();
        };
        return new NodeValue.Error("Expected %s, but got %s".formatted(expectedType, actualValueRepr));
    }
}
