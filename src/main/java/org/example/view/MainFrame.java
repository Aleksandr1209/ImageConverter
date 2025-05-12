package org.example.view;

import org.example.controller.AdjustmentsController;
import org.example.controller.ImageController;
import org.example.model.ImageModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class MainFrame extends JFrame {
    private final ImageModel model;
    private final ImageController imageController;
    private final ImagePanel imagePanel;
    private final HistogramPanel histogramPanel;
    private final AdjustmentsController adjustmentsController;
    private JSlider brightnessSlider;
    private JSlider contrastSlider;
    private JSlider saturationSlider;
    private JSlider gammaSlider;
    private JButton resetButton;
    private JButton saveButton;
    private JButton openButton;


    public MainFrame() {
        super("Image Processor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);

        model = new ImageModel();
        imageController = new ImageController(model);
        adjustmentsController = new AdjustmentsController(model);

        setLayout(new BorderLayout());

        // Панель инструментов
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);

        // Основная панель
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        imagePanel = new ImagePanel(model);
        imagePanel.setModel(model);
        model.addImageChangeListener(this::updateViews);
        histogramPanel = new HistogramPanel(model);

        mainPanel.add(new JScrollPane(imagePanel));
        mainPanel.add(new JScrollPane(histogramPanel));
        add(mainPanel, BorderLayout.CENTER);

        // Панель настроек
        JPanel settingsPanel = createSettingsPanel();
        add(settingsPanel, BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();

        openButton = new JButton("Open");
        openButton.addActionListener(e -> openImage());

        JButton grayscaleButton = new JButton("Grayscale");
        grayscaleButton.addActionListener(e -> {
            imageController.convertToGrayscale();
            updateViews();
        });

        resetButton = new JButton("Reset");
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> {
            adjustmentsController.resetAllAdjustments();
            resetSlidersToDefault();
            updateViews();
        });

        saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveImage());

        toolBar.add(openButton);
        toolBar.add(resetButton);
        toolBar.add(saveButton);

        return toolBar;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        // Яркость
        brightnessSlider = new JSlider(-100, 100, 0);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.setMajorTickSpacing(50);
        brightnessSlider.addChangeListener(new SliderChangeListener(brightnessSlider,
                value -> adjustmentsController.setBrightness(value)));

        // Контраст
        contrastSlider = new JSlider(-100, 100, 0);
        contrastSlider.setPaintTicks(true);
        contrastSlider.setPaintLabels(true);
        contrastSlider.setMajorTickSpacing(50);
        contrastSlider.addChangeListener(new SliderChangeListener(contrastSlider,
                value -> adjustmentsController.setContrast(value)));

        // Насыщенность
        saturationSlider = new JSlider(-100, 100, 0);
        saturationSlider.setPaintTicks(true);
        saturationSlider.setPaintLabels(true);
        saturationSlider.setMajorTickSpacing(50);
        saturationSlider.addChangeListener(new SliderChangeListener(saturationSlider,
                value -> adjustmentsController.setSaturation(value)));

        // Гамма
        gammaSlider = new JSlider(10, 300, 100);
        gammaSlider.setPaintTicks(true);
        gammaSlider.setPaintLabels(true);
        gammaSlider.setMajorTickSpacing(50);
        gammaSlider.addChangeListener(new SliderChangeListener(gammaSlider,
                value -> adjustmentsController.setGamma(value / 100.0)));

        // Добавление компонентов
        panel.add(new JLabel("Яркость:"));
        panel.add(brightnessSlider);
        panel.add(new JLabel("Контраст:"));
        panel.add(contrastSlider);
        panel.add(new JLabel("Насыщенность:"));
        panel.add(saturationSlider);
        panel.add(new JLabel("Гамма:"));
        panel.add(gammaSlider);

        return panel;
    }

    private class SliderChangeListener implements ChangeListener {
        private final JSlider slider;
        private final Consumer<Integer> handler;
        private int lastValue;

        public SliderChangeListener(JSlider slider, Consumer<Integer> handler) {
            this.slider = slider;
            this.handler = handler;
            this.lastValue = slider.getValue();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!slider.getValueIsAdjusting()) {
                int newValue = slider.getValue();
                if (newValue != lastValue) {
                    handler.accept(newValue);
                    lastValue = newValue;
                }
            }
        }
    }

    private void updateViews() {
        SwingUtilities.invokeLater(() -> {
            imagePanel.updateView();
            histogramPanel.updateView();

            boolean hasImage = model.hasImage();
            boolean isModified = model.isModified();
            resetButton.setEnabled(hasImage && isModified);
            saveButton.setEnabled(hasImage && isModified);
        });
    }

    private void resetSlidersToDefault() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        saturationSlider.setValue(0);
        gammaSlider.setValue(100);
    }

    // Оптимизированный слушатель для слайдеров
    private class OptimizedSliderListener implements ChangeListener {
        private final Consumer<Integer> action;
        private int lastValue;

        public OptimizedSliderListener(Consumer<Integer> action) {
            this.action = action;
            this.lastValue = 0;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int newValue = source.getValue();
                if (newValue != lastValue) {
                    action.accept(newValue);
                    lastValue = newValue;
                }
            }
        }
    }

    private void saveImage() {
        if (!model.hasImage()) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");

        // Устанавливаем фильтры для форматов файлов
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter(
                "JPEG images (*.jpg, *.jpeg)", "jpg", "jpeg");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter(
                "PNG images (*.png)", "png");
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter(
                "BMP images (*.bmp)", "bmp");

        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(bmpFilter);
        fileChooser.setFileFilter(jpgFilter); // Устанавливаем JPG по умолчанию

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String format = ((FileNameExtensionFilter)fileChooser.getFileFilter()).getExtensions()[0];

            // Добавляем расширение, если его нет
            String fileName = fileToSave.getAbsolutePath();
            if (!fileName.toLowerCase().endsWith("." + format)) {
                fileToSave = new File(fileName + "." + format);
            }

            try {
                boolean success = ImageIO.write(
                        model.getCurrentImage(),
                        format,
                        fileToSave);

                if (!success) {
                    JOptionPane.showMessageDialog(this,
                            "The image could not be saved\nUnsupported format: " + format,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getFormatFromExtension(FileFilter filter, File file) {
        // Определяем формат по выбранному фильтру
        if (filter.getDescription().contains("JPEG")) {
            return "jpg";
        } else if (filter.getDescription().contains("PNG")) {
            return "png";
        } else if (filter.getDescription().contains("BMP")) {
            return "bmp";
        }

        // Если формат не определен по фильтру, пробуем определить по расширению файла
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) return "png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "jpg";
        if (name.endsWith(".bmp")) return "bmp";

        // По умолчанию сохраняем как JPEG
        return "jpg";
    }

    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");

        // Установка фильтров для изображений
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Image files",
                        ImageIO.getReaderFileSuffixes()));

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Чтение изображения с проверкой на null
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    throw new IOException("Unsupported image format");
                }

                // Установка изображения в модель
                model.setOriginalImage(image);
                adjustmentsController.resetAllAdjustments();
                resetSlidersToDefault();
                updateViews();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}