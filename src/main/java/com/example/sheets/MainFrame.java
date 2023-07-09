package com.example.sheets;

import com.example.sheets.menu.MenuBar;
import com.example.sheets.table.TablePanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    MainFrame() {
        setTitle("My Sheets");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(150, 150));

        var icon = new ImageIcon(getClass().getResource("/icons8-sheets-64.png"));
        setIconImage(icon.getImage());
        getContentPane().setBackground(new Color(0x123456));

        var table = new TablePanel(8, 3);
        add(table);
        setJMenuBar(new MenuBar(table));

        setVisible(true);
    }
}
