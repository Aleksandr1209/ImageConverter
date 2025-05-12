package org.example.controller;

import org.example.model.ImageModel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AdjustmentsController {
    private final ImageModel model;
    private float brightness = 0;
    private float contrast = 1;
    private float saturation = 1;
    private double gamma = 1.0;

    public AdjustmentsController(ImageModel model) {
        this.model = model;
    }

    public void applyAdjustments() {
        if (!model.hasImage() || model.getOriginalImage() == null) return;

        // Всегда работаем с КОПИЕЙ оригинального изображения
        BufferedImage result = deepCopy(model.getOriginalImage());

        // Применяем все корректировки
        if (gamma != 1.0) {
            result = applyGamma(result, gamma);
        }

        result = applyColorAdjustments(result, brightness, contrast, saturation);

        // Устанавливаем новое изображение
        model.setCurrentImage(result);
    }

    private BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), source.getType());
        source.copyData(copy.getRaster());
        return copy;
    }

    private BufferedImage applyGamma(BufferedImage image, double gamma) {
        int[] gammaLUT = new int[256];
        for (int i = 0; i < 256; i++) {
            gammaLUT[i] = (int) (255 * Math.pow(i / 255.0, 1.0 / gamma));
        }

        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = gammaLUT[(rgb >> 16) & 0xFF];
                int g = gammaLUT[(rgb >> 8) & 0xFF];
                int b = gammaLUT[rgb & 0xFF];
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }

    private BufferedImage applyColorAdjustments(BufferedImage image,
                                                float brightness,
                                                float contrast,
                                                float saturation) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        float brightnessFactor = brightness * 255;
        float contrastFactor = contrast;
        float contrastOffset = 128 * (1 - contrast);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Яркость
                r = (int) (r + brightnessFactor);
                g = (int) (g + brightnessFactor);
                b = (int) (b + brightnessFactor);

                // Контраст
                r = (int) (r * contrastFactor + contrastOffset);
                g = (int) (g * contrastFactor + contrastOffset);
                b = (int) (b * contrastFactor + contrastOffset);

                // Насыщенность
                float gray = (r + g + b) / 3f;
                r = (int) (gray + saturation * (r - gray));
                g = (int) (gray + saturation * (g - gray));
                b = (int) (gray + saturation * (b - gray));

                // Ограничение значений
                r = clamp(r);
                g = clamp(g);
                b = clamp(b);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public void setBrightness(float value) {
        this.brightness = value / 100f;
        applyAdjustments();
    }

    public void setContrast(float value) {
        this.contrast = value / 100f + 1f;
        applyAdjustments();
    }

    public void setSaturation(float value) {
        this.saturation = value / 100f + 1f;
        applyAdjustments();
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
        applyAdjustments();
    }

    public void resetAllAdjustments() {
        this.brightness = 0;
        this.contrast = 1;
        this.saturation = 1;
        this.gamma = 1.0;
        model.resetToOriginal();
    }
}