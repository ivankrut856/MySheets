package com.example.sheets.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public final class SkipFirstColumnModel extends DefaultTableColumnModel {
    private final int MINIMUM_WIDTH = 150;
    private final int MAXIMUM_WIDTH = 150;
    @Override
    public void addColumn(TableColumn aColumn) {
        if (aColumn.getHeaderValue().equals("\\")) {
            return;
        }
        aColumn.setMinWidth(MINIMUM_WIDTH);
        aColumn.setMaxWidth(MAXIMUM_WIDTH);
        super.addColumn(aColumn);
    }
}
