package org.example.view;

import org.example.model.HistogramData;
import org.example.model.ImageModel;
import org.example.utils.ImageUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import javax.swing.JPanel;

public class HistogramPanel extends JPanel {
    private ImageModel model;

    public HistogramPanel(ImageModel model) {
        this.model = model;
        setPreferredSize(new Dimension(256, 256));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (model.hasImage()) {
            HistogramData data = ImageUtils.calculateHistogram(model.getCurrentImage());
            drawHistogram(g2d, data);
        }
    }

    private void drawHistogram(Graphics2D g2d, HistogramData data) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawChannel(g2d, data.getRedHistogram(), Color.RED);
        drawChannel(g2d, data.getGreenHistogram(), Color.GREEN);
        drawChannel(g2d, data.getBlueHistogram(), Color.BLUE);
        drawChannel(g2d, data.getGrayHistogram(), Color.BLACK);
    }

    private void drawChannel(Graphics2D g2d, int[] channel, Color color) {
        int max = 1;
        for (int value : channel) {
            if (value > max) max = value;
        }

        g2d.setColor(color);
        for (int i = 0; i < channel.length; i++) {
            int height = (int) (getHeight() * channel[i] / (double) max);
            g2d.drawLine(i, getHeight(), i, getHeight() - height);
        }
    }

    public void updateView() {
        repaint();
    }
}
