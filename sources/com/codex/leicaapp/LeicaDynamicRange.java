package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Gainmap;
import android.util.Log;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LeicaDynamicRange {
    static float luminance(int i) {
        return (((((i >> 16) & 255) * 0.2126f) + (((i >> 8) & 255) * 0.7152f)) + ((i & 255) * 0.0722f)) / 255.0f;
    }

    static float target(float f, float f2) {
        float fMax = f2 + (Math.max(0.0f, f - f2) * Math.max(0.0f, 1.0f - (Math.abs(f - 0.14f) / 0.14f)) * 0.35f);
        return fMax - ((Math.max(0.0f, fMax - f) * Math.max(0.0f, Math.min(1.0f, (f - 0.65f) / 0.25f))) * 0.5f);
    }

    /* JADX WARN: Code duplicated, block: B:103:0x023c  */
    /* JADX WARN: Code duplicated, block: B:105:0x0241 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:132:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:35:0x00f8  */
    /* JADX WARN: Code duplicated, block: B:96:0x022e  */
    /* JADX WARN: Code duplicated, block: B:98:0x0233 A[ADDED_TO_REGION] */
    /* JADX WARN: Type inference failed for: r14v0 */
    /* JADX WARN: Type inference failed for: r14v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r14v2 */
    public static void apply(Bitmap bitmap, byte[] bArr, boolean z) throws Throwable {
        Bitmap bitmap2;
        Bitmap bitmap3;
        ?? r14;
        int i;
        int i2;
        if (bitmap == null || !bitmap.isMutable() || bitmap.isRecycled()) {
            return;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPreferredColorSpace = bitmap.getColorSpace();
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
            try {
                try {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int i3 = 1;
                    if (!z || bitmapDecodeByteArray == null) {
                        r14 = 1;
                        i = height;
                        i2 = width;
                    } else {
                        try {
                            if (bitmapDecodeByteArray.getWidth() == width && bitmapDecodeByteArray.getHeight() == height) {
                                int[] iArr = new int[width];
                                int[] iArr2 = new int[width];
                                int i4 = 0;
                                while (i4 < height) {
                                    bitmapDecodeByteArray.getPixels(iArr, 0, width, 0, i4, width, 1);
                                    int i5 = i4;
                                    int[] iArr3 = iArr2;
                                    int i6 = i3;
                                    int i7 = height;
                                    int i8 = width;
                                    bitmap.getPixels(iArr2, 0, width, 0, i5, width, 1);
                                    for (int i9 = 0; i9 < i8; i9++) {
                                        float fLuminance = luminance(iArr3[i9]);
                                        float fTarget = target(luminance(iArr[i9]), fLuminance);
                                        if (fLuminance >= 0.004f) {
                                            float f = fTarget / fLuminance;
                                            int i10 = iArr3[i9];
                                            int i11 = (i10 >> 16) & 255;
                                            int i12 = (i10 >> 8) & 255;
                                            int i13 = i10 & 255;
                                            float fMin = Math.min(f, 255.0f / Math.max(i6, Math.max(i11, Math.max(i12, i13))));
                                            iArr3[i9] = Color.rgb(Math.round(i11 * fMin), Math.round(i12 * fMin), Math.round(i13 * fMin));
                                        }
                                    }
                                    bitmap.setPixels(iArr3, 0, i8, 0, i5, i8, 1);
                                    i4 = i5 + 1;
                                    width = i8;
                                    i3 = i6;
                                    iArr2 = iArr3;
                                    height = i7;
                                }
                                r14 = i3;
                                i = height;
                                i2 = width;
                            } else {
                                r14 = 1;
                                i = height;
                                i2 = width;
                            }
                        } catch (OutOfMemoryError | RuntimeException e) {
                            e = e;
                            bitmap3 = bitmapDecodeByteArray;
                            bitmap2 = null;
                            try {
                                Log.w("LeicaDynamicRange", "Retaining available film image", e);
                                if (bitmap3 != null) {
                                    bitmap3.recycle();
                                }
                                if (bitmap2 != null) {
                                }
                            } catch (Throwable th) {
                                th = th;
                                if (bitmap3 != null) {
                                    bitmap3.recycle();
                                }
                                if (bitmap2 != null) {
                                    bitmap2.recycle();
                                }
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            bitmap3 = bitmapDecodeByteArray;
                            bitmap2 = null;
                            if (bitmap3 != null) {
                                bitmap3.recycle();
                            }
                            if (bitmap2 != null) {
                                bitmap2.recycle();
                            }
                            throw th;
                        }
                    }
                    if (bitmapDecodeByteArray == null || !bitmapDecodeByteArray.hasGainmap()) {
                        bitmap2 = null;
                    } else {
                        try {
                            Class<?> cls = Class.forName("com.codex.leicaapp.HdrGainmaps");
                            Class<?>[] clsArr = new Class[2];
                            clsArr[0] = Bitmap.class;
                            clsArr[r14] = Boolean.TYPE;
                            Method method = cls.getMethod("capture", clsArr);
                            Object[] objArr = new Object[2];
                            objArr[0] = bitmapDecodeByteArray;
                            objArr[r14] = Boolean.valueOf(z);
                            bitmap2 = null;
                            try {
                                try {
                                    try {
                                        Gainmap gainmap = (Gainmap) method.invoke(null, objArr);
                                        if (gainmap != null) {
                                            bitmap.setGainmap(gainmap);
                                            Log.i("LeicaDynamicRange", "Captured HDR retained");
                                            if (bitmapDecodeByteArray != null) {
                                                bitmapDecodeByteArray.recycle();
                                                return;
                                            }
                                            return;
                                        }
                                    } catch (ReflectiveOperationException e2) {
                                        e = e2;
                                        Log.w("LeicaDynamicRange", "Source HDR conversion unavailable", e);
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    bitmap3 = bitmapDecodeByteArray;
                                    if (bitmap3 != null) {
                                        bitmap3.recycle();
                                    }
                                    if (bitmap2 != null && bitmap2 != bitmap) {
                                        bitmap2.recycle();
                                    }
                                    throw th;
                                }
                            } catch (OutOfMemoryError | RuntimeException e3) {
                                e = e3;
                                bitmap3 = bitmapDecodeByteArray;
                                Log.w("LeicaDynamicRange", "Retaining available film image", e);
                                if (bitmap3 != null) {
                                    bitmap3.recycle();
                                }
                                if (bitmap2 != null || bitmap2 == bitmap) {
                                }
                                bitmap2.recycle();
                                return;
                            }
                        } catch (ReflectiveOperationException e4) {
                            e = e4;
                            bitmap2 = null;
                        }
                    }
                    int iMin = Math.min(512, i2);
                    int iMax = Math.max((int) r14, Math.round((i * iMin) / i2));
                    Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmap, iMin, iMax, r14);
                    int i14 = iMin * iMax;
                    try {
                        int[] iArr4 = new int[i14];
                        bitmapCreateScaledBitmap.getPixels(iArr4, 0, iMin, 0, 0, iMin, iMax);
                        for (int i15 = 0; i15 < i14; i15++) {
                            float fMax = Math.max(0.0f, Math.min(1.0f, (luminance(iArr4[i15]) - 0.35f) / 0.6f));
                            int iRound = Math.round(fMax * 255.0f * fMax * (3.0f - (fMax * 2.0f)));
                            iArr4[i15] = Color.rgb(iRound, iRound, iRound);
                        }
                        Gainmap gainmap2 = new Gainmap(Bitmap.createBitmap(iArr4, iMin, iMax, Bitmap.Config.ARGB_8888));
                        float f2 = z ? 1.45f : 1.18f;
                        gainmap2.setRatioMin(1.0f, 1.0f, 1.0f);
                        gainmap2.setRatioMax(f2, f2, f2);
                        gainmap2.setGamma(1.0f, 1.0f, 1.0f);
                        gainmap2.setEpsilonSdr(0.0f, 0.0f, 0.0f);
                        gainmap2.setEpsilonHdr(0.0f, 0.0f, 0.0f);
                        gainmap2.setMinDisplayRatioForHdrTransition(1.0f);
                        if (!z) {
                            f2 = 1.5f;
                        }
                        gainmap2.setDisplayRatioForFullHdr(f2);
                        if (!z) {
                            f2 = 1.18f;
                        }
                        bitmap.setGainmap(gainmap2);
                        Log.i("LeicaDynamicRange", "captured tonal detail protected; display HDR max=" + f2 + " mono=" + z);
                        if (bitmapDecodeByteArray != null) {
                            bitmapDecodeByteArray.recycle();
                        }
                        if (bitmapCreateScaledBitmap == null || bitmapCreateScaledBitmap == bitmap) {
                            return;
                        }
                        bitmapCreateScaledBitmap.recycle();
                    } catch (OutOfMemoryError | RuntimeException e5) {
                        e = e5;
                        bitmap2 = bitmapCreateScaledBitmap;
                        bitmap3 = bitmapDecodeByteArray;
                        Log.w("LeicaDynamicRange", "Retaining available film image", e);
                        if (bitmap3 != null) {
                            bitmap3.recycle();
                        }
                        if (bitmap2 != null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        bitmap2 = bitmapCreateScaledBitmap;
                        bitmap3 = bitmapDecodeByteArray;
                        if (bitmap3 != null) {
                            bitmap3.recycle();
                        }
                        if (bitmap2 != null) {
                            bitmap2.recycle();
                        }
                        throw th;
                    }
                } catch (OutOfMemoryError | RuntimeException e6) {
                    e = e6;
                    bitmap2 = null;
                }
            } catch (Throwable th5) {
                th = th5;
                bitmap2 = null;
            }
        } catch (OutOfMemoryError | RuntimeException e7) {
            e = e7;
            bitmap2 = null;
            bitmap3 = null;
        } catch (Throwable th6) {
            th = th6;
            bitmap2 = null;
            bitmap3 = null;
        }
    }
}
