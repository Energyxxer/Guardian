package com.energyxxer.guardian.ui.floatingcanvas;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class DrawMultipliedComposite implements Composite, CompositeContext {
    private Color tint;
    private boolean ignoreSourceColor = false;

    public DrawMultipliedComposite(Color tint) {
        this.tint = tint;
    }

    protected void checkRasterCombination(Raster a, Raster b) {
        if (a.getSampleModel().getDataType() != b.getSampleModel().getDataType()) {
            throw new IllegalStateException("Expected consistent input raster data types: " + a.getSampleModel().getDataType() + ", " + b.getSampleModel().getDataType());
        }
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        checkRasterCombination(dstIn, dstOut);

        int srcType = src.getSampleModel().getDataType();
        int dstType = dstIn.getSampleModel().getDataType();

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        int x, y;
        Object srcPixels = srcType == DataBuffer.TYPE_INT ? new int[width] : new byte[width * 4];
        Object dstPixels = dstType == DataBuffer.TYPE_INT ? new int[width] : new byte[width * 4];

        for (y=0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstPixels);

            for (x=0; x < width; x++) {
                int srcColor = srcType == DataBuffer.TYPE_INT ? ((int[])srcPixels)[x] : pack(((byte[])srcPixels)[4*x], ((byte[])srcPixels)[4*x+1], ((byte[])srcPixels)[4*x+2], ((byte[])srcPixels)[4*x+3]);
                int dstColor = dstType == DataBuffer.TYPE_INT ? ((int[])dstPixels)[x] : pack(((byte[])dstPixels)[4*x], ((byte[])dstPixels)[4*x+1], ((byte[])dstPixels)[4*x+2], ((byte[])dstPixels)[4*x+3]);

                int mixedColor = mixPixel(srcColor, dstColor);

                if(dstType == DataBuffer.TYPE_INT) {
                    ((int[])dstPixels)[x] = mixedColor;
                } else {
                    unpack(mixedColor, (byte[]) dstPixels, 4*x);
                }
            }

            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }

    private static void unpack(int color, byte[] dstPixels, int i) {
        byte a = (byte) ((color >> 24) & 0xFF);
        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8) & 0xFF);
        byte b = (byte) ((color) & 0xFF);
        dstPixels[i] = r;
        dstPixels[i+1] = g;
        dstPixels[i+2] = b;
        dstPixels[i+3] = a;
    }

    private static int pack(byte r, byte g, byte b, byte a) {
        int ir = (r + 255) % 255;
        int ig = (g + 255) % 255;
        int ib = (b + 255) % 255;
        int ia = (a + 255) % 255;
        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    private int mixPixel(int src, int dst) {
        int sa = (src >> 24) & 0xFF;
        sa = (sa * tint.getAlpha()) / 255;
        int da = 255;
        int a = Math.min(255, sa + da);

        int sb = ignoreSourceColor ? 0xFF : (src) & 0xFF;
        sb = (sb * tint.getBlue()) / 255;
        int db = (dst) & 0xFF;
        int b = Math.min(Math.max(sb*sa/255 + db*da*(255-sa)/255/255, 0), 255);

        int sg = ignoreSourceColor ? 0xFF : (src >> 8) & 0xFF;
        sg = (sg * tint.getGreen()) / 255;
        int dg = (dst >> 8) & 0xFF;
        int g = Math.min(Math.max(sg*sa/255 + dg*da*(255-sa)/255/255, 0), 255);

        int sr = ignoreSourceColor ? 0xFF : (src >> 16) & 0xFF;
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

    public DrawMultipliedComposite setIgnoreSourceColor(boolean ignoreSourceColor) {
        this.ignoreSourceColor = ignoreSourceColor;
        return this;
    }
}