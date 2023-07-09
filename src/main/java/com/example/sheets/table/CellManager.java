package com.example.sheets.table;

import com.example.sheets.save.SparseSaveInfo;
import com.example.sheets.table.cell.CellAddress;
import com.example.sheets.table.cell.CellStore;
import com.example.sheets.expression.parser.ast.AstNode;
import com.example.sheets.expression.parser.ast.NodeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DecimalFormat;
import java.util.*;

public class CellManager {
    public record UpdateResult(Set<CellAddress> invalidated) {
    }

    private final DecimalFormat numberFormat = new DecimalFormat("0.########");
    private CellStore cells;

    public CellManager(int rowCount, int columnCount) {
        cells = new CellStore(rowCount, columnCount);
    }

    public NodeValue getValue(CellAddress address) {
        var cell = cells.getCell(address);
        if (cell.getValue().isPresent())
            return cell.getValue().get();

        var toCalculate = new Stack<CellAddress>();
        var visitationStatus = new HashMap<CellAddress, Integer>();
        final int NOT_VISITED = 0;
        final int ENTERED = 1;
        final int LEFT = 2;

        toCalculate.push(address);
        while (!toCalculate.isEmpty()) {
            var top = toCalculate.peek();
            var topCell = cells.getCell(top);
            var status = visitationStatus.getOrDefault(top, NOT_VISITED);
            if (status == LEFT || topCell.getValue().isPresent()) {
                toCalculate.pop();
                continue;
            }
            if (status == ENTERED) {
                visitationStatus.put(top, LEFT);
                toCalculate.pop();

                var value = topCell.getNodeValue((addr) -> {
                    if (isAddressInvalid(addr))
                        return new NodeValue.Error("Invalid address %s".formatted(addr));
                    var referencedCell = cells.getCell(new CellAddress(addr));
                    if (referencedCell.getValue().isEmpty())
                        throw new RuntimeException("Unexpected emptiness of cell value");
                    return referencedCell.getValue().get();
                });
                cells.setValue(top, value);
                continue;
            }
            visitationStatus.put(top, ENTERED);

            var references = topCell.getReferences();
            for (var cellRef : references) {
                if (isAddressInvalid(cellRef)) {
                    continue;
                }
                var toGo = new CellAddress(cellRef);
                if (visitationStatus.getOrDefault(toGo, NOT_VISITED) == ENTERED) {
                    cell = cells.setValue(address, new NodeValue.Error("Part of reference cycle"));
                    return cell.getValue().get();
                }
                toCalculate.push(toGo);
            }
        }

        cell = cells.getCell(address);
        if (cell.getValue().isEmpty())
            throw new RuntimeException("Unexpected emptiness of cell value");
        return cell.getValue().get();
    }

    public UpdateResult setValue(CellAddress address, String formula) {
        var oldCell = cells.getCell(address);
        if (oldCell.getFormula().equals(formula))
            return new UpdateResult(Collections.emptySet());

        var newCell = cells.set(address, formula);

        var children = newCell.getReferences();
        var oldChildren = oldCell.getReferences();

        oldChildren.forEach(c -> cells.removeDependant(address, new CellAddress(c)));
        children.forEach(c -> cells.addDependant(address, new CellAddress(c)));

        var invalidated = invalidateAll(List.of(address));

        return new UpdateResult(invalidated);
    }

    public String getVisibleValue(CellAddress address) {
        return switch (getValue(address)) {
            case NodeValue.Error error -> error.toString();
            case NodeValue.Str s -> s.value();
            case NodeValue.Number x -> numberFormat.format(x.value());
        };
    }

    public String getFormula(CellAddress address) {
        return cells.getCell(address).getFormula();
    }

    public boolean isAddressInvalid(AstNode.Reference.Address address) {
        return !cells.isValid(new CellAddress(address));
    }

    public int getRowCount() {
        return cells.getRowCount();
    }

    public int getColumnCount() {
        return cells.getColumnCount();
    }

    public void load(String json) throws JsonProcessingException {
        SparseSaveInfo saveInfo = new ObjectMapper().readerFor(SparseSaveInfo.class).readValue(json);
        cells = new CellStore(saveInfo.rowCount(), saveInfo.columnCount());
        for (var cellInfo : saveInfo.cells()) {
            setValue(new CellAddress(cellInfo.row(), cellInfo.column()), cellInfo.formula());
        }
    }

    public String save() throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cells.toSaveInfo());
    }

    public void extend(int rowNumber, int columnNumber) {
        int oldRowCount = getRowCount();
        int oldColumnCount = getColumnCount();
        cells.extend(rowNumber, columnNumber);
        int rowCount = getRowCount();
        int columnCount = getColumnCount();

        var toInvalidateFirst = new ArrayList<CellAddress>();
        for (int i = 0; i < oldRowCount; i++) {
            for (int j = 0; j < oldColumnCount; j++) {
                var cellAddress = new CellAddress(i, j);
                if (cells.getCell(cellAddress).getReferences().stream().anyMatch(addr ->
                    addr.rowIndex() >= oldRowCount && addr.rowIndex() < rowCount ||
                        addr.columnIndex() >= oldColumnCount && addr.columnIndex() < columnCount)) {
                    toInvalidateFirst.add(cellAddress);
                }
            }
        }
        invalidateAll(toInvalidateFirst);
    }

    public void shrink(int rowNumber, int columnNumber) {
        int oldRowCount = getRowCount();
        int oldColumnCount = getColumnCount();
        cells.shrink(rowNumber, columnNumber);
        int rowCount = getRowCount();
        int columnCount = getColumnCount();
        var toInvalidateFirst = new ArrayList<CellAddress>();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                var cellAddress = new CellAddress(i, j);
                if (cells.getCell(cellAddress).getReferences().stream().anyMatch(addr ->
                    addr.rowIndex() >= rowCount && addr.rowIndex() < oldRowCount ||
                        addr.columnIndex() >= columnCount && addr.columnIndex() < oldColumnCount)) {
                    toInvalidateFirst.add(cellAddress);
                }
            }
        }
        invalidateAll(toInvalidateFirst);
    }

    private Set<CellAddress> invalidateAll(List<CellAddress> toInvalidateFirst) {
        var toInvalidate = new Stack<CellAddress>();
        var invalidated = new HashSet<CellAddress>();
        toInvalidate.addAll(toInvalidateFirst);

        while (!toInvalidate.empty()) {
            var top = toInvalidate.pop();
            cells.invalidate(top);
            invalidated.add(top);
            for (var dependantAddress : cells.getDependants(top)) {
                if (invalidated.contains(dependantAddress))
                    continue;
                toInvalidate.push(dependantAddress);
            }
        }

        return invalidated;
    }
}
