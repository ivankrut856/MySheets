package com.example.sheets.table;

import com.example.sheets.expression.parser.LetterIndexUtil;
import com.example.sheets.expression.parser.ast.NodeValue;
import com.example.sheets.table.cell.CellAddress;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public final class EditorTableModel extends AbstractTableModel {

    private int selectedRow = -1;
    private int selectedColumn = -1;
    private final CellManager cellManager;

    public EditorTableModel(int rowCount, int columnCount) {
        cellManager = new CellManager(rowCount, columnCount);
    }

    @Override
    public int getRowCount() {
        return cellManager.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return cellManager.getColumnCount() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return String.valueOf(rowIndex + 1);

        if (selectedRow == rowIndex && selectedColumn == columnIndex)
            return cellManager.getFormula(new CellAddress(rowIndex, columnIndex - 1));
        return cellManager.getVisibleValue(new CellAddress(rowIndex, columnIndex - 1));
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return "\\";
        return LetterIndexUtil.toLetterIndex(column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!(aValue instanceof String formula))
            return;
        if (rowIndex < 0 || rowIndex >= getRowCount() || columnIndex < 1 || columnIndex >= getColumnCount())
            return;

        var updateResult = cellManager.setValue(
            new CellAddress(rowIndex, columnIndex - 1),
            formula
        );

        for (var address : updateResult.invalidated()) {
            fireTableCellUpdated(address.row(), address.column() + 1);
        }
    }

    public void selectionChanged(int rowIndex, int columnIndex) {
        if (rowIndex == selectedRow && columnIndex == selectedColumn)
            return;
        fireTableCellUpdated(rowIndex, columnIndex);
        fireTableCellUpdated(selectedRow, selectedColumn);
        selectedRow = rowIndex;
        selectedColumn = columnIndex;
    }

    public void onRightClick(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount() || columnIndex < 1 || columnIndex >= getColumnCount())
            return;

        if (cellManager.getValue(new CellAddress(rowIndex, columnIndex - 1)) instanceof NodeValue.Error e) {
            JOptionPane.showMessageDialog(null, e.message(), "Cell's erroneous", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void load(String json) {
        try {
            cellManager.load(json);
            fireTableDataChanged();
            fireTableStructureChanged();
        } catch (JsonProcessingException e) {
            JOptionPane.showMessageDialog(
                null,
                e.getMessage(),
                "Unable to load the file",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public String save() {
        try {
            return cellManager.save();
        } catch (JsonProcessingException e) {
            JOptionPane.showMessageDialog(
                null,
                e.getMessage(),
                "Unable to save",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    public void extend(int rowNumber, int columnNumber) {
        cellManager.extend(rowNumber, columnNumber);
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    public void shrink(int rowNumber, int columnNumber) {
        cellManager.shrink(rowNumber, columnNumber);
        fireTableStructureChanged();
        fireTableDataChanged();
    }
}
