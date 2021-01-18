package com.energyxxer.guardian.ui.floatingcanvas;

public class Alignment {
    public static final float TOP = 0, LEFT = 0;
    public static final float MIDDLE = 0.5f;
    public static final float BOTTOM = 1, RIGHT = 1;

    private float parentAlignmentX;
    private DynamicVector.Unit parentUnitX = DynamicVector.Unit.RELATIVE;
    private float childAlignmentX;
    private DynamicVector.Unit childUnitX = DynamicVector.Unit.RELATIVE;

    private float parentAlignmentY;
    private DynamicVector.Unit parentUnitY = DynamicVector.Unit.RELATIVE;
    private float childAlignmentY;
    private DynamicVector.Unit childUnitY = DynamicVector.Unit.RELATIVE;

    public Alignment() {
        this(MIDDLE, MIDDLE);
    }

    public Alignment(float alignmentX, float alignmentY) {
        this(alignmentX, alignmentX, alignmentY, alignmentY);
    }

    public Alignment(float parentAlignmentX, float childAlignmentX, float parentAlignmentY, float childAlignmentY) {
        this.parentAlignmentX = parentAlignmentX;
        this.childAlignmentX = childAlignmentX;
        this.parentAlignmentY = parentAlignmentY;
        this.childAlignmentY = childAlignmentY;
    }

    public void setAlignmentX(float alignmentX) {
        setAlignmentX(alignmentX, alignmentX);
    }

    public void setAlignmentX(float parentAlignmentX, float childAlignmentX) {
        this.parentAlignmentX = parentAlignmentX;
        this.childAlignmentX = childAlignmentX;
    }

    public void setAlignmentY(float alignmentY) {
        setAlignmentY(alignmentY, alignmentY);
    }

    public void setAlignmentY(float parentAlignmentY, float childAlignmentY) {
        this.parentAlignmentY = parentAlignmentY;
        this.childAlignmentY = childAlignmentY;
    }

    public void setUnitX(DynamicVector.Unit unitX) {
        setUnitX(unitX, unitX);
    }

    public void setUnitX(DynamicVector.Unit parentUnitX, DynamicVector.Unit childUnitX) {
        this.parentUnitX = parentUnitX;
        this.childUnitX = childUnitX;
    }

    public void setUnitY(DynamicVector.Unit unitY) {
        setUnitY(unitY, unitY);
    }

    public void setUnitY(DynamicVector.Unit parentUnitY, DynamicVector.Unit childUnitY) {
        this.parentUnitY = parentUnitY;
        this.childUnitY = childUnitY;
    }

    public int getX(int parentWidth, int childWidth, int parentHeight, int childHeight) {
        return (int) (parentUnitX.convert(parentAlignmentX, parentWidth, parentHeight) - childUnitX.convert(childAlignmentX, childWidth, childHeight));
    }

    public int getY(int parentHeight, int childHeight, int parentWidth, int childWidth) {
        return (int) (parentUnitY.convert(parentAlignmentY, parentHeight, parentWidth) - childUnitY.convert(childAlignmentY, childHeight, childWidth));
    }
}
