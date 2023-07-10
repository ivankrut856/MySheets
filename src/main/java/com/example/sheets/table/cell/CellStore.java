package com.example.sheets.table.cell;

import com.example.sheets.expression.parser.ast.NodeValue;
import com.example.sheets.save.SparseSaveInfo;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;
import java.util.stream.Collectors;

public class CellStore {

    record CellWithDependants(Cell cell, Set<CellAddress> dependants) {
    }

    private final BiMap<CellAddress, CellWithDependants> data;

    private int rowCount;
    private int columnCount;

    private final CellWithDependants DEFAULT_CELL;
    public static final int MIN_ROW_COUNT = 2;
    public static final int MIN_COLUMN_COUNT = 2;
    public static final int MAX_ROW_COUNT = 10000;
    public static final int MAX_COLUMN_COUNT = 10000;

    public CellStore(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        data = HashBiMap.create();
        DEFAULT_CELL = new CellWithDependants(new Cell(""), new HashSet<>());
        DEFAULT_CELL.cell.setValue(Optional.of(new NodeValue.Str("")));
    }

    public Cell getCell(CellAddress address) {
        if (data.containsKey(address))
            return data.get(address).cell;

        return DEFAULT_CELL.cell;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<CellAddress> getDependants(CellAddress address) {
        return data.getOrDefault(address, DEFAULT_CELL).dependants.stream().toList();
    }

    public void addDependant(CellAddress dependant, CellAddress dependee) {
        CellWithDependants cell = getOrCreate(dependee);
        cell.dependants.add(dependant);
    }

    public void removeDependant(CellAddress dependant, CellAddress dependee) {
        CellWithDependants cell = getOrCreate(dependee);
        cell.dependants.remove(dependant);
    }

    public Cell invalidate(CellAddress address) {
        var cell = getOrCreate(address).cell();
        cell.setValue(Optional.empty());
        return cell;
    }

    private CellWithDependants getOrCreate(CellAddress address) {
        if (data.containsKey(address))
            return data.get(address);
        CellWithDependants newCell = new CellWithDependants(new Cell(""), new HashSet<>());
        data.put(address, newCell);
        return newCell;
    }

    public boolean isInvalid(CellAddress address) {
        return (address.row() < 0 || address.row() >= rowCount || address.column() < 0 || address.column() >= columnCount);
    }

    public Cell set(CellAddress address, String formula) {
        var dependants = getDependants(address);
        var newCell = new CellWithDependants(new Cell(formula), new HashSet<>(dependants));
        data.put(address, newCell);
        return newCell.cell;
    }

    public Cell setValue(CellAddress address, NodeValue value) {
        var cell = getOrCreate(address).cell();
        cell.setValue(Optional.of(value));
        return cell;
    }

    public SparseSaveInfo toSaveInfo() {
        return new SparseSaveInfo(rowCount, columnCount,
            data.entrySet().stream().map(e ->
                new SparseSaveInfo.CellInfo(e.getKey().row(), e.getKey().column(), e.getValue().cell.getFormula())
            ).collect(Collectors.toList())
        );
    }

    public void extend(int rowNumber, int columnNumber) {
        rowCount += rowNumber;
        rowCount = Math.min(rowCount, MAX_ROW_COUNT);
        columnCount += columnNumber;
        columnCount = Math.min(columnCount, MAX_COLUMN_COUNT);
    }

    public void shrink(int rowNumber, int columnNumber) {
        rowCount -= rowNumber;
        rowCount = Math.max(rowCount, MIN_ROW_COUNT);
        columnCount -= columnNumber;
        columnCount = Math.max(columnCount, MIN_COLUMN_COUNT);
        var toRemove = data.keySet().stream().filter(this::isInvalid).toList();
        toRemove.forEach(data::remove);
    }
}
