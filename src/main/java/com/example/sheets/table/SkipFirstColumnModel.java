package com.example.sheets.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class SkipFirstColumnModel extends DefaultTableColumnModel {
    private boolean skipped = false;
    @Override
    public void addColumn(TableColumn aColumn) {
        if (aColumn.getHeaderValue().equals("\\")) {
            return;
        }
        aColumn.setMinWidth(150);
        aColumn.setMaxWidth(150);
        super.addColumn(aColumn);
    }
}
