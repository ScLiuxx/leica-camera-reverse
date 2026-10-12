package com.codex.fullm9;

import ai.onnxruntime.TensorInfo;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class DngMeta {
    final ByteBuffer b;
    String camera;
    double[] cameraToSrgb;
    double cct;
    double lux;
    float[] wb;
    double zoom;
    final Map<Integer, double[]> tags = new HashMap();
    final Set<Integer> seen = new HashSet();

    DngMeta(byte[] bArr) throws IOException {
        double d;
        int i;
        int i2;
        double d2;
        double dMax;
        double d3;
        int i3;
        this.cct = 5000.0d;
        this.lux = 300.0d;
        this.zoom = 1.0d;
        this.camera = "main";
        if (bArr.length < 8) {
            throw new IOException("Truncated DNG");
        }
        int i4 = 0;
        boolean z = bArr[0] == 73 && bArr[1] == 73;
        boolean z2 = bArr[0] == 77 && bArr[1] == 77;
        if (!z && !z2) {
            throw new IOException("Only TIFF/DNG RAW metadata is supported");
        }
        this.b = ByteBuffer.wrap(bArr).order(z ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        if ((this.b.getShort(2) & 65535) == 42) {
            visit(this.b.getInt(4), 0);
            double[] dArr = this.tags.get(50728);
            double[] dArr2 = this.tags.get(50721);
            double[] dArr3 = this.tags.get(50722);
            if (dArr2 == null || dArr2.length != 9 || (dArr3 != null && dArr3.length != 9)) {
                throw new IOException("DNG requires 3-channel ColorMatrix");
            }
            if (dArr == null) {
                float[] fArrRawNeutral = NativeM9.rawNeutral(bArr);
                dArr = new double[]{fArrRawNeutral[0], fArrRawNeutral[1], fArrRawNeutral[2]};
            }
            if (dArr.length == 3) {
                double dIlluminant = illuminant((int) value(50778, 21.0d));
                double dIlluminant2 = illuminant((int) value(50779, 17.0d));
                if (dArr3 != null) {
                    int i5 = 0;
                    int i6 = 9;
                    double d4 = 5000.0d;
                    while (true) {
                        d2 = 1.0E-9d;
                        if (i5 >= 30) {
                            d3 = dIlluminant;
                            i3 = i4;
                            i2 = i6;
                        } else {
                            double d5 = dIlluminant;
                            i2 = i6;
                            d3 = d5;
                            double[] dArrVector = vector(Tuning.inverse(blend(dArr2, dArr3, d4, d5, dIlluminant2)), dArr);
                            double d6 = dArrVector[i4] + dArrVector[1] + dArrVector[2];
                            if (Math.abs(d6) < 1.0E-12d) {
                                throw new IOException("Invalid DNG white point");
                            }
                            double d7 = dArrVector[i4] / d6;
                            double d8 = 0.1858d - (dArrVector[1] / d6);
                            if (Math.abs(d8) < 1.0E-9d) {
                                i3 = i4;
                            } else {
                                double d9 = (d7 - 0.332d) / d8;
                                i3 = i4;
                                double[] dArr4 = dArr;
                                double d10 = (449.0d * d9 * d9 * d9) + (3525.0d * d9 * d9) + (d9 * 6823.3d) + 5520.33d;
                                if (Double.isFinite(d10)) {
                                    i = i3;
                                    d = d3;
                                    dMax = Math.max(1500.0d, Math.min(20000.0d, d10));
                                    if (Math.abs(dMax - d4) < 1.0d) {
                                        break;
                                    }
                                    i5++;
                                    i4 = i;
                                    i6 = i2;
                                    d4 = dMax;
                                    dIlluminant = d;
                                    dArr = dArr4;
                                }
                            }
                        }
                        i = i3;
                        d = d3;
                        dMax = d4;
                        break;
                    }
                } else {
                    d = dIlluminant;
                    i = 0;
                    i2 = 9;
                    d2 = 1.0E-9d;
                    dMax = 5000.0d;
                }
                this.cct = dMax;
                double[] dArrBlend = blend(dArr2, dArr3, 5003.0d, d, dIlluminant2);
                double[] dArrVector2 = vector(dArrBlend, new double[]{0.96422d, 1.0d, 0.82521d});
                double d11 = dArrVector2[1];
                for (int i7 = i; i7 < 3; i7++) {
                    dArrVector2[i7] = dArrVector2[i7] / d11;
                }
                float f = (float) (1.0d / dArrVector2[i]);
                float f2 = (float) (1.0d / dArrVector2[1]);
                float f3 = (float) (1.0d / dArrVector2[2]);
                float f4 = (float) (1.0d / dArrVector2[1]);
                float[] fArr = new float[4];
                fArr[i] = f;
                fArr[1] = f2;
                fArr[2] = f3;
                fArr[3] = f4;
                this.wb = fArr;
                float[] fArr2 = this.wb;
                int length = fArr2.length;
                for (int i8 = i; i8 < length; i8++) {
                    float f5 = fArr2[i8];
                    if (!Float.isFinite(f5) || f5 <= 0.0f || f5 > 64.0f) {
                        throw new IOException("Invalid D50 white balance");
                    }
                }
                double[] dArr5 = new double[i2];
                // fill-array-data instruction
                dArr5[0] = 0.4124564d;
                dArr5[1] = 0.3575761d;
                dArr5[2] = 0.1804375d;
                dArr5[3] = 0.2126729d;
                dArr5[4] = 0.7151522d;
                dArr5[5] = 0.072175d;
                dArr5[6] = 0.0193339d;
                dArr5[7] = 0.119192d;
                dArr5[8] = 0.9503041d;
                double[] dArrMultiply = Tuning.multiply(dArrBlend, dArr5);
                for (int i9 = i; i9 < 3; i9++) {
                    int i10 = i9 * 3;
                    double d12 = dArrMultiply[i10] + dArrMultiply[i10 + 1] + dArrMultiply[i10 + 2];
                    if (Math.abs(d12) < d2) {
                        throw new IOException("Invalid camera color row");
                    }
                    for (int i11 = i; i11 < 3; i11++) {
                        int i12 = i10 + i11;
                        dArrMultiply[i12] = dArrMultiply[i12] / d12;
                    }
                }
                this.cameraToSrgb = Tuning.inverse(dArrMultiply);
                double dValue = value(33434, 0.0d);
                double dValue2 = value(33437, 0.0d);
                double dValue3 = value(34855, value(34867, 0.0d));
                if (dValue > 0.0d && dValue2 > 0.0d && dValue3 > 0.0d) {
                    this.lux = Math.max(0.0d, Math.min(999.0d, 474.9d - (((Math.log((dValue2 * dValue2) / dValue) / Math.log(2.0d)) - (Math.log(dValue3 / 100.0d) / Math.log(2.0d))) * 23.45d)));
                }
                double dValue4 = value(41989, 0.0d);
                if (dValue4 >= 50.0d) {
                    this.camera = "tele";
                    this.zoom = (float) Math.max(3.2d, Math.min(4.3d, dValue4 / 23.0d));
                    return;
                } else {
                    if (dValue4 > 0.0d && dValue4 < 18.0d) {
                        throw new IOException("Ultra-wide RAW front end is not implemented in the source project");
                    }
                    return;
                }
            }
            throw new IOException("DNG white-point channels");
        }
        throw new IOException("Unsupported TIFF header");
    }

    /* JADX WARN: Code duplicated, block: B:55:0x00e6  */
    void visit(int i, int i2) throws IOException {
        int i3;
        int i4;
        int i5 = i;
        int i6 = i2;
        int i7 = 12;
        if (i6 > 12 || this.seen.size() > 256) {
            throw new IOException("DNG IFD recursion limit");
        }
        if (i5 == 0 || this.seen.contains(Integer.valueOf(i5))) {
            return;
        }
        if (i5 >= 0) {
            int i8 = 2;
            if (i5 <= this.b.limit() - 2) {
                this.seen.add(Integer.valueOf(i5));
                short s = 65535;
                int i9 = this.b.getShort(i5) & 65535;
                if (i9 <= 4096) {
                    long j = 4;
                    if (((long) i5) + 2 + (((long) i9) * 12) + 4 <= this.b.limit()) {
                        ArrayList arrayList = new ArrayList();
                        int i10 = 0;
                        while (true) {
                            int i11 = 1;
                            if (i10 < i9) {
                                int i12 = i5 + 2 + (i10 * 12);
                                int i13 = this.b.getShort(i12) & s;
                                short s2 = s;
                                int i14 = this.b.getShort(i12 + 2) & s2;
                                long j2 = j;
                                long unsignedLong = Integer.toUnsignedLong(this.b.getInt(i12 + 4));
                                if (i14 != 1 && i14 != i8 && i14 != 6 && i14 != 7) {
                                    if (i14 != 3) {
                                        i11 = 8;
                                        if (i14 == 8) {
                                            i11 = 2;
                                        } else if (i14 == 4 || i14 == 9 || i14 == 11 || i14 == 13) {
                                            i11 = 4;
                                        } else if (i14 != 5 && i14 != 10 && i14 != i7) {
                                            i11 = 0;
                                        }
                                    } else {
                                        i11 = 2;
                                    }
                                }
                                if (i11 != 0) {
                                    i3 = i9;
                                    if (unsignedLong > 1000000) {
                                        i4 = i10;
                                    } else {
                                        long j3 = ((long) i11) * unsignedLong;
                                        if (j3 > this.b.limit()) {
                                            throw new IOException("DNG tag length");
                                        }
                                        long unsignedLong2 = j3 <= j2 ? i12 + 8 : Integer.toUnsignedLong(this.b.getInt(i12 + 8));
                                        i4 = i10;
                                        if (j3 + unsignedLong2 > this.b.limit()) {
                                            throw new IOException("DNG tag offset");
                                        }
                                        int i15 = (int) unsignedLong;
                                        double[] dArr = new double[i15];
                                        int i16 = (int) unsignedLong2;
                                        int i17 = 0;
                                        while (true) {
                                            if (i17 < i15) {
                                                int i18 = (i17 * i11) + i16;
                                                switch (i14) {
                                                    case 1:
                                                    case 7:
                                                        dArr[i17] = this.b.get(i18) & 255;
                                                        break;
                                                    case 2:
                                                    case 6:
                                                    default:
                                                        dArr[i17] = 0.0d;
                                                        break;
                                                    case 3:
                                                        dArr[i17] = this.b.getShort(i18) & s2;
                                                        break;
                                                    case 4:
                                                    case 13:
                                                        dArr[i17] = Integer.toUnsignedLong(this.b.getInt(i18));
                                                        break;
                                                    case 5:
                                                    case 10:
                                                        int i19 = this.b.getInt(i18);
                                                        double unsignedLong3 = i14 == 5 ? Integer.toUnsignedLong(i19) : i19;
                                                        double unsignedLong4 = i14 == 5 ? Integer.toUnsignedLong(this.b.getInt(i18 + 4)) : this.b.getInt(i18 + 4);
                                                        dArr[i17] = unsignedLong4 != 0.0d ? unsignedLong3 / unsignedLong4 : 0.0d;
                                                        break;
                                                    case TensorInfo.MAX_DIMENSIONS /* 8 */:
                                                        dArr[i17] = this.b.getShort(i18);
                                                        break;
                                                    case 9:
                                                        dArr[i17] = this.b.getInt(i18);
                                                        break;
                                                    case 11:
                                                        dArr[i17] = this.b.getFloat(i18);
                                                        break;
                                                    case 12:
                                                        dArr[i17] = this.b.getDouble(i18);
                                                        break;
                                                }
                                                i17++;
                                            } else if (i13 == 330 || i13 == 34665) {
                                                for (int i20 = 0; i20 < i15; i20++) {
                                                    double d = dArr[i20];
                                                    if (d > 0.0d && d <= 2.147483647E9d) {
                                                        arrayList.add(Integer.valueOf((int) d));
                                                    }
                                                }
                                            } else if (!this.tags.containsKey(Integer.valueOf(i13))) {
                                                this.tags.put(Integer.valueOf(i13), dArr);
                                            }
                                        }
                                    }
                                } else {
                                    i3 = i9;
                                    i4 = i10;
                                }
                                i10 = i4 + 1;
                                i5 = i;
                                i6 = i2;
                                i9 = i3;
                                s = s2;
                                j = j2;
                                i7 = 12;
                                i8 = 2;
                            } else {
                                Iterator it = arrayList.iterator();
                                while (it.hasNext()) {
                                    visit(((Integer) it.next()).intValue(), i6 + 1);
                                }
                                visit(this.b.getInt(i5 + i8 + (i9 * i7)), i6 + 1);
                                return;
                            }
                        }
                    }
                }
                throw new IOException("DNG IFD entries");
            }
        }
        throw new IOException("DNG IFD offset");
    }

    double value(int i, double d) {
        double[] dArr = this.tags.get(Integer.valueOf(i));
        return (dArr == null || dArr.length == 0) ? d : dArr[0];
    }

    static double[] vector(double[] dArr, double[] dArr2) {
        double[] dArr3 = new double[3];
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                dArr3[i] = dArr3[i] + (dArr[(i * 3) + i2] * dArr2[i2]);
            }
        }
        return dArr3;
    }

    static double[] blend(double[] dArr, double[] dArr2, double d, double d2, double d3) {
        double dMax;
        if (dArr2 == null) {
            return (double[]) dArr.clone();
        }
        if (d2 == d3) {
            dMax = 1.0d;
        } else {
            double d4 = 1.0d / d3;
            dMax = Math.max(0.0d, Math.min(1.0d, ((1.0d / d) - d4) / ((1.0d / d2) - d4)));
        }
        double[] dArr3 = new double[9];
        for (int i = 0; i < 9; i++) {
            dArr3[i] = (dArr[i] * dMax) + ((1.0d - dMax) * dArr2[i]);
        }
        return dArr3;
    }

    static double illuminant(int i) {
        switch (i) {
            case 1:
                return 5500.0d;
            case 2:
                return 4200.0d;
            case 3:
            case 17:
                return 2856.0d;
            case 4:
            case 5:
            case 6:
            case 7:
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
            case 9:
            case 16:
            case 21:
            default:
                return 6504.0d;
            case 10:
                return 6500.0d;
            case 11:
                return 7500.0d;
            case 12:
                return 6400.0d;
            case 13:
                return 5000.0d;
            case 14:
                return 4150.0d;
            case 15:
                return 3450.0d;
            case 18:
                return 4874.0d;
            case 19:
                return 6774.0d;
            case 20:
                return 5503.0d;
            case 22:
                return 7504.0d;
            case 23:
                return 5003.0d;
            case 24:
                return 3200.0d;
        }
    }
}
