package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class AiAestheticChoice {
    public static final float MIN_GAIN = 0.05f;

    public static int choose(float[] fArr, boolean[] zArr) {
        if (fArr.length == 0 || zArr.length != fArr.length || !Float.isFinite(fArr[0])) {
            throw new IllegalArgumentException("Missing original aesthetic score");
        }
        int i = 0;
        for (int i2 = 1; i2 < fArr.length; i2++) {
            if (zArr[i2] && Float.isFinite(fArr[i2])) {
                float f = fArr[i2];
                if (f >= fArr[0] + 0.05f && f > fArr[i]) {
                    i = i2;
                }
            }
        }
        return i;
    }

    public static boolean preserves(float[] fArr, float[] fArr2) {
        return fArr[0] <= fArr2[0] && fArr[1] <= fArr2[1] && fArr[2] >= fArr2[2] && fArr[3] >= fArr2[3];
    }
}
