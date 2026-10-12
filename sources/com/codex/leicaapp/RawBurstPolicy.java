package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class RawBurstPolicy {
    private RawBurstPolicy() {
    }

    public static boolean useIspInput() {
        return true;
    }

    public static int frames(int i, long j, boolean z) {
        if (z || i <= 0 || j <= 0) {
            return 1;
        }
        if (i >= 1200 || j >= 30000000) {
            return 5;
        }
        if (i >= 600 || j >= 15000000) {
            return 4;
        }
        if (i >= 200 || j >= 8000000) {
            return 3;
        }
        if (i < 100 && j < 4000000) {
            return 1;
        }
        return 2;
    }

    public static int frames(int i, long j, boolean z, double[] dArr) {
        int iFrames = frames(i, j, z);
        if (z || i <= 0 || j <= 0 || dArr == null || dArr.length != 8) {
            return iFrames;
        }
        double dMax = 0.0d;
        for (int i2 = 0; i2 < 8; i2 += 2) {
            if (Double.isFinite(dArr[i2])) {
                int i3 = i2 + 1;
                if (Double.isFinite(dArr[i3])) {
                    double d = dArr[i2];
                    if (d >= 0.0d) {
                        double d2 = dArr[i3];
                        if (d2 >= 0.0d) {
                            dMax = Math.max(dMax, (d * 0.02d) + d2);
                        }
                    }
                }
            }
            return iFrames;
        }
        return Math.max(iFrames, (int) Math.min(5.0d, Math.ceil(dMax / 4.0E-6d)));
    }
}
