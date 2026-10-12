package com.codex.fullm9;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class ExifOrientation {
    ExifOrientation() {
    }

    static long read(byte[] bArr, int i, int i2, boolean z) {
        long j = 0;
        for (int i3 = 0; i3 < i2; i3++) {
            j = (j << 8) | ((long) (bArr[(z ? (i2 - 1) - i3 : i3) + i] & 255));
        }
        return j;
    }

    /* JADX WARN: Type inference failed for: r7v18 */
    /* JADX WARN: Type inference failed for: r7v19, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r7v22 */
    static byte[] upright(byte[] bArr) {
        int i;
        if (bArr.length < 4 || (bArr[0] & 255) != 255 || (bArr[1] & 255) != 216) {
            return bArr;
        }
        int i2 = 2;
        while (i2 + 3 < bArr.length) {
            int i3 = i2 + 1;
            if ((bArr[i2] & 255) != 255) {
                return bArr;
            }
            while (i3 < bArr.length && (bArr[i3] & 255) == 255) {
                i3++;
            }
            if (i3 >= bArr.length) {
                return bArr;
            }
            i2 = i3 + 1;
            int i4 = bArr[i3] & 255;
            if (i4 == 218 || i4 == 217) {
                return bArr;
            }
            if (i4 != 1 && (i4 < 208 || i4 > 215)) {
                int i5 = i2 + 2;
                if (i5 > bArr.length || (i = ((bArr[i2] & 255) * 256) + (bArr[i2 + 1] & 255)) < 2 || i > bArr.length - i2) {
                    return bArr;
                }
                i2 += i;
                if (i4 == 225 && i >= 16 && bArr[i5] == 69 && bArr[i5 + 1] == 120 && bArr[i5 + 2] == 105 && bArr[i5 + 3] == 102 && bArr[i5 + 4] == 0 && bArr[i5 + 5] == 0) {
                    int i6 = i5 + 6;
                    ?? r7 = (bArr[i6] == 73 && bArr[i6 + 1] == 73) ? 1 : 0;
                    if ((r7 != 0 || (bArr[i6] == 77 && bArr[i6 + 1] == 77)) && read(bArr, i6 + 2, 2, r7) == 42) {
                        long j = read(bArr, i6 + 4, 4, r7);
                        if (j < 8 || j > (i2 - i6) - 2) {
                            return bArr;
                        }
                        int i7 = i6 + ((int) j);
                        long j2 = read(bArr, i7, 2, r7);
                        if (j2 > ((i2 - i7) - 2) / 12) {
                            return bArr;
                        }
                        for (int i8 = 0; i8 < j2; i8++) {
                            int i9 = i7 + 2 + (i8 * 12);
                            if (read(bArr, i9, 2, r7) == 274 && read(bArr, i9 + 2, 2, r7) == 3 && read(bArr, i9 + 4, 4, r7) == 1) {
                                byte[] bArr2 = (byte[]) bArr.clone();
                                bArr2[i9 + 8] = (byte) r7;
                                bArr2[i9 + 9] = (byte) (r7 ^ 1);
                                return bArr2;
                            }
                        }
                    } else {
                        return bArr;
                    }
                }
            }
        }
        return bArr;
    }
}
