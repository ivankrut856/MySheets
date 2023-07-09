package com.example.sheets.expression.parser.ast;

import java.util.Optional;

public sealed interface NodeValue permits NodeValue.Error, NodeValue.Number, NodeValue.Str {
    default Optional<String> getStrValue() {
        return Optional.empty();
    }

    default Optional<Double> getDoubleValue() {
        return Optional.empty();
    }

    record Str(String value) implements NodeValue {

        @Override
        public Optional<String> getStrValue() {
            return Optional.of(value);
        }
    }

    record Number(double value) implements NodeValue {

        @Override
        public Optional<Double> getDoubleValue() {
            return Optional.of(value);
        }
    }

    record Error(String message) implements NodeValue {
    }
}
