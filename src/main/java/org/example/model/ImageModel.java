package org.example.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageModel {
    private BufferedImage originalImage;
    private BufferedImage currentImage;
    private boolean modified = false;
    private List<ImageChangeListener> listeners = new ArrayList<>();

    public interface ImageChangeListener {
        void onImageChanged();
    }

    public void addImageChangeListener(ImageChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (ImageChangeListener listener : listeners) {
            listener.onImageChanged();
        }
    }

    public void setOriginalImage(BufferedImage image) {
        this.originalImage = image;
        this.currentImage = copyImage(image);
        this.modified = false;
        notifyListeners();
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(BufferedImage image) {
        this.currentImage = image;
        this.modified = !imagesEqual(originalImage, currentImage);
        notifyListeners();
    }

    public void resetToOriginal() {
        if (originalImage != null) {
            this.currentImage = copyImage(originalImage);
            this.modified = false;
            notifyListeners();
        }
    }

    public boolean hasImage() {
        return originalImage != null;
    }

    public boolean isModified() {
        return modified;
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                source.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private boolean imagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1 == img2) return true;
        if (img1 == null || img2 == null) return false;
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) return false;

        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}