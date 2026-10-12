package com.codex.leicaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.hardware.camera2.CaptureResult;
import android.os.SystemClock;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class MadridLegendary {
    private static boolean loaded;

    static native boolean filter(Bitmap bitmap, String str, float f, float f2, float f3, float f4);

    static native boolean grain(Bitmap bitmap, String str, String str2);

    static synchronized void load(String str) {
        if (!loaded) {
            System.load(str + "/libmadridlegend.so");
            loaded = true;
        }
    }

    public static synchronized byte[] process(Context context, byte[] bArr, String str, CaptureResult captureResult) {
        String str2;
        Bitmap bitmapDecodeByteArray;
        Float f;
        if ("M3".equals(str)) {
            str2 = "leica_filter_param_m3.bin";
        } else if ("M9".equals(str)) {
            str2 = "leica_filter_param_m9_snapshot.bin";
        } else {
            if (!"M10R".equals(str)) {
                return bArr;
            }
            str2 = "leica_filter_param_m10r_snapshot.bin";
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        if (bArr != null) {
            try {
                if (bArr.length >= 4) {
                    String str3 = context.getApplicationInfo().nativeLibraryDir;
                    load(str3);
                    File fileParameters = parameters(context, str2);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    boolean zGrain = false;
                    BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                    if (options.outWidth > 0 && options.outHeight > 0 && ((long) options.outWidth) * ((long) options.outHeight) <= 50000000) {
                        options.inJustDecodeBounds = false;
                        options.inMutable = true;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
                        bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                        if (bitmapDecodeByteArray == null) {
                            if (bitmapDecodeByteArray != null) {
                                bitmapDecodeByteArray.recycle();
                            }
                            return bArr;
                        }
                        try {
                            float fNumber = number(captureResult, false);
                            float fNumber2 = number(captureResult, true);
                            boolean zIsNaN = Float.isNaN(fNumber);
                            boolean zIsNaN2 = Float.isNaN(fNumber2);
                            if (zIsNaN) {
                                fNumber = 200.0f;
                            }
                            if (zIsNaN2) {
                                fNumber2 = 5500.0f;
                            }
                            float fMax = (captureResult == null || (f = (Float) captureResult.get(CaptureResult.CONTROL_ZOOM_RATIO)) == null || !Float.isFinite(f.floatValue())) ? 1.0f : Math.max(0.1f, Math.min(200.0f, f.floatValue()));
                            float fMax2 = Math.max(0.0f, Math.min(1000.0f, fNumber));
                            float fMax3 = Math.max(1500.0f, Math.min(15000.0f, fNumber2));
                            if (!filter(bitmapDecodeByteArray, fileParameters.getAbsolutePath(), fMax2, fMax3, fMax, 0.0f)) {
                                throw new IOException("Madrid LUT/shading render failed: " + str);
                            }
                            if ("M3".equals(str) && !(zGrain = grain(bitmapDecodeByteArray, str3 + "/libmonopan_runner.so", context.getCacheDir().getAbsolutePath()))) {
                                ColorMatrix colorMatrix = new ColorMatrix();
                                colorMatrix.setSaturation(0.0f);
                                Paint paint = new Paint();
                                paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                                new Canvas(bitmapDecodeByteArray).drawBitmap(bitmapDecodeByteArray, 0.0f, 0.0f, paint);
                                Log.w("MadridLegend", "M3 grain unavailable; using grayscale fallback");
                            }
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                            if (!bitmapDecodeByteArray.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                                if (bitmapDecodeByteArray != null) {
                                    bitmapDecodeByteArray.recycle();
                                }
                                return bArr;
                            }
                            byte[] bArrCopyExif = copyExif(bArr, byteArrayOutputStream.toByteArray());
                            Log.i("MadridLegend", "saved style=" + str + " stockParameters=true grain=" + zGrain + " lux=" + fMax2 + " defaultLux=" + zIsNaN + " cct=" + fMax3 + " defaultCct=" + zIsNaN2 + " zoom=" + fMax + " size=" + bitmapDecodeByteArray.getWidth() + "x" + bitmapDecodeByteArray.getHeight() + " ms=" + (SystemClock.elapsedRealtime() - jElapsedRealtime));
                            if (bitmapDecodeByteArray != null) {
                                bitmapDecodeByteArray.recycle();
                            }
                            return bArrCopyExif;
                        } catch (Throwable th) {
                            th = th;
                        }
                    }
                    return bArr;
                }
            } catch (Throwable th2) {
                th = th2;
                bitmapDecodeByteArray = null;
            }
            try {
                Log.e("MadridLegend", "Style rendering failed; preserving source JPEG", th);
                if (bitmapDecodeByteArray != null) {
                    bitmapDecodeByteArray.recycle();
                }
                return bArr;
            } catch (Throwable th3) {
                if (bitmapDecodeByteArray != null) {
                    bitmapDecodeByteArray.recycle();
                }
                throw th3;
            }
        }
        return bArr;
    }

    static byte[] copyExif(byte[] bArr, byte[] bArr2) {
        int i;
        int i2;
        if (bArr.length < 4 || bArr2.length < 4 || (bArr[0] & 255) != 255 || (bArr[1] & 255) != 216) {
            return bArr2;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr2.length + 8192);
        byteArrayOutputStream.write(bArr2, 0, 2);
        int i3 = 2;
        while (true) {
            int i4 = i3 + 4;
            if (i4 > bArr.length || (bArr[i3] & 255) != 255 || (i = bArr[i3 + 1] & 255) == 218 || i == 217 || (i2 = ((bArr[i3 + 2] & 255) << 8) | (bArr[i3 + 3] & 255)) < 2 || i3 + i2 + 2 > bArr.length) {
                break;
            }
            if (i == 225 && i2 >= 8 && bArr[i4] == 69 && bArr[i3 + 5] == 120 && bArr[i3 + 6] == 105 && bArr[i3 + 7] == 102 && bArr[i3 + 8] == 0 && bArr[i3 + 9] == 0) {
                byteArrayOutputStream.write(bArr, i3, i2 + 2);
            }
            i3 += i2 + 2;
        }
        byteArrayOutputStream.write(bArr2, 2, bArr2.length - 2);
        return byteArrayOutputStream.toByteArray();
    }

    /* JADX WARN: Code duplicated, block: B:46:0x00a3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private static File parameters(Context context, String str) throws IOException {
        File file = new File(context.getFilesDir(), "madrid-legend18-v1");
        if (!file.isDirectory() && !file.mkdirs()) {
            throw new IOException("Cannot create parameter directory");
        }
        File file2 = new File(file, str);
        if (file2.isFile() && file2.length() > 0) {
            return file2;
        }
        File file3 = new File(file, str + ".tmp");
        InputStream inputStreamOpen = context.getAssets().open("legend18/" + str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file3);
            try {
                byte[] bArr = new byte[32768];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, i);
                    if (inputStreamOpen != null) {
                        try {
                            inputStreamOpen.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                    }
                    throw th;
                }
                fileOutputStream.close();
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
                if (!file3.renameTo(file2)) {
                    throw new IOException("Cannot publish parameters");
                }
                return file2;
            } catch (Throwable th2) {
                try {
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                }
                throw th2;
            }
        } catch (Throwable th4) {
            if (inputStreamOpen != null) {
                inputStreamOpen.close();
            }
            throw th4;
        }
    }

    public static float number(CaptureResult captureResult, boolean z) {
        if (captureResult != null) {
            try {
                for (CaptureResult.Key<?> key : captureResult.getKeys()) {
                    String name = key.getName();
                    boolean z2 = false;
                    if (z) {
                        if (name.equals("org.quic.camera2.statsconfigs.AWBFrameControlCCT") || name.equals("org.quic.camera2.statsconfigs.AWBFrameControl") || name.equals("com.qti.stats.internal.perFrame.frameControl.AWBFrameControl") || name.equals("xiaomi.camera.awb.colorTemperature")) {
                            z2 = true;
                        }
                    } else if (name.equals("com.xiaomi.mivi2.luxIndex") || name.equals("org.quic.camera2.statsconfigs.AECLuxIndex")) {
                        z2 = true;
                    }
                    if (z2) {
                        float fValue = value(captureResult.get(key), z);
                        if (Float.isFinite(fValue)) {
                            return fValue;
                        }
                    }
                }
                return Float.NaN;
            } catch (RuntimeException e) {
                return Float.NaN;
            }
        }
        return Float.NaN;
    }

    static float value(Object obj, boolean z) {
        if (z && (obj instanceof byte[])) {
            byte[] bArr = (byte[]) obj;
            if (bArr.length >= 16) {
                return ByteBuffer.wrap(bArr).order(ByteOrder.nativeOrder()).getInt(12);
            }
        }
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        if (obj instanceof float[]) {
            float[] fArr = (float[]) obj;
            if (fArr.length > 0) {
                return fArr[0];
            }
        }
        if (obj instanceof int[]) {
            int[] iArr = (int[]) obj;
            if (iArr.length > 0) {
                return iArr[0];
            }
        }
        return Float.NaN;
    }
}
