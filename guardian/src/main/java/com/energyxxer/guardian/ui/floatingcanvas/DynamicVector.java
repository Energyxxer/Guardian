package com.energyxxer.guardian.ui.floatingcanvas;

public class DynamicVector {
    public float x;
    public Unit unitX;
    public float y;
    public Unit unitY;

    public DynamicVector(int x, int y) {
        this(x, Unit.ABSOLUTE, y, Unit.ABSOLUTE);
    }

    public DynamicVector(float x, Unit unitX, float y, Unit unitY) {
        this.x = x;
        this.unitX = unitX;
        this.y = y;
        this.unitY = unitY;
    }

    public void setX(int x) {
        setX(x, Unit.ABSOLUTE);
    }

    public void setX(float x, Unit unit) {
        this.x = x;
        this.unitX = unit;
    }

    public void setY(int y) {
        setY(y, Unit.ABSOLUTE);
    }

    public void setY(float y, Unit unit) {
        this.y = y;
        this.unitY = unit;
    }

    public int getAbsoluteX(int containerWidth, int containerHeight) {
        return unitX.convert(x, containerWidth, containerHeight);
    }

    public int getAbsoluteY(int containerWidth, int containerHeight) {
        return unitY.convert(y, containerHeight, containerWidth);
    }

    public interface Unit {
        Unit ABSOLUTE = (m, cpa, csa) -> (int) m;
        Unit RELATIVE = (m, cpa, csa) -> (int) (m*cpa);
        Unit RELATIVE_MIN = (m, cpa, csa) -> (int) (m*Math.min(cpa, csa));
        Unit RELATIVE_MAX = (m, cpa, csa) -> (int) (m*Math.max(cpa, csa));

        int convert(float magnitude, int containerPrimaryAxis, int containerSecondaryAxis);
    }
}
