package com.example.sheets.table;

import com.example.sheets.expression.parser.ast.NodeValue;
import com.example.sheets.table.cell.CellAddress;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class EditorTableModel extends AbstractTableModel {

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
        System.out.println("GCC!!!");
        return cellManager.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return String.valueOf(rowIndex + 1);

        if (selectedRow == rowIndex && selectedColumn == columnIndex)
            return cellManager.getFormula(new CellAddress(rowIndex, columnIndex));
        return cellManager.getVisibleValue(new CellAddress(rowIndex, columnIndex));
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return "\\";

        column--;
        if (column == 0)
            return "A";

        var sb = new StringBuilder();
        while (column > 0) {
            sb.append((char) ((column % 26) + 'A'));
            column /= 26;
        }

        return sb.reverse().toString();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!(aValue instanceof String formula))
            return;
        if (rowIndex < 0 || rowIndex >= cellManager.getRowCount() || columnIndex < 1 || columnIndex >= cellManager.getColumnCount())
            return;

        var updateResult = cellManager.setValue(
            new CellAddress(rowIndex, columnIndex),
            formula
        );

        for (var address : updateResult.invalidated()) {
            fireTableCellUpdated(address.row(), address.column());
        }
    }

    public void selectionChanged(int rowIndex, int columnIndex) {
        if (rowIndex == selectedRow && columnIndex == selectedColumn)
            return;
        System.out.println(String.format("SC: %d;%d", rowIndex, columnIndex));
        fireTableCellUpdated(rowIndex, columnIndex);
        fireTableCellUpdated(selectedRow, selectedColumn);
        selectedRow = rowIndex;
        selectedColumn = columnIndex;
    }

    public void onRightClick(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= cellManager.getRowCount() || columnIndex < 1 || columnIndex >= cellManager.getRowCount())
            return;

        System.out.println("RC: %d;%d".formatted(rowIndex, columnIndex));
        if (cellManager.getValue(new CellAddress(rowIndex, columnIndex)) instanceof NodeValue.Error e) {
            JOptionPane.showMessageDialog(null, e.message(), "Cell's erroneous", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void load(String json) {
        try {
            cellManager.load(json);
            fireTableDataChanged();
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
