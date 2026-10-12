package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class RawCrop {
    private RawCrop() {
    }

    public static float teleCorrection(float f, float f2, float f3, float f4) {
        if (!Float.isFinite(f4) || f4 <= 1.05f || f3 <= 1.0f || f2 <= 0.0f) {
            return 1.0f;
        }
        float f5 = f2 / f3;
        if (Math.abs(f - f5) <= Math.max(0.002f, f5 * 0.02f)) {
            return f4;
        }
        return 1.0f;
    }

    public static float[] map(float[] fArr, float[] fArr2, float f, int i, int i2, int i3, int i4) {
        float f2 = fArr[2] - fArr[0];
        float f3 = fArr[3] - fArr[1];
        if (f2 <= 0.0f || f3 <= 0.0f || i <= 0 || i2 <= 0 || i3 <= 0 || i4 <= 0 || !Float.isFinite(f) || f <= 0.0f) {
            throw new IllegalArgumentException("Invalid RAW geometry");
        }
        float f4 = fArr2[0];
        float f5 = fArr2[2];
        float f6 = (f4 + f5) / 2.0f;
        float f7 = (fArr2[1] + fArr2[3]) / 2.0f;
        float fMin = Math.min((f5 - f4) * f, Math.min(f6 - fArr[0], fArr[2] - f6) * 2.0f);
        float fMin2 = Math.min((fArr2[3] - fArr2[1]) * f, Math.min(f7 - fArr[1], fArr[3] - f7) * 2.0f);
        float f8 = i3 / i4;
        if (fMin / fMin2 > f8) {
            fMin = fMin2 * f8;
        } else {
            fMin2 = fMin / f8;
        }
        if (Float.isFinite(fMin) && Float.isFinite(fMin2) && fMin > 0.0f && fMin2 > 0.0f) {
            float f9 = fMin / 2.0f;
            float f10 = i;
            float f11 = fMin2 / 2.0f;
            float f12 = i2;
            return new float[]{(((f6 - f9) - fArr[0]) * f10) / f2, (((f7 - f11) - fArr[1]) * f12) / f3, (((f6 + f9) - fArr[0]) * f10) / f2, (((f7 + f11) - fArr[1]) * f12) / f3};
        }
        throw new IllegalArgumentException("Empty RAW crop");
    }
}
