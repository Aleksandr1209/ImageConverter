package org.example.model;

public class HistogramData {
    private final int[] red;
    private final int[] green;
    private final int[] blue;
    private final int[] gray;

    public HistogramData(int[] red, int[] green, int[] blue, int[] gray) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.gray = gray;
    }

    // Геттеры
    public int[] getRedHistogram() { return red; }
    public int[] getGreenHistogram() { return green; }
    public int[] getBlueHistogram() { return blue; }
    public int[] getGrayHistogram() { return gray; }
}