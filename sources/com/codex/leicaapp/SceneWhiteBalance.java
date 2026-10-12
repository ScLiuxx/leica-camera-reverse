package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class SceneWhiteBalance {
    public static boolean valid(float f) {
        return Float.isFinite(f) && f >= 2000.0f && f <= 12000.0f;
    }

    public static float target(float f) {
        if (!valid(f)) {
            return 5500.0f;
        }
        float fMax = Math.max(0.0f, Math.min(1.0f, (f - 4800.0f) / 700.0f));
        return (700.0f * fMax * fMax * (3.0f - (fMax * 2.0f))) + 4800.0f;
    }

    public static float[] gains(float f, boolean z) {
        if (!z || !valid(f)) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
        float fMax = Math.max(-0.075f, Math.min(0.075f, ((1000000.0f / f) - (1000000.0f / target(f))) * 0.0012f));
        return new float[]{(float) Math.exp(fMax), 1.0f, (float) Math.exp(-fMax)};
    }

    public static float jpegTarget(int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            return Float.NaN;
        }
        double dMax = 0.0d;
        int i = 0;
        for (int i2 : iArr) {
            float f = ((i2 >>> 16) & 255) / 255.0f;
            float f2 = ((i2 >>> 8) & 255) / 255.0f;
            float f3 = (i2 & 255) / 255.0f;
            float fMax = Math.max(f, Math.max(f2, f3));
            float fMin = Math.min(f, Math.min(f2, f3));
            if (fMin >= 0.15f && fMax <= 0.85f && fMax - fMin <= 0.12f) {
                float f4 = f + f3;
                if (Math.abs(f2 - (0.5f * f4)) <= 0.04f) {
                    dMax += (double) ((f - f3) / Math.max(0.01f, f4));
                    i++;
                }
            }
        }
        if (i < Math.max(16, iArr.length / 50)) {
            return Float.NaN;
        }
        return 5150.0f - (((float) Math.max(-1.0d, Math.min(1.0d, (dMax / ((double) i)) / 0.08d))) * 350.0f);
    }

    public static float[] jpegGains(float f) {
        if (!Float.isFinite(f)) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
        float fMax = (194.17476f - (1000000.0f / Math.max(4800.0f, Math.min(5500.0f, f)))) * 0.0012f;
        return new float[]{(float) Math.exp(fMax), 1.0f, (float) Math.exp(-fMax)};
    }
}
