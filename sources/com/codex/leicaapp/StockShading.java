package com.codex.leicaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CaptureResult;
import java.io.File;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class StockShading {
    private static boolean loaded;

    private static native boolean render(Bitmap bitmap, String str, float f, float f2, float f3);

    public static void apply(Bitmap bitmap, CaptureResult captureResult, int i) throws Exception {
        if (CameraZoomCalibration.stockOptical()) {
            Context context = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
            if (!loaded) {
                System.loadLibrary("leicashading");
                loaded = true;
            }
            Class<?> cls = Class.forName("com.codex.leicaapp.MadridLegendary");
            Method declaredMethod = cls.getDeclaredMethod("parameters", Context.class, String.class);
            declaredMethod.setAccessible(true);
            File file = (File) declaredMethod.invoke(null, context, "leica_filter_param_m9_snapshot.bin");
            float fFloatValue = ((Number) cls.getMethod("number", CaptureResult.class, Boolean.TYPE).invoke(null, captureResult, false)).floatValue();
            if (!Float.isFinite(fFloatValue)) {
                fFloatValue = 200.0f;
            }
            Float f = captureResult != null ? (Float) captureResult.get(CaptureResult.CONTROL_ZOOM_RATIO) : null;
            if (!render(bitmap, file.getAbsolutePath(), Math.max(0.0f, Math.min(1000.0f, fFloatValue)), i > 0 ? i : 5500.0f, (f == null || !Float.isFinite(f.floatValue()) || f.floatValue() <= 0.0f) ? 1.0f : f.floatValue())) {
                throw new IllegalStateException("Stock shading unavailable");
            }
        }
    }
}
