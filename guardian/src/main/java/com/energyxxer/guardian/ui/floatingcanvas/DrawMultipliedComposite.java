package com.energyxxer.guardian.ui.floatingcanvas;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class DrawMultipliedComposite implements Composite, CompositeContext {
    private Color tint;

    public DrawMultipliedComposite(Color tint) {
        this.tint = tint;
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
                dstPixels[x] = mixPixel(srcPixels[x], dstPixels[x]);
            }

            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }

    private int mixPixel(int src, int dst) {

        int sa = (src >> 24) & 0xFF;
//        int da = (dst >> 24) & 0xFF;
        int da = 255;
        int a = Math.min(255, sa + da);

        int sb = (src) & 0xFF;
        sb = (sb * tint.getBlue()) / 255;
        int db = (dst) & 0xFF;
        int b = Math.min(Math.max(sb*sa/255 + db*da*(255-sa)/255/255, 0), 255);

        int sg = (src >> 8) & 0xFF;
        sg = (sg * tint.getGreen()) / 255;
        int dg = (dst >> 8) & 0xFF;
        int g = Math.min(Math.max(sg*sa/255 + dg*da*(255-sa)/255/255, 0), 255);

        int sr = (src >> 16) & 0xFF;
        sr = (sr * tint.getRed()) / 255;
        int dr = (dst >> 16) & 0xFF;
        int r = Math.min(Math.max(sr*sa/255 + dr*da*(255-sa)/255/255, 0), 255);

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