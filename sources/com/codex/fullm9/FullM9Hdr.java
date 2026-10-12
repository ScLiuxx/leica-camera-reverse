package com.codex.fullm9;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Gainmap;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class FullM9Hdr {
    private static final ThreadLocal<String> STATE = new ThreadLocal<>();
    private static final ThreadLocal<Bitmap> RAW_MAP = new ThreadLocal<>();
    private static final ThreadLocal<Float> RAW_BOOST = new ThreadLocal<>();

    public static void clear() {
        STATE.remove();
        RAW_BOOST.remove();
        RAW_MAP.remove();
    }

    public static void attachRaw(Bitmap bitmap, Bitmap bitmap2, float f) {
        if (Build.VERSION.SDK_INT < 34) {
            return;
        }
        if (!Float.isFinite(f) || f <= 1.0f || f > 8.0f) {
            throw new IllegalArgumentException("RAW HDR range");
        }
        RAW_BOOST.set(Float.valueOf(f));
        RAW_MAP.set(bitmap2);
        Api34.clear(bitmap);
        STATE.set("source=linear-RAW-deferred max=" + f);
    }

    public static void transfer(Bitmap bitmap, Bitmap bitmap2, byte[] bArr, boolean z) {
        String str;
        if (Build.VERSION.SDK_INT < 34 || bitmap2 == null || RAW_BOOST.get() != null) {
            return;
        }
        try {
            Api34.clear(bitmap2);
            STATE.set("source=deferred-final-SDR max=" + HdrMath.limit(z));
        } catch (Exception e) {
            e = e;
            str = "HDR preparation failed";
            Log.w("FullM9HDR", str, e);
        } catch (LinkageError e2) {
            e = e2;
            str = "HDR API unavailable";
            Log.w("FullM9HDR", str, e);
        } catch (OutOfMemoryError e3) {
            e = e3;
            str = "HDR preparation memory";
            Log.w("FullM9HDR", str, e);
        }
    }

    public static void finish(Bitmap bitmap, Bitmap bitmap2, int i) {
        boolean z = true;
        if (i < 1 || i > 8 || Build.VERSION.SDK_INT < 34 || bitmap2 == null) {
            return;
        }
        try {
            if (RAW_BOOST.get() != null && bitmap == bitmap2 && RAW_MAP.get() != null) {
                Api34.attachRaw(bitmap2, RAW_MAP.get(), RAW_BOOST.get().floatValue());
                return;
            }
            Api34.generate(bitmap2, i == 1);
            ThreadLocal<String> threadLocal = STATE;
            StringBuilder sbAppend = new StringBuilder("source=final-canvas-area-average style=").append(i).append(" max=");
            if (i != 1) {
                z = false;
            }
            threadLocal.set(sbAppend.append(HdrMath.limit(z)).toString());
        } catch (Exception e) {
            Api34.clear(bitmap2);
            Log.w("FullM9HDR", "Final HDR failed", e);
        } catch (LinkageError e2) {
            Log.w("FullM9HDR", "Final HDR API unavailable", e2);
        } catch (OutOfMemoryError e3) {
            Api34.clear(bitmap2);
            Log.w("FullM9HDR", "Final HDR memory", e3);
        }
    }

    public static void verify(Context context, byte[] bArr, int i) {
        String str;
        if (i < 1) {
            return;
        }
        try {
            if (i > 8) {
                return;
            }
            try {
                String str2 = "style=" + i + " " + STATE.get() + " " + (Build.VERSION.SDK_INT >= 34 ? Api34.inspect(bArr) : "output=SDR(api<34)");
                Log.i("FullM9HDR", str2);
                if (context != null) {
                    Throwable th = null;
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(context.getFilesDir(), "lmoment-hdr-last.txt"));
                        try {
                            fileOutputStream.write((String.valueOf(str2) + "\n").getBytes("UTF-8"));
                            fileOutputStream.close();
                        } catch (Throwable th2) {
                            fileOutputStream.close();
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
                }
            } catch (Exception e) {
                e = e;
                str = "HDR verification failed";
                Log.w("FullM9HDR", str, e);
            } catch (LinkageError e2) {
                e = e2;
                str = "HDR verification API";
                Log.w("FullM9HDR", str, e);
            } catch (OutOfMemoryError e3) {
                e = e3;
                str = "HDR verification memory";
                Log.w("FullM9HDR", str, e);
            }
            clear();
        } catch (Throwable th4) {
            clear();
            throw th4;
        }
    }

    private static final class Api34 {
        static final int EDGE = 384;

        private Api34() {
        }

        static void attach(Bitmap bitmap, int[] iArr, int i, int i2, float f) {
            Gainmap gainmap = new Gainmap(Bitmap.createBitmap(iArr, i, i2, Bitmap.Config.ARGB_8888));
            gainmap.setRatioMin(1.0f, 1.0f, 1.0f);
            gainmap.setRatioMax(f, f, f);
            gainmap.setGamma(1.0f, 1.0f, 1.0f);
            gainmap.setEpsilonSdr(0.0f, 0.0f, 0.0f);
            gainmap.setEpsilonHdr(0.0f, 0.0f, 0.0f);
            gainmap.setMinDisplayRatioForHdrTransition(1.0f);
            gainmap.setDisplayRatioForFullHdr(f);
            bitmap.setGainmap(gainmap);
        }

        static void generate(Bitmap bitmap, boolean z) {
            float fMin = Math.min(1.0f, 384.0f / Math.max(bitmap.getWidth(), bitmap.getHeight()));
            int iMax = Math.max(1, Math.round(bitmap.getWidth() * fMin));
            int iMax2 = Math.max(1, Math.round(bitmap.getHeight() * fMin));
            attach(bitmap, GainPlane.smooth(GainPlane.fromPixels(NativeM9.hdrSample(bitmap, iMax, iMax2), iMax, iMax2), iMax, iMax2), iMax, iMax2, HdrMath.limit(z));
        }

        static void attachRaw(Bitmap bitmap, Bitmap bitmap2, float f) {
            float fMin = Math.min(1.0f, 384.0f / Math.max(bitmap2.getWidth(), bitmap2.getHeight()));
            int iMax = Math.max(1, Math.round(bitmap2.getWidth() * fMin));
            int iMax2 = Math.max(1, Math.round(bitmap2.getHeight() * fMin));
            attach(bitmap, GainPlane.smooth(NativeM9.hdrSample(bitmap2, iMax, iMax2), iMax, iMax2), iMax, iMax2, f);
        }

        static void clear(Bitmap bitmap) {
            try {
                bitmap.setGainmap(null);
            } catch (RuntimeException e) {
                Log.w("FullM9HDR", "Cannot clear invalid HDR", e);
            }
        }

        static boolean valid(Bitmap bitmap) {
            return bitmap.hasGainmap() && bitmap.getGainmap().getGainmapContents() != null;
        }

        static String inspect(byte[] bArr) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 64;
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
            if (bitmapDecodeByteArray == null) {
                return "output=decode-failed";
            }
            try {
                if (valid(bitmapDecodeByteArray)) {
                    Gainmap gainmap = bitmapDecodeByteArray.getGainmap();
                    Bitmap gainmapContents = gainmap.getGainmapContents();
                    float[] ratioMax = gainmap.getRatioMax();
                    return "output=UltraHDR map=" + gainmapContents.getWidth() + "x" + gainmapContents.getHeight() + " max=" + ratioMax[0] + "," + ratioMax[1] + "," + ratioMax[2];
                }
                return "output=SDR(no-gainmap)";
            } finally {
                bitmapDecodeByteArray.recycle();
            }
        }
    }
}
