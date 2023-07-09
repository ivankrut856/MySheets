package com.example.sheets.menu;

import com.example.sheets.table.TablePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class MenuBar extends JMenuBar {

    public MenuBar(TablePanel table) {
        var fileMenu = new JMenu("File");

        var saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFileHandler(e, table));
        fileMenu.add(saveItem);

        var loadItem = new JMenuItem("Load");
        loadItem.addActionListener(e -> loadFileHandler(e, table));
        fileMenu.add(loadItem);

        var tableMenu = new JMenu("Table");

        var extendItem = new JMenuItem("Extend");
        extendItem.addActionListener(e -> extendTableHandler(e, table));
        tableMenu.add(extendItem);

        var shrinkItem = new JMenuItem("Shrink");
        shrinkItem.addActionListener(e -> shrinkTableHandler(e, table));
        tableMenu.add(shrinkItem);

        add(fileMenu);
        add(tableMenu);
    }

    private void saveFileHandler(ActionEvent e, TablePanel table) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(table) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                table.save(file);
                JOptionPane.showMessageDialog(
                    table,
                    "File %s saved successfully".formatted(file.toPath().getFileName()),
                    "File saved",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    table,
                    ex.getMessage(),
                    "Unable to save",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void loadFileHandler(ActionEvent e, TablePanel table) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(table) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                table.load(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    table,
                    ex.getMessage(),
                    "Unable to load the file",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void extendTableHandler(ActionEvent e, TablePanel table) {
        var rowNumber = new SpinnerNumberModel(0, 0, 1_000, 1);
        var columnNumber = new SpinnerNumberModel(0, 0, 1_000, 1);
        Object[] message = {
            "Add rows (1000 max):", new JSpinner(rowNumber),
            "Add columns (1000 max):", new JSpinner(columnNumber)
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Extend table", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            System.out.printf("Extending the table %d %d\n", rowNumber.getNumber().intValue(), columnNumber.getNumber().intValue());
            table.extend(rowNumber.getNumber().intValue(), columnNumber.getNumber().intValue());
        }
    }

    private void shrinkTableHandler(ActionEvent e, TablePanel table) {
        var rowNumber = new SpinnerNumberModel(0, 0, 1_000, 1);
        var columnNumber = new SpinnerNumberModel(0, 0, 1_000, 1);
        Object[] message = {
            "Remove rows (1000 max):", new JSpinner(rowNumber),
            "Remove columns (1000 max):", new JSpinner(columnNumber)
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Shrink table", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            System.out.printf("Shrinking the table %d %d\n", rowNumber.getNumber().intValue(), columnNumber.getNumber().intValue());
            table.shrink(rowNumber.getNumber().intValue(), columnNumber.getNumber().intValue());
        }
    }
}
