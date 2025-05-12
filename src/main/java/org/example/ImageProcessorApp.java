package org.example;

import org.example.view.MainFrame;

import javax.swing.SwingUtilities;

public class ImageProcessorApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}