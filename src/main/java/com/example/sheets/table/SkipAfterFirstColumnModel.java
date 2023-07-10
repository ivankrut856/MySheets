package com.example.sheets.table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public final class SkipAfterFirstColumnModel extends DefaultTableColumnModel {
    private boolean skipped = false;
    @Override
    public void addColumn(TableColumn aColumn) {
        if (!skipped) {
            aColumn.setMaxWidth(aColumn.getPreferredWidth());
            super.addColumn(aColumn);
            skipped = true;
        }
    }
}
