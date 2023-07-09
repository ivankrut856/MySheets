package com.example.sheets.table.cell;

import com.example.sheets.expression.parser.ast.AstNode;
import com.example.sheets.expression.parser.ast.AstParser;
import com.example.sheets.expression.parser.ast.NodeValue;
import com.example.sheets.expression.parser.ast.ParseException;
import com.example.sheets.expression.parser.lexer.Lexer;
import com.example.sheets.expression.parser.lexer.LexerException;

import java.util.*;
import java.util.function.Function;

public class Cell {

    private final String formula;
    private final AstNode ast;
    private Optional<NodeValue> value = Optional.empty();

    public Cell(String formula) {
        AstNode ast;
        this.formula = formula;

        try {
            var tokens = new Lexer(formula).toTokens();
            ast = AstParser.parse(tokens);
        } catch (LexerException | ParseException e) {
            ast = new AstNode.Error(e.getMessage());
        }
        this.ast = ast;
    }

    public NodeValue getNodeValue(Function<AstNode.Reference.Address, NodeValue> memory) {
        return ast.getValue(memory);
    }

    public Optional<NodeValue> getValue() {
        return value;
    }

    void setValue(Optional<NodeValue> value) {
        this.value = value;
    }

    public List<AstNode.Reference.Address> getReferences() {
        return ast.getReferences().stream().toList();
    }

    public String getFormula() {
        return formula;
    }
}
