package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes12.dex */
public final class B43Tone {
    private static final boolean LOADED;

    private static native boolean applyNative(Bitmap bitmap);

    static {
        boolean z;
        try {
            System.loadLibrary("b43tone");
            z = true;
        } catch (Throwable th) {
            Log.w("B43Tone", "Tone library unavailable", th);
            z = false;
        }
        LOADED = z;
    }

    private B43Tone() {
    }

    public static boolean apply(Bitmap bitmap, boolean z) {
        if (z || !LOADED || bitmap == null || bitmap.isRecycled() || !bitmap.isMutable()) {
            return false;
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        try {
            boolean zApplyNative = applyNative(bitmap);
            Log.i("B43Tone", "applied=" + zApplyNative + " elapsedMs=" + (SystemClock.elapsedRealtime() - jElapsedRealtime));
            return zApplyNative;
        } catch (Throwable th) {
            Log.w("B43Tone", "Keep B13 image after tone error", th);
            return false;
        }
    }

    public static float smoothZoom(float f, float f2, float f3) {
        if (f3 <= 0.0f) {
            return f;
        }
        if (f3 >= 1.0f) {
            return f2;
        }
        float fMax = Math.max(0.0f, Math.min(1.0f, f3));
        double d = f;
        return (float) Math.exp(Math.log(d) + ((Math.log(f2) - Math.log(d)) * ((double) (fMax * fMax * (3.0f - (fMax * 2.0f))))));
    }
}
