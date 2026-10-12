package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class M9AutoExposure {
    private long candidateSince = -1;
    private long lastSample = -1;
    private boolean protecting;

    public float bias() {
        return this.protecting ? 0.6666667f : 0.33333334f;
    }

    public void reset() {
        this.protecting = false;
        this.lastSample = -1L;
        this.candidateSince = -1L;
    }

    /* JADX WARN: Code duplicated, block: B:70:0x0117  */
    public float update(int[] iArr, int i, int i2, int i3, long j, long j2) {
        M9AutoExposure m9AutoExposure;
        boolean z;
        long j3;
        int[] iArr2 = iArr;
        long j4 = this.lastSample;
        if (!(j4 >= 0 && j2 - j4 <= 1500 && j2 >= j4)) {
            this.candidateSince = -1L;
        }
        this.lastSample = j2;
        if (!(i3 > 0 && i3 <= 250 && j > 0 && j <= 8000000) || iArr2 == null || iArr2.length != i * i2 || iArr2.length == 0) {
            this.protecting = false;
            this.candidateSince = -1L;
            return bias();
        }
        float f = this.protecting ? 0.88f : 0.975f;
        double d = 0.0d;
        double d2 = 0.0d;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        while (i5 < i2) {
            int i8 = 0;
            while (i8 < i) {
                int i9 = iArr2[(i5 * i) + i8];
                float f2 = ((i9 >> 16) & 255) / 255.0f;
                float f3 = ((i9 >> 8) & 255) / 255.0f;
                float f4 = (i9 & 255) / 255.0f;
                float f5 = (0.2126f * f2) + (0.7152f * f3) + (0.0722f * f4);
                double d3 = f5;
                d += d3;
                if (f5 > 0.65f) {
                    i7++;
                }
                if (Math.max(f2, Math.max(f3, f4)) >= f && f5 > 0.65f) {
                    i4++;
                }
                if (i8 >= i / 4 && i8 < (i * 3) / 4 && i5 >= i2 / 4 && i5 < (i2 * 3) / 4) {
                    d2 += d3;
                    i6++;
                }
                i8++;
                iArr2 = iArr;
            }
            i5++;
            iArr2 = iArr;
        }
        int length = iArr.length;
        if (d / ((double) length) < 0.32d || i6 <= 0 || d2 / ((double) i6) < 0.28d) {
            m9AutoExposure = this;
        } else {
            float f6 = length;
            if (i7 / f6 < 0.15f) {
                m9AutoExposure = this;
            } else {
                float f7 = i4 / f6;
                m9AutoExposure = this;
                z = f7 >= (m9AutoExposure.protecting ? 0.004f : 0.015f);
            }
        }
        if (z == m9AutoExposure.protecting) {
            m9AutoExposure.candidateSince = -1L;
        } else {
            if (m9AutoExposure.candidateSince < 0) {
                j3 = j2;
                m9AutoExposure.candidateSince = j3;
            } else {
                j3 = j2;
            }
            if (j3 - m9AutoExposure.candidateSince >= (z ? 1500 : 3000)) {
                m9AutoExposure.protecting = z;
                m9AutoExposure.candidateSince = -1L;
            }
        }
        return bias();
    }
}
