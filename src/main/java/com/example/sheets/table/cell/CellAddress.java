package com.example.sheets.table.cell;

import com.example.sheets.expression.parser.ast.AstNode;


public record CellAddress(int row, int column) {
    public CellAddress(AstNode.Reference.Address address) {
        this(address.rowIndex(), address.columnIndex());
    }
}
