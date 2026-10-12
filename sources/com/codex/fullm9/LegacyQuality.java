package com.codex.fullm9;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.codex.lmport.MomentBridge;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class LegacyQuality {
    private static final ThreadLocal<byte[]> SOURCE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> DECODED = new ThreadLocal<>();

    public static byte[] render(Context context, byte[] bArr, Object obj, int[] iArr) throws Exception {
        byte[] bArrRenderLegacy;
        synchronized (FullM9Engine.renderLock()) {
            CaptureState captureState = new CaptureState(obj);
            SOURCE.set(bArr);
            DECODED.remove();
            FullM9Hdr.clear();
            OutputQuality.begin(iArr);
            try {
                try {
                    try {
                        bArrRenderLegacy = MomentBridge.renderLegacy(context, bArr, obj, iArr);
                        CaptureState.commitJpeg(obj, bArrRenderLegacy);
                        SOURCE.remove();
                        DECODED.remove();
                        FullM9Hdr.clear();
                        OutputQuality.end();
                    } catch (Exception e) {
                        captureState.restore();
                        throw e;
                    }
                } catch (Error e2) {
                    captureState.restore();
                    throw e2;
                }
            } catch (Throwable th) {
                SOURCE.remove();
                DECODED.remove();
                FullM9Hdr.clear();
                OutputQuality.end();
                throw th;
            }
        }
        return bArrRenderLegacy;
    }

    /* JADX WARN: Code duplicated, block: B:93:0x015a  */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v10, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r1v11, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r1v12, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r1v14 */
    /* JADX WARN: Type inference failed for: r1v15 */
    /* JADX WARN: Type inference failed for: r1v16 */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX WARN: Type inference failed for: r1v2, types: [byte[]] */
    /* JADX WARN: Type inference failed for: r1v20 */
    /* JADX WARN: Type inference failed for: r1v22 */
    /* JADX WARN: Type inference failed for: r1v24, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r1v3 */
    /* JADX WARN: Type inference failed for: r1v5 */
    /* JADX WARN: Type inference failed for: r1v7 */
    /* JADX WARN: Type inference failed for: r1v9 */
    /* JADX WARN: Type inference failed for: r2v0 */
    /* JADX WARN: Type inference failed for: r2v1, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v21 */
    /* JADX WARN: Type inference failed for: r3v1, types: [android.graphics.Bitmap] */
    /* JADX WARN: Type inference failed for: r3v19 */
    /* JADX WARN: Type inference failed for: r3v3 */
    /* JADX WARN: Type inference failed for: r3v34 */
    public static Bitmap decodeFinal(byte[] bArr) throws Throwable {
        Bitmap bitmap;
        ?? r3;
        Bitmap bitmapCreateScaledBitmap;
        OutOfMemoryError e;
        LinkageError e2;
        Exception e3;
        byte[] bArrCreateScaledBitmap = SOURCE.get();
        ?? r2 = 0;
        r2 = 0;
        if (bArrCreateScaledBitmap != 0 && Boolean.TRUE.equals(DECODED.get()) && bArr == bArrCreateScaledBitmap) {
            return null;
        }
        Bitmap bitmapDecodeLegacy = MomentBridge.decodeLegacy(bArr);
        OutputQuality.removeSourceHdr(bitmapDecodeLegacy);
        ThreadLocal<Boolean> threadLocal = DECODED;
        threadLocal.set(Boolean.TRUE);
        if (bitmapDecodeLegacy != null) {
            try {
                if (bArrCreateScaledBitmap != 0) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(bArrCreateScaledBitmap, 0, bArrCreateScaledBitmap.length, options);
                        int i = options.outWidth;
                        int i2 = options.outHeight;
                        int iOrientation = FullM9Input.orientation(bArrCreateScaledBitmap);
                        if (iOrientation >= 5 && iOrientation <= 8) {
                            i = i2;
                            i2 = i;
                        }
                        if (i > bitmapDecodeLegacy.getWidth() && i2 > bitmapDecodeLegacy.getHeight() && Math.abs((((double) i) / ((double) i2)) - (((double) bitmapDecodeLegacy.getWidth()) / ((double) bitmapDecodeLegacy.getHeight()))) <= 0.015d) {
                            Bitmap bitmapDecode = FullM9Input.decode(bArrCreateScaledBitmap, iOrientation);
                            try {
                                bitmap = (Bitmap) Reflect.call(Class.forName("com.codex.lmport.DenoiseFlow"), "apply", new Class[]{Bitmap.class, Boolean.TYPE}, bitmapDecode, false);
                                try {
                                    bArrCreateScaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmapDecodeLegacy.getWidth(), bitmapDecodeLegacy.getHeight(), true);
                                    try {
                                        bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmapDecodeLegacy, i, i2, true);
                                        try {
                                            NativeM9.restoreDetail(bitmap, bArrCreateScaledBitmap, bitmapCreateScaledBitmap);
                                            bitmapDecodeLegacy.recycle();
                                            if (bArrCreateScaledBitmap != bitmap) {
                                                FullM9Engine.recycle(bArrCreateScaledBitmap);
                                            }
                                            FullM9Engine.recycle(bitmap);
                                            FullM9Engine.recycle(null);
                                            return bitmapCreateScaledBitmap;
                                        } catch (Exception e4) {
                                            e3 = e4;
                                            Log.w("LegacyQuality", "Preserve model output", e3);
                                            if (bArrCreateScaledBitmap != bitmap) {
                                                FullM9Engine.recycle(bArrCreateScaledBitmap);
                                            }
                                            FullM9Engine.recycle(bitmap);
                                            FullM9Engine.recycle(bitmapCreateScaledBitmap);
                                            return bitmapDecodeLegacy;
                                        } catch (LinkageError e5) {
                                            e2 = e5;
                                            Log.w("LegacyQuality", "Detail library unavailable", e2);
                                            if (bArrCreateScaledBitmap != bitmap) {
                                                FullM9Engine.recycle(bArrCreateScaledBitmap);
                                            }
                                            FullM9Engine.recycle(bitmap);
                                            FullM9Engine.recycle(bitmapCreateScaledBitmap);
                                            return bitmapDecodeLegacy;
                                        } catch (OutOfMemoryError e6) {
                                            e = e6;
                                            Log.w("LegacyQuality", "Preserve model output after memory limit", e);
                                            if (bArrCreateScaledBitmap != bitmap) {
                                                FullM9Engine.recycle(bArrCreateScaledBitmap);
                                            }
                                            FullM9Engine.recycle(bitmap);
                                            FullM9Engine.recycle(bitmapCreateScaledBitmap);
                                            return bitmapDecodeLegacy;
                                        }
                                    } catch (Exception e7) {
                                        bitmapCreateScaledBitmap = null;
                                        e3 = e7;
                                    } catch (LinkageError e8) {
                                        bitmapCreateScaledBitmap = null;
                                        e2 = e8;
                                    } catch (OutOfMemoryError e9) {
                                        bitmapCreateScaledBitmap = null;
                                        e = e9;
                                    } catch (Throwable th) {
                                        th = th;
                                        threadLocal = null;
                                        r2 = bArrCreateScaledBitmap;
                                        r3 = threadLocal;
                                        if (r2 != bitmap) {
                                            FullM9Engine.recycle(r2);
                                        }
                                        FullM9Engine.recycle(bitmap);
                                        FullM9Engine.recycle(r3);
                                        throw th;
                                    }
                                } catch (Exception e10) {
                                    bitmapCreateScaledBitmap = null;
                                    e3 = e10;
                                    bArrCreateScaledBitmap = 0;
                                } catch (LinkageError e11) {
                                    bitmapCreateScaledBitmap = null;
                                    e2 = e11;
                                    bArrCreateScaledBitmap = 0;
                                } catch (OutOfMemoryError e12) {
                                    bitmapCreateScaledBitmap = null;
                                    e = e12;
                                    bArrCreateScaledBitmap = 0;
                                } catch (Throwable th2) {
                                    th = th2;
                                    r3 = 0;
                                    if (r2 != bitmap) {
                                        FullM9Engine.recycle(r2);
                                    }
                                    FullM9Engine.recycle(bitmap);
                                    FullM9Engine.recycle(r3);
                                    throw th;
                                }
                            } catch (Exception e13) {
                                bitmap = bitmapDecode;
                                bArrCreateScaledBitmap = 0;
                                e3 = e13;
                                bitmapCreateScaledBitmap = null;
                            } catch (LinkageError e14) {
                                bitmap = bitmapDecode;
                                bArrCreateScaledBitmap = 0;
                                e2 = e14;
                                bitmapCreateScaledBitmap = null;
                            } catch (OutOfMemoryError e15) {
                                bitmap = bitmapDecode;
                                bArrCreateScaledBitmap = 0;
                                e = e15;
                                bitmapCreateScaledBitmap = null;
                            } catch (Throwable th3) {
                                th = th3;
                                bitmap = bitmapDecode;
                            }
                        }
                        FullM9Engine.recycle(null);
                        FullM9Engine.recycle(null);
                        return bitmapDecodeLegacy;
                    } catch (Exception e16) {
                        bitmapCreateScaledBitmap = null;
                        bitmap = null;
                        e3 = e16;
                        bArrCreateScaledBitmap = 0;
                    } catch (LinkageError e17) {
                        bitmapCreateScaledBitmap = null;
                        bitmap = null;
                        e2 = e17;
                        bArrCreateScaledBitmap = 0;
                    } catch (OutOfMemoryError e18) {
                        bitmapCreateScaledBitmap = null;
                        bitmap = null;
                        e = e18;
                        bArrCreateScaledBitmap = 0;
                    } catch (Throwable th4) {
                        th = th4;
                        r3 = 0;
                        bitmap = null;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
            }
        }
        return bitmapDecodeLegacy;
    }

    public static byte[] exif(byte[] bArr, byte[] bArr2, boolean z, int i, int i2, boolean z2) throws Exception {
        byte[] bArrExifLegacy = MomentBridge.exifLegacy(bArr, bArr2, z, i, i2, z2);
        return SOURCE.get() != null ? ExifOrientation.upright(bArrExifLegacy) : bArrExifLegacy;
    }
}
