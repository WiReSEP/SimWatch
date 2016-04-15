package de.tu_bs.wire.simwatch.api.types;

/**
 * Utility class, offering type disambiguation for attribute types
 */
public class Types {

    public static final Type NUMBER = Type.NUMBER;
    public static final Type STRING = Type.STRING;
    public static final Type VECTOR = Type.VECTOR;
    public static final Type MATRIX = Type.MATRIX;
    public static final Type PLOTTABLE = Type.PLOTTABLE;
    public static final Type PLOT_REFERENCE = Type.PLOT_REFERENCE;
    public static final String NUMBER_STR = "number";
    public static final String STRING_STR = "string";
    public static final String VECTOR_STR = "vector";
    public static final String MATRIX_STR = "matrix";
    public static final String PLOTTABLE_STR = "plottable";
    public static final String BOOLEAN_STR = "boolean";
    public static final String PLOT_REFERENCE_STR = "plotReference";

    public static Type getType(String str) {
        switch (str) {
            case NUMBER_STR:
                return Type.NUMBER;
            case STRING_STR:
                return Type.STRING;
            case VECTOR_STR:
                return Type.VECTOR;
            case MATRIX_STR:
                return Type.MATRIX;
            case PLOTTABLE_STR:
                return Type.PLOTTABLE;
            case PLOT_REFERENCE_STR:
                return Type.PLOT_REFERENCE;
            case BOOLEAN_STR:
                return Type.BOOLEAN;
            default:
                throw new IllegalArgumentException("Given String '" + str + "' is not a valid type name");
        }
    }

    public static boolean isValidType(String str) {
        try {
            getType(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getString(Type t) {
        switch (t) {
            case NUMBER:
                return NUMBER_STR;
            case STRING:
                return STRING_STR;
            case VECTOR:
                return VECTOR_STR;
            case MATRIX:
                return MATRIX_STR;
            case PLOTTABLE:
                return PLOTTABLE_STR;
            case PLOT_REFERENCE:
                return PLOT_REFERENCE_STR;
            case BOOLEAN:
                return BOOLEAN_STR;
            default:
                return "";
        }
    }

    public enum Type {NUMBER, STRING, VECTOR, MATRIX, PLOTTABLE, BOOLEAN, PLOT_REFERENCE}

}
