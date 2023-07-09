package com.example.sheets.save;

import com.example.sheets.table.cell.CellStore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SparseSaveInfo(int rowCount, int columnCount, List<CellInfo> cells) {

    public String getType() {
        return "SPARSE";
    }

    @JsonCreator
    public SparseSaveInfo(
        @JsonProperty("rowCount") int rowCount,
        @JsonProperty("columnCount") int columnCount,
        @JsonProperty("cells") List<CellInfo> cells,
        @JsonProperty("type") String type
    ) {
        this(rowCount, columnCount, cells);
    }

    public record CellInfo(int row, int column, String formula) {
    }
}
