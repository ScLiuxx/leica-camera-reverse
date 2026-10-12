package com.codex.leicaapp;

import java.lang.reflect.Array;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class AiCropCandidates {
    public static float[] choose(float[] fArr, float[][] fArr2) {
        int i;
        char c;
        int i2;
        double[][] dArr;
        float f;
        double d;
        int i3;
        int length = fArr2.length;
        int i4 = 0;
        int length2 = fArr2[0].length;
        double d2 = 0.0d;
        double d3 = 0.0d;
        for (float[] fArr3 : fArr2) {
            for (float f2 : fArr3) {
                if (!Float.isFinite(f2)) {
                    return fArr;
                }
                d2 += (double) f2;
                d3 += (double) (f2 * f2);
            }
        }
        double d4 = length2 * length;
        double d5 = d2 / d4;
        if ((d3 / d4) - (d5 * d5) < 4.0E-4d) {
            return fArr;
        }
        double[][] dArr2 = (double[][]) Array.newInstance((Class<?>) Double.TYPE, length + 1, length2 + 1);
        for (int i5 = 0; i5 < length; i5++) {
            int i6 = 0;
            while (i6 < length2) {
                int i7 = i5 + 1;
                double[] dArr3 = dArr2[i7];
                int i8 = i6 + 1;
                double dMax = Math.max(0.0d, ((double) fArr2[i5][i6]) - d5);
                double[] dArr4 = dArr2[i5];
                dArr3[i8] = ((dMax + dArr4[i8]) + dArr2[i7][i6]) - dArr4[i6];
                i6 = i8;
            }
        }
        double d6 = dArr2[length][length2];
        char c2 = 0;
        float f3 = (fArr[0] + fArr[2]) / 2.0f;
        boolean z = true;
        float f4 = (fArr[1] + fArr[3]) / 2.0f;
        float[] fArr4 = {0.45f, 0.55f, 0.65f, 0.75f, 0.85f, 0.95f, 1.0f};
        int i9 = 0;
        double d7 = Double.POSITIVE_INFINITY;
        float[] fArr5 = fArr;
        for (int i10 = 7; i9 < i10; i10 = 7) {
            float f5 = fArr4[i9];
            int i11 = i4;
            while (true) {
                if (i11 <= 10) {
                    int i12 = i4;
                    for (int i13 = 10; i12 <= i13; i13 = 10) {
                        float f6 = 1.0f - f5;
                        float f7 = (i12 * f6) / 10.0f;
                        float f8 = (f6 * i11) / 10.0f;
                        float f9 = length2;
                        int iRound = Math.round(f7 * f9);
                        float f10 = length;
                        int iRound2 = Math.round(f8 * f10);
                        float f11 = f7 + f5;
                        int iMin = Math.min(length2, Math.round(f9 * f11));
                        float f12 = f8 + f5;
                        double[] dArr5 = dArr2[Math.min(length, Math.round(f10 * f12))];
                        double d8 = dArr5[iMin];
                        double[] dArr6 = dArr2[iRound2];
                        if ((((d8 - dArr6[iMin]) - dArr5[iRound]) + dArr6[iRound]) / d6 < 0.9d) {
                            f = f3;
                            i = length;
                            i2 = length2;
                            dArr = dArr2;
                            d = d6;
                            i3 = 0;
                            c = 0;
                        } else {
                            i = length;
                            c = 0;
                            float f13 = f5 / 2.0f;
                            i2 = length2;
                            dArr = dArr2;
                            f = f3;
                            d = d6;
                            double dHypot = ((double) (f5 * f5)) + (Math.hypot((f7 + f13) - f3, (f8 + f13) - f4) * 0.02d);
                            if (dHypot < d7) {
                                i3 = 0;
                                fArr5 = new float[]{f7, f8, f11, f12};
                                d7 = dHypot;
                            } else {
                                i3 = 0;
                            }
                        }
                        i12++;
                        i4 = i3;
                        length = i;
                        c2 = c;
                        dArr2 = dArr;
                        length2 = i2;
                        f3 = f;
                        d6 = d;
                    }
                    i11++;
                    z = true;
                    c2 = c2;
                    dArr2 = dArr2;
                    length2 = length2;
                    d6 = d6;
                }
            }
            i9++;
            c2 = c2;
            dArr2 = dArr2;
            length2 = length2;
            d6 = d6;
        }
        return fArr5;
    }
}
