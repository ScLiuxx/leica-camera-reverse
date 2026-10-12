package com.codex.leicaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Gainmap;
import android.graphics.Paint;
import android.hardware.camera2.CaptureResult;
import android.os.SystemClock;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class M9ReferenceTone {
    static final int N = 33;
    static final int PROFILES = 5;
    static final int STRIDE = 107811;
    static volatile byte[] table;

    static synchronized void load(InputStream inputStream) throws Exception {
        if (table != null) {
            return;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(539055);
            try {
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStream.read(bArr);
                    if (i == -1) {
                        break;
                    } else {
                        byteArrayOutputStream.write(bArr, 0, i);
                    }
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                if (byteArray.length != 539055) {
                    throw new IllegalArgumentException("Invalid color LUT size");
                }
                table = byteArray;
                byteArrayOutputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                }
            }
            throw th3;
        }
    }

    static int profile(int i, long j, int i2) {
        if (i2 >= 2000 && i2 < 3400) {
            return 3;
        }
        if (i2 < 3400 || i2 >= 4800) {
            return (i <= 0 || i > 64 || j <= 0 || j > 12000000) ? 1 : 0;
        }
        return 2;
    }

    static int measuredCct(CaptureResult captureResult) {
        int iRound;
        int iRound2;
        if (captureResult == null) {
            return 0;
        }
        String[] strArr = {"xiaomi.camera.awb.cct", "xiaomi.camera.awb.colorTemperature", "com.xiaomi.miCam.awbWrapper.awbCct", "org.quic.camera2.statsconfigs.AWBFrameControlCCT"};
        for (int i = 0; i < 4; i++) {
            String str = strArr[i];
            Class[] clsArr = {Float.class, Integer.class};
            for (int i2 = 0; i2 < 2; i2++) {
                try {
                    Object obj = captureResult.get(new CaptureResult.Key(str, clsArr[i2]));
                    if ((obj instanceof Number) && (iRound2 = Math.round(((Number) obj).floatValue())) >= 2000 && iRound2 <= 12000) {
                        return iRound2;
                    }
                } catch (IllegalArgumentException e) {
                } catch (UnsupportedOperationException e2) {
                    if (!CameraZoomCalibration.stockOptical()) {
                        throw e2;
                    }
                }
            }
        }
        try {
            Object objInvoke = Class.forName("com.codex.leicaapp.MadridLegendary").getMethod("number", CaptureResult.class, Boolean.TYPE).invoke(null, captureResult, true);
            if (!(objInvoke instanceof Number) || (iRound = Math.round(((Number) objInvoke).floatValue())) < 2000 || iRound > 12000) {
                return 0;
            }
            return iRound;
        } catch (Exception | LinkageError e3) {
        }
    }

    static int fallbackProfile(Bitmap bitmap, int i, long j) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i2 = 0;
        int i3 = 0;
        for (int iMax = height / 32; iMax < height; iMax += Math.max(1, height / 16)) {
            for (int iMax2 = width / 32; iMax2 < width; iMax2 += Math.max(1, width / 16)) {
                int pixel = bitmap.getPixel(iMax2, iMax);
                float f = (pixel >>> 16) & 255;
                float f2 = (pixel >>> 8) & 255;
                float f3 = pixel & 255;
                float fMax = Math.max(f, Math.max(f2, f3));
                float fMin = Math.min(f, Math.min(f2, f3));
                if (fMax > 40.0f && fMax < 245.0f) {
                    i2++;
                    if (f > f2 + 8.0f && f2 > f3 + 8.0f && (fMax - fMin) / Math.max(1.0f, fMax) > 0.2f) {
                        i3++;
                    }
                }
            }
        }
        float f4 = i2 > 0 ? i3 / i2 : 0.0f;
        if (f4 > 0.45f) {
            return 3;
        }
        if (profile(i, j, 5500) == 0) {
            return 0;
        }
        if (i >= 640) {
            return f4 > 0.24f ? 2 : 4;
        }
        return 1;
    }

    public static boolean allowLegacy() {
        return !CameraZoomCalibration.stockOptical();
    }

    public static void apply(Bitmap bitmap, byte[] bArr, int i, CaptureResult captureResult) throws Throwable {
        tryApply(bitmap, bArr, i, captureResult);
    }

    /* JADX WARN: Code duplicated, block: B:96:0x01f4  */
    /* JADX WARN: Code duplicated, block: B:99:0x01fa  */
    public static boolean tryApply(Bitmap bitmap, byte[] bArr, int i, CaptureResult captureResult) throws Throwable {
        int iFallbackProfile;
        if (bitmap == null || bitmap.isRecycled() || !bitmap.isMutable() || bArr == null) {
            return false;
        }
        Bitmap bitmap2 = null;
        try {
            try {
                boolean zStockOptical = CameraZoomCalibration.stockOptical();
                if (!zStockOptical) {
                    try {
                        try {
                            if (table == null) {
                                Context context = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
                                if (context == null) {
                                    throw new IllegalStateException("Missing application context");
                                }
                                load(context.getAssets().open("leica-color-v3.rgb"));
                            }
                        } catch (Exception | LinkageError | OutOfMemoryError e) {
                            e = e;
                            Log.e("M9ReferenceTone", "color-v17 failed; retain available pixels", e);
                            if (bitmap2 != null) {
                                bitmap2.recycle();
                            }
                            return false;
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (bitmap2 != null) {
                            bitmap2.recycle();
                        }
                        throw th;
                    }
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                if (bitmapDecodeByteArray != null) {
                    try {
                        try {
                            if (bitmapDecodeByteArray.getWidth() == bitmap.getWidth() && bitmapDecodeByteArray.getHeight() == bitmap.getHeight()) {
                                Integer num = captureResult == null ? null : (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
                                Long l = captureResult == null ? null : (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                                int iMeasuredCct = measuredCct(captureResult);
                                long jLongValue = 0;
                                if (iMeasuredCct > 0) {
                                    int iIntValue = num == null ? 0 : num.intValue();
                                    if (l != null) {
                                        jLongValue = l.longValue();
                                    }
                                    iFallbackProfile = profile(iIntValue, jLongValue, iMeasuredCct);
                                } else {
                                    int iIntValue2 = num == null ? 0 : num.intValue();
                                    if (l != null) {
                                        jLongValue = l.longValue();
                                    }
                                    iFallbackProfile = fallbackProfile(bitmapDecodeByteArray, iIntValue2, jLongValue);
                                }
                                long jElapsedRealtime = SystemClock.elapsedRealtime();
                                if (!zStockOptical && SceneColorTransfer.model == null) {
                                    Context context2 = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
                                    if (context2 == null) {
                                        throw new IllegalStateException("Missing scene transfer model");
                                    }
                                    SceneColorTransfer.load(context2.getAssets().open("scene-transfer.bin"));
                                }
                                if (zStockOptical) {
                                    new Canvas(bitmap).drawBitmap(bitmapDecodeByteArray, 0.0f, 0.0f, (Paint) null);
                                } else {
                                    renderAdaptive(bitmapDecodeByteArray, bitmap, iFallbackProfile);
                                }
                                StockShading.apply(bitmap, captureResult, iMeasuredCct > 0 ? iMeasuredCct : i);
                                String str = "capture";
                                if (bitmapDecodeByteArray.hasGainmap()) {
                                    Gainmap gainmap = (Gainmap) Class.forName("com.codex.leicaapp.HdrGainmaps").getMethod("capture", Bitmap.class, Boolean.TYPE).invoke(null, bitmapDecodeByteArray, false);
                                    if (gainmap == null) {
                                        throw new IllegalStateException("Source HDR copy failed");
                                    }
                                    bitmap.setGainmap(gainmap);
                                }
                                StringBuilder sbAppend = new StringBuilder().append("color-v36 sourceJPEG nativeColor=").append(zStockOptical).append(" profile=").append(iFallbackProfile).append(" modelCct=").append(i).append(" measuredCct=").append(iMeasuredCct).append(" selector=");
                                if (iMeasuredCct <= 0) {
                                    str = "color-content";
                                }
                                Log.i("M9ReferenceTone", sbAppend.append(str).append(" iso=").append(num).append(" ns=").append(l).append(" size=").append(bitmap.getWidth()).append("x").append(bitmap.getHeight()).append(" ms=").append(SystemClock.elapsedRealtime() - jElapsedRealtime).toString());
                                if (bitmapDecodeByteArray != null) {
                                    bitmapDecodeByteArray.recycle();
                                }
                                return true;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            bitmap2 = bitmapDecodeByteArray;
                            if (bitmap2 != null) {
                                bitmap2.recycle();
                            }
                            throw th;
                        }
                    } catch (Exception | LinkageError | OutOfMemoryError e2) {
                        e = e2;
                        bitmap2 = bitmapDecodeByteArray;
                        Log.e("M9ReferenceTone", "color-v17 failed; retain available pixels", e);
                        if (bitmap2 != null) {
                            bitmap2.recycle();
                        }
                        return false;
                    }
                }
                throw new IllegalArgumentException("Source and output dimensions differ");
            } catch (Exception | LinkageError | OutOfMemoryError e3) {
                e = e3;
            }
        } catch (Throwable th3) {
            th = th3;
        }
    }

    static void render(Bitmap bitmap, Bitmap bitmap2, int i) {
        if (table == null || i < 0 || i >= PROFILES) {
            throw new IllegalArgumentException("Invalid grade");
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (bitmap2.getWidth() != width || bitmap2.getHeight() != height) {
            throw new IllegalArgumentException("Size");
        }
        int[] iArr = new int[width];
        for (int i2 = 0; i2 < height; i2++) {
            bitmap.getPixels(iArr, 0, width, 0, i2, width, 1);
            for (int i3 = 0; i3 < width; i3++) {
                iArr[i3] = pixel(iArr[i3], i);
            }
            bitmap2.setPixels(iArr, 0, width, 0, i2, width, 1);
        }
    }

    static void renderAdaptive(Bitmap bitmap, Bitmap bitmap2, int i) {
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        SceneColorTransfer.Plan planAnalyse = SceneColorTransfer.analyse(bitmap, i);
        byte[] bArr = new byte[STRIDE];
        for (int i2 = 0; i2 < N; i2++) {
            for (int i3 = 0; i3 < N; i3++) {
                for (int i4 = 0; i4 < N; i4++) {
                    int i5 = ((((i2 * N) + i3) * N) + i4) * 3;
                    int iRound = (Math.round((i2 * 255.0f) / 32.0f) << 16) | (-16777216) | (Math.round((i3 * 255.0f) / 32.0f) << 8) | Math.round((i4 * 255.0f) / 32.0f);
                    int iApply = planAnalyse.apply(iRound, pixel(iRound, planAnalyse.styleMode));
                    bArr[i5] = (byte) (iApply >>> 16);
                    bArr[i5 + 1] = (byte) (iApply >>> 8);
                    bArr[i5 + 2] = (byte) iApply;
                }
            }
        }
        Log.i("M9ReferenceTone", "scene-transfer style=" + planAnalyse.styleMode + " nearest=" + planAnalyse.nearest + " confidence=" + planAnalyse.confidence + " analyseMs=" + (SystemClock.elapsedRealtime() - jElapsedRealtime));
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[width];
        for (int i6 = 0; i6 < height; i6++) {
            bitmap.getPixels(iArr, 0, width, 0, i6, width, 1);
            for (int i7 = 0; i7 < width; i7++) {
                iArr[i7] = pixel(iArr[i7], 0, bArr);
            }
            bitmap2.setPixels(iArr, 0, width, 0, i6, width, 1);
        }
    }

    static int pixel(int i, int i2) {
        return pixel(i, i2, table);
    }

    static int pixel(int i, int i2, byte[] bArr) {
        float f;
        float f2;
        float f3;
        int i3;
        float f4 = ((i >>> 16) & 255) * 0.1254902f;
        float f5 = ((i >>> 8) & 255) * 0.1254902f;
        float f6 = (i & 255) * 0.1254902f;
        int iMin = Math.min(31, (int) f4);
        int iMin2 = Math.min(31, (int) f5);
        int iMin3 = Math.min(31, (int) f6);
        float f7 = f4 - iMin;
        float f8 = f5 - iMin2;
        float f9 = f6 - iMin3;
        int i4 = (STRIDE * i2) + (((((iMin * N) + iMin2) * N) + iMin3) * 3);
        int i5 = 3366;
        if (f7 >= f8) {
            if (f8 >= f9) {
                f = 1.0f - f7;
                f2 = f7 - f8;
                f3 = f8 - f9;
                f8 = f9;
                i3 = 3267;
            } else {
                i5 = 3270;
                if (f7 >= f9) {
                    f = 1.0f - f7;
                    f2 = f7 - f9;
                    f3 = f9 - f8;
                    i3 = 3267;
                } else {
                    f = 1.0f - f9;
                    float f10 = f9 - f7;
                    i3 = 3;
                    f3 = f7 - f8;
                    f2 = f10;
                }
            }
        } else if (f9 >= f8) {
            f = 1.0f - f9;
            float f11 = f9 - f8;
            float f12 = f8 - f7;
            i3 = 3;
            i5 = 102;
            f8 = f7;
            f2 = f11;
            f3 = f12;
        } else if (f9 >= f7) {
            f = 1.0f - f8;
            float f13 = f8 - f9;
            float f14 = f9 - f7;
            i3 = 99;
            i5 = 102;
            f8 = f7;
            f2 = f13;
            f3 = f14;
        } else {
            f = 1.0f - f8;
            float f15 = f8 - f7;
            float f16 = f7 - f9;
            f2 = f15;
            f3 = f16;
            f8 = f9;
            i3 = 99;
        }
        int iMax = i & (-16777216);
        for (int i6 = 0; i6 < 3; i6++) {
            int i7 = i4 + i6;
            iMax |= Math.max(0, Math.min(255, Math.round(((((bArr[i7] & 255) * f) + ((bArr[i7 + i3] & 255) * f2)) + ((bArr[i7 + i5] & 255) * f3)) + ((bArr[((i7 + 3267) + 99) + 3] & 255) * f8)))) << (16 - (i6 * 8));
        }
        return iMax;
    }
}
