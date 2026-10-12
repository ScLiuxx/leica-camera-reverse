package com.codex.leicaapp;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class AiCompositionGate {
    private boolean fired;
    private long stableSince = -1;
    private long previous = -1;

    public void reset() {
        this.previous = -1L;
        this.stableSince = -1L;
        this.fired = false;
    }

    public boolean update(long j, float f, float f2, boolean z) {
        return update(j, f, f2, z, 0.006f);
    }

    public boolean update(long j, float f, float f2, boolean z, float f3) {
        boolean z2 = (f3 <= 0.0f || !Float.isFinite(f3)) ? false : z;
        long j2 = this.previous;
        if (j2 >= 0 && (j < j2 || j - j2 > 150)) {
            this.stableSince = -1L;
        }
        this.previous = j;
        if (this.fired || !z2 || !Float.isFinite(f) || !Float.isFinite(f2) || Math.hypot(f - 0.5f, f2 - 0.5f) > f3) {
            this.stableSince = -1L;
            return false;
        }
        if (this.stableSince < 0) {
            this.stableSince = j;
        }
        if (j - this.stableSince < 350) {
            return false;
        }
        this.fired = true;
        return true;
    }
}
