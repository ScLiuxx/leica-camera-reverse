package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Gainmap;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import java.io.ByteArrayOutputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class PalettePostProcessor {
    private static final String TAG = "PalettePost";

    private PalettePostProcessor() {
    }

    public static byte[] process(byte[] bArr, String str) {
        return processCombined(bArr, str, null);
    }

    public static byte[] processStyle(byte[] bArr, String str) {
        return processCombined(bArr, null, str);
    }

    /* JADX WARN: Code duplicated, block: B:127:0x01f2 A[DONT_GENERATE] */
    public static byte[] processCombined(byte[] bArr, String str, String str2) {
        String str3;
        String str4;
        Bitmap bitmapDraw;
        Bitmap bitmapDraw2;
        ColorMatrix colorMatrixMatrix;
        if (bArr == null || bArr.length < 4) {
            return bArr;
        }
        boolean z = str != null && str.contains("@StyleAdjustEffect");
        float fValue = z ? value(str, "ContrastIntensity", 0.0f) : 0.0f;
        float fValue2 = z ? value(str, "SaturationIntensity", 0.0f) : 0.0f;
        float fValue3 = z ? value(str, "Temperature", 0.0f) : 0.0f;
        float fValue4 = z ? value(str, "Tint", 0.0f) : 0.0f;
        float fValue5 = z ? value(str, "Grain", 0.0f) : 0.0f;
        float fValue6 = z ? value(str, "VignetteIntensity", 0.0f) : 0.0f;
        boolean z2 = (fValue == 0.0f && fValue2 == 0.0f && fValue3 == 0.0f && fValue4 == 0.0f && fValue5 == 0.0f && fValue6 == 0.0f) ? false : true;
        boolean z3 = "0".equals(str2) || "1".equals(str2) || "2".equals(str2);
        if (!z2 && !z3) {
            return bArr;
        }
        Bitmap bitmap = null;
        Gainmap gainmapCapture = null;
        try {
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
            if (bitmapDecodeByteArray == null) {
                if (bitmapDecodeByteArray != null) {
                    bitmapDecodeByteArray.recycle();
                }
                return bArr;
            }
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    gainmapCapture = HdrGainmaps.capture(bitmapDecodeByteArray, false);
                }
                Gainmap gainmap = gainmapCapture;
                if (z2) {
                    float f = fValue2 + 1.0f;
                    str4 = TAG;
                    try {
                        float f2 = 6.0f * fValue4;
                        bitmapDraw = draw(bitmapDecodeByteArray, matrix(clamp(f, 0.0f, 2.0f), clamp((fValue * 0.5f) + 1.0f, 0.5f, 1.5f), (fValue3 * 18.0f) + f2, (-fValue4) * 12.0f, ((-fValue3) * 18.0f) + f2));
                        if (fValue5 != 0.0f || fValue6 != 0.0f) {
                            try {
                                texture(bitmapDraw, fValue5, fValue6);
                            } catch (Throwable th) {
                                th = th;
                                bitmap = bitmapDraw;
                                str3 = str4;
                                Log.e(str3, "Could not apply saved palette/style", th);
                                return bArr;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        bitmap = bitmapDecodeByteArray;
                        str3 = str4;
                        Log.e(str3, "Could not apply saved palette/style", th);
                        return bArr;
                    }
                } else {
                    str4 = TAG;
                    bitmapDraw = bitmapDecodeByteArray;
                }
                if (z3) {
                    if ("0".equals(str2)) {
                        colorMatrixMatrix = matrix(1.2f, 1.06f, 1.0f, 0.0f, 0.0f);
                    } else {
                        colorMatrixMatrix = "1".equals(str2) ? matrix(0.78f, 1.12f, 4.0f, 0.0f, -4.0f) : matrix(0.9f, 0.96f, 0.0f, 0.0f, 0.0f);
                    }
                    bitmapDraw2 = draw(bitmapDraw, colorMatrixMatrix);
                } else {
                    bitmapDraw2 = bitmapDraw;
                }
                if (gainmap != null) {
                    try {
                        HdrGainmaps.attach(bitmapDraw2, gainmap);
                    } catch (Throwable th3) {
                        th = th3;
                        bitmap = bitmapDraw2;
                        str3 = str4;
                    }
                }
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                    if (!bitmapDraw2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                        if (bitmapDraw2 != null) {
                            bitmapDraw2.recycle();
                        }
                        return bArr;
                    }
                    byte[] bArrCopyExif = JpegMetadata.copyExif(bArr, byteArrayOutputStream.toByteArray(), false, bitmapDraw2.getWidth(), bitmapDraw2.getHeight(), false);
                    StringBuilder sbAppend = new StringBuilder().append("single encode quality=100 palette=").append(z2).append(" style=").append(str2).append(" size=").append(bitmapDraw2.getWidth()).append("x").append(bitmapDraw2.getHeight()).append(" hdr=");
                    boolean z4 = gainmap != null;
                    str3 = str4;
                    try {
                        Log.i(str3, sbAppend.append(z4).append(" bytes=").append(bArrCopyExif.length).toString());
                        if (bitmapDraw2 != null) {
                            bitmapDraw2.recycle();
                        }
                        return bArrCopyExif;
                    } catch (Throwable th4) {
                        th = th4;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    str3 = str4;
                }
                bitmap = bitmapDraw2;
            } catch (Throwable th6) {
                th = th6;
                str3 = TAG;
                bitmap = bitmapDecodeByteArray;
            }
            try {
                Log.e(str3, "Could not apply saved palette/style", th);
                return bArr;
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } catch (Throwable th7) {
            th = th7;
            str3 = TAG;
        }
    }

    private static Bitmap draw(Bitmap bitmap, ColorMatrix colorMatrix) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888, true, bitmap.getColorSpace());
        try {
            Paint paint = new Paint(2);
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            new Canvas(bitmapCreateBitmap).drawBitmap(bitmap, 0.0f, 0.0f, paint);
            bitmap.recycle();
            return bitmapCreateBitmap;
        } catch (Throwable th) {
            bitmapCreateBitmap.recycle();
            throw th;
        }
    }

    private static ColorMatrix matrix(float f, float f2, float f3, float f4, float f5) {
        float f6 = 1.0f - f;
        float f7 = 0.213f * f6;
        float f8 = 0.715f * f6;
        float f9 = f6 * 0.072f;
        float f10 = (1.0f - f2) * 128.0f;
        float f11 = f2 * f8;
        float f12 = f2 * f9;
        float f13 = f7 * f2;
        return new ColorMatrix(new float[]{(f7 + f) * f2, f11, f12, 0.0f, f3 + f10, f13, (f8 + f) * f2, f12, 0.0f, f4 + f10, f13, f11, f2 * (f9 + f), 0.0f, f10 + f5, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    }

    private static float value(String str, String str2, float f) {
        char cCharAt;
        int iIndexOf = str.indexOf(str2 + "=");
        if (iIndexOf < 0) {
            return f;
        }
        int length = iIndexOf + str2.length() + 1;
        int i = length;
        while (i < str.length() && (cCharAt = str.charAt(i)) != ';' && cCharAt != '@' && cCharAt != ']' && cCharAt != ',') {
            i++;
        }
        try {
            return Float.parseFloat(str.substring(length, i).trim());
        } catch (NumberFormatException e) {
            return f;
        }
    }

    private static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    private static void texture(Bitmap bitmap, float f, float f2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[width];
        float fClamp = clamp(Math.abs(f), 0.0f, 1.0f) * 18.0f;
        float fClamp2 = clamp(Math.abs(f2), 0.0f, 1.0f) * 0.45f;
        int i = 0;
        while (i < height) {
            int i2 = i;
            bitmap.getPixels(iArr, 0, width, 0, i, width, 1);
            float f3 = 2.0f;
            float f4 = ((i2 * 2.0f) / height) - 1.0f;
            int i3 = 0;
            while (i3 < width) {
                int i4 = iArr[i3];
                float f5 = ((i3 * f3) / width) - 1.0f;
                float fMax = 1.0f - (Math.max(0.0f, ((f5 * f5) + (f4 * f4)) - 0.15f) * fClamp2);
                float f6 = ((((((73856093 * i3) ^ (i2 * 19349663)) >>> 16) & 255) / 127.5f) - 1.0f) * fClamp;
                iArr[i3] = channel(((i4 & 255) * fMax) + f6) | (i4 & (-16777216)) | (channel((((i4 >>> 16) & 255) * fMax) + f6) << 16) | (channel((((i4 >>> 8) & 255) * fMax) + f6) << 8);
                i3++;
                f3 = 2.0f;
            }
            bitmap.setPixels(iArr, 0, width, 0, i2, width, 1);
            i = i2 + 1;
        }
    }

    private static int channel(float f) {
        return Math.max(0, Math.min(255, Math.round(f)));
    }
}
