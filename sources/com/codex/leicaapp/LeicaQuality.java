package com.codex.leicaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LeicaQuality {
    private static final boolean loaded;

    public static void apply(Bitmap bitmap, byte[] bArr, boolean z, Context context) {
        if (z || !loaded || context == null || bitmap == null || bitmap.isRecycled() || !bitmap.isMutable() || bArr == null) {
            return;
        }
        Bitmap bitmapDecodeByteArray = null;
        try {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inPreferredColorSpace = bitmap.getColorSpace();
                bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                if (bitmapDecodeByteArray != null && bitmapDecodeByteArray.getWidth() == bitmap.getWidth() && bitmapDecodeByteArray.getHeight() == bitmap.getHeight() && bitmap.getColorSpace().equals(bitmapDecodeByteArray.getColorSpace())) {
                    finish(bitmapDecodeByteArray, bitmap, null, -52, 0, 0);
                    applyPerson(bitmapDecodeByteArray, bitmap, new ExifInterface(new ByteArrayInputStream(bArr)).getAttributeInt("Orientation", 1), context.getApplicationInfo().nativeLibraryDir, new File(context.getFilesDir(), "leica-v4"), context.getCacheDir());
                    return;
                }
            } catch (IOException | OutOfMemoryError | RuntimeException e) {
                Log.w("LeicaQuality", "Keep source style: person correction unavailable", e);
            }
        } finally {
            if (bitmapDecodeByteArray != null) {
                bitmapDecodeByteArray.recycle();
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:116:0x025b  */
    /* JADX WARN: Code duplicated, block: B:120:0x0266  */
    /* JADX WARN: Code duplicated, block: B:122:0x026b  */
    /* JADX WARN: Code duplicated, block: B:124:0x0270  */
    static boolean applyPerson(Bitmap bitmap, Bitmap bitmap2, int i, String str, File file, File file2) throws Throwable {
        File fileCreateTempFile;
        Process process;
        File fileCreateTempFile2;
        File fileCreateTempFile3;
        int i2;
        int i3;
        int i4 = i == 0 ? 1 : i;
        int i5 = 0;
        if (!loaded || i4 < 1 || i4 > 8) {
            return false;
        }
        Bitmap bitmapUpright = upright(bitmap, i4);
        try {
            int width = bitmapUpright.getWidth();
            int height = bitmapUpright.getHeight();
            int i6 = width * height;
            int[] iArr = new int[i6];
            bitmapUpright.getPixels(iArr, 0, width, 0, 0, width, height);
            byte[] bArr = new byte[i6 * 3];
            for (int i7 = 0; i7 < i6; i7++) {
                int i8 = i7 * 3;
                int i9 = iArr[i7];
                bArr[i8] = (byte) (i9 >> 16);
                bArr[i8 + 1] = (byte) (i9 >> 8);
                bArr[i8 + 2] = (byte) i9;
            }
            fileCreateTempFile2 = File.createTempFile("leica-person-", ".rgb", file2);
            try {
                fileCreateTempFile3 = File.createTempFile("leica-person-", ".mask", file2);
                try {
                    fileCreateTempFile = File.createTempFile("leica-person-", ".log", file2);
                    try {
                        Files.write(fileCreateTempFile2.toPath(), bArr, new OpenOption[0]);
                        ProcessBuilder processBuilderRedirectOutput = new ProcessBuilder(str + "/libperson_runner.so", str, file.getAbsolutePath(), fileCreateTempFile2.getAbsolutePath(), fileCreateTempFile3.getAbsolutePath(), Integer.toString(width), Integer.toString(height)).redirectErrorStream(true).redirectOutput(fileCreateTempFile);
                        processBuilderRedirectOutput.environment().clear();
                        processBuilderRedirectOutput.environment().put("LD_LIBRARY_PATH", str);
                        Process processStart = processBuilderRedirectOutput.start();
                        try {
                            try {
                                if (processStart.waitFor(8L, TimeUnit.SECONDS) && processStart.exitValue() == 0 && fileCreateTempFile3.length() == i6) {
                                    byte[] allBytes = Files.readAllBytes(fileCreateTempFile3.toPath());
                                    byte[] bArr2 = new byte[allBytes.length];
                                    int i10 = 0;
                                    int i11 = 0;
                                    while (i10 < height) {
                                        int i12 = i5;
                                        while (i12 < width) {
                                            int iMax = Math.max(i5, i10 + 0);
                                            int i13 = 255;
                                            while (iMax <= Math.min(height - 1, i10 + 0)) {
                                                int iMax2 = Math.max(0, i12 + 0);
                                                int iMin = i13;
                                                while (true) {
                                                    i2 = i4;
                                                    i3 = height;
                                                    if (iMax2 <= Math.min(width - 1, i12 + 0)) {
                                                        iMin = Math.min(iMin, allBytes[(iMax * width) + iMax2] & 255);
                                                        iMax2++;
                                                        i4 = i2;
                                                        height = i3;
                                                    }
                                                }
                                                iMax++;
                                                i13 = iMin;
                                                i4 = i2;
                                                height = i3;
                                            }
                                            int i14 = height;
                                            int i15 = i4;
                                            int i16 = i13;
                                            if (i16 > 0) {
                                                bArr2[(i10 * width) + i12] = (byte) i16;
                                                if (i16 > 128) {
                                                    i11++;
                                                }
                                            }
                                            i12++;
                                            i4 = i15;
                                            height = i14;
                                            i5 = 0;
                                        }
                                        i10++;
                                        i5 = 0;
                                    }
                                    int i17 = height;
                                    int i18 = i4;
                                    if (i11 == 0) {
                                        Log.i("LeicaQuality", "person pixels=0; source M9 unchanged");
                                        if (processStart != null && processStart.isAlive()) {
                                            processStart.destroyForcibly();
                                        }
                                        if (fileCreateTempFile2 != null) {
                                            fileCreateTempFile2.delete();
                                        }
                                        if (fileCreateTempFile3 != null) {
                                            fileCreateTempFile3.delete();
                                        }
                                        if (fileCreateTempFile != null) {
                                            fileCreateTempFile.delete();
                                        }
                                        bitmapUpright.recycle();
                                        return true;
                                    }
                                    boolean zFinish = finish(bitmap, bitmap2, bArr2, width, i17, i18);
                                    Log.i("LeicaQuality", "person pixels=" + i11 + "/" + allBytes.length + " masked correction=" + zFinish);
                                    if (processStart != null && processStart.isAlive()) {
                                        processStart.destroyForcibly();
                                    }
                                    if (fileCreateTempFile2 != null) {
                                        fileCreateTempFile2.delete();
                                    }
                                    if (fileCreateTempFile3 != null) {
                                        fileCreateTempFile3.delete();
                                    }
                                    if (fileCreateTempFile != null) {
                                        fileCreateTempFile.delete();
                                    }
                                    bitmapUpright.recycle();
                                    return zFinish;
                                }
                                Log.w("LeicaQuality", "Keep source style: person model failed or timed out");
                                if (processStart != null && processStart.isAlive()) {
                                    processStart.destroyForcibly();
                                }
                                if (fileCreateTempFile2 != null) {
                                    fileCreateTempFile2.delete();
                                }
                                if (fileCreateTempFile3 != null) {
                                    fileCreateTempFile3.delete();
                                }
                                if (fileCreateTempFile != null) {
                                    fileCreateTempFile.delete();
                                }
                                bitmapUpright.recycle();
                                return false;
                            } catch (Throwable th) {
                                th = th;
                                process = processStart;
                                if (process != null && process.isAlive()) {
                                    process.destroyForcibly();
                                }
                                if (fileCreateTempFile2 != null) {
                                    fileCreateTempFile2.delete();
                                }
                                if (fileCreateTempFile3 != null) {
                                    fileCreateTempFile3.delete();
                                }
                                if (fileCreateTempFile != null) {
                                    fileCreateTempFile.delete();
                                }
                                bitmapUpright.recycle();
                                throw th;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            if (processStart != null && processStart.isAlive()) {
                                processStart.destroyForcibly();
                            }
                            if (fileCreateTempFile2 != null) {
                                fileCreateTempFile2.delete();
                            }
                            if (fileCreateTempFile3 != null) {
                                fileCreateTempFile3.delete();
                            }
                            if (fileCreateTempFile != null) {
                                fileCreateTempFile.delete();
                            }
                            bitmapUpright.recycle();
                            return false;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        process = null;
                        if (process != null) {
                            process.destroyForcibly();
                        }
                        if (fileCreateTempFile2 != null) {
                            fileCreateTempFile2.delete();
                        }
                        if (fileCreateTempFile3 != null) {
                            fileCreateTempFile3.delete();
                        }
                        if (fileCreateTempFile != null) {
                            fileCreateTempFile.delete();
                        }
                        bitmapUpright.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileCreateTempFile = null;
                }
            } catch (Throwable th4) {
                th = th4;
                fileCreateTempFile = null;
                process = null;
                fileCreateTempFile3 = null;
                if (process != null) {
                    process.destroyForcibly();
                }
                if (fileCreateTempFile2 != null) {
                    fileCreateTempFile2.delete();
                }
                if (fileCreateTempFile3 != null) {
                    fileCreateTempFile3.delete();
                }
                if (fileCreateTempFile != null) {
                    fileCreateTempFile.delete();
                }
                bitmapUpright.recycle();
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            fileCreateTempFile = null;
            process = null;
            fileCreateTempFile2 = null;
        }
    }

    static native boolean finish(Bitmap bitmap, Bitmap bitmap2, byte[] bArr, int i, int i2, int i3);

    static {
        boolean z;
        try {
            System.loadLibrary("leicaquality");
            z = true;
        } catch (LinkageError e) {
            Log.w("LeicaQuality", "Keep source style: library unavailable", e);
            z = false;
        }
        loaded = z;
    }

    static Bitmap upright(Bitmap bitmap, int i) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float f = width;
        float f2 = height;
        float[][] fArr = {new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f}, new float[]{-1.0f, 0.0f, f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f}, new float[]{-1.0f, 0.0f, f, 0.0f, -1.0f, f2, 0.0f, 0.0f, 1.0f}, new float[]{1.0f, 0.0f, 0.0f, 0.0f, -1.0f, f2, 0.0f, 0.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}, new float[]{0.0f, -1.0f, f2, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}, new float[]{0.0f, -1.0f, f2, -1.0f, 0.0f, f, 0.0f, 0.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, -1.0f, 0.0f, f, 0.0f, 0.0f, 1.0f}};
        int i2 = i >= 5 ? height : width;
        if (i < 5) {
            width = height;
        }
        float fMin = Math.min(1.0f, 768.0f / Math.max(i2, width));
        float f3 = i2;
        int iMax = Math.max(1, Math.round(f3 * fMin));
        float f4 = width;
        int iMax2 = Math.max(1, Math.round(fMin * f4));
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iMax, iMax2, Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.setValues(fArr[i - 1]);
        matrix.postScale(iMax / f3, iMax2 / f4);
        new Canvas(bitmapCreateBitmap).drawBitmap(bitmap, matrix, new Paint(2));
        return bitmapCreateBitmap;
    }
}
