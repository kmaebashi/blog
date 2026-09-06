package com.kmaebashi.blog.service;

import com.kmaebashi.nctfw.BadRequestException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.ByteOrder;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class ImageUtil {
    // suffixは.jpgか.png限定
    public static void convertImage(Path srcPath, Path destPath, int destWidth, String suffix)
            throws IOException, BadRequestException {
        BufferedImage srcImage = ImageIO.read(srcPath.toFile());
        if (suffix.equals("jpg")) {
            int orientation = getOrientation(srcPath);
            if (orientation >= 1) {
                srcImage = rotateImage(srcImage, orientation);
            }
        }
        shrinkImage(srcImage, destPath, destWidth, suffix);
    }

    public static void shrinkImage(BufferedImage srcImage, Path destPath, int destWidth, String suffix)
            throws IOException {
        BufferedImage destImage;

        // TODO https://community.oracle.com/docs/DOC-983611 The Perils of Image.getScaledInstance()
        int srcWidth = srcImage.getWidth();
        if (destWidth < srcWidth) {
            double scale = (double)destWidth / srcWidth;
            int destHeight = (int)(srcImage.getHeight() * scale);
            Image tempImage = srcImage.getScaledInstance(destWidth, destHeight, Image.SCALE_SMOOTH);
            destImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = destImage.getGraphics();
            g.drawImage(tempImage, 0, 0, null);
            g.dispose();
        } else {
            destImage = srcImage;
        }
        ImageIO.write(destImage, suffix, destPath.toFile());
    }

    public static BufferedImage rotateImage(BufferedImage srcImage, int orientation) {
        if (orientation == 1) {
            return srcImage;
        }

        assert(srcImage.getType() == BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage destImage;
        if (orientation <= 4) {
            destImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        } else {
            destImage = new BufferedImage(srcImage.getHeight(), srcImage.getWidth(),
                    BufferedImage.TYPE_INT_RGB);
        }
        int srcWidth = srcImage.getWidth();
        int srcHeight = srcImage.getHeight();
        int destWidth = destImage.getWidth();
        int destHeight = destImage.getHeight();
        int destX = 0, destY = 0; // make compiler happy
        for (int srcX = 0; srcX < srcWidth; srcX++) {
            for (int srcY = 0; srcY < srcHeight; srcY++) {
                int pixel = srcImage.getRGB(srcX, srcY);

                switch (orientation) {
                    case 2:
                        destX = srcWidth - srcX - 1;
                        destY = srcY;
                        break;
                    case 3:
                        destX = srcWidth - srcX - 1;
                        destY = srcHeight - srcY - 1;
                        break;
                    case 4:
                        destX = srcX;
                        destY = srcHeight - srcY - 1;
                        break;
                    case 5:
                        destX = srcY;
                        destY = srcX;
                        break;
                    case 6:
                        destX = destWidth - srcY - 1;
                        destY = srcX;
                        break;
                    case 7:
                        destX = destWidth - srcY - 1;
                        destY = destHeight - srcX - 1;
                        break;
                    case 8:
                        destX = srcY;
                        destY = destHeight - srcX - 1;
                        break;
                    default:
                        assert(false);
                }
                destImage.setRGB(destX, destY, pixel);
            }
        }
        return destImage;
    }

    /**
     *
     * @param jpegFilePath
     * @return
     * 1: The 0th row is at the visual top of the image, and the 0th column is the visual left-hand side.
     * 2: The 0th row is at the visual top of the image, and the 0th column is the visual right-hand side.
     * 3: The 0th row is at the visual bottom of the image, and the 0th column is the visual right-hand side.
     * 4: The 0th row is at the visual bottom of the image, and the 0th column is the visual left-hand side.
     * 5: The 0th row is the visual left-hand side of the image, and the 0th column is the visual top.
     * 6: The 0th row is the visual right-hand side of the image, and the 0th column is the visual top.
     * 7: The 0th row is the visual right-hand side of the image, and the 0th column is the visual bottom.
     * 8: The 0th row is the visual left-hand side of the image, and the 0th column is the visual bottom.
     * Other: reserved
     * @throws IOException
     */
    public static int getOrientation(Path jpegFilePath) throws BadRequestException, IOException {
        int orientation = -1;

        try (DataInputStream inStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(jpegFilePath.toFile())))) {
            // 冒頭12バイトの構成
            // 先頭2バイト: 0xff 0xd8 JPEGファイルはこれで固定
            // 次の2バイト: 0xff 0xe1 APP1(Application Marker Segment 1)のマーカー
            // 次の4バイト: APP1領域のサイズ(ビッグエンディアン)
            // 次の6バイト: Exifのマーカー('E', 'x', 'i', 'f', 00, 00)
            byte[] headersByte = new byte[2];
            int[] headers;
            int app1Size;
            if (inStream.read(headersByte) != headersByte.length) {
                throw new BadRequestException("JPEGのファイル形式が不正です");
            }
            headers = unsignedByteArrayToIntArray(headersByte);
            if (headers[0] != 0xff || headers[1] != 0xd8) {
                throw new BadRequestException("JPEGのファイル形式が不正です");
            }
            int[] app1Data = null;
            for (;;) {
                byte[] appNHeaderBytes = new byte[2];
                if (inStream.read(appNHeaderBytes) != appNHeaderBytes.length) {
                    return orientation;
                }
                int[] appNHeader = unsignedByteArrayToIntArray(appNHeaderBytes);
                if (appNHeader[0] != 0xff || !(appNHeader[1] >= 0xe0 && appNHeader[1] <= 0xef)) {
                    return orientation;
                }
                byte[] appNSizeBytes = new byte[2];
                if (inStream.read(appNSizeBytes) != appNSizeBytes.length) {
                    throw new BadRequestException("JPEGのファイル形式が不正です");
                }
                int[] appNSizeIntArray = unsignedByteArrayToIntArray(appNSizeBytes);
                // sizeはサイズ自身の2バイトを含むので、データ本体のサイズはsize - 2
                int appNSize = read2Byte(appNSizeIntArray, 0, ByteOrder.BIG_ENDIAN) - 2;
                byte[] appNDataBytes = new byte[appNSize];
                if (inStream.read(appNDataBytes) != appNDataBytes.length) {
                    throw new BadRequestException("JPEGのファイル形式が不正です");
                }
                int[] appNData = unsignedByteArrayToIntArray(appNDataBytes);
                if (appNHeader[1] == 0xe1) {
                    // APP1
                    app1Data = appNData;
                    break;
                }
            }
            if (app1Data == null) {
                return orientation;
            }
            if (app1Data[0] != (byte)'E'
                    || app1Data[1] != (byte)'x'
                    || app1Data[2] != (byte)'i'
                    || app1Data[3] != (byte)'f'
                    || app1Data[4] != 0x00 || app1Data[5] != 0x00) {
                return orientation;
            }

            // APP1のEXIFヘッダの次の8バイトはTIFFヘッダ
            // 先頭2バイト: バイトオーダーを示す。
            //　　　　　　　0x49, 0x49('I', 'I')...リトルエンディアン(Intelの略らしい)
            //　　　　　　　0x4d, 0x4d('M', 'M')...ビッグエンディアン(Motorolaの略らしい)
            // 次の2バイト: 0x00, 0x2a TIFF識別コード(固定)
            // 次の4バイト:
            final int EXIF_HEADER_SIZE = 6;
            ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN; // make compiler happy
            if (app1Data[EXIF_HEADER_SIZE] == 0x49 && app1Data[EXIF_HEADER_SIZE + 1] == 0x49) {
                byteOrder = ByteOrder.LITTLE_ENDIAN;
            } else if (app1Data[EXIF_HEADER_SIZE] == 0x4d && app1Data[EXIF_HEADER_SIZE + 1] == 0x4d) {
                byteOrder = ByteOrder.BIG_ENDIAN;
            } else {
                throw new BadRequestException("JPEGのファイル形式が不正です");
            }
            int tiffVersion = read2Byte(app1Data, EXIF_HEADER_SIZE + 2, byteOrder);
            if (tiffVersion != 0x002a) {
                throw new BadRequestException("JPEGのファイル形式が不正です");
            }

            // 最初のIFD(Image File Directory)である0th IFDのオフセットを取得。
            // これを含め、以後出てくるオフセットは、すべてAPP1の先頭を起点とする。
            // ここまで、0th IFDのオフセットを含めて8バイト使っているので、
            // その続きとなる0th IFDの先頭のオフセットはたいてい8。
            int offsetOf0thIFD = read4Byte(app1Data, EXIF_HEADER_SIZE + 4, byteOrder);

            // 0th IFDのタグの数を取得(先頭2バイト)
            int numOf0thIFDTags = read2Byte(app1Data, EXIF_HEADER_SIZE + offsetOf0thIFD, byteOrder);

            // 8はTIFFヘッダ、2はタグの数の分
            int offset = EXIF_HEADER_SIZE + 8 + 2;
            for (int i = 0; i < numOf0thIFDTags; i++) {
                // ひとつのIFDタグ(固定長12バイト)の構成は以下の通り。
                // 先頭2バイト: タグNo。定義はExifの仕様書(JEITA CP-3451)を参照。
                // 次の2バイト: そのタグの型。
                // 次の4バイト: そのタグに含まれる値の数。
                // 次の4バイト: 値またはオフセット。示すべき値が4バイトに収まる場合はここに
                //              格納され、収まらない場合は、値の場所を示すオフセットが格納される。
                // 画像方向を示すタグNoである0x0112では、型はSHORT(3)、値の数は1。4バイトに収まる。
                int tagNo = read2Byte(app1Data, offset, byteOrder);

                if (tagNo == 0x0112) {
                    orientation = read2Byte(app1Data, offset + 8, byteOrder);
                    int tagType = read2Byte(app1Data, offset + 2, byteOrder);
                    int numOfValues = read4Byte(app1Data, offset + 4, byteOrder);
                    if (tagType != 3 || numOfValues != 1) {
                        throw new BadRequestException("JPEGのファイル形式が不正です");
                    }
                }
                offset += 12;
            }
        }

        return orientation;
    }

    private static int[] unsignedByteArrayToIntArray(byte[] src) {
        int[] dest = new int[src.length];

        for (int i = 0; i < src.length; i++) {
            dest[i] = Byte.toUnsignedInt(src[i]);
        }

        return dest;
    }

    public static int read2Byte(int[] array, int offset, ByteOrder byteOrder) {
        int ret;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            ret = array[offset] * 256 + array[offset + 1];
        } else {
            ret = array[offset + 1] * 256 + array[offset];
        }
        return ret;
    }

    public static int read4Byte(int[] array, int offset, ByteOrder byteOrder) {
        int ret;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            ret = array[offset] * (256 * 256 * 256) + array[offset + 1] * 65536
                    + array[offset + 2] * 256 + array[offset + 3];
        } else {
            ret = array[offset + 3] * (256 * 256 * 256) + array[offset + 2] * 65536
                    + array[offset + 1] * 256 + array[offset];
        }
        return ret;
    }
}
