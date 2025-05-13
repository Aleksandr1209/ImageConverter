package org.example.controller;

import org.example.model.ImageModel;
import org.example.utils.ImageUtils;

import java.awt.image.BufferedImage;
import javax.swing.*;



public class ImageController {
    private final ImageModel model;

    public ImageController(ImageModel model) {
        this.model = model;
    }

    public void convertToGrayscale() {
        if (model.hasImage()) {
            BufferedImage grayImage = ImageUtils.convertToGrayscale(model.getCurrentImage());
            model.setCurrentImage(grayImage);
        }
    }

    public void applyLinearCorrection() {
        if (!model.hasImage() || model.getCurrentImage() == null) {
            System.err.println("No image loaded!");
            return;
        }

        try {
            BufferedImage result = ImageUtils.linearCorrection(model.getCurrentImage());
            if (result == null) {
                System.err.println("Linear correction returned null!");
                return;
            }
            model.setCurrentImage(result);
        } catch (Exception e) {
            System.err.println("Error in linear correction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void applyNonlinearCorrection() {
        if (!model.hasImage()) return;

        String input = JOptionPane.showInputDialog(
                "Enter gamma value (0.1-5.0, 1.0=no change):",
                "1.5");

        try {
            double gamma = Double.parseDouble(input);
            gamma = Math.max(0.1, Math.min(5.0, gamma));

            BufferedImage result = ImageUtils.gammaCorrection(
                    model.getCurrentImage(),
                    gamma);
            model.setCurrentImage(result);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "Invalid gamma value",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}