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

    public static BufferedImage adjustBrightness(BufferedImage image, float brightness) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        float factor = brightness; // [-1.0, 1.0]

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (int) (((rgb >> 16) & 0xFF) + factor * 255);
                int g = (int) (((rgb >> 8) & 0xFF) + factor * 255);
                int b = (int) ((rgb & 0xFF) + factor * 255);

                r = clamp(r);
                g = clamp(g);
                b = clamp(b);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    public static BufferedImage adjustContrast(BufferedImage image, float contrast) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        float factor = (259f * (contrast + 255f)) / (255f * (259f - contrast));

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (int) (factor * (((rgb >> 16) & 0xFF) - 128) + 128);
                int g = (int) (factor * (((rgb >> 8) & 0xFF) - 128) + 128);
                int b = (int) (factor * ((rgb & 0xFF) - 128) + 128);

                r = clamp(r);
                g = clamp(g);
                b = clamp(b);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    public static BufferedImage adjustSaturation(BufferedImage image, float saturation) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                float intensity = (r + g + b) / 3f;
                r = (int) (intensity + saturation * (r - intensity));
                g = (int) (intensity + saturation * (g - intensity));
                b = (int) (intensity + saturation * (b - intensity));

                r = clamp(r);
                g = clamp(g);
                b = clamp(b);

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
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

        // Вычисляем гистограмму
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                histogram[gray]++;
            }
        }

        // Находим минимальное и максимальное значения
        int min = 0;
        while (min < 255 && histogram[min] == 0) min++;

        int max = 255;
        while (max > 0 && histogram[max] == 0) max--;

        // Применяем линейное преобразование
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
            // Если изображение уже полностью выровнено
            result.getGraphics().drawImage(image, 0, 0, null);
        }

        return result;
    }

    private static BufferedImage linearCorrectionColor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Вычисляем гистограммы для каждого канала
        int[][] histograms = new int[3][256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                histograms[0][(rgb >> 16) & 0xFF]++; // R
                histograms[1][(rgb >> 8) & 0xFF]++;  // G
                histograms[2][rgb & 0xFF]++;         // B
            }
        }

        // Находим min/max для каждого канала
        int[] mins = new int[3];
        int[] maxs = new int[3];

        for (int c = 0; c < 3; c++) {
            mins[c] = 0;
            while (mins[c] < 255 && histograms[c][mins[c]] == 0) mins[c]++;

            maxs[c] = 255;
            while (maxs[c] > 0 && histograms[c][maxs[c]] == 0) maxs[c]--;
        }

        // Применяем коррекцию для каждого пикселя
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

        // Предварительно вычисленная таблица преобразования
        int[] lookupTable = new int[256];
        for (int i = 0; i < 256; i++) {
            lookupTable[i] = (int) (255 * Math.pow(i / 255.0, 1.0 / gamma));
        }

        // Применяем коррекцию с использованием таблицы
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

    public static BufferedImage applyAllAdjustments(
            BufferedImage image,
            float brightness,
            float contrast,
            float saturation
    ) {
        BufferedImage result = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Яркость
                r = (int) (r + brightness * 255);
                g = (int) (g + brightness * 255);
                b = (int) (b + brightness * 255);

                // Контраст
                r = (int) (((r - 128) * contrast) + 128);
                g = (int) (((g - 128) * contrast) + 128);
                b = (int) (((b - 128) * contrast) + 128);

                // Насыщенность
                float gray = (r + g + b) / 3f;
                r = (int) (gray + saturation * (r - gray));
                g = (int) (gray + saturation * (g - gray));
                b = (int) (gray + saturation * (b - gray));

                // Ограничение значений
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    public static BufferedImage applyColorAdjustments(BufferedImage src,
                                                      float brightness,
                                                      float contrast,
                                                      float saturation) {
        BufferedImage dest = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[] pixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        int[] outPixels = ((DataBufferInt) dest.getRaster().getDataBuffer()).getData();

        // Параллельная обработка для производительности
        IntStream.range(0, pixels.length).parallel().forEach(i -> {
            int rgb = pixels[i];
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            // Яркость
            r = adjustValue(r + (int)(brightness * 255));
            g = adjustValue(g + (int)(brightness * 255));
            b = adjustValue(b + (int)(brightness * 255));

            // Контраст
            r = adjustValue((int)((r - 128) * contrast + 128));
            g = adjustValue((int)((g - 128) * contrast + 128));
            b = adjustValue((int)((b - 128) * contrast + 128));

            // Насыщенность
            float gray = (r + g + b) / 3f;
            r = adjustValue((int)(gray + saturation * (r - gray)));
            g = adjustValue((int)(gray + saturation * (g - gray)));
            b = adjustValue((int)(gray + saturation * (b - gray)));

            outPixels[i] = (r << 16) | (g << 8) | b;
        });

        return dest;
    }

    private static int adjustValue(int value) {
        return Math.max(0, Math.min(255, value));
    }
}