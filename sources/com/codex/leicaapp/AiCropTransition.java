package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class AiCropTransition {
    public static final long LOCK_MS = 120;
    public static final long ZOOM_MS = 650;
    public final float endZoom;
    public final float startZoom;

    public AiCropTransition(float f, float f2) {
        if (!Float.isFinite(f) || !Float.isFinite(f2) || f <= 0.0f || f2 < f) {
            throw new IllegalArgumentException("Invalid crop zoom");
        }
        this.startZoom = f;
        this.endZoom = f2;
    }

    public static float ease(float f) {
        float fMax = Math.max(0.0f, Math.min(1.0f, f));
        return fMax * fMax * (3.0f - (fMax * 2.0f));
    }

    public static float smoothZoom(float f, float f2, float f3) {
        if (f3 <= 0.0f) {
            return f;
        }
        if (f3 >= 1.0f) {
            return f2;
        }
        double d = f;
        return (float) Math.exp(Math.log(d) + ((Math.log(f2) - Math.log(d)) * ((double) ease(f3))));
    }

    public float zoomAt(long j) {
        float f = this.startZoom;
        return f + ((this.endZoom - f) * ease((j - 120) / 650.0f));
    }

    public float frameFraction(float f) {
        return Math.max(0.01f, Math.min(1.0f, f / this.endZoom));
    }

    public boolean finished(long j) {
        return j >= 770;
    }

    public boolean needsZoom() {
        return this.endZoom - this.startZoom > 0.03f;
    }
}
