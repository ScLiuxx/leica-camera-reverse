package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class AiDirectionTarget {
    private static final float HALF_LONG_SIDE_TANGENT = 0.65f;
    private boolean inFront;
    private long lastMotion;
    private boolean locked;
    private final float[] world = new float[3];
    private final float[] previous = new float[9];
    private float viewZoom = 1.0f;
    private long lastSample = -1;
    public float x = 0.8f;
    public float y = 0.3f;
    public float bearingX = 1.0f;
    public float bearingY = 0.0f;

    public void setViewZoom(float f) {
        if (!Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        this.viewZoom = f;
    }

    public static boolean sameFraming(float[] fArr, float[] fArr2) {
        double d = 0.0d;
        double d2 = 0.0d;
        double d3 = 0.0d;
        double d4 = 0.0d;
        for (int i = 0; i < 3; i++) {
            int i2 = i * 3;
            float f = fArr[i2];
            float f2 = fArr2[i2];
            d += (double) (f * f2);
            int i3 = i2 + 1;
            float f3 = fArr2[i3];
            d4 += (double) (f * f3);
            float f4 = fArr[i3];
            d3 += (double) (f2 * f4);
            d2 += (double) (f4 * f3);
        }
        double d5 = d + d2;
        double d6 = d3 - d4;
        return Double.isFinite(d5) && Double.isFinite(d6) && d5 > 0.0d && Math.abs(d6) < d5;
    }

    public void reset() {
        this.inFront = false;
        this.locked = false;
        this.lastSample = -1L;
        this.x = 0.8f;
        this.y = 0.3f;
    }

    public void reset(float f, float f2) {
        reset();
        this.x = f;
        this.y = f2;
    }

    public void update(float[] fArr, int i, float f, long j) {
        float f2;
        if (f <= 0.0f || !Float.isFinite(f) || j <= this.lastSample) {
            return;
        }
        for (float f3 : fArr) {
            if (!Float.isFinite(f3)) {
                return;
            }
        }
        float fMax = (HALF_LONG_SIDE_TANGENT / this.viewZoom) / Math.max(1.0f, f);
        float f4 = f * fMax;
        if (!this.locked) {
            float f5 = (this.x - 0.5f) * 2.0f * f4;
            float f6 = (0.5f - this.y) * 2.0f * fMax;
            switch (i) {
                case 1:
                    float f7 = -f6;
                    f6 = f5;
                    f5 = f7;
                    break;
                case 2:
                    f5 = -f5;
                    f6 = -f6;
                    break;
                case 3:
                    f6 = -f5;
                    f5 = f6;
                    break;
            }
            for (int i2 = 0; i2 < 3; i2++) {
                int i3 = i2 * 3;
                this.world[i2] = ((fArr[i3] * f5) + (fArr[i3 + 1] * f6)) - fArr[i3 + 2];
            }
            this.locked = true;
            this.lastMotion = j;
        }
        if (this.lastSample >= 0) {
            double d = 0.0d;
            for (int i4 = 0; i4 < 9; i4++) {
                double d2 = ((double) fArr[i4]) - ((double) this.previous[i4]);
                d += d2 * d2;
            }
            double dAsin = Math.asin(Math.min(1.0d, Math.sqrt(d / 8.0d))) * 2.0d;
            double d3 = (j - this.lastSample) / 1.0E9d;
            if (d3 > 0.15d || dAsin / d3 > 0.08d) {
                this.lastMotion = j;
            }
        }
        System.arraycopy(fArr, 0, this.previous, 0, 9);
        this.lastSample = j;
        float f8 = fArr[0];
        float[] fArr2 = this.world;
        float f9 = fArr2[0];
        float f10 = fArr[3];
        float f11 = fArr2[1];
        float f12 = (f8 * f9) + (f10 * f11);
        float f13 = fArr[6];
        float f14 = fArr2[2];
        float f15 = f12 + (f13 * f14);
        float f16 = (fArr[1] * f9) + (fArr[4] * f11) + (fArr[7] * f14);
        float f17 = (fArr[2] * f9) + (fArr[5] * f11) + (fArr[8] * f14);
        this.inFront = f17 < -0.001f;
        switch (i) {
            case 1:
                f2 = -f15;
                f15 = f16;
                break;
            case 2:
                f15 = -f15;
                f2 = -f16;
                break;
            case 3:
                f15 = -f16;
                f2 = f15;
                break;
            default:
                f2 = f16;
                break;
        }
        float f18 = f15 / f4;
        this.bearingX = f18;
        float f19 = (-f2) / fMax;
        this.bearingY = f19;
        if (Math.hypot(f18, f19) < 0.001d) {
            this.bearingX = 1.0f;
        }
        if (!this.inFront) {
            this.y = Float.NaN;
            this.x = Float.NaN;
        } else {
            float f20 = (-f17) * 2.0f;
            this.x = (f15 / (f4 * f20)) + 0.5f;
            this.y = 0.5f - (f2 / (f20 * fMax));
        }
    }

    public boolean fresh(long j) {
        if (this.locked && this.inFront) {
            long j2 = this.lastSample;
            if (j >= j2 && j - j2 <= 150000000) {
                return true;
            }
        }
        return false;
    }

    public boolean stable(long j) {
        return fresh(j) && j - this.lastMotion >= 200000000;
    }
}
