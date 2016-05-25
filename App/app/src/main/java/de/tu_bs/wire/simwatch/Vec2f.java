package de.tu_bs.wire.simwatch;

/**
 * Created by mw on 14.05.16.
 */
public class Vec2f {

    private float x;
    private float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f() {
        this(0, 0);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float x() {
        return getX();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float y() {
        return getY();
    }

    public void set(float x, float y) {
        setX(x);
        setY(y);
    }

    public float length() {
        return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public Vec2f negate() {
        return new Vec2f(-x(), -y());
    }

    public Vec2f plus(Vec2f other) {
        return new Vec2f(x() + other.x(), y() + other.y());
    }

    public Vec2f times(float multiplier) {
        return new Vec2f(x() * multiplier, y() * multiplier);
    }

    public Vec2f minus(Vec2f other) {
        return new Vec2f(x() - other.x(), y() - other.y());
    }

    public float dotProduct(Vec2f other) {
        return x() * other.x() + y() * other.y();
    }
}
