package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class M9CaptureTuning {
    public final int cct;
    public final String cctSource;
    public final int lux;
    public final String luxSource;
    public final boolean measuredCct;
    public final boolean measuredLux;

    public static int exposureCompensation(int i, float f, int i2, int i3) {
        return exposureCompensation(i, f, i2, i3, 0.33333334f);
    }

    public static int exposureCompensation(int i, float f, int i2, int i3, float f2) {
        if (i2 > i3) {
            return i;
        }
        return (int) Math.max(i2, Math.min(i3, ((long) i) - ((!Float.isFinite(f) || f <= 0.0f) ? 0L : Math.round(f2 / f))));
    }

    private static boolean cctValid(float f) {
        return Float.isFinite(f) && f >= 1000.0f && f <= 20000.0f;
    }

    private static boolean luxValid(float f) {
        return Float.isFinite(f) && f >= 0.0f && f <= 10000.0f;
    }

    public M9CaptureTuning(float f, float f2) {
        this(f, f2, null);
    }

    public M9CaptureTuning(float f, float f2, M9CaptureTuning m9CaptureTuning) {
        String str;
        boolean zCctValid = cctValid(f);
        boolean zLuxValid = luxValid(f2);
        boolean z = false;
        boolean z2 = zCctValid || (m9CaptureTuning != null && m9CaptureTuning.measuredCct);
        this.measuredCct = z2;
        if (zLuxValid || (m9CaptureTuning != null && m9CaptureTuning.measuredLux)) {
            z = true;
        }
        this.measuredLux = z;
        this.cct = zCctValid ? Math.round(f) : z2 ? m9CaptureTuning.cct : 5500;
        this.lux = zLuxValid ? Math.round(f2) : z ? m9CaptureTuning.lux : 200;
        String str2 = "preview";
        if (zCctValid) {
            str = "capture";
        } else {
            str = z2 ? "preview" : "reference";
        }
        this.cctSource = str;
        if (zLuxValid) {
            str2 = "capture";
        } else if (!z) {
            str2 = "reference";
        }
        this.luxSource = str2;
    }
}
