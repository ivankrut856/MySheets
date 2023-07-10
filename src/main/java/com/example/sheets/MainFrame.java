package com.example.sheets;

import com.example.sheets.menu.MenuBar;
import com.example.sheets.table.TablePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public final class MainFrame extends JFrame {

    private final int STARTING_WIDTH = 800;
    private final int STARTING_HEIGHT = 600;
    private final int MINIMUM_WIDTH = 150;
    private final int MINIMUM_HEIGHT = 150;
    private final int STARTING_NUMBER_OF_ROWS = 8;
    private final int STARTING_NUMBER_OF_COLUMNS = 3;

    private final Color BACKGROUND_COLOR = new Color(0x123456);

    MainFrame() {
        setTitle("My Sheets");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(STARTING_WIDTH, STARTING_HEIGHT);
        setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));

        var icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons8-sheets-64.png")));
        setIconImage(icon.getImage());
        getContentPane().setBackground(BACKGROUND_COLOR);

        var table = new TablePanel(STARTING_NUMBER_OF_ROWS, STARTING_NUMBER_OF_COLUMNS);
        add(table);
        setJMenuBar(new MenuBar(table));

        setVisible(true);
    }
}
