package org.example.view;

import org.example.model.ImageModel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private ImageModel model;

    // Конструктор без параметров (для совместимости)
    public ImagePanel() {
        super();
    }

    // Конструктор с моделью
    public ImagePanel(ImageModel model) {
        super();
        this.model = model;
    }

    public void setModel(ImageModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (model != null && model.getCurrentImage() != null) {
            BufferedImage img = model.getCurrentImage();
            Image scaled = img.getScaledInstance(
                    getWidth(), -1, Image.SCALE_SMOOTH);
            g.drawImage(scaled, 0, 0, this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // Сначала проверяем по ширине
        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        // Затем проверяем по высоте
        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }

    public void updateView() {
        repaint();
    }
}