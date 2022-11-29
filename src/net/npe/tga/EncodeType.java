/**
 * EncodeType.java (Split out of TGAWriter.java)
 *
 * Copyright (c) 2015 Kenji Sasaki
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

public enum EncodeType {
    NONE, // No RLE encoding
    RLE,  // RLE encoding
    AUTO, // auto
}
