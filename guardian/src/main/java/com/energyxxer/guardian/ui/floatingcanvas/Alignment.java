package com.energyxxer.guardian.ui.floatingcanvas;

public class Alignment {
    public static final float TOP = 0, LEFT = 0;
    public static final float MIDDLE = 0.5f;
    public static final float BOTTOM = 1, RIGHT = 1;

    private float parentAlignmentX;
    private float childAlignmentX;
    private float parentAlignmentY;
    private float childAlignmentY;

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

    public int getX(int parentWidth, int childWidth) {
        return (int) (parentWidth*parentAlignmentX - childWidth*childAlignmentX);
    }

    public int getY(int parentHeight, int childHeight) {
        return (int) (parentHeight*parentAlignmentY - childHeight*childAlignmentY);
    }
}
