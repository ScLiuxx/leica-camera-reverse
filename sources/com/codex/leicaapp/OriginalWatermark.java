package com.codex.leicaapp;

import ai.onnxruntime.TensorInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Gainmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.codex.fullm9.CaptureSnapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OriginalWatermark {
    public static byte[] processJpeg(byte[] bArr, Object obj) throws Throwable {
        Bitmap bitmapDecodeByteArray;
        Bitmap bitmap = null;
        try {
            if (!Boolean.TRUE.equals(field(field(obj, "l"), "e"))) {
                return bArr;
            }
            bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
            if (bitmapDecodeByteArray == null) {
                if (bitmapDecodeByteArray != null) {
                    bitmapDecodeByteArray.recycle();
                }
                return bArr;
            }
            try {
                try {
                    Bitmap bitmapApply = apply(bitmapDecodeByteArray, obj, bArr, null);
                    if (bitmapApply == bitmapDecodeByteArray) {
                        if (bitmapApply != null && bitmapApply != bitmapDecodeByteArray) {
                            bitmapApply.recycle();
                        }
                        if (bitmapDecodeByteArray != null) {
                            bitmapDecodeByteArray.recycle();
                        }
                        return bArr;
                    }
                    try {
                        try {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            if (!bitmapApply.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                                if (bitmapApply != null && bitmapApply != bitmapDecodeByteArray) {
                                    bitmapApply.recycle();
                                }
                                if (bitmapDecodeByteArray != null) {
                                    bitmapDecodeByteArray.recycle();
                                }
                                return bArr;
                            }
                            byte[] bArr2 = (byte[]) Class.forName("com.codex.leicaapp.JpegMetadata").getMethod("copyExif", byte[].class, byte[].class, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE).invoke(null, bArr, byteArrayOutputStream.toByteArray(), false, Integer.valueOf(bitmapApply.getWidth()), Integer.valueOf(bitmapApply.getHeight()), true);
                            if (bitmapApply != null && bitmapApply != bitmapDecodeByteArray) {
                                bitmapApply.recycle();
                            }
                            if (bitmapDecodeByteArray != null) {
                                bitmapDecodeByteArray.recycle();
                            }
                            return bArr2;
                        } catch (Exception | OutOfMemoryError e) {
                            e = e;
                            bitmap = bitmapApply;
                        }
                    } catch (Throwable th) {
                        th = th;
                        bitmap = bitmapApply;
                        if (bitmap != null && bitmap != bitmapDecodeByteArray) {
                            bitmap.recycle();
                        }
                        if (bitmapDecodeByteArray != null) {
                            bitmapDecodeByteArray.recycle();
                        }
                        throw th;
                    }
                } catch (Exception | OutOfMemoryError e2) {
                    e = e2;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception | OutOfMemoryError e3) {
            e = e3;
            bitmapDecodeByteArray = null;
        } catch (Throwable th3) {
            th = th3;
            bitmapDecodeByteArray = null;
        }
        Log.e("LegendaryPost", "Watermark unavailable; retaining photo", e);
        if (bitmap != null && bitmap != bitmapDecodeByteArray) {
            bitmap.recycle();
        }
        if (bitmapDecodeByteArray != null) {
            bitmapDecodeByteArray.recycle();
        }
        return bArr;
    }

    public static String model(String str, String str2) {
        if (str == null) {
            return str2;
        }
        if (str.startsWith("1847")) {
            return "18 Pro Max";
        }
        if (str.equals("170046") || str.startsWith("1747")) {
            return "17 Ultra";
        }
        try {
            int i = Integer.parseInt(str);
            return (i < 1 || i > 200) ? str2 : "17 Pro";
        } catch (NumberFormatException e) {
        }
    }

    private static Object field(Object obj, String str) throws ReflectiveOperationException {
        return obj.getClass().getField(str).get(obj);
    }

    private static void put(Object obj, String str, Object obj2) throws ReflectiveOperationException {
        obj.getClass().getField(str).set(obj, obj2);
    }

    /* JADX WARN: Code duplicated, block: B:146:0x047e A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:149:0x0485 A[ADDED_TO_REGION] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0 */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v12 */
    /* JADX WARN: Type inference failed for: r2v13 */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v20 */
    /* JADX WARN: Type inference failed for: r2v26 */
    /* JADX WARN: Type inference failed for: r2v3 */
    /* JADX WARN: Type inference failed for: r2v30 */
    /* JADX WARN: Type inference failed for: r2v4 */
    /* JADX WARN: Type inference failed for: r2v47, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r2v5 */
    /* JADX WARN: Type inference failed for: r2v6, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r2v62 */
    /* JADX WARN: Type inference failed for: r2v77 */
    /* JADX WARN: Type inference failed for: r2v8 */
    /* JADX WARN: Type inference failed for: r34v0, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r34v1, types: [android.graphics.Bitmap] */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 3 */
    static Bitmap apply(Bitmap bitmap, Object obj, byte[] bArr, CaptureResult captureResult) throws Throwable {
        Bitmap bitmap2;
        Bitmap bitmapCreateBitmap;
        Bitmap bitmap3;
        ExifInterface exifInterface;
        String str;
        String str2 = "LegendaryPost";
        if (obj == null) {
            return bitmap;
        }
        try {
            try {
                Object objField = field(obj, "l");
                if (!Boolean.TRUE.equals(field(objField, "e"))) {
                    return bitmap;
                }
                String str3 = (String) field(objField, "w");
                if (str3 == null || str3.isEmpty()) {
                    bitmap = bitmap;
                    try {
                        throw new IllegalStateException("Missing captured watermark ID");
                    } catch (Exception e) {
                        e = e;
                    } catch (Throwable th) {
                        th = th;
                    }
                } else {
                    ExifInterface exifInterface2 = new ExifInterface(new ByteArrayInputStream(bArr));
                    int attributeInt = exifInterface2.getAttributeInt("Orientation", 1);
                    Matrix matrixOrientation = orientation(attributeInt);
                    try {
                        bitmapCreateBitmap = Bitmap.createBitmap((Bitmap) bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrixOrientation, true);
                        try {
                            try {
                                Class<?> cls = Class.forName("qs.b");
                                Class<?> cls2 = Class.forName("zi.a");
                                Object objNewInstance = cls2.getConstructor(Bitmap.class, cls, Integer.TYPE).newInstance(bitmapCreateBitmap, cls.getField("b").get(null), 0);
                                put(objNewInstance, "a", str3);
                                Object objField2 = field(obj, "e");
                                boolean zEquals = Boolean.TRUE.equals(field(objField, "m"));
                                put(objNewInstance, "p", Boolean.valueOf(zEquals));
                                if (zEquals) {
                                    try {
                                        put(objNewInstance, "m", objField2.getClass().getMethod("getLocation", new Class[0]).invoke(objField2, new Object[0]));
                                        put(objNewInstance, "n", objField2.getClass().getMethod("getLocationAddress", new Class[0]).invoke(objField2, new Object[0]));
                                        put(objNewInstance, "o", objField2.getClass().getMethod("getLatlngStringCache", new Class[0]).invoke(objField2, new Object[0]));
                                    } catch (Exception e2) {
                                        e = e2;
                                        bitmap2 = null;
                                        bitmap = bitmap;
                                        str2 = "LegendaryPost";
                                    } catch (Throwable th2) {
                                        th = th2;
                                        bitmap2 = null;
                                        bitmap3 = bitmap;
                                        if (bitmapCreateBitmap != null) {
                                            bitmapCreateBitmap.recycle();
                                        }
                                        if (bitmap2 != null) {
                                            bitmap2.recycle();
                                        }
                                        throw th;
                                    }
                                }
                                try {
                                    if (captureResult != null) {
                                        try {
                                            Object objInvoke = Class.forName("U8.e").getMethod("c", CaptureResult.class).invoke(null, captureResult);
                                            put(objNewInstance, "f", field(objInvoke, "c"));
                                            put(objNewInstance, "g", field(objInvoke, "d"));
                                            long jLongValue = ((Long) field(field(obj, "b"), "e")).longValue();
                                            put(objNewInstance, "h", jLongValue == 0 ? field(objInvoke, "a") : Long.valueOf(jLongValue));
                                            put(objNewInstance, "i", field(objInvoke, "b"));
                                            str = "ExposureTime";
                                            exifInterface = exifInterface2;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            bitmap2 = null;
                                            bitmap3 = bitmap;
                                            bitmapCreateBitmap = bitmapCreateBitmap;
                                        }
                                    } else {
                                        exifInterface = exifInterface2;
                                        try {
                                            put(objNewInstance, "f", Short.valueOf((short) exifInterface.getAttributeInt("FocalLengthIn35mmFilm", 0)));
                                            put(objNewInstance, "g", Float.valueOf((float) exifInterface.getAttributeDouble("FNumber", 0.0d)));
                                            str = "ExposureTime";
                                            put(objNewInstance, "h", Long.valueOf(Math.round(exifInterface.getAttributeDouble(str, 0.0d) * 1.0E9d)));
                                            put(objNewInstance, "i", Integer.valueOf(exifInterface.getAttributeInt("ISOSpeedRatings", 0)));
                                        } catch (Throwable th4) {
                                            th = th4;
                                            bitmapCreateBitmap = bitmapCreateBitmap;
                                            bitmap2 = null;
                                            bitmap3 = bitmap;
                                            if (bitmapCreateBitmap != null) {
                                                bitmapCreateBitmap.recycle();
                                            }
                                            if (bitmap2 != null) {
                                                bitmap2.recycle();
                                            }
                                            throw th;
                                        }
                                    }
                                    if (((Short) field(objNewInstance, "f")).shortValue() <= 0) {
                                        put(objNewInstance, "f", Short.valueOf((short) exifInterface.getAttributeInt("FocalLengthIn35mmFilm", 0)));
                                    }
                                    if (((Float) field(objNewInstance, "g")).floatValue() <= 0.0f) {
                                        put(objNewInstance, "g", Float.valueOf((float) exifInterface.getAttributeDouble("FNumber", 0.0d)));
                                    }
                                    if (((Long) field(objNewInstance, "h")).longValue() <= 0) {
                                        put(objNewInstance, "h", Long.valueOf(Math.round(exifInterface.getAttributeDouble(str, 0.0d) * 1.0E9d)));
                                    }
                                    if (((Integer) field(objNewInstance, "i")).intValue() <= 0) {
                                        put(objNewInstance, "i", Integer.valueOf(exifInterface.getAttributeInt("ISOSpeedRatings", 0)));
                                    }
                                    CaptureSnapshot.watermarkInputs(objNewInstance, obj);
                                    String str4 = "watermark inputs id=" + str3 + " focal=" + field(objNewInstance, "f") + " aperture=" + field(objNewInstance, "g") + " exposureNs=" + field(objNewInstance, "h") + " iso=" + field(objNewInstance, "i");
                                    str2 = str2;
                                    try {
                                        Log.i(str2, str4);
                                        put(objNewInstance, "j", field(field(field(obj, "d"), "k"), "d"));
                                        bitmap = "a";
                                        put(objNewInstance, "l", field(field(obj, bitmap), "g"));
                                        Class<?> cls3 = Class.forName("U8.d");
                                        Object objInvoke2 = cls3.getMethod(bitmap, new Class[0]).invoke(null, new Object[0]);
                                        bitmapCreateBitmap = bitmapCreateBitmap;
                                        try {
                                            Gainmap gainmapRenderGainmap = renderGainmap(bitmapCreateBitmap, objInvoke2, cls2, objNewInstance);
                                            bitmap2 = (Bitmap) cls3.getMethod("e", cls2).invoke(objInvoke2, objNewInstance);
                                            if (gainmapRenderGainmap != null && bitmap2 != null) {
                                                try {
                                                    bitmap2.setGainmap(gainmapRenderGainmap);
                                                } catch (Exception e3) {
                                                    e = e3;
                                                    bitmap = bitmap;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    bitmap3 = bitmap;
                                                    if (bitmapCreateBitmap != null) {
                                                        bitmapCreateBitmap.recycle();
                                                    }
                                                    if (bitmap2 != null) {
                                                        bitmap2.recycle();
                                                    }
                                                    throw th;
                                                }
                                            }
                                            try {
                                                if (bitmap2 == null || bitmap2 == bitmapCreateBitmap) {
                                                    throw new IllegalStateException("Original watermark renderer returned no rendered bitmap");
                                                }
                                                if (!bitmap.getColorSpace().equals(bitmap2.getColorSpace())) {
                                                    throw new IllegalStateException("Watermark renderer changed working color space");
                                                }
                                                Matrix matrix = new Matrix();
                                                if (!matrixOrientation.invert(matrix)) {
                                                    throw new IllegalStateException("Invalid image orientation");
                                                }
                                                Bitmap bitmapCreateBitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
                                                Object objField3 = field(obj, bitmap);
                                                put(objField3, bitmap, Integer.valueOf(bitmapCreateBitmap2.getWidth()));
                                                put(objField3, "b", Integer.valueOf(bitmapCreateBitmap2.getHeight()));
                                                Log.i(str2, "watermark rendered id=" + str3 + " orientation=" + attributeInt + " size=" + bitmapCreateBitmap2.getWidth() + "x" + bitmapCreateBitmap2.getHeight());
                                                if (bitmap2 != bitmapCreateBitmap2) {
                                                    bitmap2.recycle();
                                                }
                                                if (bitmapCreateBitmap != null && bitmapCreateBitmap != bitmap) {
                                                    bitmapCreateBitmap.recycle();
                                                }
                                                return bitmapCreateBitmap2;
                                            } catch (Exception e4) {
                                                e = e4;
                                            }
                                        } catch (Exception e5) {
                                            e = e5;
                                            bitmap2 = null;
                                        }
                                    } catch (Exception e6) {
                                        e = e6;
                                        bitmapCreateBitmap = bitmapCreateBitmap;
                                    }
                                } catch (Exception e7) {
                                    e = e7;
                                    bitmapCreateBitmap = bitmapCreateBitmap;
                                    str2 = str2;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                            }
                        } catch (Exception e8) {
                            e = e8;
                            str2 = "LegendaryPost";
                        }
                    } catch (Exception e9) {
                        e = e9;
                        bitmap = bitmap;
                        bitmap2 = null;
                        bitmapCreateBitmap = null;
                    } catch (Throwable th7) {
                        th = th7;
                        bitmap = bitmap;
                        bitmap2 = null;
                        bitmapCreateBitmap = null;
                        bitmap3 = bitmap;
                    }
                }
                bitmap2 = null;
                bitmapCreateBitmap = null;
                bitmap3 = bitmap;
                if (bitmapCreateBitmap != null && bitmapCreateBitmap != bitmap3) {
                    bitmapCreateBitmap.recycle();
                }
                if (bitmap2 != null && bitmap2 != bitmap3 && bitmap2 != bitmapCreateBitmap) {
                    bitmap2.recycle();
                }
                throw th;
            } catch (Throwable th8) {
                th = th8;
            }
        } catch (Exception e10) {
            e = e10;
            bitmap = bitmap;
        } catch (Throwable th9) {
            th = th9;
            bitmap = bitmap;
        }
        bitmap2 = null;
        bitmapCreateBitmap = null;
        Log.e(str2, "Original watermark rendering failed; retaining styled image", e);
        try {
            final Context context = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.codex.leicaapp.OriginalWatermark$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    Toast.makeText(context, "水印添加失败，照片已保存", 1).show();
                }
            });
        } catch (Exception e11) {
        }
        if (bitmapCreateBitmap != null && bitmapCreateBitmap != bitmap) {
            bitmapCreateBitmap.recycle();
        }
        if (bitmap2 != null && bitmap2 != bitmap && bitmap2 != bitmapCreateBitmap) {
            bitmap2.recycle();
        }
        return bitmap;
    }

    /* JADX WARN: Code duplicated, block: B:108:0x02ab A[ADDED_TO_REGION, REMOVE] */
    /* JADX WARN: Code duplicated, block: B:169:0x004d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    static Gainmap renderGainmap(Bitmap bitmap, Object obj, Class<?> cls, Object obj2) throws Throwable {
        Bitmap bitmapCreateBitmap;
        Bitmap bitmapRenderCarrier;
        Bitmap bitmapCreateBitmap2;
        Bitmap bitmapRenderCarrier2;
        Bitmap bitmapCreateBitmap3;
        Bitmap bitmapRenderCarrier3;
        Bitmap bitmap2;
        Bitmap bitmap3 = null;
        if (Build.VERSION.SDK_INT < 34 || !bitmap.hasGainmap()) {
            return null;
        }
        Gainmap gainmap = bitmap.getGainmap();
        Bitmap gainmapContents = gainmap.getGainmapContents();
        float fMin = Math.min(1.0f, 1024.0f / Math.max(gainmapContents.getWidth(), gainmapContents.getHeight()));
        int i = 0;
        if (fMin < 1.0f) {
            bitmapCreateBitmap3 = Bitmap.createBitmap(Math.max(1, Math.round(gainmapContents.getWidth() * fMin)), Math.max(1, Math.round(gainmapContents.getHeight() * fMin)), Bitmap.Config.ARGB_8888);
            new Canvas(bitmapCreateBitmap3).drawBitmap(gainmapContents, (Rect) null, new Rect(0, 0, bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight()), new Paint(2));
            bitmapCreateBitmap2 = Bitmap.createBitmap(bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCreateBitmap = Bitmap.createBitmap(bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight(), Bitmap.Config.ARGB_8888);
            bitmapCreateBitmap2.eraseColor(-16777216);
            bitmapCreateBitmap.eraseColor(-1);
            bitmapRenderCarrier3 = renderCarrier(obj, cls, obj2, bitmapCreateBitmap3);
            bitmapRenderCarrier2 = renderCarrier(obj, cls, obj2, bitmapCreateBitmap2);
            bitmapRenderCarrier = renderCarrier(obj, cls, obj2, bitmapCreateBitmap);
            if (bitmapRenderCarrier3 != null) {
                throw new IllegalStateException("HDR watermark geometry mismatch");
            }
            throw new IllegalStateException("HDR watermark geometry mismatch");
        }
        try {
            if (gainmapContents.getColorSpace() == null) {
                try {
                    bitmapCreateBitmap3 = Bitmap.createBitmap(Math.max(1, Math.round(gainmapContents.getWidth() * fMin)), Math.max(1, Math.round(gainmapContents.getHeight() * fMin)), Bitmap.Config.ARGB_8888);
                    try {
                        new Canvas(bitmapCreateBitmap3).drawBitmap(gainmapContents, (Rect) null, new Rect(0, 0, bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight()), new Paint(2));
                    } catch (Throwable th) {
                        th = th;
                        gainmapContents = gainmapContents;
                        i = 0;
                        bitmapCreateBitmap = null;
                        bitmapRenderCarrier = null;
                        bitmapCreateBitmap2 = null;
                        bitmapRenderCarrier2 = null;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    gainmapContents = gainmapContents;
                    i = 0;
                    bitmapCreateBitmap = null;
                    bitmapRenderCarrier = null;
                    bitmapCreateBitmap2 = null;
                    bitmapRenderCarrier2 = null;
                    bitmapCreateBitmap3 = gainmapContents;
                }
            } else {
                bitmapCreateBitmap3 = gainmapContents;
            }
            bitmapCreateBitmap2 = Bitmap.createBitmap(bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight(), Bitmap.Config.ARGB_8888);
            try {
                bitmapCreateBitmap = Bitmap.createBitmap(bitmapCreateBitmap3.getWidth(), bitmapCreateBitmap3.getHeight(), Bitmap.Config.ARGB_8888);
                try {
                    bitmapCreateBitmap2.eraseColor(-16777216);
                    bitmapCreateBitmap.eraseColor(-1);
                    bitmapRenderCarrier3 = renderCarrier(obj, cls, obj2, bitmapCreateBitmap3);
                    try {
                        bitmapRenderCarrier2 = renderCarrier(obj, cls, obj2, bitmapCreateBitmap2);
                        try {
                            bitmapRenderCarrier = renderCarrier(obj, cls, obj2, bitmapCreateBitmap);
                            if (bitmapRenderCarrier3 != null || bitmapRenderCarrier2 == null || bitmapRenderCarrier == null) {
                                try {
                                    throw new IllegalStateException("HDR watermark geometry mismatch");
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            } else {
                                try {
                                    if (bitmapRenderCarrier3.getWidth() != bitmapRenderCarrier2.getWidth() || bitmapRenderCarrier3.getHeight() != bitmapRenderCarrier2.getHeight() || bitmapRenderCarrier3.getWidth() != bitmapRenderCarrier.getWidth() || bitmapRenderCarrier3.getHeight() != bitmapRenderCarrier.getHeight()) {
                                        throw new IllegalStateException("HDR watermark geometry mismatch");
                                    }
                                    int width = bitmapRenderCarrier3.getWidth();
                                    int height = bitmapRenderCarrier3.getHeight();
                                    int i2 = width * height;
                                    int[] iArr = new int[i2];
                                    int[] iArr2 = new int[i2];
                                    int[] iArr3 = new int[i2];
                                    bitmapRenderCarrier3.getPixels(iArr, 0, width, 0, 0, width, height);
                                    bitmapRenderCarrier2.getPixels(iArr2, 0, width, 0, 0, width, height);
                                    bitmapRenderCarrier.getPixels(iArr3, 0, width, 0, 0, width, height);
                                    bitmapCreateBitmap = bitmapCreateBitmap;
                                    try {
                                        double dLog = Math.log(gainmap.getRatioMin()[0]);
                                        try {
                                            Bitmap bitmap4 = bitmapCreateBitmap3;
                                            try {
                                                float fLog = (float) (((-dLog) * 255.0d) / (Math.log(gainmap.getRatioMax()[0]) - dLog));
                                                int i3 = 0;
                                                while (i3 < i2) {
                                                    int i4 = 0;
                                                    int iMax = -16777216;
                                                    while (i4 <= 16) {
                                                        try {
                                                            int i5 = (iArr[i3] >> i4) & 255;
                                                            int i6 = i2;
                                                            int i7 = (iArr2[i3] >> i4) & 255;
                                                            int[] iArr4 = iArr2;
                                                            try {
                                                                iMax |= Math.max(0, Math.min(255, Math.round((i5 - i7) + ((1.0f - Math.max(0.0f, Math.min(1.0f, (((iArr3[i3] >> i4) & 255) - i7) / 255.0f))) * fLog)))) << i4;
                                                                i4 += 8;
                                                                i2 = i6;
                                                                iArr2 = iArr4;
                                                            } catch (Throwable th4) {
                                                                th = th4;
                                                                bitmapRenderCarrier = bitmapRenderCarrier;
                                                                i = 0;
                                                                bitmap3 = bitmapRenderCarrier3;
                                                                bitmapCreateBitmap = bitmapCreateBitmap;
                                                                bitmapCreateBitmap3 = bitmap4;
                                                                gainmapContents = gainmapContents;
                                                            }
                                                        } catch (Throwable th5) {
                                                            th = th5;
                                                            bitmapRenderCarrier = bitmapRenderCarrier;
                                                            bitmap3 = bitmapRenderCarrier3;
                                                            bitmapCreateBitmap = bitmapCreateBitmap;
                                                            bitmapCreateBitmap3 = bitmap4;
                                                            gainmapContents = gainmapContents;
                                                            i = 0;
                                                        }
                                                    }
                                                    int i8 = i2;
                                                    int[] iArr5 = iArr2;
                                                    iArr[i3] = iMax;
                                                    i3++;
                                                    i2 = i8;
                                                    iArr2 = iArr5;
                                                }
                                                try {
                                                    Gainmap gainmap2 = new Gainmap(Bitmap.createBitmap(iArr, width, height, Bitmap.Config.ARGB_8888));
                                                    float[] ratioMin = gainmap.getRatioMin();
                                                    i = 0;
                                                    try {
                                                        gainmap2.setRatioMin(ratioMin[0], ratioMin[1], ratioMin[2]);
                                                        float[] ratioMax = gainmap.getRatioMax();
                                                        gainmap2.setRatioMax(ratioMax[0], ratioMax[1], ratioMax[2]);
                                                        float[] gamma = gainmap.getGamma();
                                                        gainmap2.setGamma(gamma[0], gamma[1], gamma[2]);
                                                        float[] epsilonSdr = gainmap.getEpsilonSdr();
                                                        gainmap2.setEpsilonSdr(epsilonSdr[0], epsilonSdr[1], epsilonSdr[2]);
                                                        float[] epsilonHdr = gainmap.getEpsilonHdr();
                                                        i = 0;
                                                        gainmap2.setEpsilonHdr(epsilonHdr[0], epsilonHdr[1], epsilonHdr[2]);
                                                        gainmap2.setMinDisplayRatioForHdrTransition(gainmap.getMinDisplayRatioForHdrTransition());
                                                        gainmap2.setDisplayRatioForFullHdr(gainmap.getDisplayRatioForFullHdr());
                                                        Bitmap[] bitmapArr = {bitmapRenderCarrier3, bitmapRenderCarrier2, bitmapRenderCarrier, bitmapCreateBitmap2, bitmapCreateBitmap};
                                                        int i9 = 0;
                                                        while (i9 < 5) {
                                                            Bitmap bitmap5 = bitmapArr[i9];
                                                            if (bitmap5 != null) {
                                                                bitmap2 = bitmap4;
                                                                if (bitmap5 != bitmap2 && !bitmap5.isRecycled()) {
                                                                    bitmap5.recycle();
                                                                }
                                                            } else {
                                                                bitmap2 = bitmap4;
                                                            }
                                                            i9++;
                                                            bitmap4 = bitmap2;
                                                        }
                                                        Bitmap bitmap6 = bitmap4;
                                                        if (bitmap6 != gainmapContents && !bitmap6.isRecycled()) {
                                                            bitmap6.recycle();
                                                        }
                                                        return gainmap2;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        bitmapRenderCarrier = bitmapRenderCarrier;
                                                        bitmapCreateBitmap = bitmapCreateBitmap;
                                                        bitmapCreateBitmap3 = bitmap4;
                                                        gainmapContents = gainmapContents;
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    bitmapRenderCarrier = bitmapRenderCarrier;
                                                    bitmapCreateBitmap3 = bitmap4;
                                                    gainmapContents = gainmapContents;
                                                    i = 0;
                                                }
                                            } catch (Throwable th8) {
                                                th = th8;
                                                gainmapContents = gainmapContents;
                                                bitmapRenderCarrier = bitmapRenderCarrier;
                                                bitmapCreateBitmap3 = bitmap4;
                                            }
                                        } catch (Throwable th9) {
                                            th = th9;
                                            gainmapContents = gainmapContents;
                                            bitmapRenderCarrier = bitmapRenderCarrier;
                                            i = 0;
                                            bitmapCreateBitmap = bitmapCreateBitmap;
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        gainmapContents = gainmapContents;
                                        bitmapRenderCarrier = bitmapRenderCarrier;
                                    }
                                } catch (Throwable th11) {
                                    th = th11;
                                    gainmapContents = gainmapContents;
                                    bitmapRenderCarrier = bitmapRenderCarrier;
                                    i = 0;
                                    bitmapCreateBitmap = bitmapCreateBitmap;
                                }
                                bitmap3 = bitmapRenderCarrier3;
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            gainmapContents = gainmapContents;
                            i = 0;
                            bitmapCreateBitmap = bitmapCreateBitmap;
                            bitmapRenderCarrier = null;
                        }
                    } catch (Throwable th13) {
                        th = th13;
                        gainmapContents = gainmapContents;
                        i = 0;
                        bitmapCreateBitmap = bitmapCreateBitmap;
                        bitmapRenderCarrier = null;
                        bitmapRenderCarrier2 = null;
                    }
                } catch (Throwable th14) {
                    th = th14;
                    bitmapCreateBitmap = bitmapCreateBitmap;
                    bitmapRenderCarrier = null;
                    bitmapRenderCarrier2 = bitmapRenderCarrier;
                }
            } catch (Throwable th15) {
                th = th15;
                bitmapCreateBitmap = null;
                bitmapRenderCarrier = null;
            }
        } catch (Throwable th16) {
            th = th16;
            gainmapContents = gainmapContents;
            bitmapCreateBitmap3 = gainmapContents;
            bitmapCreateBitmap = null;
            bitmapRenderCarrier = null;
            bitmapCreateBitmap2 = null;
            bitmapRenderCarrier2 = null;
            i = 0;
        }
        Bitmap[] bitmapArr2 = {bitmap3, bitmapRenderCarrier2, bitmapRenderCarrier, bitmapCreateBitmap2, bitmapCreateBitmap};
        for (int i10 = i; i10 < 5; i10++) {
            Bitmap bitmap7 = bitmapArr2[i10];
            if (bitmap7 != null && bitmap7 != bitmapCreateBitmap3 && !bitmap7.isRecycled()) {
                bitmap7.recycle();
            }
        }
        if (bitmapCreateBitmap3 != gainmapContents && !bitmapCreateBitmap3.isRecycled()) {
            bitmapCreateBitmap3.recycle();
        }
        throw th;
    }

    private static Bitmap renderCarrier(Object obj, Class<?> cls, Object obj2, Bitmap bitmap) throws ReflectiveOperationException {
        Class<?> cls2 = Class.forName("qs.b");
        Object objNewInstance = cls.getConstructor(Bitmap.class, cls2, Integer.TYPE).newInstance(bitmap, cls2.getField("b").get(null), 0);
        for (Field field : cls.getFields()) {
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && field.getType() != Bitmap.class) {
                field.set(objNewInstance, field.get(obj2));
            }
        }
        return (Bitmap) obj.getClass().getMethod("e", cls).invoke(obj, objNewInstance);
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
                matrix.setScale(1.0f, -1.0f);
                return matrix;
            case 5:
                matrix.setValues(new float[]{0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});
                return matrix;
            case 6:
                matrix.setRotate(90.0f);
                return matrix;
            case 7:
                matrix.setValues(new float[]{0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});
                return matrix;
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                matrix.setRotate(270.0f);
                return matrix;
            default:
                return matrix;
        }
    }
}
