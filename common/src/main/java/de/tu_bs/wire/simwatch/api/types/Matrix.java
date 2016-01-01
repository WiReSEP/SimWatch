package de.tu_bs.wire.simwatch.api.types;

import java.util.Arrays;

public class Matrix {
    private int width, height;
    private double[] data;

    public Matrix(Number[][] data) {
        if (data == null || data.length == 0 || data[0].length == 0)
            throw new IllegalArgumentException("Matrix data must be at least 1x1 and not null");
        this.height = data.length;
        this.width = data[0].length;
        this.data = new double[width * height];
        int i = 0;
        for (Number[] line : data) {
            if (line.length == this.width) {
                for (Number number : line) {
                    this.data[i++] = number.doubleValue();
                }
            } else {
                throw new IllegalArgumentException("All rows must be of equal size");
            }
        }
    }

    public double get(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw new IndexOutOfBoundsException();
        return data[y * width + x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matrix matrix = (Matrix) o;

        return width == matrix.width
                && height == matrix.height
                && Arrays.equals(data, matrix.data);
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

}
