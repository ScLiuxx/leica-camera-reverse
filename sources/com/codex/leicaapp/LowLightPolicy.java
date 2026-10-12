package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LowLightPolicy {
    private LowLightPolicy() {
    }

    public static boolean dark(int i, int i2, long j) {
        return i > 0 && j > 0 && (((long) i) * ((long) Math.max(100, i2)) >= 80000 || j >= 20000000);
    }

    public static long exposureLimit(float f) {
        if (!Float.isFinite(f) || f <= 0.0f) {
            f = 1.0f;
        }
        return f >= 5.0f ? 16666666L : 33333333L;
    }

    public static long[] exposure(int i, long j, float f, int i2, int i3, long j2, long j3) {
        if (i <= 0 || j <= 0 || i2 <= 0 || i3 < i2 || j2 <= 0 || j3 < j2) {
            return null;
        }
        long jMax = Math.max(j2, Math.min(j3, exposureLimit(f)));
        if (j <= jMax) {
            return null;
        }
        double d = (((double) i) * j) / jMax;
        if (d > i3) {
            return null;
        }
        return new long[]{jMax, Math.max(i2, Math.round(d))};
    }
}
