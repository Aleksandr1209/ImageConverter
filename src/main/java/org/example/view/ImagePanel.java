package org.example.view;

import org.example.model.ImageModel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private ImageModel model;

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

    public void updateView() {
        repaint();
    }
}