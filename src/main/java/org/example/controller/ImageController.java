package org.example.controller;

import org.example.model.ImageModel;
import org.example.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ImageController {
    private final ImageModel model;

    public ImageController(ImageModel model) {
        this.model = model;
    }

    public void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Image");

        // Установка фильтров
        String[] extensions = ImageIO.getReaderFileSuffixes();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("Image Files", extensions));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedImage image = ImageIO.read(file);

                if (image == null) {
                    throw new IOException("Unsupported image format");
                }

                model.setOriginalImage(image);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not load image: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void convertToGrayscale() {
        if (model.hasImage()) {
            BufferedImage grayImage = ImageUtils.convertToGrayscale(model.getCurrentImage());
            model.setCurrentImage(grayImage);
        }
    }

    public void resetImage() {
        model.resetToOriginal();
    }

    // Другие методы управления изображением...
}