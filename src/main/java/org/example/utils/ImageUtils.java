package org.example.utils;

import org.example.model.HistogramData;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;

public class ImageUtils {
    public static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayImage.getGraphics().drawImage(image, 0, 0, null);
        return grayImage;
    }

    public static HistogramData calculateHistogram(BufferedImage image) {
        int[] red = new int[256];
        int[] green = new int[256];
        int[] blue = new int[256];
        int[] gray = new int[256];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                red[r]++;
                green[g]++;
                blue[b]++;
                gray[(r + g + b) / 3]++;
            }
        }

        return new HistogramData(red, green, blue, gray);
    }

    public static BufferedImage linearCorrection(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return linearCorrectionGrayscale(image);
        }
        return linearCorrectionColor(image);
    }

    private static BufferedImage linearCorrectionGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                histogram[gray]++;
            }
        }

        int min = 0;
        while (min < 255 && histogram[min] == 0) min++;

        int max = 255;
        while (max > 0 && histogram[max] == 0) max--;

        if (min != max) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int gray = image.getRGB(x, y) & 0xFF;
                    int corrected = (gray - min) * 255 / (max - min);
                    corrected = Math.max(0, Math.min(255, corrected));
                    result.setRGB(x, y, (corrected << 16) | (corrected << 8) | corrected);
                }
            }
        } else {
            result.getGraphics().drawImage(image, 0, 0, null);
        }

        return result;
    }

    private static BufferedImage linearCorrectionColor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[][] histograms = new int[3][256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                histograms[0][(rgb >> 16) & 0xFF]++; // R
                histograms[1][(rgb >> 8) & 0xFF]++;  // G
                histograms[2][rgb & 0xFF]++;         // B
            }
        }

        int[] mins = new int[3];
        int[] maxs = new int[3];

        for (int c = 0; c < 3; c++) {
            mins[c] = 0;
            while (mins[c] < 255 && histograms[c][mins[c]] == 0) mins[c]++;

            maxs[c] = 255;
            while (maxs[c] > 0 && histograms[c][maxs[c]] == 0) maxs[c]--;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (mins[0] != maxs[0]) r = (r - mins[0]) * 255 / (maxs[0] - mins[0]);
                if (mins[1] != maxs[1]) g = (g - mins[1]) * 255 / (maxs[1] - mins[1]);
                if (mins[2] != maxs[2]) b = (b - mins[2]) * 255 / (maxs[2] - mins[2]);

                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }

    public static BufferedImage gammaCorrection(BufferedImage image, double gamma) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] lookupTable = new int[256];
        for (int i = 0; i < 256; i++) {
            lookupTable[i] = (int) (255 * Math.pow(i / 255.0, 1.0 / gamma));
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = lookupTable[(rgb >> 16) & 0xFF];
                int g = lookupTable[(rgb >> 8) & 0xFF];
                int b = lookupTable[rgb & 0xFF];

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return result;
    }
}