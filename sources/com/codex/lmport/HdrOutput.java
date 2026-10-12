package com.codex.lmport;

import ai.onnxruntime.TensorInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Gainmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes16.dex */
public final class HdrOutput {
    private static final ThreadLocal<String> STATE = new ThreadLocal<>();
    private static final String TAG = "LMomentHDR-R1";

    private HdrOutput() {
    }

    public static void transfer(Bitmap bitmap, Bitmap bitmap2, byte[] bArr, boolean z) throws Throwable {
        STATE.set("api=" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT < 34 || bitmap2 == null) {
            return;
        }
        try {
            Api34.transfer(bitmap, bitmap2, bArr, z);
        } catch (LinkageError e) {
            Log.w(TAG, "Gain map API unavailable", e);
        } catch (OutOfMemoryError e2) {
            Log.w(TAG, "Gain map allocation failed", e2);
        } catch (RuntimeException e3) {
            Log.w(TAG, "Gain map transfer failed; retry at output", e3);
        }
    }

    public static void finish(Bitmap bitmap, Bitmap bitmap2, int i) {
        if (i < 1 || i > 7 || Build.VERSION.SDK_INT < 34 || bitmap2 == null) {
            return;
        }
        try {
            Api34.finish(bitmap, bitmap2, i == 1);
        } catch (LinkageError e) {
            Log.w(TAG, "Output HDR API unavailable", e);
        } catch (OutOfMemoryError e2) {
            Log.w(TAG, "Output HDR allocation failed; keeping styled SDR", e2);
        } catch (RuntimeException e3) {
            Log.w(TAG, "Output HDR failed; keeping styled SDR", e3);
        }
    }

    public static void verify(Context context, byte[] bArr, int i) {
        if (i < 1 || i > 7) {
            return;
        }
        try {
            try {
                try {
                    String str = String.valueOf("style=" + i + " " + STATE.get()) + (Build.VERSION.SDK_INT >= 34 ? Api34.inspect(bArr) : " output=SDR(api<34)");
                    Log.i(TAG, str);
                    if (context != null) {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(context.getFilesDir(), "lmoment-hdr-last.txt"));
                        try {
                            fileOutputStream.write((String.valueOf(str) + "\n").getBytes("UTF-8"));
                            fileOutputStream.close();
                        } catch (Throwable th) {
                            fileOutputStream.close();
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    STATE.remove();
                    throw th2;
                }
            } catch (OutOfMemoryError e) {
                Log.w(TAG, "HDR verification allocation failed", e);
            }
        } catch (Exception e2) {
            Log.w(TAG, "HDR verification failed", e2);
        } catch (LinkageError e3) {
            Log.w(TAG, "HDR verification API unavailable", e3);
        }
        STATE.remove();
    }

    private static final class Api34 {
        private static final int EDGE = 384;

        private Api34() {
        }

        static void state(String str) {
            HdrOutput.STATE.set(str);
            Log.i(HdrOutput.TAG, str);
        }

        static boolean usable(Bitmap bitmap) {
            return (bitmap == null || bitmap.isRecycled()) ? false : true;
        }

        static boolean aspect(int i, int i2, int i3, int i4) {
            double d = ((double) i) / ((double) i2);
            return Math.abs(d - (((double) i3) / ((double) i4))) <= d * 0.025d;
        }

        static Bitmap small(Bitmap bitmap, int i) {
            float fMin = Math.min(1.0f, i / Math.max(bitmap.getWidth(), bitmap.getHeight()));
            return Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * fMin)), Math.max(1, Math.round(bitmap.getHeight() * fMin)), true);
        }

        static void recycle(Bitmap bitmap, Bitmap bitmap2) {
            if (bitmap == null || bitmap == bitmap2) {
                return;
            }
            bitmap.recycle();
        }

        static void attach(Bitmap bitmap, Bitmap bitmap2, boolean z) {
            Gainmap gainmap = new Gainmap(bitmap2);
            float fLimit = HdrMath.limit(z);
            gainmap.setRatioMin(1.0f, 1.0f, 1.0f);
            gainmap.setRatioMax(fLimit, fLimit, fLimit);
            gainmap.setGamma(1.0f, 1.0f, 1.0f);
            gainmap.setEpsilonSdr(0.0f, 0.0f, 0.0f);
            gainmap.setEpsilonHdr(0.0f, 0.0f, 0.0f);
            gainmap.setMinDisplayRatioForHdrTransition(1.0f);
            gainmap.setDisplayRatioForFullHdr(fLimit);
            bitmap.setGainmap(gainmap);
        }

        static void generate(Bitmap bitmap, boolean z) {
            Bitmap bitmapSmall = small(bitmap, EDGE);
            try {
                int width = bitmapSmall.getWidth();
                int height = bitmapSmall.getHeight();
                int i = width * height;
                int[] iArr = new int[i];
                bitmapSmall.getPixels(iArr, 0, width, 0, 0, width, height);
                for (int i2 = 0; i2 < i; i2++) {
                    iArr[i2] = HdrMath.generated(iArr[i2]);
                }
                attach(bitmap, Bitmap.createBitmap(iArr, width, height, Bitmap.Config.ARGB_8888), z);
                state("source=SDR display-gain-generated max=" + HdrMath.limit(z));
            } finally {
                recycle(bitmapSmall, bitmap);
            }
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        static Matrix orientation(int i) {
            Matrix matrix = new Matrix();
            switch (i) {
                case 2:
                    matrix.setScale(-1.0f, 1.0f);
                    return matrix;
                case 3:
                    matrix.setRotate(180.0f);
                    return matrix;
                case 4:
                    matrix.setRotate(180.0f);
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case 5:
                    matrix.setRotate(90.0f);
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case 6:
                    matrix.setRotate(90.0f);
                    return matrix;
                case 7:
                    matrix.setRotate(270.0f);
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    matrix.setRotate(270.0f);
                    return matrix;
                default:
                    return matrix;
            }
        }

        /* JADX WARN: Code duplicated, block: B:81:0x01ce A[PHI: r11 r12
          0x01ce: PHI (r11v6 android.graphics.Bitmap) = (r11v7 android.graphics.Bitmap), (r11v10 android.graphics.Bitmap) binds: [B:80:0x01cc, B:53:0x0170] A[DONT_GENERATE, DONT_INLINE]
          0x01ce: PHI (r12v5 android.graphics.Bitmap) = (r12v6 android.graphics.Bitmap), (r12v13 android.graphics.Bitmap) binds: [B:80:0x01cc, B:53:0x0170] A[DONT_GENERATE, DONT_INLINE]] */
        static void transfer(Bitmap bitmap, Bitmap bitmap2, byte[] bArr, boolean z) throws Throwable {
            int attributeInt;
            Bitmap bitmapCreateBitmap;
            Bitmap bitmapSmall;
            String str = HdrOutput.TAG;
            if (!usable(bitmap) || !bitmap.hasGainmap()) {
                generate(bitmap2, z);
                return;
            }
            Gainmap gainmap = bitmap.getGainmap();
            Bitmap gainmapContents = gainmap.getGainmapContents();
            if (!usable(gainmapContents)) {
                generate(bitmap2, z);
                return;
            }
            Bitmap bitmapCreateScaledBitmap = null;
            try {
                try {
                    attributeInt = new ExifInterface(new ByteArrayInputStream(bArr)).getAttributeInt("Orientation", 1);
                } catch (Throwable th) {
                    th = th;
                    bitmapCreateBitmap = null;
                    bitmapSmall = null;
                    recycle(bitmapCreateScaledBitmap, bitmap2);
                    if (bitmapSmall != null && bitmapSmall != bitmapCreateBitmap) {
                        bitmapSmall.recycle();
                    }
                    recycle(bitmapCreateBitmap, gainmapContents);
                    throw th;
                }
            } catch (RuntimeException e) {
                e = e;
                bitmapCreateBitmap = null;
                bitmapSmall = null;
            } catch (Exception e2) {
                try {
                    Log.w(HdrOutput.TAG, "Cannot read source orientation", e2);
                    attributeInt = 1;
                } catch (RuntimeException e3) {
                    e = e3;
                    str = HdrOutput.TAG;
                    bitmapCreateBitmap = null;
                    bitmapSmall = null;
                }
            }
            bitmapCreateBitmap = Bitmap.createBitmap(gainmapContents, 0, 0, gainmapContents.getWidth(), gainmapContents.getHeight(), orientation(attributeInt), false);
            try {
                try {
                    if (aspect(bitmap2.getWidth(), bitmap2.getHeight(), bitmapCreateBitmap.getWidth(), bitmapCreateBitmap.getHeight())) {
                        bitmapSmall = small(bitmapCreateBitmap, EDGE);
                        try {
                            try {
                                bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmap2, bitmapSmall.getWidth(), bitmapSmall.getHeight(), true);
                                try {
                                    int width = bitmapSmall.getWidth();
                                    int height = bitmapSmall.getHeight();
                                    int i = width * height;
                                    int[] iArr = new int[i];
                                    int[] iArr2 = new int[i];
                                    bitmapSmall.getPixels(iArr, 0, width, 0, 0, width, height);
                                    int[] iArr3 = iArr;
                                    try {
                                        try {
                                            bitmapCreateScaledBitmap.getPixels(iArr2, 0, width, 0, 0, width, height);
                                            float[] ratioMin = gainmap.getRatioMin();
                                            float[] ratioMax = gainmap.getRatioMax();
                                            float[] gamma = gainmap.getGamma();
                                            boolean z2 = gainmapContents.getConfig() == Bitmap.Config.ALPHA_8;
                                            int i2 = 0;
                                            int iMax = 0;
                                            while (i2 < i) {
                                                int i3 = i2;
                                                try {
                                                    int i4 = iArr3[i3];
                                                    int[] iArr4 = iArr3;
                                                    int i5 = iArr2[i3];
                                                    int i6 = i;
                                                    float[] fArr = ratioMax;
                                                    float[] fArr2 = gamma;
                                                    iArr4[i3] = HdrMath.source(i4, z2, ratioMin, fArr, fArr2, i5, z);
                                                    iMax = Math.max(iMax, iArr4[i3] & 255);
                                                    i2 = i3 + 1;
                                                    gamma = fArr2;
                                                    iArr3 = iArr4;
                                                    ratioMax = fArr;
                                                    i = i6;
                                                } catch (RuntimeException e4) {
                                                    e = e4;
                                                    bitmapCreateScaledBitmap = bitmapCreateScaledBitmap;
                                                    Log.w(str, "Invalid source gain map; generating display gain", e);
                                                    generate(bitmap2, z);
                                                    recycle(bitmapCreateScaledBitmap, bitmap2);
                                                    if (bitmapSmall != null) {
                                                        bitmapSmall.recycle();
                                                    }
                                                    recycle(bitmapCreateBitmap, gainmapContents);
                                                    return;
                                                }
                                            }
                                            if (iMax < 2) {
                                                generate(bitmap2, z);
                                                state(String.valueOf((String) HdrOutput.STATE.get()) + " source-map-rejected=neutral");
                                                recycle(bitmapCreateScaledBitmap, bitmap2);
                                                if (bitmapSmall != null && bitmapSmall != bitmapCreateBitmap) {
                                                    bitmapSmall.recycle();
                                                }
                                                recycle(bitmapCreateBitmap, gainmapContents);
                                                return;
                                            }
                                            attach(bitmap2, Bitmap.createBitmap(iArr3, width, height, Bitmap.Config.ARGB_8888), z);
                                            state("source=HDR scalar-adapted orientation=" + attributeInt + " max=" + HdrMath.limit(z));
                                            recycle(bitmapCreateScaledBitmap, bitmap2);
                                            if (bitmapSmall != null && bitmapSmall != bitmapCreateBitmap) {
                                                bitmapSmall.recycle();
                                            }
                                            recycle(bitmapCreateBitmap, gainmapContents);
                                            return;
                                        } catch (RuntimeException e5) {
                                            e = e5;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        bitmapCreateScaledBitmap = bitmapCreateScaledBitmap;
                                        recycle(bitmapCreateScaledBitmap, bitmap2);
                                        if (bitmapSmall != null) {
                                            bitmapSmall.recycle();
                                        }
                                        recycle(bitmapCreateBitmap, gainmapContents);
                                        throw th;
                                    }
                                } catch (RuntimeException e6) {
                                    e = e6;
                                    str = HdrOutput.TAG;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            } catch (RuntimeException e7) {
                                e = e7;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                        }
                    } else {
                        try {
                            generate(bitmap2, z);
                            state(String.valueOf((String) HdrOutput.STATE.get()) + " source-map-rejected=aspect");
                            recycle(null, bitmap2);
                            recycle(bitmapCreateBitmap, gainmapContents);
                            return;
                        } catch (RuntimeException e8) {
                            e = e8;
                            bitmapSmall = null;
                        }
                    }
                } catch (RuntimeException e9) {
                    e = e9;
                    str = HdrOutput.TAG;
                    bitmapSmall = null;
                }
                if (bitmapSmall != null && bitmapSmall != bitmapCreateBitmap) {
                    bitmapSmall.recycle();
                }
                recycle(bitmapCreateBitmap, gainmapContents);
                return;
            } catch (Throwable th5) {
                th = th5;
                bitmapSmall = null;
            }
            str = HdrOutput.TAG;
            Log.w(str, "Invalid source gain map; generating display gain", e);
            generate(bitmap2, z);
            recycle(bitmapCreateScaledBitmap, bitmap2);
        }

        static Bitmap map(Bitmap bitmap) {
            if (!usable(bitmap) || !bitmap.hasGainmap()) {
                return null;
            }
            Bitmap gainmapContents = bitmap.getGainmap().getGainmapContents();
            if (usable(gainmapContents) && aspect(bitmap.getWidth(), bitmap.getHeight(), gainmapContents.getWidth(), gainmapContents.getHeight())) {
                return gainmapContents;
            }
            return null;
        }

        static void finish(Bitmap bitmap, Bitmap bitmap2, boolean z) {
            boolean z2 = z;
            if (!usable(bitmap)) {
                if (map(bitmap2) == null) {
                    bitmap2.setGainmap(null);
                }
                state(String.valueOf((String) HdrOutput.STATE.get()) + " watermark=photo-unavailable");
                return;
            }
            if (map(bitmap) == null) {
                generate(bitmap, z2);
            }
            if (bitmap == bitmap2) {
                return;
            }
            Bitmap bitmapSmall = small(bitmap, 192);
            Bitmap bitmapSmall2 = small(bitmap2, 192);
            try {
                int i = 0;
                Placement placementFind = Placement.find(bitmapSmall, bitmapSmall2, bitmap.getWidth() == bitmap2.getWidth() && bitmap.getHeight() == bitmap2.getHeight());
                if (placementFind == null) {
                    if (map(bitmap2) != null) {
                        state(String.valueOf((String) HdrOutput.STATE.get()) + " watermark=native-carrier geometry=unmatched");
                        recycle(bitmapSmall, bitmap);
                        recycle(bitmapSmall2, bitmap2);
                        return;
                    } else {
                        bitmap2.setGainmap(null);
                        state(String.valueOf((String) HdrOutput.STATE.get()) + " output=SDR watermark-geometry-unmatched");
                        recycle(bitmapSmall, bitmap);
                        recycle(bitmapSmall2, bitmap2);
                        return;
                    }
                }
                Bitmap gainmapContents = bitmap.getGainmap().getGainmapContents();
                int width = bitmapSmall2.getWidth();
                int height = bitmapSmall2.getHeight();
                int i2 = width * height;
                int[] iArr = new int[i2];
                boolean[] zArr = new boolean[i2];
                int i3 = 0;
                while (i3 < height) {
                    int i4 = 0;
                    while (i4 < width) {
                        int i5 = (i3 * width) + i4;
                        float f = ((i4 + 0.5f) - placementFind.x) / placementFind.w;
                        float f2 = ((i3 + 0.5f) - placementFind.y) / placementFind.h;
                        if (f < 0.0f || f2 < 0.0f || f >= 1.0f || f2 >= 1.0f) {
                            iArr[i5] = -16777216;
                        } else {
                            placementFind = placementFind;
                            zArr[i5] = HdrMath.difference(pixel(bitmapSmall, f, f2), bitmapSmall2.getPixel(i4, i3)) > 32;
                            iArr[i5] = pixel(gainmapContents, f, f2);
                        }
                        i4++;
                        z2 = z;
                        placementFind = placementFind;
                    }
                    i3++;
                    i = 0;
                }
                int i6 = i;
                int i7 = i6;
                while (i6 < height) {
                    int i8 = i;
                    while (i8 < width) {
                        if (zArr[(i6 * width) + i8]) {
                            i7++;
                            int iMax = Math.max(i, i6 - 1);
                            while (iMax <= Math.min(height - 1, i6 + 1)) {
                                for (int iMax2 = Math.max(i, i8 - 1); iMax2 <= Math.min(width - 1, i8 + 1); iMax2++) {
                                    iArr[(iMax * width) + iMax2] = -16777216;
                                }
                                iMax++;
                                i = 0;
                            }
                        }
                        i8++;
                        i = 0;
                    }
                    i6++;
                }
                attach(bitmap2, Bitmap.createBitmap(iArr, width, height, Bitmap.Config.ARGB_8888), z2);
                state(String.valueOf((String) HdrOutput.STATE.get()) + " watermark=aligned-neutral overlay-texels=" + i7);
                recycle(bitmapSmall, bitmap);
                recycle(bitmapSmall2, bitmap2);
            } catch (Throwable th) {
                recycle(bitmapSmall, bitmap);
                recycle(bitmapSmall2, bitmap2);
                throw th;
            }
        }

        static int pixel(Bitmap bitmap, float f, float f2) {
            float fClamp = HdrMath.clamp((f * bitmap.getWidth()) - 0.5f, 0.0f, bitmap.getWidth() - 1);
            float fClamp2 = HdrMath.clamp((f2 * bitmap.getHeight()) - 0.5f, 0.0f, bitmap.getHeight() - 1);
            int i = (int) fClamp;
            int i2 = (int) fClamp2;
            int iMin = Math.min(i + 1, bitmap.getWidth() - 1);
            int iMin2 = Math.min(i2 + 1, bitmap.getHeight() - 1);
            float f3 = fClamp - i;
            float f4 = fClamp2 - i2;
            int pixel = bitmap.getPixel(i, i2);
            int pixel2 = bitmap.getPixel(iMin, i2);
            int pixel3 = bitmap.getPixel(i, iMin2);
            int pixel4 = bitmap.getPixel(iMin, iMin2);
            int iRound = -16777216;
            for (int i3 = 0; i3 <= 16; i3 += 8) {
                float f5 = 1.0f - f3;
                iRound |= Math.round((((((pixel >>> i3) & 255) * f5) + (((pixel2 >>> i3) & 255) * f3)) * (1.0f - f4)) + (((((pixel3 >>> i3) & 255) * f5) + (((pixel4 >>> i3) & 255) * f3)) * f4)) << i3;
            }
            return iRound;
        }

        static String inspect(byte[] bArr) {
            if (bArr == null) {
                return " output=missing";
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 64;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
            if (bitmapDecodeByteArray == null) {
                return " output=decode-failed";
            }
            try {
                Bitmap map = map(bitmapDecodeByteArray);
                if (map != null) {
                    float[] ratioMax = bitmapDecodeByteArray.getGainmap().getRatioMax();
                    return " output=UltraHDR bytes=" + bArr.length + " map=" + map.getWidth() + "x" + map.getHeight() + " ratioMax=" + ratioMax[0] + "," + ratioMax[1] + "," + ratioMax[2];
                }
                return " output=SDR(no-valid-gainmap)";
            } finally {
                bitmapDecodeByteArray.recycle();
            }
        }

        private static final class Placement {
            float error;
            float h;
            float w;
            float x;
            float y;

            Placement(float f, float f2, float f3, float f4) {
                this.x = f;
                this.y = f2;
                this.w = f3;
                this.h = f4;
            }

            static int lowFrequency(Bitmap bitmap, float f, float f2) {
                int i = 0;
                int i2 = 0;
                int i3 = 0;
                for (int i4 = -1; i4 <= 1; i4++) {
                    for (int i5 = -1; i5 <= 1; i5++) {
                        int iPixel = Api34.pixel(bitmap, ((i5 * 2.0f) / bitmap.getWidth()) + f, ((i4 * 2.0f) / bitmap.getHeight()) + f2);
                        i += (iPixel >>> 16) & 255;
                        i2 += (iPixel >>> 8) & 255;
                        i3 += iPixel & 255;
                    }
                }
                return ((i / 9) << 16) | (-16777216) | ((i2 / 9) << 8) | (i3 / 9);
            }

            static float score(Bitmap bitmap, Bitmap bitmap2, Placement placement) {
                return score(bitmap, bitmap2, placement, true);
            }

            static float score(Bitmap bitmap, Bitmap bitmap2, Placement placement, boolean z) {
                if (placement.x < -0.5f || placement.y < -0.5f || placement.x + placement.w > bitmap2.getWidth() + 0.5f || placement.y + placement.h > bitmap2.getHeight() + 0.5f) {
                    return 255.0f;
                }
                float fDifference = 0.0f;
                for (int i = 1; i <= 7; i++) {
                    for (int i2 = 1; i2 <= 7; i2++) {
                        float f = i2 / 8.0f;
                        float f2 = i / 8.0f;
                        float width = (placement.x + (placement.w * f)) / bitmap2.getWidth();
                        float height = (placement.y + (placement.h * f2)) / bitmap2.getHeight();
                        fDifference += HdrMath.difference(z ? lowFrequency(bitmap, f, f2) : Api34.pixel(bitmap, f, f2), z ? lowFrequency(bitmap2, width, height) : Api34.pixel(bitmap2, width, height));
                    }
                }
                return fDifference / 49.0f;
            }

            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r6v16 */
            /* JADX WARN: Type inference failed for: r6v3 */
            /* JADX WARN: Type inference failed for: r6v4, types: [int] */
            /* JADX WARN: Type inference failed for: r8v0 */
            /* JADX WARN: Type inference failed for: r8v1, types: [int] */
            /* JADX WARN: Type inference failed for: r8v6 */
            static Placement find(Bitmap bitmap, Bitmap bitmap2, boolean z) {
                Placement placement;
                float f;
                int i;
                Placement placement2;
                if (z) {
                    return new Placement(0.0f, 0.0f, bitmap2.getWidth(), bitmap2.getHeight());
                }
                float fMin = Math.min(bitmap2.getWidth() / bitmap.getWidth(), bitmap2.getHeight() / bitmap.getHeight());
                Placement placement3 = null;
                boolean z2 = false;
                Placement placement4 = null;
                int i2 = 0;
                while (i2 <= 10) {
                    float f2 = (1.0f - (i2 * 0.04f)) * fMin;
                    float width = bitmap.getWidth() * f2;
                    float height = bitmap.getHeight() * f2;
                    for (int i3 = 0; i3 <= 4; i3++) {
                        for (int i4 = 0; i4 <= 4; i4++) {
                            Placement placement5 = new Placement(((bitmap2.getWidth() - width) * i3) / 4.0f, ((bitmap2.getHeight() - height) * i4) / 4.0f, width, height);
                            placement5.error = score(bitmap, bitmap2, placement5);
                            if (placement4 == null || placement5.error < placement4.error) {
                                placement4 = placement5;
                            }
                        }
                    }
                    i2++;
                    placement3 = null;
                    z2 = false;
                }
                float fMax = Math.max(bitmap2.getWidth(), bitmap2.getHeight()) * 0.02f;
                ?? r8 = z2;
                while (true) {
                    placement = placement4;
                    f = 0.5f;
                    i = 1;
                    if (r8 >= 5) {
                        break;
                    }
                    placement4 = placement;
                    for (int i5 = -1; i5 <= 1; i5++) {
                        for (int i6 = -1; i6 <= 1; i6++) {
                            int i7 = -1;
                            while (i7 <= 1) {
                                float f3 = placement.w + (i5 * fMax);
                                Placement placement6 = new Placement(placement.x + (i6 * fMax), placement.y + (i7 * fMax), f3, (bitmap.getHeight() * f3) / bitmap.getWidth());
                                placement6.error = score(bitmap, bitmap2, placement6);
                                if (placement6.error < placement4.error) {
                                    placement4 = placement6;
                                }
                                i7++;
                                z2 = false;
                            }
                        }
                    }
                    fMax *= 0.5f;
                    placement3 = null;
                    r8++;
                }
                float f4 = 14.0f;
                if (placement.error > 14.0f) {
                    return placement3;
                }
                placement.error = score(bitmap, bitmap2, placement, z2);
                float f5 = 1.5f;
                ?? r6 = z2;
                while (true) {
                    placement2 = placement;
                    if (r6 >= 8) {
                        break;
                    }
                    placement = placement2;
                    for (int i8 = -1; i8 <= i; i8++) {
                        for (int i9 = -1; i9 <= i; i9++) {
                            int i10 = -1;
                            while (i10 <= i) {
                                float f6 = placement2.w + (i8 * f5);
                                float f7 = f;
                                Placement placement7 = new Placement(placement2.x + (i9 * f5), placement2.y + (i10 * f5), f6, (bitmap.getHeight() * f6) / bitmap.getWidth());
                                placement7.error = score(bitmap, bitmap2, placement7, z2);
                                if (placement7.error < placement.error) {
                                    placement = placement7;
                                }
                                i10++;
                                f = f7;
                                placement3 = null;
                                f4 = 14.0f;
                                i = 1;
                            }
                        }
                    }
                    f5 *= f;
                    r6++;
                }
                Log.i(HdrOutput.TAG, "watermark match error=" + placement2.error + " rect=" + placement2.x + "," + placement2.y + "," + placement2.w + "," + placement2.h);
                return placement2.error <= f4 ? placement2 : placement3;
            }
        }
    }
}
