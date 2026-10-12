package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class FilmTone {
    public static float linear(float f) {
        return f <= 0.04045f ? f / 12.92f : (float) Math.pow((f + 0.055f) / 1.055f, 2.4d);
    }

    public static float encoded(float f) {
        return f <= 0.0031308f ? f * 12.92f : (((float) Math.pow(f, 0.4166666666666667d)) * 1.055f) - 0.055f;
    }

    public static float luma(float f, float f2, float f3, boolean z) {
        return encoded((linear(f) * (z ? 0.22897457f : 0.2126f)) + (linear(f2) * (z ? 0.69173855f : 0.7152f)) + (linear(f3) * (z ? 0.07928691f : 0.0722f)));
    }

    private static float clamp(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public static float smooth(float f, float f2, float f3) {
        float fClamp = clamp((f3 - f) / (f2 - f));
        return fClamp * fClamp * (3.0f - (fClamp * 2.0f));
    }

    public static float percentile(int[] iArr, float f) {
        long j = 0;
        long j2 = 0;
        for (int i : iArr) {
            j2 += (long) i;
        }
        if (j2 == 0) {
            return 0.5f;
        }
        long jMax = Math.max(1L, (long) Math.ceil(j2 * f));
        for (int i2 = 0; i2 < iArr.length; i2++) {
            j += (long) iArr[i2];
            if (j >= jMax) {
                return i2 / 255.0f;
            }
        }
        return 1.0f;
    }

    public static float gamma(int[] iArr, float f) {
        float fSmooth = 1.0f - smooth(0.18f, 0.55f, percentile(iArr, 0.5f));
        float fSmooth2 = 0.0f;
        if (Float.isFinite(f) && f > 0.0f) {
            fSmooth2 = smooth(200.0f, 1600.0f, f);
        }
        return (fSmooth * (0.14f - (fSmooth2 * 0.07f))) + 1.08f;
    }

    public static float denoise(float f) {
        if (!Float.isFinite(f) || f <= 0.0f) {
            return 0.0f;
        }
        return smooth(400.0f, 2400.0f, f);
    }

    public static float whitePoint(int[] iArr) {
        float fPercentile = percentile(iArr, 0.998f);
        float fPercentile2 = percentile(iArr, 0.1f);
        if (fPercentile < 0.82f || fPercentile >= 0.98f || fPercentile - fPercentile2 < 0.25f) {
            return 1.0f;
        }
        return Math.max(0.9f, fPercentile + 0.015f);
    }

    public static float detailStrength(int[] iArr, float f) {
        return (1.0f - ((!Float.isFinite(f) || f <= 0.0f) ? 0.4f : smooth(400.0f, 1600.0f, f))) * 0.12f * smooth(0.08f, 0.35f, percentile(iArr, 0.5f));
    }

    public static float target(float f, float f2, float f3, float f4) {
        float fClamp = clamp(f + ((Math.min(1.0f, (((f - 0.65f) * 0.35f) / Math.max(0.2f, f4 - 0.65f)) + 0.65f) - f) * smooth(0.65f, 0.85f, f)));
        float fClamp2 = clamp(fClamp * ((f3 * 0.12f * smooth(0.06f, 0.25f, fClamp) * (1.0f - smooth(0.65f, 0.94f, fClamp))) + 1.0f));
        float fSmooth = smooth(0.06f, 0.25f, fClamp2) * (1.0f - smooth(0.65f, 0.94f, fClamp2));
        double d = fClamp2;
        float fPow = (float) Math.pow(d, 1.12d);
        return clamp(fPow + ((fClamp2 - fPow) * smooth(0.035f, 0.16f, fClamp2)) + ((((float) Math.pow(d, 1.0f / f2)) - fClamp2) * fSmooth));
    }
}
