package com.codex.fullm9;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class JpegScan {
    JpegScan() {
    }

    static int start(byte[] bArr, int i, int i2) {
        int i3;
        if (i < 0 || i2 < 4 || i > bArr.length - i2 || (bArr[i] & 255) != 255 || (bArr[i + 1] & 255) != 216) {
            return -1;
        }
        int i4 = i + 2;
        int i5 = i + i2;
        while (i4 + 3 < i5) {
            int i6 = i4 + 1;
            if ((bArr[i4] & 255) != 255) {
                return -1;
            }
            while (i6 < i5 && (bArr[i6] & 255) == 255) {
                i6++;
            }
            if (i6 >= i5) {
                return -1;
            }
            i4 = i6 + 1;
            int i7 = bArr[i6] & 255;
            if (i7 == 218) {
                return i4 - 2;
            }
            if (i7 == 217) {
                return -1;
            }
            if (i7 != 1 && (i7 < 208 || i7 > 215)) {
                if (i4 + 2 > i5 || (i3 = ((bArr[i4] & 255) * 256) + (bArr[i4 + 1] & 255)) < 2 || i3 > i5 - i4) {
                    return -1;
                }
                i4 += i3;
            }
        }
        return -1;
    }

    static boolean same(byte[] bArr, byte[] bArr2, int i, int i2) {
        if (bArr == null || bArr2 == null) {
            return false;
        }
        int iStart = start(bArr, 0, bArr.length);
        int iStart2 = start(bArr2, i, i2);
        if (iStart < 0 || iStart2 < 0 || bArr.length - iStart != (i + i2) - iStart2) {
            return false;
        }
        while (iStart < bArr.length) {
            int i3 = iStart + 1;
            byte b = bArr[iStart];
            int i4 = iStart2 + 1;
            if (b != bArr2[iStart2]) {
                return false;
            }
            iStart2 = i4;
            iStart = i3;
        }
        return true;
    }
}
