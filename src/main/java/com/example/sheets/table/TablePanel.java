package com.example.sheets.table;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.awt.event.MouseEvent.BUTTON3;
import static java.nio.file.StandardOpenOption.*;

public class TablePanel extends JPanel {

    private EditorTableModel tableModel;

    public TablePanel(int rowCount, int columnCount) {

        setMinimumSize(new Dimension(150, 150));

        tableModel = new EditorTableModel(rowCount, columnCount);
        var columnModel = new SkipFirstColumnModel();
        var columnModelHead = new SkipAfterFirstColumnModel();

        var bodyTable = new JTable(tableModel, columnModel) {
            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                tableModel.selectionChanged(rowIndex, columnIndex + 1);
            }

            @Override
            public void editingStopped(ChangeEvent e) {
                super.editingStopped(e);
                var rowIndex = getSelectedRow() + 1;
                if (rowIndex >= getRowCount())
                    rowIndex = 0;
                var columnIndex = getSelectedColumn();
                changeSelection(rowIndex, columnIndex, false, false);
            }
        };
        bodyTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != BUTTON3)
                    return;;
                int rowIndex = bodyTable.rowAtPoint(e.getPoint());
                int columnIndex = bodyTable.columnAtPoint(e.getPoint());
                tableModel.onRightClick(rowIndex, columnIndex + 1);
            }
        });
        var headTable = new JTable(tableModel, columnModelHead);

        bodyTable.setAutoCreateColumnsFromModel(true);
        headTable.createDefaultColumnsFromModel();

        bodyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bodyTable.setSelectionModel(bodyTable.getSelectionModel());


        bodyTable.getTableHeader().setBackground(Color.lightGray);
        bodyTable.getTableHeader().setReorderingAllowed(false);
        bodyTable.setRowSelectionAllowed(false);
        headTable.setBackground(Color.lightGray);
        headTable.getTableHeader().setReorderingAllowed(false);
        headTable.setColumnSelectionAllowed(false);
        headTable.setRowSelectionAllowed(false);
        headTable.setCellSelectionEnabled(false);
        headTable.setFocusable(false);

        var viewPort = new JViewport();
        viewPort.setView(headTable);
        viewPort.setPreferredSize(headTable.getMaximumSize());

        var pane = new JScrollPane(bodyTable);
        pane.setRowHeader(viewPort);
        pane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, headTable.getTableHeader());

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
    }

    public void load(File file) throws IOException {
        var saveInfo = Files.readString(file.toPath());
        tableModel.load(saveInfo);
    }

    public void save(File file) throws IOException {
        var saveInfo = tableModel.save();
        Files.writeString(file.toPath(), saveInfo, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    public void extend(int rowNumber, int columnNumber) {
        tableModel.extend(rowNumber, columnNumber);
    }

    public void shrink(int rowNumber, int columnNumber) {
        tableModel.shrink(rowNumber, columnNumber);
    }
}
