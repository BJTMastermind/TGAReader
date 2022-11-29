/**
 * Order.java (Split out of TGAReader.java)
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

class Order {
    public int redShift;
    public int greenShift;
    public int blueShift;
    public int alphaShift;

    Order(int redShift, int greenShift, int blueShift, int alphaShift) {
        this.redShift = redShift;
        this.greenShift = greenShift;
        this.blueShift = blueShift;
        this.alphaShift = alphaShift;
    }
}
