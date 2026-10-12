package com.codex.fullm9;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.camera2.CaptureResult;
import android.media.ExifInterface;
import android.util.Log;
import com.codex.lmport.MomentBridge;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Locale;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class FullM9Engine {
    private static final Object LOCK = renderLock();

    static final class Settings {
        int lux = 300;
        int cct = 5000;
        float zoom = 1.0f;
        String provenance = "reference";

        Settings() {
        }
    }

    static Object renderLock() {
        try {
            Field declaredField = MomentBridge.class.getDeclaredField("RENDER");
            declaredField.setAccessible(true);
            return declaredField.get(null);
        } catch (Exception e) {
            return MomentBridge.class;
        }
    }

    static Settings settings(byte[] bArr, Object obj) {
        if (obj instanceof CaptureSnapshot) {
            return ((CaptureSnapshot) obj).settings();
        }
        Settings settings = new Settings();
        try {
            ExifInterface exifInterface = new ExifInterface(new ByteArrayInputStream(bArr));
            double attributeDouble = exifInterface.getAttributeDouble("ExposureTime", 0.0d);
            double attributeDouble2 = exifInterface.getAttributeDouble("FNumber", 0.0d);
            double attributeDouble3 = exifInterface.getAttributeDouble("PhotographicSensitivity", exifInterface.getAttributeDouble("ISOSpeedRatings", 0.0d));
            if (attributeDouble > 0.0d && attributeDouble2 > 0.0d && attributeDouble3 > 0.0d) {
                settings.lux = (int) Math.max(0.0d, Math.min(999.0d, 474.9d - (((Math.log((attributeDouble2 * attributeDouble2) / attributeDouble) / Math.log(2.0d)) - (Math.log(attributeDouble3 / 100.0d) / Math.log(2.0d))) * 23.45d)));
                settings.provenance = "EXIF EV estimate";
            }
            double attributeDouble4 = exifInterface.getAttributeDouble("FocalLengthIn35mmFilm", 0.0d);
            if (attributeDouble4 >= 50.0d) {
                settings.zoom = (float) Math.max(3.2d, Math.min(4.3d, attributeDouble4 / 23.0d));
            }
        } catch (Exception e) {
            Log.d("FullM9", "No EXIF metering", e);
        }
        Object objCaptureResult = captureResult(obj);
        if (objCaptureResult instanceof CaptureResult) {
            CaptureResult captureResult = (CaptureResult) objCaptureResult;
            for (CaptureResult.Key<?> key : captureResult.getKeys()) {
                try {
                    String lowerCase = key.getName().toLowerCase(Locale.US);
                    Object obj2 = captureResult.get(key);
                    float fFloatValue = obj2 instanceof Number ? ((Number) obj2).floatValue() : (!(obj2 instanceof float[]) || ((float[]) obj2).length <= 0) ? Float.NaN : ((float[]) obj2)[0];
                    if (Float.isFinite(fFloatValue)) {
                        if ((lowerCase.contains("luxindex") || lowerCase.contains("lux_index")) && fFloatValue >= 0.0f && fFloatValue <= 1000.0f) {
                            settings.lux = Math.round(fFloatValue);
                            settings.provenance = "capture lux";
                        }
                        if ((lowerCase.endsWith("cct") || lowerCase.contains("colortemperature") || lowerCase.contains("color_temperature")) && fFloatValue >= 1500.0f && fFloatValue <= 20000.0f) {
                            settings.cct = Math.round(fFloatValue);
                            settings.provenance = String.valueOf(settings.provenance) + " capture CCT";
                        }
                        if (lowerCase.equals("android.control.zoomratio") && fFloatValue > 0.0f) {
                            settings.zoom = fFloatValue;
                        }
                    }
                } catch (Exception e2) {
                    Log.d("FullM9", "Capture trigger key unavailable", e2);
                }
            }
        }
        return settings;
    }

    static Object captureResult(Object obj) {
        Object objField = Reflect.field(obj, "f");
        Object objField2 = Reflect.field(objField, "b");
        if (!(objField2 instanceof CaptureResult)) {
            objField2 = Reflect.field(objField, "c");
        }
        if (!(objField2 instanceof CaptureResult)) {
            objField2 = Reflect.field(obj, "legendaryPreviewResult");
        }
        if (objField2 instanceof CaptureResult) {
            return objField2;
        }
        return null;
    }

    static byte[] metadata(byte[] bArr, byte[] bArr2, int i, int i2) throws Exception {
        return ExifOrientation.upright((byte[]) Reflect.call(Class.forName("com.codex.leicaapp.JpegMetadata"), "copyExif", new Class[]{byte[].class, byte[].class, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE}, bArr, bArr2, true, Integer.valueOf(i), Integer.valueOf(i2), true));
    }

    static void recycle(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        bitmap.recycle();
    }

    static Bitmap p3(Bitmap bitmap) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888, false, ColorSpace.get(ColorSpace.Named.DISPLAY_P3));
        new Canvas(bitmapCreateBitmap).drawBitmap(bitmap, 0.0f, 0.0f, new Paint(3));
        return bitmapCreateBitmap;
    }

    static Bitmap rotate(Bitmap bitmap, float f) {
        Matrix matrix = new Matrix();
        matrix.setRotate(f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    static float boost(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[width];
        int i = 0;
        int i2 = 0;
        while (i < height) {
            Bitmap bitmap2 = bitmap;
            bitmap2.getPixels(iArr, 0, width, 0, i, width, 1);
            for (int i3 = 0; i3 < width; i3++) {
                if ((iArr[i3] & 255) >= 250) {
                    i2++;
                }
            }
            i++;
            bitmap = bitmap2;
        }
        double d = (((double) i2) * 100.0d) / (((long) width) * ((long) height));
        return ((int) ((d > 8.0d ? d >= 20.0d ? 0.8d : 1.0d - (((d - 8.0d) / 12.0d) * 0.2d) : 1.0d) * 500.0d)) / 100.0f;
    }

    static byte[] rawExif(Context context, byte[] bArr, int i, int i2) throws Exception {
        ExifInterface exifInterface;
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            bitmapCreateBitmap.compress(Bitmap.CompressFormat.JPEG, 97, byteArrayOutputStream);
            bitmapCreateBitmap.recycle();
            if (bArr.length > 1 && (bArr[0] & 255) == 255 && (bArr[1] & 255) == 216) {
                return metadata(bArr, byteArrayOutputStream.toByteArray(), i, i2);
            }
            File fileCreateTempFile = File.createTempFile("fullm9-meta-", ".jpg", context.getCacheDir());
            Throwable th = null;
            try {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileCreateTempFile);
                    try {
                        fileOutputStream.write(byteArrayOutputStream.toByteArray());
                        fileOutputStream.close();
                        try {
                            exifInterface = new ExifInterface(new ByteArrayInputStream(bArr));
                        } catch (IOException e) {
                            Log.d("FullM9", "No readable source EXIF", e);
                            exifInterface = null;
                        }
                        ExifInterface exifInterface2 = new ExifInterface(fileCreateTempFile.getAbsolutePath());
                        String[] strArr = {"Make", "Model", "ExposureTime", "FNumber", "PhotographicSensitivity", "ISOSpeedRatings", "FocalLength", "FocalLengthIn35mmFilm", "DateTime", "DateTimeOriginal", "DateTimeDigitized", "ExposureBiasValue", "WhiteBalance", "GPSLatitude", "GPSLatitudeRef", "GPSLongitude", "GPSLongitudeRef", "GPSAltitude", "GPSAltitudeRef", "GPSTimeStamp", "GPSDateStamp", "LensModel"};
                        for (int i3 = 0; i3 < 22; i3++) {
                            String str = strArr[i3];
                            String attribute = exifInterface == null ? null : exifInterface.getAttribute(str);
                            if (attribute != null) {
                                exifInterface2.setAttribute(str, attribute);
                            }
                        }
                        exifInterface2.setAttribute("Orientation", "1");
                        exifInterface2.setAttribute("ImageWidth", Integer.toString(i));
                        exifInterface2.setAttribute("ImageLength", Integer.toString(i2));
                        exifInterface2.setAttribute("PixelXDimension", Integer.toString(i));
                        exifInterface2.setAttribute("PixelYDimension", Integer.toString(i2));
                        exifInterface2.saveAttributes();
                        try {
                            FileInputStream fileInputStream = new FileInputStream(fileCreateTempFile);
                            try {
                                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                                byte[] bArr2 = new byte[8192];
                                while (true) {
                                    int i4 = fileInputStream.read(bArr2);
                                    if (i4 != -1) {
                                        byteArrayOutputStream2.write(bArr2, 0, i4);
                                    } else {
                                        byte[] byteArray = byteArrayOutputStream2.toByteArray();
                                        fileInputStream.close();
                                        fileCreateTempFile.delete();
                                        return byteArray;
                                    }
                                }
                            } catch (Throwable th2) {
                                fileInputStream.close();
                                throw th2;
                            }
                        } catch (Throwable th3) {
                            if (0 == 0) {
                                throw th3;
                            }
                            if (null != th3) {
                                th.addSuppressed(th3);
                            }
                            throw null;
                        }
                    } catch (Throwable th4) {
                        fileOutputStream.close();
                        throw th4;
                    }
                } catch (Throwable th5) {
                    if (0 == 0) {
                        throw th5;
                    }
                    if (null != th5) {
                        th.addSuppressed(th5);
                    }
                    throw null;
                }
            } catch (Throwable th6) {
                fileCreateTempFile.delete();
                throw th6;
            }
        } catch (Throwable th7) {
            bitmapCreateBitmap.recycle();
            throw th7;
        }
    }

    /*  JADX ERROR: Type inference failed
        jadx.core.utils.exceptions.JadxOverflowException: Type inference error: updates count limit reached with updateSeq = 19131. Try increasing type updates limit count.
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:59)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:19)
        	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:79)
    */
    public static byte[] render(android.content.Context r42, byte[] r43, java.lang.Object r44, int[] r45) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 1913
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.codex.fullm9.FullM9Engine.render(android.content.Context, byte[], java.lang.Object, int[]):byte[]");
    }

    static void failure(Context context, Throwable th) {
        CaptureTrace.failed(context, th);
        Log.e("FullM9", "M9 capture failed", th);
        if (context != null) {
            Throwable th2 = null;
            try {
                PrintWriter printWriter = new PrintWriter(new File(context.getFilesDir(), "fullm9-last-error.txt"), "UTF-8");
                try {
                    th.printStackTrace(printWriter);
                } finally {
                    printWriter.close();
                }
            } catch (Throwable th3) {
                if (0 == 0) {
                    throw th3;
                }
                if (null != th3) {
                    try {
                        th2.addSuppressed(th3);
                    } catch (Exception e) {
                        return;
                    }
                }
                throw null;
            }
        }
    }

    private static final class HdrCopy {
        private HdrCopy() {
        }

        static void copy(Bitmap bitmap, Bitmap bitmap2) {
            if (bitmap == bitmap2 || !bitmap.hasGainmap()) {
                return;
            }
            bitmap2.setGainmap(bitmap.getGainmap());
        }
    }
}
