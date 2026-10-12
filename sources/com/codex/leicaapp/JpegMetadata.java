package com.codex.leicaapp;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class JpegMetadata {
    public static native byte[] copyExif(byte[] bArr, byte[] bArr2, boolean z);

    private JpegMetadata() {
    }

    public static byte[] copyExif(byte[] bArr, byte[] bArr2) {
        return copyExif(bArr, bArr2, false);
    }

    public static byte[] copyExif(byte[] bArr, byte[] bArr2, boolean z, int i, int i2) {
        return copyExif(bArr, bArr2, z, i, i2, false);
    }

    public static byte[] copyExif(byte[] bArr, byte[] bArr2, boolean z, int i, int i2, boolean z2) {
        int i3;
        if (bArr.length >= 4 && (bArr[0] & 255) == 255) {
            if ((bArr[1] & 255) == 216) {
                int i4 = 2;
                while (i4 + 3 < bArr.length) {
                    int i5 = i4 + 1;
                    if ((bArr[i4] & 255) == 255) {
                        while (i5 < bArr.length && (bArr[i5] & 255) == 255) {
                            i5++;
                        }
                        if (i5 < bArr.length) {
                            i4 = i5 + 1;
                            int i6 = bArr[i5] & 255;
                            if (i6 != 218 && i6 != 217) {
                                if (i6 != 1 && (i6 < 208 || i6 > 215)) {
                                    int i7 = i4 + 2;
                                    if (i7 > bArr.length || (i3 = ((bArr[i4] & 255) << 8) | (bArr[i4 + 1] & 255)) < 2 || i3 > bArr.length - i4) {
                                        break;
                                    }
                                    if (i6 == 225 && i3 >= 16 && bArr[i7] == 69 && bArr[i4 + 3] == 120 && bArr[i4 + 4] == 105 && bArr[i4 + 5] == 102 && bArr[i4 + 6] == 0 && bArr[i4 + 7] == 0) {
                                        byte[] bArrCopyOfRange = Arrays.copyOfRange(bArr, i7, i4 + i3);
                                        boolean z3 = bArrCopyOfRange[6] == 73 && bArrCopyOfRange[7] == 73;
                                        long jInteger = integer(bArrCopyOfRange, 10, 4, z3) + 6;
                                        if (jInteger >= 14 && jInteger <= bArrCopyOfRange.length - 2) {
                                            long jInteger2 = 2 + jInteger + (integer(bArrCopyOfRange, (int) jInteger, 2, z3) * 12);
                                            if (jInteger2 <= bArrCopyOfRange.length - 4) {
                                                int i8 = (int) jInteger2;
                                                Arrays.fill(bArrCopyOfRange, i8, i8 + 4, (byte) 0);
                                            }
                                            if (z) {
                                                markIccColorSpace(bArrCopyOfRange, jInteger, z3);
                                            }
                                            if (i > 0 && i2 > 0) {
                                                updateDimensions(bArrCopyOfRange, jInteger, z3, i, i2, true);
                                            }
                                        }
                                        if (z2) {
                                            bArrCopyOfRange = markMoment(bArrCopyOfRange, z3);
                                        }
                                        int length = bArrCopyOfRange.length + 2;
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr2.length + length + 2);
                                        byteArrayOutputStream.write(bArr2, 0, 2);
                                        byteArrayOutputStream.write(255);
                                        byteArrayOutputStream.write(225);
                                        byteArrayOutputStream.write(length >> 8);
                                        byteArrayOutputStream.write(length & 255);
                                        byteArrayOutputStream.write(bArrCopyOfRange, 0, bArrCopyOfRange.length);
                                        byteArrayOutputStream.write(bArr2, 2, bArr2.length - 2);
                                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                                        extendPrimaryMpf(byteArray, length + 2);
                                        return byteArray;
                                    }
                                    i4 += i3;
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                return bArr2;
            }
        }
        throw new IllegalArgumentException("Invalid JPEG");
    }

    private static byte[] markMoment(byte[] bArr, boolean z) {
        byte[] bArr2;
        long jInteger = integer(bArr, 10, 4, z) + 6;
        if (jInteger < 14 || jInteger > bArr.length - 2) {
            return bArr;
        }
        long jInteger2 = integer(bArr, (int) jInteger, 2, z);
        long j = jInteger + 2;
        while (true) {
            long j2 = jInteger2 - 1;
            if (jInteger2 <= 0 || j > bArr.length - 12) {
                break;
            }
            int i = (int) j;
            if (integer(bArr, i, 2, z) != 34665 || integer(bArr, i + 2, 2, z) != 4 || integer(bArr, i + 4, 4, z) != 1) {
                j += 12;
                jInteger2 = j2;
            } else {
                int i2 = i + 8;
                long jInteger3 = integer(bArr, i2, 4, z) + 6;
                if (jInteger3 < 14 || jInteger3 > bArr.length - 2) {
                    return bArr;
                }
                int i3 = (int) jInteger3;
                int iInteger = (int) integer(bArr, i3, 2, z);
                if (jInteger3 + 6 + (((long) iInteger) * 12) > bArr.length) {
                    return bArr;
                }
                int i4 = 0;
                for (int i5 = 0; i5 < iInteger; i5++) {
                    int i6 = i3 + 2 + (i5 * 12);
                    if (integer(bArr, i6, 2, z) == 34992) {
                        if (integer(bArr, i6 + 2, 2, z) == 1 && integer(bArr, i6 + 4, 4, z) == 1) {
                            bArr[i6 + 8] = 1;
                        }
                        return bArr;
                    }
                }
                int length = (bArr.length + 1) & (-2);
                int i7 = iInteger + 1;
                int i8 = length + 6 + (i7 * 12);
                if (i8 > 65533) {
                    return bArr;
                }
                byte[] bArrCopyOf = Arrays.copyOf(bArr, i8);
                byte[] bArr3 = bArrCopyOf;
                writeInt(bArrCopyOf, i2, length - 6, 4, z);
                writeInt(bArr3, length, i7, 2, z);
                int i9 = length + 2;
                boolean z2 = false;
                while (i4 < iInteger) {
                    int i10 = i3 + 2 + (i4 * 12);
                    if (z2 || integer(bArr, i10, 2, z) <= 34992) {
                        bArr2 = bArr3;
                    } else {
                        bArr2 = bArr3;
                        writeMomentEntry(bArr2, i9, z);
                        i9 += 12;
                        z2 = true;
                    }
                    System.arraycopy(bArr, i10, bArr2, i9, 12);
                    i9 += 12;
                    i4++;
                    bArr3 = bArr2;
                }
                byte[] bArr4 = bArr3;
                if (!z2) {
                    writeMomentEntry(bArr4, i9, z);
                    i9 += 12;
                }
                System.arraycopy(bArr, i3 + 2 + (iInteger * 12), bArr4, i9, 4);
                return bArr4;
            }
        }
        return bArr;
    }

    private static void writeMomentEntry(byte[] bArr, int i, boolean z) {
        writeInt(bArr, i, 34992L, 2, z);
        writeInt(bArr, i + 2, 1L, 2, z);
        writeInt(bArr, i + 4, 1L, 4, z);
        bArr[i + 8] = 1;
    }

    private static void writeInt(byte[] bArr, int i, long j, int i2, boolean z) {
        for (int i3 = 0; i3 < i2; i3++) {
            bArr[(z ? i3 : (i2 - 1) - i3) + i] = (byte) (j >>> (i3 * 8));
        }
    }

    private static void extendPrimaryMpf(byte[] bArr, int i) {
        int i2;
        int i3 = 2;
        while (true) {
            int i4 = i3 + 4;
            if (i4 > bArr.length || (bArr[i3] & 255) != 255 || (i2 = bArr[i3 + 1] & 255) == 218 || i2 == 217) {
                return;
            }
            int i5 = i3 + 2;
            int i6 = (255 & bArr[i3 + 3]) | ((bArr[i5] & 255) << 8);
            if (i6 < 2 || ((long) i3) + 2 + ((long) i6) > bArr.length) {
                return;
            }
            int i7 = i5 + i6;
            int i8 = i3 + 8;
            if (i2 == 226 && i6 >= 18 && bArr[i4] == 77 && bArr[i3 + 5] == 80 && bArr[i3 + 6] == 70 && bArr[i3 + 7] == 0) {
                byte b = bArr[i8];
                boolean z = b == 73 && bArr[i8 + 1] == 73;
                if (!z && (b != 77 || bArr[i8 + 1] != 77)) {
                    return;
                }
                long j = i8;
                long jInteger = integer(bArr, i8 + 4, 4, z) + j;
                long j2 = i8 + 8;
                if (jInteger >= j2 && jInteger <= i7 - 2) {
                    long jInteger2 = integer(bArr, (int) jInteger, 2, z);
                    long j3 = jInteger + 2;
                    while (true) {
                        long j4 = jInteger2 - 1;
                        if (jInteger2 <= 0 || j3 > i7 - 12) {
                            break;
                        }
                        int i9 = (int) j3;
                        if (integer(bArr, i9, 2, z) == 45058) {
                            long jInteger3 = j + integer(bArr, i9 + 8, 4, z);
                            long jInteger4 = integer(bArr, i9 + 4, 4, z);
                            if (jInteger4 < 16 || jInteger3 < j2 || jInteger4 + jInteger3 > i7) {
                                return;
                            }
                            int i10 = ((int) jInteger3) + 4;
                            long jInteger5 = integer(bArr, i10, 4, z) + ((long) i);
                            if (jInteger5 > 4294967295L) {
                                throw new IllegalArgumentException("MPF image size overflow");
                            }
                            for (int i11 = 0; i11 < 4; i11++) {
                                bArr[(z ? i11 : 3 - i11) + i10] = (byte) (jInteger5 >>> (i11 * 8));
                            }
                            return;
                        }
                        j3 += 12;
                        jInteger2 = j4;
                    }
                } else {
                    return;
                }
            }
            i3 = i7;
        }
    }

    private static void updateDimensions(byte[] bArr, long j, boolean z, int i, int i2, boolean z2) {
        if (j < 14 || j > bArr.length - 2) {
            return;
        }
        long jInteger = integer(bArr, (int) j, 2, z);
        long j2 = j + 2;
        while (true) {
            long j3 = jInteger - 1;
            if (jInteger > 0 && j2 <= bArr.length - 12) {
                int i3 = (int) j2;
                int iInteger = (int) integer(bArr, i3, 2, z);
                int iInteger2 = (int) integer(bArr, i3 + 2, 2, z);
                if (integer(bArr, i3 + 4, 4, z) == 1) {
                    if (z2 && iInteger == 34665 && iInteger2 == 4) {
                        updateDimensions(bArr, integer(bArr, i3 + 8, 4, z) + 6, z, i, i2, false);
                    } else if ((iInteger == 256 || iInteger == 257 || iInteger == 40962 || iInteger == 40963) && (iInteger2 == 3 || iInteger2 == 4)) {
                        int i4 = (iInteger == 256 || iInteger == 40962) ? i : i2;
                        int i5 = iInteger2 == 3 ? 2 : 4;
                        for (int i6 = 0; i6 < i5; i6++) {
                            bArr[i3 + 8 + (z ? i6 : (i5 - i6) - 1)] = (byte) (i4 >>> (i6 * 8));
                        }
                    }
                }
                j2 += 12;
                jInteger = j3;
            } else {
                return;
            }
        }
    }

    private static void markIccColorSpace(byte[] bArr, long j, boolean z) {
        long jInteger = integer(bArr, (int) j, 2, z);
        long j2 = j + 2;
        while (true) {
            long j3 = jInteger - 1;
            if (jInteger > 0 && j2 <= bArr.length - 12) {
                int i = (int) j2;
                if (integer(bArr, i, 2, z) == 34665 && integer(bArr, i + 2, 2, z) == 4 && integer(bArr, i + 4, 4, z) == 1) {
                    long jInteger2 = integer(bArr, i + 8, 4, z) + 6;
                    if (jInteger2 >= 14 && jInteger2 <= bArr.length - 2) {
                        long jInteger3 = integer(bArr, (int) jInteger2, 2, z);
                        long j4 = jInteger2 + 2;
                        while (true) {
                            long j5 = jInteger3 - 1;
                            if (jInteger3 > 0 && j4 <= bArr.length - 12) {
                                int i2 = (int) j4;
                                if (integer(bArr, i2, 2, z) == 40961 && integer(bArr, i2 + 2, 2, z) == 3 && integer(bArr, i2 + 4, 4, z) == 1) {
                                    bArr[i2 + 8] = -1;
                                    bArr[i2 + 9] = -1;
                                }
                                j4 += 12;
                                jInteger3 = j5;
                            } else {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    j2 += 12;
                    jInteger = j3;
                }
            } else {
                return;
            }
        }
    }

    private static long integer(byte[] bArr, int i, int i2, boolean z) {
        long j = 0;
        for (int i3 = 0; i3 < i2; i3++) {
            j = (j << 8) | ((long) (bArr[(z ? (i2 - i3) - 1 : i3) + i] & 255));
        }
        return j;
    }
}
