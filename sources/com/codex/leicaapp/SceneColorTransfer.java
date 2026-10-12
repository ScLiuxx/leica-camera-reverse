package com.codex.leicaapp;

import android.graphics.Bitmap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
final class SceneColorTransfer {
    static float[][] model;
    static int[] modes;
    static float[] region;
    static final float[] QUANTILES = {0.05f, 0.25f, 0.5f, 0.75f, 0.95f};
    static final float[] CENTERS = {0.0f, 32.0f, 60.0f, 115.0f, 180.0f, 225.0f, 300.0f};

    SceneColorTransfer() {
    }

    static synchronized void load(InputStream inputStream) throws IOException {
        if (model != null) {
            inputStream.close();
            return;
        }
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        try {
            if (dataInputStream.readInt() != 1279480881) {
                throw new IOException("Invalid transfer model");
            }
            int i = dataInputStream.readInt();
            if (i < 1 || i > 100) {
                throw new IOException("Invalid count");
            }
            float[][] fArr = (float[][]) Array.newInstance((Class<?>) Float.TYPE, i, 47);
            int[] iArr = new int[i];
            for (int i2 = 0; i2 < i; i2++) {
                iArr[i2] = dataInputStream.readInt();
                for (int i3 = 0; i3 < 47; i3++) {
                    fArr[i2][i3] = dataInputStream.readFloat();
                    if (!Float.isFinite(fArr[i2][i3])) {
                        throw new IOException("Invalid feature");
                    }
                }
            }
            if (dataInputStream.available() > 0) {
                if (dataInputStream.readInt() != 1279480369) {
                    throw new IOException("Invalid region model");
                }
                region = new float[23];
                for (int i4 = 0; i4 < 23; i4++) {
                    region[i4] = dataInputStream.readFloat();
                    if (!Float.isFinite(region[i4])) {
                        throw new IOException("Invalid region parameter");
                    }
                }
            }
            modes = iArr;
            model = fArr;
            dataInputStream.close();
        } catch (Throwable th) {
            try {
                dataInputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    static float y(int i) {
        return (((((i >>> 16) & 255) * 0.2126f) + (((i >>> 8) & 255) * 0.7152f)) + ((i & 255) * 0.0722f)) / 255.0f;
    }

    static float smooth(float f, float f2, float f3) {
        float fMax = Math.max(0.0f, Math.min(1.0f, (f3 - f) / (f2 - f)));
        return fMax * fMax * (3.0f - (fMax * 2.0f));
    }

    static float[] hsv(int i) {
        float f;
        float f2 = ((i >>> 16) & 255) / 255.0f;
        float f3 = ((i >>> 8) & 255) / 255.0f;
        float f4 = (i & 255) / 255.0f;
        float fMax = Math.max(f2, Math.max(f3, f4));
        float fMin = fMax - Math.min(f2, Math.min(f3, f4));
        if (fMin < 1.0E-5f) {
            f = 0.0f;
        } else if (fMax == f2) {
            f = ((f3 - f4) / fMin) * 60.0f;
        } else {
            f = fMax == f3 ? (((f4 - f2) / fMin) + 2.0f) * 60.0f : (((f2 - f3) / fMin) + 4.0f) * 60.0f;
        }
        return new float[]{(f + 360.0f) % 360.0f, fMin / Math.max(1.0E-5f, fMax), fMax};
    }

    static float quantile(float[] fArr, float f) {
        float length = f * (fArr.length - 1);
        int i = (int) length;
        return fArr[i] + ((fArr[Math.min(fArr.length - 1, i + 1)] - fArr[i]) * (length - i));
    }

    static float median(float[] fArr, int i) {
        if (i == 0) {
            return 0.0f;
        }
        float[] fArrCopyOf = Arrays.copyOf(fArr, i);
        Arrays.sort(fArrCopyOf);
        return quantile(fArrCopyOf, 0.5f);
    }

    static float[] features(int[] iArr) {
        int i;
        if (iArr.length == 0) {
            throw new IllegalArgumentException("Empty samples");
        }
        float[] fArr = new float[iArr.length];
        float[] fArr2 = new float[iArr.length];
        float[] fArr3 = new float[iArr.length];
        float[] fArr4 = new float[11];
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (i2 >= iArr.length) {
                break;
            }
            int i4 = iArr[i2];
            float fY = y(i4);
            fArr[i2] = fY;
            float[] fArrHsv = hsv(i4);
            float f = fArrHsv[1];
            if (f < 0.25f) {
                fArr4[9] = fArr4[9] + 1.0f;
            }
            if (f < 0.25f && fY > 0.08f && fY < 0.85f) {
                fArr2[i3] = (((i4 >>> 16) & 255) / 255.0f) - fY;
                fArr3[i3] = ((i4 & 255) / 255.0f) - fY;
                i3++;
            }
            float f2 = fArrHsv[0];
            if (f2 > 10.0f && f2 < 75.0f && fArrHsv[1] > 0.2f) {
                fArr4[7] = fArr4[7] + 1.0f;
            }
            if (f2 > 180.0f && f2 < 265.0f && fArrHsv[1] > 0.2f) {
                fArr4[8] = fArr4[8] + 1.0f;
            }
            if (fY < 0.1254902f) {
                fArr4[10] = fArr4[10] + 1.0f;
            }
            i2++;
        }
        Arrays.sort(fArr);
        for (int i5 = 0; i5 < 5; i5++) {
            fArr4[i5] = quantile(fArr, QUANTILES[i5]);
        }
        if (i3 > 20) {
            fArr4[5] = median(fArr2, i3);
            fArr4[6] = median(fArr3, i3);
        }
        for (i = 7; i < 11; i++) {
            fArr4[i] = fArr4[i] / iArr.length;
        }
        return fArr4;
    }

    static final class Plan {
        final float confidence;
        final float[] curve = new float[256];
        final float highlightEnd;
        final float highlightStart;
        final int nearest;
        final float[] record;
        final float regionStrength;
        final int styleMode;
        final float whiteEvidence;

        Plan(int[] iArr, int[] iArr2, int i) {
            float f;
            float f2;
            this.styleMode = i;
            float[] fArrFeatures = SceneColorTransfer.features(iArr);
            float[] fArrFeatures2 = SceneColorTransfer.features(iArr2);
            if (SceneColorTransfer.region != null) {
                int i2 = 0;
                f = 0.0f;
                while (i2 < 11) {
                    float f3 = (fArrFeatures[i2] - SceneColorTransfer.region[i2]) / (i2 < 5 ? 0.15f : i2 < 7 ? 0.04f : 0.2f);
                    f += f3 * f3;
                    i2++;
                }
            } else {
                f = 0.0f;
            }
            this.regionStrength = (SceneColorTransfer.region == null || i != 0) ? 0.0f : (float) Math.exp((f * (-8.0f)) / 11.0f);
            this.whiteEvidence = SceneColorTransfer.smooth(0.96f, 0.99f, fArrFeatures[4]);
            float f4 = fArrFeatures[3];
            this.highlightStart = f4;
            this.highlightEnd = Math.max(fArrFeatures[4], f4 + 0.05f);
            float f5 = Float.MAX_VALUE;
            float f6 = Float.MAX_VALUE;
            int i3 = 0;
            int i4 = 0;
            for (int i5 = 0; i5 < SceneColorTransfer.model.length; i5++) {
                if (i != 4 || SceneColorTransfer.modes[i5] == 4) {
                    if ((i == 0) == (SceneColorTransfer.modes[i5] == 0)) {
                        int i6 = 0;
                        float f7 = 0.0f;
                        while (i6 < 11) {
                            float f8 = (fArrFeatures[i6] - SceneColorTransfer.model[i5][i6]) / (i6 < 5 ? 0.2f : i6 < 7 ? 0.04f : 0.25f);
                            f7 += f8 * f8;
                            i6++;
                        }
                        float f9 = f7 / 11.0f;
                        if (i != SceneColorTransfer.modes[i5] && i != 4) {
                            f9 += 0.4f;
                        }
                        if (f9 < f5) {
                            i4 = i3;
                            f6 = f5;
                            f5 = f9;
                            i3 = i5;
                        } else if (f9 < f6) {
                            f6 = f9;
                            i4 = i5;
                        }
                    }
                }
            }
            this.nearest = i3;
            this.confidence = f5 == Float.MAX_VALUE ? 0.0f : (float) Math.exp((-3.0f) * f5);
            float f10 = f5 + 0.04f;
            float f11 = 1.0f / (f10 * f10);
            if (f6 == Float.MAX_VALUE) {
                f2 = 0.0f;
            } else {
                float f12 = f6 + 0.04f;
                f2 = 1.0f / (f12 * f12);
            }
            this.record = new float[47];
            int i7 = 0;
            for (int i8 = 47; i7 < i8; i8 = 47) {
                this.record[i7] = ((SceneColorTransfer.model[i3][i7] * f11) + (SceneColorTransfer.model[i4][i7] * f2)) / Math.max(1.0E-12f, f11 + f2);
                i7++;
            }
            float[] fArr = new float[7];
            float[] fArr2 = new float[7];
            fArr2[6] = 1.0f;
            fArr[6] = 1.0f;
            if (i == 0) {
                for (int i9 = 11; i9 < 16; i9++) {
                    float[] fArr3 = new float[SceneColorTransfer.model.length];
                    int i10 = 0;
                    for (int i11 = 0; i11 < SceneColorTransfer.model.length; i11++) {
                        if (SceneColorTransfer.modes[i11] == i) {
                            fArr3[i10] = SceneColorTransfer.model[i11][i9];
                            i10++;
                        }
                    }
                    float[] fArr4 = this.record;
                    fArr4[i9] = (fArr4[i9] * 0.5f) + (SceneColorTransfer.median(fArr3, i10) * 0.5f);
                }
            }
            float f13 = this.confidence;
            int i12 = 0;
            while (i12 < 5) {
                int i13 = i12 + 1;
                fArr[i13] = fArrFeatures2[i12];
                float f14 = fArrFeatures2[i12];
                float fMin = Math.min(1.0f, fArrFeatures[i12] * this.record[i12 + 11]);
                float f15 = fArrFeatures2[i12];
                fArr2[i13] = f14 + ((fMin - f15) * f13) + ((1.0f - this.confidence) * 0.65f * (fArrFeatures[i12] - f15));
                i12 = i13;
            }
            for (int i14 = 1; i14 < 7; i14++) {
                int i15 = i14 - 1;
                fArr[i14] = Math.max(fArr[i14], fArr[i15]);
                fArr2[i14] = Math.max(fArr2[i14], fArr2[i15]);
            }
            for (int i16 = 0; i16 < 256; i16++) {
                float f16 = i16 / 255.0f;
                int i17 = 0;
                while (i17 < 5) {
                    int i18 = i17 + 1;
                    if (f16 <= fArr[i18]) {
                        break;
                    } else {
                        i17 = i18;
                    }
                }
                float f17 = fArr[i17];
                int i19 = i17 + 1;
                float fMax = (f16 - f17) / Math.max(1.0E-5f, fArr[i19] - f17);
                float[] fArr5 = this.curve;
                float f18 = fArr2[i17];
                fArr5[i16] = Math.max(0.0f, Math.min(1.0f, f18 + (fMax * (fArr2[i19] - f18))));
            }
            float[] fArr6 = this.curve;
            fArr6[0] = 0.0f;
            fArr6[255] = 1.0f;
        }

        int migrate(int i, int i2) {
            float f;
            float f2;
            float f3;
            if (this.regionStrength < 0.02f || SceneColorTransfer.y(i) < 0.08f || SceneColorTransfer.y(i) > 0.98f) {
                return i2;
            }
            float[] fArrHsv = SceneColorTransfer.hsv(i);
            float[] fArrHsv2 = SceneColorTransfer.hsv(i2);
            float fY = SceneColorTransfer.y(i2);
            float f4 = fArrHsv2[0];
            float f5 = fArrHsv2[1];
            float fSmooth = SceneColorTransfer.smooth(180.0f, 200.0f, fArrHsv[0]) * (1.0f - SceneColorTransfer.smooth(245.0f, 265.0f, fArrHsv[0])) * SceneColorTransfer.smooth(0.35f, 0.55f, SceneColorTransfer.y(i)) * SceneColorTransfer.smooth(0.03f, 0.12f, fArrHsv[1]);
            float f6 = fArrHsv[0];
            float fSmooth2 = (1.0f - SceneColorTransfer.smooth(15.0f, 30.0f, Math.min(f6, 360.0f - f6))) * SceneColorTransfer.smooth(0.3f, 0.5f, fArrHsv[1]) * (1.0f - SceneColorTransfer.smooth(0.65f, 0.8f, SceneColorTransfer.y(i)));
            float fSmooth3 = SceneColorTransfer.smooth(20.0f, 35.0f, fArrHsv[0]) * (1.0f - SceneColorTransfer.smooth(75.0f, 90.0f, fArrHsv[0])) * (1.0f - SceneColorTransfer.smooth(0.25f, 0.4f, fArrHsv[1])) * SceneColorTransfer.smooth(0.25f, 0.45f, SceneColorTransfer.y(i)) * (1.0f - SceneColorTransfer.smooth(0.85f, 0.98f, SceneColorTransfer.y(i)));
            float fSmooth4 = SceneColorTransfer.smooth(70.0f, 90.0f, fArrHsv[0]) * (1.0f - SceneColorTransfer.smooth(170.0f, 190.0f, fArrHsv[0])) * SceneColorTransfer.smooth(0.1f, 0.2f, fArrHsv[1]) * (1.0f - SceneColorTransfer.smooth(0.4f, 0.55f, SceneColorTransfer.y(i)));
            float[] fArr = {fSmooth, fSmooth2, fSmooth3, fSmooth4};
            float fMax = this.regionStrength / Math.max(1.0f, ((fSmooth + fSmooth2) + fSmooth3) + fSmooth4);
            for (int i3 = 0; i3 < 4; i3++) {
                float f7 = fArr[i3] * fMax;
                int i4 = i3 * 3;
                f4 += SceneColorTransfer.region[i4 + 11] * f7;
                f5 *= ((SceneColorTransfer.region[i4 + 12] - 1.0f) * f7) + 1.0f;
                fY *= (f7 * (SceneColorTransfer.region[i4 + 13] - 1.0f)) + 1.0f;
            }
            float fMax2 = Math.max(0.0f, Math.min(1.0f, f5));
            float fMax3 = Math.max(0.0f, Math.min(1.0f, fY));
            float f8 = ((f4 + 360.0f) % 360.0f) / 60.0f;
            float fAbs = (1.0f - Math.abs((f8 % 2.0f) - 1.0f)) * fMax2;
            switch ((int) f8) {
                case 0:
                    f = 0.0f;
                    f2 = fAbs;
                    fAbs = fMax2;
                    break;
                case 1:
                    f2 = fMax2;
                    f = 0.0f;
                    break;
                case 2:
                    f2 = fMax2;
                    f = fAbs;
                    fAbs = 0.0f;
                    break;
                case 3:
                    f = fMax2;
                    f2 = fAbs;
                    fAbs = 0.0f;
                    break;
                case 4:
                    f = fMax2;
                    f2 = 0.0f;
                    break;
                default:
                    f2 = 0.0f;
                    f = fAbs;
                    fAbs = fMax2;
                    break;
            }
            float f9 = 1.0f - fMax2;
            float f10 = fAbs + f9;
            float f11 = f2 + f9;
            float f12 = f + f9;
            float fMax4 = fMax3 / Math.max(1.0E-5f, ((0.2126f * f10) + (0.7152f * f11)) + (0.0722f * f12));
            float f13 = (f10 * fMax4) - fMax3;
            float f14 = (f11 * fMax4) - fMax3;
            float f15 = (f12 * fMax4) - fMax3;
            float[] fArr2 = {f13, f14, f15};
            float fMin = 1.0f;
            for (int i5 = 0; i5 < 3; i5++) {
                float f16 = fArr2[i5];
                if (f16 > 0.0f) {
                    f3 = 1.0f - fMax3;
                } else {
                    if (f16 < 0.0f) {
                        f3 = -fMax3;
                    }
                }
                fMin = Math.min(fMin, f3 / f16);
            }
            return (i & (-16777216)) | (Math.round(Math.max(0.0f, Math.min(1.0f, (f13 * fMin) + fMax3)) * 255.0f) << 16) | (Math.round(Math.max(0.0f, Math.min(1.0f, (f14 * fMin) + fMax3)) * 255.0f) << 8) | Math.round(Math.max(0.0f, Math.min(1.0f, fMax3 + (f15 * fMin))) * 255.0f);
        }

        float parameter(float f, int i) {
            int i2;
            float f2;
            int i3;
            float f3;
            int i4 = 0;
            int i5 = 0;
            while (true) {
                i2 = 6;
                if (i5 >= 6) {
                    f2 = -60.0f;
                    i3 = 0;
                    i5 = 6;
                    f3 = 0.0f;
                    break;
                }
                if (f >= SceneColorTransfer.CENTERS[i5]) {
                    i3 = i5 + 1;
                    if (f < SceneColorTransfer.CENTERS[i3]) {
                        f2 = SceneColorTransfer.CENTERS[i5];
                        f3 = SceneColorTransfer.CENTERS[i3];
                        break;
                    }
                }
                i5++;
            }
            if (f >= SceneColorTransfer.CENTERS[6]) {
                f2 = SceneColorTransfer.CENTERS[6];
                f3 = 360.0f;
            } else {
                i2 = i5;
                i4 = i3;
            }
            float f4 = (f - f2) / (f3 - f2);
            float[] fArr = this.record;
            int i6 = i2 * 4;
            float f5 = fArr[i6 + 16 + i];
            int i7 = i4 * 4;
            float f6 = fArr[i7 + 16 + i];
            float f7 = 1.0f - f4;
            float f8 = (fArr[i6 + 19] * f7) + (fArr[i7 + 19] * f4);
            float f9 = i != 0 ? 1.0f : 0.0f;
            return f9 + (this.confidence * 0.6f * f8 * (((f5 * f7) + (f6 * f4)) - f9));
        }

        int apply(int i, int i2) {
            float f;
            float f2;
            float[] fArrHsv = SceneColorTransfer.hsv(i2);
            float fY = SceneColorTransfer.y(i2) * 255.0f;
            int iMin = Math.min(254, (int) fY);
            float[] fArr = this.curve;
            float f3 = fArr[iMin];
            float f4 = f3 + ((fArr[iMin + 1] - f3) * (fY - iMin));
            int i3 = this.styleMode;
            float fSmooth = f4 + (((i3 == 2 || i3 == 3) ? 0.0f : this.whiteEvidence) * SceneColorTransfer.smooth(0.6f, 0.85f, SceneColorTransfer.y(i)) * (1.0f - SceneColorTransfer.smooth(0.08f, 0.22f, SceneColorTransfer.hsv(i)[1])) * (Math.max(f4, SceneColorTransfer.y(i)) - f4));
            float f5 = fArrHsv[0];
            float fParameter = ((f5 + (parameter(f5, 0) * SceneColorTransfer.smooth(0.08f, 0.25f, fArrHsv[1]))) + 360.0f) % 360.0f;
            float fMin = Math.min(1.0f, fArrHsv[1] * parameter(fArrHsv[0], 1));
            float fMax = Math.max(0.0f, Math.min(1.0f, fSmooth * (((parameter(fArrHsv[0], 2) - 1.0f) * SceneColorTransfer.smooth(0.08f, 0.3f, fArrHsv[1])) + 1.0f)));
            int i4 = this.styleMode;
            float fSmooth2 = (i4 == 2 || i4 == 3) ? SceneColorTransfer.smooth(this.highlightStart, this.highlightEnd, SceneColorTransfer.y(i)) * (1.0f - SceneColorTransfer.smooth(0.35f, 0.65f, SceneColorTransfer.hsv(i)[1])) : 0.0f;
            float fMax2 = fMax - (Math.max(0.0f, fMax - SceneColorTransfer.y(i)) * fSmooth2);
            float f6 = fParameter / 60.0f;
            float fAbs = (1.0f - Math.abs((f6 % 2.0f) - 1.0f)) * fMin;
            switch ((int) f6) {
                case 0:
                    f = 0.0f;
                    f2 = fAbs;
                    fAbs = fMin;
                    break;
                case 1:
                    f = 0.0f;
                    f2 = fMin;
                    break;
                case 2:
                    f = fAbs;
                    f2 = fMin;
                    fAbs = 0.0f;
                    break;
                case 3:
                    f2 = fAbs;
                    f = fMin;
                    fAbs = 0.0f;
                    break;
                case 4:
                    f2 = 0.0f;
                    f = fMin;
                    break;
                default:
                    f2 = 0.0f;
                    f = fAbs;
                    fAbs = fMin;
                    break;
            }
            float f7 = 1.0f - fMin;
            float f8 = fAbs + f7;
            float f9 = f2 + f7;
            float f10 = f + f7;
            float fMax3 = fMax2 / Math.max(1.0E-5f, ((f8 * 0.2126f) + (f9 * 0.7152f)) + (f10 * 0.0722f));
            float f11 = (f8 * fMax3) - fMax2;
            float f12 = (f9 * fMax3) - fMax2;
            float f13 = (f10 * fMax3) - fMax2;
            float fSmooth3 = (1.0f - SceneColorTransfer.smooth(0.1f, 0.3f, SceneColorTransfer.hsv(i)[1])) * (1.0f - SceneColorTransfer.smooth(this.highlightStart, this.highlightEnd, SceneColorTransfer.y(i))) * this.confidence * this.record[46];
            if (this.styleMode == 3 && SceneColorTransfer.modes[this.nearest] == 3) {
                fSmooth3 = SceneColorTransfer.smooth(0.02f, 0.1f, (((i >>> 16) & 255) - (i & 255)) / 255.0f) * (1.0f - SceneColorTransfer.smooth(0.15f, 0.45f, SceneColorTransfer.hsv(i)[1])) * SceneColorTransfer.smooth(0.15f, 0.35f, SceneColorTransfer.y(i)) * (1.0f - SceneColorTransfer.smooth(0.95f, 1.0f, SceneColorTransfer.y(i))) * this.confidence * this.record[46];
            }
            float[] fArr2 = this.record;
            float f14 = fArr2[44] * fMax2;
            float f15 = fArr2[45] * fMax2;
            float f16 = f11 + ((f14 - f11) * fSmooth3);
            float f17 = f12 + ((((-((f14 * 0.2126f) + (f15 * 0.0722f))) / 0.7152f) - f12) * fSmooth3);
            float f18 = f13 + (fSmooth3 * (f15 - f13));
            float fSmooth4 = (1.0f - SceneColorTransfer.smooth(0.1f, 0.3f, SceneColorTransfer.hsv(i)[1])) * 0.65f;
            float f19 = this.confidence;
            float f20 = fSmooth4 * (1.0f - f19);
            if (this.styleMode == 4 && f19 < 0.05f) {
                f20 = 0.8f;
            }
            float fMax4 = Math.max(1.0E-5f, SceneColorTransfer.y(i));
            int i5 = (i >>> 16) & 255;
            float f21 = (((i5 / 255.0f) * fMax2) / fMax4) - fMax2;
            int i6 = i & 255;
            float f22 = (((i6 / 255.0f) * fMax2) / fMax4) - fMax2;
            float f23 = f16 + ((f21 - f16) * f20);
            float f24 = f17 + ((((-((0.2126f * f21) + (0.0722f * f22))) / 0.7152f) - f17) * f20);
            float f25 = f18 + (f20 * (f22 - f18));
            float fMax5 = Math.max(fMax2, 1.0f - (fSmooth2 * (1.0f - (Math.max(i5, Math.max((i >>> 8) & 255, i6)) / 255.0f))));
            float[] fArr3 = {f23, f24, f25};
            float fMin2 = 1.0f;
            for (int i7 = 0; i7 < 3; i7++) {
                float f26 = fArr3[i7];
                if (f26 > 0.0f) {
                    fMin2 = Math.min(fMin2, (fMax5 - fMax2) / f26);
                } else if (f26 < 0.0f) {
                    fMin2 = Math.min(fMin2, (-fMax2) / f26);
                }
            }
            return migrate(i, Math.round(Math.max(0.0f, Math.min(1.0f, fMax2 + (f25 * fMin2))) * 255.0f) | ((-16777216) & i) | (Math.round(Math.max(0.0f, Math.min(1.0f, (f23 * fMin2) + fMax2)) * 255.0f) << 16) | (Math.round(Math.max(0.0f, Math.min(1.0f, (f24 * fMin2) + fMax2)) * 255.0f) << 8));
        }
    }

    static Plan analyse(Bitmap bitmap, int i) {
        int[] iArr = new int[4096];
        int[] iArr2 = new int[4096];
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (i2 >= 64) {
                break;
            }
            int i4 = 0;
            while (i4 < 64) {
                iArr[i3] = bitmap.getPixel(Math.min(bitmap.getWidth() - 1, (((i4 * 2) + 1) * bitmap.getWidth()) / 128), Math.min(bitmap.getHeight() - 1, (((i2 * 2) + 1) * bitmap.getHeight()) / 128));
                i4++;
                i3++;
            }
            i2++;
        }
        float[] fArrFeatures = features(iArr);
        float f = Float.MAX_VALUE;
        int i5 = i;
        for (int i6 = 0; i6 < model.length; i6++) {
            float f2 = 0.0f;
            int i7 = 0;
            while (i7 < 11) {
                float f3 = (fArrFeatures[i7] - model[i6][i7]) / (i7 < 5 ? 0.2f : i7 < 7 ? 0.04f : 0.25f);
                f2 += f3 * f3;
                i7++;
            }
            float f4 = f2 / 11.0f;
            int i8 = modes[i6];
            if (i8 != i) {
                f4 += 0.08f;
            }
            if (f4 < f) {
                f = f4;
                i5 = i8;
            }
        }
        if ((fArrFeatures[2] < 0.25f && fArrFeatures[3] < 0.62f && fArrFeatures[10] > 0.33f) && i != 2 && i != 3) {
            i5 = 4;
        }
        if (i != 2 && i != 3) {
            i = i5;
        }
        for (int i9 = 0; i9 < i3; i9++) {
            iArr2[i9] = M9ReferenceTone.pixel(iArr[i9], i);
        }
        return new Plan(iArr, iArr2, i);
    }
}
