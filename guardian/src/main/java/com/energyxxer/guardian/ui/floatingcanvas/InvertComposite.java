package com.energyxxer.guardian.ui.floatingcanvas;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class InvertComposite implements Composite, CompositeContext {

    public static final InvertComposite INSTANCE = new InvertComposite();

    private InvertComposite() {
    }

    protected void checkRaster(Raster r) {
        if (r.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
            throw new IllegalStateException("Expected integer sample type");
        }
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        checkRaster(src);
        checkRaster(dstIn);
        checkRaster(dstOut);

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        int x, y;
        int[] srcPixels = new int[width];
        int[] dstPixels = new int[width];

        for (y=0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstPixels);

            for (x=0; x < width; x++) {
                dstPixels[x] = perform(srcPixels[x], dstPixels[x]);
            }

            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }

    private int perform(int src, int dst) {

        int da = 255;
        int a = 255;

        int db = (dst) & 0xFF;
        int b = 255-db;

        int dg = (dst >> 8) & 0xFF;
        int g = 255-dg;

        int dr = (dst >> 16) & 0xFF;
        int r = 255-dr;

        return (b) | (g << 8) | (r << 16) | (a << 24);
    }


    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return this;
    }

    @Override
    public void dispose() {

    }
}
