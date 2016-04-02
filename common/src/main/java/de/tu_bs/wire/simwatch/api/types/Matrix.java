package de.tu_bs.wire.simwatch.api.types;

import java.util.Arrays;

public final class Matrix {
    private int width, height;
    private double[][] data;

    public Matrix(Number[][] data) {
        if (data == null || data.length == 0 || data[0].length == 0) {
            throw new IllegalArgumentException("Matrix data must be at least 1x1 and not null");
        }
        this.height = data.length;
        this.width = data[0].length;
        this.data = new double[height][width];
        for (int i = 0; i < height; i++) {
            Number[] line = data[i];
            if (line.length == this.width) {
                for (int j = 0; j < height; j++) {
                    Number number = line[j];
                    this.data[i][j] = number.doubleValue();
                }
            } else {
                throw new IllegalArgumentException("All rows must be of equal size");
            }
        }
    }

    public double get(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            throw new IndexOutOfBoundsException();
        return data[y][x];
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
                && Arrays.deepEquals(data, matrix.data);
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

}
