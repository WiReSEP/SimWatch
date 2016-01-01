package de.tu_bs.wire.simwatch.api.types;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class MatrixTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInvalid() throws Exception {
        Double[][] data = {
                {1.0d, 2.3d, 3.0d},
                {4.0d, 5.0d},
                {7.0d, 8.0d, 9.0d}};
        thrown.expect(IllegalArgumentException.class);
        new Matrix(data);
    }

    @Test
    public void testCorrect() throws Exception {
        Double[][] data = {
                {1.0d, 2.3d, 3.0d},
                {4.0d, 5.0d, 6.0d},
                {7.0d, 8.0d, 9.0d}};
        Matrix matrix = new Matrix(data);
        assertEquals(8.0d, matrix.get(1, 2), 0.0001);
        assertEquals(4.0d, matrix.get(0, 1), 0.0001);
        assertEquals(1.0d, matrix.get(0, 0), 0.0001);
    }
}