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
    private JButton grayscaleButton;
    private JButton linearCorrectionButton;
    private JButton nonlinearCorrectionButton;


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

        grayscaleButton = new JButton("Grayscale");
        grayscaleButton.setEnabled(false);
        grayscaleButton.addActionListener(e -> {
            imageController.convertToGrayscale();
            updateViews();
        });

        linearCorrectionButton = new JButton("Linear Correction");
        linearCorrectionButton.setEnabled(false);
        linearCorrectionButton.addActionListener(e -> {
            imageController.applyLinearCorrection();
            updateViews();
        });

        nonlinearCorrectionButton = new JButton("Nonlinear Correction");
        nonlinearCorrectionButton.setEnabled(false);
        nonlinearCorrectionButton.addActionListener(e -> {
            imageController.applyNonlinearCorrection();
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
        toolBar.add(grayscaleButton);
        toolBar.add(linearCorrectionButton);
        toolBar.add(nonlinearCorrectionButton);

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
            grayscaleButton.setEnabled(hasImage);
            linearCorrectionButton.setEnabled(hasImage);
            nonlinearCorrectionButton.setEnabled(hasImage);
        });
    }

    private void resetSlidersToDefault() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        saturationSlider.setValue(0);
        gammaSlider.setValue(100);
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

    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Image files",
                        ImageIO.getReaderFileSuffixes()));

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    throw new IOException("Unsupported image format");
                }

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