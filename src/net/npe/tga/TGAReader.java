/**
 * TGAReader.java
 *
 * Copyright (c) 2014 Kenji Sasaki
 * Released under the MIT license.
 * https://github.com/npedotnet/TGAReader/blob/master/LICENSE
 *
 * English document
 * https://github.com/npedotnet/TGAReader/blob/master/README.md
 *
 * Japanese document
 * https://web.archive.org/web/20150719013130/http://3dtech.jp/wiki/index.php?TGAReader
 *
 */

package net.npe.tga;

import java.io.IOException;

public final class TGAReader {
    private static final int COLORMAP = 1;
    private static final int RGB = 2;
    private static final int GRAYSCALE = 3;
    private static final int COLORMAP_RLE = 9;
    private static final int RGB_RLE = 10;
    private static final int GRAYSCALE_RLE = 11;
    private static final int RIGHT_ORIGIN = 0x10;
    private static final int UPPER_ORIGIN = 0x20;

    public static final Order ARGB = new Order(16, 8, 0, 24);
    public static final Order ABGR = new Order(0, 8, 16, 24);

    public static int getWidth(byte[] buffer) {
        return (buffer[12] & 0xFF) | (buffer[13] & 0xFF) << 8;
    }

    public static int getHeight(byte[] buffer) {
        return (buffer[14] & 0xFF) | (buffer[15] & 0xFF) << 8;
    }

    public static int[] read(byte[] buffer, Order order) throws IOException {
        // header
        // int idFieldLength = buffer[0] & 0xFF;
        // int colormapType = buffer[1] & 0xFF;
        int type = buffer[2] & 0xFF;
        int colormapOrigin = (buffer[3] & 0xFF) | (buffer[4] & 0xFF) << 8;
        int colormapLength = (buffer[5] & 0xFF) | (buffer[6] & 0xFF) << 8;
        int colormapDepth = buffer[7] & 0xFF;
        // int originX = (buffer[8] & 0xFF) | (buffer[9] & 0xFF) << 8; // unsupported
        // int originY = (buffer[10] & 0xFF) | (buffer[11] & 0xFF) << 8; // unsupported
        int width = getWidth(buffer);
        int height = getHeight(buffer);
        int depth = buffer[16] & 0xFF;
        int descriptor = buffer[17] & 0xFF;

        int[] pixels = null;

        // data
        switch(type) {
            case COLORMAP: {
                int imageDataOffset = 18 + (colormapDepth / 8) * colormapLength;
                pixels = createPixelsFromColormap(width, height, colormapDepth, buffer, imageDataOffset, buffer, colormapOrigin, descriptor, order);
                break;
            }
            case RGB: {
                pixels = createPixelsFromRGB(width, height, depth, buffer, 18, descriptor, order);
                break;
            }
            case GRAYSCALE: {
                pixels = createPixelsFromGrayscale(width, height, depth, buffer, 18, descriptor, order);
                break;
            }
            case COLORMAP_RLE: {
                int imageDataOffset = 18 + (colormapDepth / 8) * colormapLength;
                byte[] decodeBuffer = decodeRLE(width, height, depth, buffer, imageDataOffset);
                pixels = createPixelsFromColormap(width, height, colormapDepth, decodeBuffer, 0, buffer, colormapOrigin, descriptor, order);
                break;
            }
            case RGB_RLE: {
                byte[] decodeBuffer = decodeRLE(width, height, depth, buffer, 18);
                pixels = createPixelsFromRGB(width, height, depth, decodeBuffer, 0, descriptor, order);
                break;
            }
            case GRAYSCALE_RLE: {
                byte[] decodeBuffer = decodeRLE(width, height, depth, buffer, 18);
                pixels = createPixelsFromGrayscale(width, height, depth, decodeBuffer, 0, descriptor, order);
                break;
            }
            default: {
                throw new IOException("Unsupported image type: "+type);
            }
        }
        return pixels;
    }

    private static byte[] decodeRLE(int width, int height, int depth, byte[] buffer, int offset) {
        int elementCount = depth / 8;
        byte[] elements = new byte[elementCount];
        int decodeBufferLength = elementCount * width * height;
        byte[] decodeBuffer = new byte[decodeBufferLength];
        int decoded = 0;

        while(decoded < decodeBufferLength) {
            int packet = buffer[offset++] & 0xFF;
            if((packet & 0x80) != 0) { // RLE
                for(int i = 0; i < elementCount; i++) {
                    elements[i] = buffer[offset++];
                }
                int count = (packet & 0x7F) + 1;
                for(int i = 0; i < count; i++) {
                    for(int j = 0; j < elementCount; j++) {
                        decodeBuffer[decoded++] = elements[j];
                    }
                }
            } else { // RAW
                int count = (packet + 1) * elementCount;
                for(int i = 0; i < count; i++) {
                    decodeBuffer[decoded++] = buffer[offset++];
                }
            }
        }
        return decodeBuffer;
    }

    private static int[] createPixelsFromColormap(int width, int height, int depth, byte[] bytes, int offset, byte[] palette, int colormapOrigin, int descriptor, Order order) throws IOException {
        int[] pixels = null;
        int rs = order.redShift;
        int gs = order.greenShift;
        int bs = order.blueShift;
        int as = order.alphaShift;

        switch(depth) {
            case 24:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 3, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 3, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 3, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 3, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            case 32:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 4, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 4, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 4, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromColormap(width, bytes, offset, 4, palette, colormapOrigin, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            default:
                throw new IOException("Unsupported depth:"+depth);
        }
        return pixels;
    }

    private static int[] createPixelsFromRGB(int width, int height, int depth, byte[] bytes, int offset, int descriptor, Order order) throws IOException {
        int[] pixels = null;
        int rs = order.redShift;
        int gs = order.greenShift;
        int bs = order.blueShift;
        int as = order.alphaShift;

        switch(depth) {
            case 24:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 3, i, j, rs, gs, bs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 3, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 3, i, j, rs, gs, bs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 3, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            case 32:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 4, i, j, rs, gs, bs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 4, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 4, i, j, rs, gs, bs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromRGB(width, bytes, offset, 4, i, j, rs, gs, bs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            default:
                throw new IOException("Unsupported depth:"+depth);
        }
        return pixels;
    }

    private static int[] createPixelsFromGrayscale(int width, int height, int depth, byte[] bytes, int offset, int descriptor, Order order) throws IOException {
        int[] pixels = null;
        int rs = order.redShift;
        int gs = order.greenShift;
        int bs = order.blueShift;
        int as = order.alphaShift;

        switch(depth) {
            case 8:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 0, i, j, rs, bs, gs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 0, i, j, rs, bs, gs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 0, i, j, rs, bs, gs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 0, i, j, rs, bs, gs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            case 16:
                pixels = new int[width * height];
                if((descriptor & RIGHT_ORIGIN) != 0) {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 1, i, j, rs, bs, gs, as);
                                pixels[width * i + (width - j - 1)] = color;
                            }
                        }
                    } else {
                        // LowerRight
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 1, i, j, rs, bs, gs, as);
                                pixels[width * (height - i - 1) + (width - j - 1)] = color;
                            }
                        }
                    }
                } else {
                    if((descriptor & UPPER_ORIGIN) != 0) {
                        // UpperLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 1, i, j, rs, bs, gs, as);
                                pixels[width * i + j] = color;
                            }
                        }
                    } else {
                        // LowerLeft
                        for(int i = 0; i < height; i++) {
                            for(int j = 0; j < width; j++) {
                                int color = getColorFromGrayscale(width, bytes, offset, 1, i, j, rs, bs, gs, as);
                                pixels[width * (height - i - 1) + j] = color;
                            }
                        }
                    }
                }
                break;
            default:
                throw new IOException("Unsupported depth:"+depth);
        }
        return pixels;
    }

    private static int getColorFromColormap(int width, byte[] bytes, int offset, int indexValue, byte[] palette, int colormapOrigin, int loopI, int loopJ, int rs, int gs, int bs, int as) {
        int colormapIndex = bytes[offset + width * loopI + loopJ] & 0xFF - colormapOrigin;
        int color = 0xFFFFFFFF;
        if(colormapIndex >= 0) {
            int index = indexValue * colormapIndex + 18;
            int b = palette[index + 0] & 0xFF;
            int g = palette[index + 1] & 0xFF;
            int r = palette[index + 2] & 0xFF;
            int a;
            if(indexValue == 3) {
                a = 0xFF;
            } else {
                a = palette[index + 3] & 0xFF;
            }
            color = (r << rs) | (g << gs) | (b << bs) | (a << as);
        }
        return color;
    }

    private static int getColorFromRGB(int width, byte[] bytes, int offset, int indexValue, int loopI, int loopJ, int rs, int gs, int bs, int as) {
        int index = offset + indexValue * width * loopI + indexValue * loopJ;
        int b = bytes[index + 0] & 0xFF;
        int g = bytes[index + 1] & 0xFF;
        int r = bytes[index + 2] & 0xFF;
        int a;
        if(indexValue == 3) {
            a = 0xFF;
        } else {
            a = bytes[index + 3] & 0xFF;
        }
        int color = (r << rs) | (g << gs) | (b << bs) | (a << as);
        return color;
    }

    private static int getColorFromGrayscale(int width, byte[] bytes, int offset, int indexValue, int loopI, int loopJ, int rs, int gs, int bs, int as) {
        int e;
        int a;
        if(indexValue == 0) {
            e = bytes[offset + width * loopI + loopJ] & 0xFF;
            a = 0xFF;
        } else {
            e = bytes[offset + 2 * width * loopI + 2 * loopJ + 0] & 0xFF;
            a = bytes[offset + 2 * width * loopI + 2 * loopJ + 1] & 0xFF;
        }
        int color = (e << rs) | (e << gs) | (e << bs) | (a << as);
        return color;
    }
}
