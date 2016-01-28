package de.tu_bs.wire.simwatch.api.types;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;

public final class Vector {
    private int length;
    private double[] data;

    public Vector(@NotNull Number... data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Vector data must not be empty or null");
        }
        this.length = data.length;
        this.data = new double[length];
        for (int i = 0; i < length; i++) {
            Number number = data[i];
            this.data[i] = number.doubleValue();
        }
    }

    public double get(int i) {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException("Given " + i + ", expected 0 <= i <= " + (length - 1));
        return data[i];
    }

    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return length == vector.length
                && Arrays.equals(data, vector.data);
    }

    @Override
    public int hashCode() {
        int result = length;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
