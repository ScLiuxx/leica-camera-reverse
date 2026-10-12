package com.codex.leicaapp;

import Sh.h;
import Sh.t;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Gainmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.widget.Toast;
import com.codex.lmport.MomentBridge;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class LegendaryPostProcessor {
    private static final float M9_DENSITY_EV = -0.95f;
    private static final float M9_NEUTRAL_WARMTH = 0.3f;
    private static final String TAG = "LegendaryPost";
    private static final boolean WARM_GRAY_M9 = true;
    private static final CaptureRequest.Key<Integer> WB_PARTIAL;
    private static final CaptureRequest.Key<Integer> WB_TEMPERATURE;
    private static Context appContext = null;
    private static String cachePath = null;
    private static volatile long liveZoomTime = 0;
    private static volatile String m3ModelPath = null;
    private static volatile String m9ModelPath = null;
    private static String monopanRunner = null;
    private static volatile long previewMetadataTime = 0;
    private static CaptureResult previewResult = null;
    private static final RenderQueue renderQueue;
    private static String styleModels = null;
    private static String styleRunner = null;
    private static volatile boolean useM3 = false;
    private static final int whiteBalanceCct = 5000;
    private static final Map<Integer, Range<Float>> zoomRanges;
    private static final Map<CaptureRequest, Boolean> captureStyles = Collections.synchronizedMap(new WeakHashMap());
    private static final Map<CaptureRequest, CaptureResult> captureTuning = Collections.synchronizedMap(new WeakHashMap());
    private static volatile float previewZoom = 1.0f;
    private static volatile float liveZoom = Float.NaN;
    private static final String[] LUX_KEYS = {"com.qti.stats.internal.perFrame.AecLux", "com.qti.chi.statsaec.AecLux", "com.xiaomi.statsconfigs.AecLux", "com.xiaomi.mivi2.luxIndex", "org.quic.camera2.statsconfigs.AECLuxIndex"};
    private static final String[] CCT_KEYS = {"xiaomi.camera.awb.cct", "xiaomi.camera.awb.colorTemperature", "com.xiaomi.miCam.awbWrapper.awbCct", "org.quic.camera2.statsconfigs.AWBFrameControlCCT"};

    private static native boolean applyAdaptiveLut(Bitmap bitmap, String str, float f, float f2);

    private static native boolean applyBoldOriginalFilter(Bitmap bitmap, String str, float f, float f2, float f3, float f4, float f5);

    private static native boolean applyLut(Bitmap bitmap, String str, int i);

    private static native boolean applyMonochrome(Bitmap bitmap, String str, String str2);

    private static native boolean applyOriginalFilter(Bitmap bitmap, String str, float f, float f2, float f3, float f4);

    private static native boolean applyStyle(Bitmap bitmap, String str, String str2, String str3, int i, int i2, int i3);

    private static native boolean applyWarmOriginalFilter(Bitmap bitmap, String str, float f, float f2, float f3, float f4, float f5, float f6, float f7);

    private static CaptureResult captureResult(Object obj) {
        if (!(obj instanceof t)) {
            return null;
        }
        t tVar = (t) obj;
        h hVar = tVar.f;
        if (hVar != null) {
            TotalCaptureResult totalCaptureResult = hVar.b;
            if (totalCaptureResult != null) {
                return totalCaptureResult;
            }
            CaptureResult captureResult = hVar.c;
            if (captureResult != null) {
                return captureResult;
            }
        }
        return tVar.legendaryPreviewResult;
    }

    public static synchronized float logicalZoom(float f, int i) {
        Boolean bool;
        if (!Float.isFinite(f) || f <= 0.0f) {
            return Float.NaN;
        }
        if (CameraZoomCalibration.nativeMomentZoom(i)) {
            return f;
        }
        synchronized (LegendaryPostProcessor.class) {
            try {
                if (appContext == null) {
                    appContext = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
                }
                Map<Integer, Range<Float>> map = zoomRanges;
                Integer numValueOf = Integer.valueOf(i);
                if (!map.containsKey(numValueOf)) {
                    CameraCharacteristics cameraCharacteristics = ((CameraManager) appContext.getSystemService("camera")).getCameraCharacteristics(String.valueOf(i));
                    Range<Float> range = (Range) cameraCharacteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE);
                    int[] iArr = (int[]) cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                    boolean z = false;
                    if (iArr != null) {
                        for (int i2 : iArr) {
                            if (i2 == 11) {
                                z = WARM_GRAY_M9;
                                break;
                            }
                        }
                    }
                    map.put(numValueOf, range);
                    map.put((Integer) String.valueOf(i), (Range<Float>) Boolean.valueOf(z));
                }
                Range<Float> range2 = map.get(numValueOf);
                if (range2 != null) {
                    float fFloatValue = ((Float) range2.getLower()).floatValue();
                    float fFloatValue2 = ((Float) range2.getUpper()).floatValue();
                    if (Float.isFinite(fFloatValue) && Float.isFinite(fFloatValue2) && fFloatValue > 0.0f && fFloatValue2 >= fFloatValue && (fFloatValue < 1.0f || (f >= 1.0f && (bool = (Boolean) map.get(String.valueOf(i))) != null && bool.booleanValue()))) {
                        float fFloatValue3 = ((Float) range2.clamp(Float.valueOf(f))).floatValue();
                        SoftwareRaw.setZoom(String.valueOf(i), fFloatValue3);
                        return fFloatValue3;
                    }
                }
            } catch (Throwable th) {
            }
            return Float.NaN;
        }
    }

    public static byte[] process(byte[] bArr, Object obj) {
        byte[] bArrProcess = MomentBridge.process(bArr, obj);
        return bArrProcess != null ? bArrProcess : processOriginal(bArr, obj);
    }

    /* JADX WARN: Code duplicated, block: B:136:0x02ee  */
    /* JADX WARN: Code duplicated, block: B:178:0x0399  */
    /* JADX WARN: Code duplicated, block: B:181:0x03a7 A[Catch: all -> 0x02ea, TRY_ENTER, TryCatch #6 {all -> 0x02ea, blocks: (B:125:0x02b9, B:127:0x02bd, B:129:0x02d2, B:181:0x03a7, B:185:0x03b9, B:208:0x0437, B:138:0x02f2, B:140:0x02f6, B:159:0x032d, B:163:0x0338, B:165:0x0340, B:176:0x0378, B:168:0x034c, B:170:0x0352, B:172:0x035a), top: B:286:0x02b9 }] */
    /* JADX WARN: Code duplicated, block: B:183:0x03b4  */
    /* JADX WARN: Code duplicated, block: B:184:0x03b7  */
    /* JADX WARN: Code duplicated, block: B:195:0x040a  */
    /* JADX WARN: Code duplicated, block: B:197:0x040d A[Catch: all -> 0x0556, TryCatch #12 {all -> 0x0556, blocks: (B:123:0x02ab, B:197:0x040d, B:199:0x0412, B:204:0x0427, B:201:0x041c, B:203:0x0422), top: B:297:0x02ab }] */
    /* JADX WARN: Code duplicated, block: B:199:0x0412 A[Catch: all -> 0x0556, TryCatch #12 {all -> 0x0556, blocks: (B:123:0x02ab, B:197:0x040d, B:199:0x0412, B:204:0x0427, B:201:0x041c, B:203:0x0422), top: B:297:0x02ab }] */
    /* JADX WARN: Code duplicated, block: B:200:0x041a A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:201:0x041c A[Catch: all -> 0x0556, TryCatch #12 {all -> 0x0556, blocks: (B:123:0x02ab, B:197:0x040d, B:199:0x0412, B:204:0x0427, B:201:0x041c, B:203:0x0422), top: B:297:0x02ab }] */
    /* JADX WARN: Code duplicated, block: B:206:0x0432  */
    /* JADX WARN: Code duplicated, block: B:208:0x0437 A[Catch: all -> 0x02ea, TRY_ENTER, TRY_LEAVE, TryCatch #6 {all -> 0x02ea, blocks: (B:125:0x02b9, B:127:0x02bd, B:129:0x02d2, B:181:0x03a7, B:185:0x03b9, B:208:0x0437, B:138:0x02f2, B:140:0x02f6, B:159:0x032d, B:163:0x0338, B:165:0x0340, B:176:0x0378, B:168:0x034c, B:170:0x0352, B:172:0x035a), top: B:286:0x02b9 }] */
    /* JADX WARN: Code duplicated, block: B:210:0x043c  */
    /* JADX WARN: Code duplicated, block: B:213:0x044d A[Catch: all -> 0x055c, TRY_ENTER, TRY_LEAVE, TryCatch #15 {all -> 0x055c, blocks: (B:95:0x0241, B:102:0x0269, B:117:0x028e, B:122:0x02a8, B:213:0x044d, B:120:0x02a2, B:108:0x0276, B:110:0x0283, B:101:0x025a), top: B:303:0x0241 }] */
    /* JADX WARN: Code duplicated, block: B:215:0x0455  */
    /* JADX WARN: Code duplicated, block: B:217:0x045a  */
    /* JADX WARN: Code duplicated, block: B:220:0x0483 A[Catch: all -> 0x0552, TRY_ENTER, TryCatch #11 {all -> 0x0552, blocks: (B:211:0x043d, B:220:0x0483, B:224:0x048c), top: B:295:0x043d }] */
    /* JADX WARN: Code duplicated, block: B:222:0x0489  */
    /* JADX WARN: Code duplicated, block: B:223:0x048b  */
    /* JADX WARN: Code duplicated, block: B:228:0x04b3  */
    /* JADX WARN: Code duplicated, block: B:229:0x04b6  */
    /* JADX WARN: Code duplicated, block: B:232:0x0520  */
    /* JADX WARN: Code duplicated, block: B:234:0x0525  */
    /* JADX WARN: Code duplicated, block: B:24:0x0074  */
    /* JADX WARN: Code duplicated, block: B:262:0x059b A[DONT_GENERATE] */
    /* JADX WARN: Code duplicated, block: B:264:0x05a0 A[DONT_GENERATE] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v116 */
    /* JADX WARN: Type inference failed for: r0v58 */
    /* JADX WARN: Type inference failed for: r0v66 */
    /* JADX WARN: Type inference failed for: r0v86 */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 7 */
    public static byte[] processOriginal(byte[] bArr, Object obj) {
        boolean z;
        Bitmap bitmap;
        Gainmap gainmap;
        ColorSpace.Named named;
        Gainmap gainmap2;
        CaptureResult captureResult;
        ?? r0;
        boolean z2;
        int i;
        Bitmap bitmapApply;
        ByteArrayOutputStream byteArrayOutputStream;
        boolean z3;
        Bitmap bitmap2;
        String str;
        String str2;
        int i2;
        char c;
        String str3 = "render queue done pending=";
        if (bArr == null || bArr.length < 4) {
            return bArr;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.e(TAG, "Unexpected main-thread saver; preserve source without blocking shutter");
            return JpegMetadata.copyExif(bArr, bArr, useM3);
        }
        CaptureResult captureResultCapture = SoftwareRaw.capture(obj, captureResult(obj));
        CaptureResult captureResultImageMetadata = SoftwareRaw.imageMetadata(captureResultCapture);
        boolean z4 = captureResultCapture instanceof TotalCaptureResult;
        Boolean bool = z4 ? captureStyles.get(((TotalCaptureResult) captureResultCapture).getRequest()) : null;
        boolean zBooleanValue = bool != null ? bool.booleanValue() : useM3;
        if (RawBurstPolicy.useIspInput() || !z4) {
            z = false;
        } else {
            Integer num = 1;
            if (num.equals(((TotalCaptureResult) captureResultCapture).getRequest().get(WB_PARTIAL))) {
                z = true;
            } else {
                z = false;
            }
        }
        CaptureResult captureResult2 = z4 ? captureTuning.get(((TotalCaptureResult) captureResultCapture).getRequest()) : null;
        String[] strArr = CCT_KEYS;
        float fMetadata = metadata(captureResult2, Float.NaN, strArr);
        String[] strArr2 = LUX_KEYS;
        M9CaptureTuning m9CaptureTuning = new M9CaptureTuning(fMetadata, metadata(captureResult2, Float.NaN, strArr2));
        float fMetadata2 = metadata(z ? null : captureResultImageMetadata, Float.NaN, strArr);
        float fMetadata3 = metadata(captureResultImageMetadata, Float.NaN, strArr2);
        if (z) {
            m9CaptureTuning = null;
        }
        M9CaptureTuning m9CaptureTuning2 = new M9CaptureTuning(fMetadata2, fMetadata3, m9CaptureTuning);
        int iStyleCct = m9CaptureTuning2.cct;
        if (!zBooleanValue) {
            iStyleCct = LeicaDaylight.styleCct(iStyleCct, m9CaptureTuning2.cctSource, captureResultImageMetadata);
        }
        int iStyleLux = m9CaptureTuning2.lux;
        if (!zBooleanValue) {
            iStyleLux = LeicaDaylight.styleLux(iStyleLux, m9CaptureTuning2.luxSource, captureResultImageMetadata);
        }
        if (!zBooleanValue) {
            Log.i(TAG, "M9 shot tuning cct=" + iStyleCct + " source=" + m9CaptureTuning2.cctSource + " lux=" + iStyleLux + " source=" + m9CaptureTuning2.luxSource + " wb=" + (RawBurstPolicy.useIspInput() ? "ISP_AUTO_SCENE_BIAS" : "RAW_REQUEST_5000"));
        }
        float fMax = Math.max(0.1f, Math.min(200.0f, SoftwareRaw.displayZoom(captureResultCapture, previewZoom)));
        float fUptimeMillis = SystemClock.uptimeMillis() % 60000;
        if (bArr == null || bArr.length < 4) {
            return bArr;
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        try {
            RenderQueue renderQueue2 = renderQueue;
            int i3 = iStyleLux;
            RenderQueue.Ticket ticketEnter = renderQueue2.enter(bArr.length);
            if (ticketEnter == null) {
                Log.w(TAG, "render queue full; original saved");
                return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
            }
            CaptureResult captureResult3 = captureResult2;
            Log.i(TAG, "render queue start pending=" + renderQueue2.pending() + " waitedMs=" + (SystemClock.elapsedRealtime() - jElapsedRealtime));
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = WARM_GRAY_M9;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                if (zBooleanValue) {
                    try {
                        named = ColorSpace.Named.SRGB;
                    } catch (Throwable th) {
                        th = th;
                        bitmap = null;
                        gainmap = null;
                        try {
                            Log.e(TAG, "post-process failed", th);
                            return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                        } finally {
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            if (gainmap != null) {
                                gainmap.getGainmapContents().recycle();
                            }
                            ticketEnter.close();
                            Log.i(TAG, str3 + renderQueue.pending());
                        }
                    }
                } else {
                    named = ColorSpace.Named.DISPLAY_P3;
                }
                options.inPreferredColorSpace = ColorSpace.get(named);
                Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
                if (bitmapDecodeByteArray == null) {
                    try {
                        Log.e(TAG, "JPEG decode returned null");
                        if (bitmapDecodeByteArray != null) {
                            bitmapDecodeByteArray.recycle();
                        }
                        ticketEnter.close();
                        Log.i(TAG, "render queue done pending=" + renderQueue2.pending());
                        return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                    } catch (Throwable th2) {
                        th = th2;
                        bitmap = bitmapDecodeByteArray;
                        gainmap = null;
                        Log.e(TAG, "post-process failed", th);
                        return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                    }
                }
                try {
                    if (!options.inPreferredColorSpace.equals(bitmapDecodeByteArray.getColorSpace())) {
                        gainmap2 = null;
                        try {
                            throw new IllegalStateException("JPEG working color space unavailable");
                        } catch (Throwable th3) {
                            th = th3;
                            gainmap = gainmap2;
                            bitmap = bitmapDecodeByteArray;
                            Log.e(TAG, "post-process failed", th);
                            return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                        }
                    }
                    gainmap = null;
                    try {
                        HdrGainmaps.attach(bitmapDecodeByteArray, null);
                        try {
                            Bitmap bitmapDevelop = SoftwareRaw.develop(captureResultCapture, bitmapDecodeByteArray, zBooleanValue ^ WARM_GRAY_M9);
                            if (bitmapDevelop != bitmapDecodeByteArray) {
                                bitmapDecodeByteArray.recycle();
                            }
                            bitmap = bitmapDevelop;
                        } catch (Exception e) {
                            bitmapDecodeByteArray = bitmapDecodeByteArray;
                            try {
                                Log.w(TAG, "RAW fallback to camera JPEG: " + e);
                                ensureModels();
                                if (appContext != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.codex.leicaapp.LegendaryPostProcessor$$ExternalSyntheticLambda1
                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            Toast.makeText(LegendaryPostProcessor.appContext, "本帧 RAW 不可用，已使用相机 JPEG 处理", 0).show();
                                        }
                                    });
                                }
                                bitmap = bitmapDecodeByteArray;
                            } catch (Throwable th4) {
                                th = th4;
                                str3 = "render queue done pending=";
                                ticketEnter = ticketEnter;
                                bitmap = bitmapDecodeByteArray;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            str3 = "render queue done pending=";
                            ticketEnter = ticketEnter;
                            bitmap = bitmapDecodeByteArray;
                        }
                        try {
                            float fMetadata4 = metadata(captureResultImageMetadata, Float.NaN, "android.sensor.sensitivity");
                            if (!Float.isFinite(fMetadata4) || fMetadata4 <= 0.0f) {
                                fMetadata4 = metadata(captureResult3, Float.NaN, "android.sensor.sensitivity");
                            }
                            float attributeInt = fMetadata4;
                            if (!Float.isFinite(attributeInt) || attributeInt <= 0.0f) {
                                try {
                                    try {
                                        attributeInt = new ExifInterface(new ByteArrayInputStream(bArr)).getAttributeInt("ISOSpeedRatings", 0);
                                    } catch (IOException e2) {
                                    }
                                } catch (IOException e3) {
                                }
                            }
                            float f = iStyleCct;
                            float[] fArrGains = SceneWhiteBalance.gains(f, m9CaptureTuning2.measuredCct);
                            SceneWhiteBalance.target(f);
                            boolean z5 = m9CaptureTuning2.measuredCct;
                            Bitmap bitmapApply2 = !zBooleanValue ? bitmap : FilmInput.apply(bitmap, zBooleanValue, attributeInt, fArrGains);
                            if (bitmapApply2 != bitmap) {
                                bitmap.recycle();
                            }
                            try {
                                int width = bitmapApply2.getWidth();
                                int height = bitmapApply2.getHeight();
                                ensureModels();
                                if (zBooleanValue) {
                                    try {
                                        if (m3ModelPath != null) {
                                            captureResult = captureResultCapture;
                                            r0 = (applyOriginalFilter(bitmapApply2, m3ModelPath, (float) i3, f, fMax, fUptimeMillis) && applyMonochrome(bitmapApply2, monopanRunner, cachePath)) ? WARM_GRAY_M9 : false;
                                            z2 = WARM_GRAY_M9;
                                            i = i3;
                                        } else {
                                            i = i3;
                                            if (!zBooleanValue || m9ModelPath == null) {
                                                captureResult = captureResultCapture;
                                                z2 = WARM_GRAY_M9;
                                                r0 = 0;
                                            } else {
                                                ExifInterface exifInterface = new ExifInterface(new ByteArrayInputStream(bArr));
                                                captureResult = captureResultCapture;
                                                z2 = WARM_GRAY_M9;
                                                int attributeInt2 = exifInterface.getAttributeInt("Orientation", 1);
                                                if (attributeInt2 == 3 || attributeInt2 == 4) {
                                                    i2 = 180;
                                                } else if (attributeInt2 == 5 || attributeInt2 == 6) {
                                                    i2 = 90;
                                                } else {
                                                    i2 = (attributeInt2 == 7 || attributeInt2 == 8) ? 270 : 0;
                                                }
                                                float fDenoise = (!Float.isFinite(attributeInt) || attributeInt <= 0.0f) ? 0.6f : FilmTone.denoise(attributeInt);
                                                int i4 = i2;
                                                if (M9ReferenceTone.tryApply(bitmapApply2, bArr, iStyleCct, captureResult)) {
                                                    c = 2;
                                                } else if (M9ReferenceTone.allowLegacy()) {
                                                    c = (applyStyleOriented(bitmapApply2, i4, i, iStyleCct) && applyWarmOriginalFilter(bitmapApply2, m9ModelPath, (float) i, f, fMax, fUptimeMillis, fDenoise, M9_DENSITY_EV, M9_NEUTRAL_WARMTH)) ? (char) 1 : (char) 0;
                                                }
                                                Log.i(TAG, "M9 source color; legacy model only on color failure " + fDenoise + " ISO=" + attributeInt);
                                                r0 = c;
                                            }
                                        }
                                        if (r0 == 0) {
                                            StringBuilder sbAppend = new StringBuilder().append("Original algorithm failed for ");
                                            if (zBooleanValue) {
                                                str2 = "M3";
                                            } else {
                                                str2 = "M9";
                                            }
                                            Log.e(TAG, sbAppend.append(str2).toString());
                                        } else {
                                            if (r0 != 2) {
                                                B43Tone.apply(bitmapApply2, zBooleanValue);
                                                if (!zBooleanValue) {
                                                    CaptureResult captureResult4 = captureResult;
                                                    LeicaDaylight.apply(bitmapApply2, iStyleCct, captureResult4);
                                                    M9ReferenceTone.apply(bitmapApply2, bArr, iStyleCct, captureResult4);
                                                    if (!zBooleanValue && !CameraZoomCalibration.stockOptical()) {
                                                        LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                                    }
                                                }
                                            } else if (!zBooleanValue) {
                                                LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                            }
                                            bitmapApply = OriginalWatermark.apply(bitmapApply2, obj, bArr, captureResult);
                                            if (!zBooleanValue) {
                                            }
                                            if (bitmapApply != bitmapApply2) {
                                                bitmapApply2.recycle();
                                                bitmap = bitmapApply;
                                            } else {
                                                bitmap = bitmapApply2;
                                            }
                                            try {
                                                byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                                                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                                                    Log.e(TAG, "JPEG encode failed");
                                                    if (bitmap != null) {
                                                        bitmap.recycle();
                                                    }
                                                    if (0 != 0) {
                                                        gainmap.getGainmapContents().recycle();
                                                    }
                                                    ticketEnter.close();
                                                    Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                                    return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                                }
                                                byte[] byteArray = byteArrayOutputStream.toByteArray();
                                                if (zBooleanValue) {
                                                    z3 = false;
                                                } else {
                                                    z3 = z2;
                                                }
                                                bitmap2 = bitmap;
                                                ticketEnter = ticketEnter;
                                                try {
                                                    byte[] bArrCopyExif = JpegMetadata.copyExif(bArr, byteArray, z3, bitmap.getWidth(), bitmap.getHeight(), WARM_GRAY_M9);
                                                    StringBuilder sbAppend2 = new StringBuilder().append("processed=");
                                                    if (zBooleanValue) {
                                                        str = "M3";
                                                    } else {
                                                        str = "M9";
                                                    }
                                                    Log.i(TAG, sbAppend2.append(str).append(" lux=").append(i).append(" cct=").append(iStyleCct).append(" zoom=").append(fMax).append(" ").append(width).append("x").append(height).append(" space=").append(bitmap2.getColorSpace().getName()).append(" bytes=").append(bArr.length).append("->").append(bArrCopyExif.length).toString());
                                                    if (bitmap2 != null) {
                                                        bitmap2.recycle();
                                                    }
                                                    if (0 != 0) {
                                                        gainmap.getGainmapContents().recycle();
                                                    }
                                                    ticketEnter.close();
                                                    Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                                    return bArrCopyExif;
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    str3 = "render queue done pending=";
                                                    bitmap = bitmap2;
                                                }
                                            } catch (Throwable th7) {
                                                th = th7;
                                                str3 = "render queue done pending=";
                                                ticketEnter = ticketEnter;
                                            }
                                            str3 = "render queue done pending=";
                                            ticketEnter = ticketEnter;
                                        }
                                    } catch (Throwable th8) {
                                        th = th8;
                                        bitmap = bitmapApply2;
                                    }
                                    Log.e(TAG, "post-process failed", th);
                                    return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                }
                                i = i3;
                                if (zBooleanValue) {
                                    captureResult = captureResultCapture;
                                    z2 = WARM_GRAY_M9;
                                    r0 = 0;
                                    if (r0 == 0) {
                                        StringBuilder sbAppend3 = new StringBuilder().append("Original algorithm failed for ");
                                        if (zBooleanValue) {
                                            str2 = "M3";
                                        } else {
                                            str2 = "M9";
                                        }
                                        Log.e(TAG, sbAppend3.append(str2).toString());
                                    } else {
                                        if (r0 != 2) {
                                            B43Tone.apply(bitmapApply2, zBooleanValue);
                                            if (!zBooleanValue) {
                                                CaptureResult captureResult5 = captureResult;
                                                LeicaDaylight.apply(bitmapApply2, iStyleCct, captureResult5);
                                                M9ReferenceTone.apply(bitmapApply2, bArr, iStyleCct, captureResult5);
                                                if (!zBooleanValue) {
                                                    LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                                }
                                            }
                                        } else if (!zBooleanValue) {
                                            LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                        }
                                        bitmapApply = OriginalWatermark.apply(bitmapApply2, obj, bArr, captureResult);
                                        if (!zBooleanValue) {
                                        }
                                        if (bitmapApply != bitmapApply2) {
                                            bitmapApply2.recycle();
                                            bitmap = bitmapApply;
                                        } else {
                                            bitmap = bitmapApply2;
                                        }
                                        byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                                            Log.e(TAG, "JPEG encode failed");
                                            if (bitmap != null) {
                                                bitmap.recycle();
                                            }
                                            if (0 != 0) {
                                                gainmap.getGainmapContents().recycle();
                                            }
                                            ticketEnter.close();
                                            Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                            return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                        }
                                        byte[] byteArray2 = byteArrayOutputStream.toByteArray();
                                        if (zBooleanValue) {
                                            z3 = z2;
                                        } else {
                                            z3 = false;
                                        }
                                        bitmap2 = bitmap;
                                        ticketEnter = ticketEnter;
                                        byte[] bArrCopyExif2 = JpegMetadata.copyExif(bArr, byteArray2, z3, bitmap.getWidth(), bitmap.getHeight(), WARM_GRAY_M9);
                                        StringBuilder sbAppend4 = new StringBuilder().append("processed=");
                                        if (zBooleanValue) {
                                            str = "M3";
                                        } else {
                                            str = "M9";
                                        }
                                        Log.i(TAG, sbAppend4.append(str).append(" lux=").append(i).append(" cct=").append(iStyleCct).append(" zoom=").append(fMax).append(" ").append(width).append("x").append(height).append(" space=").append(bitmap2.getColorSpace().getName()).append(" bytes=").append(bArr.length).append("->").append(bArrCopyExif2.length).toString());
                                        if (bitmap2 != null) {
                                            bitmap2.recycle();
                                        }
                                        if (0 != 0) {
                                            gainmap.getGainmapContents().recycle();
                                        }
                                        ticketEnter.close();
                                        Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                        return bArrCopyExif2;
                                        str3 = "render queue done pending=";
                                        ticketEnter = ticketEnter;
                                    }
                                    Log.e(TAG, "post-process failed", th);
                                    return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                }
                                captureResult = captureResultCapture;
                                z2 = WARM_GRAY_M9;
                                r0 = 0;
                                if (r0 == 0) {
                                    StringBuilder sbAppend5 = new StringBuilder().append("Original algorithm failed for ");
                                    if (zBooleanValue) {
                                        str2 = "M3";
                                    } else {
                                        str2 = "M9";
                                    }
                                    Log.e(TAG, sbAppend5.append(str2).toString());
                                } else {
                                    if (r0 != 2) {
                                        B43Tone.apply(bitmapApply2, zBooleanValue);
                                        if (!zBooleanValue) {
                                            CaptureResult captureResult6 = captureResult;
                                            LeicaDaylight.apply(bitmapApply2, iStyleCct, captureResult6);
                                            M9ReferenceTone.apply(bitmapApply2, bArr, iStyleCct, captureResult6);
                                            if (!zBooleanValue) {
                                                LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                            }
                                        }
                                    } else if (!zBooleanValue) {
                                        LeicaQuality.apply(bitmapApply2, bArr, zBooleanValue, appContext);
                                    }
                                    bitmapApply = OriginalWatermark.apply(bitmapApply2, obj, bArr, captureResult);
                                    if (!zBooleanValue) {
                                    }
                                    if (bitmapApply != bitmapApply2) {
                                        bitmapApply2.recycle();
                                        bitmap = bitmapApply;
                                    } else {
                                        bitmap = bitmapApply2;
                                    }
                                    byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                                        Log.e(TAG, "JPEG encode failed");
                                        if (bitmap != null) {
                                            bitmap.recycle();
                                        }
                                        if (0 != 0) {
                                            gainmap.getGainmapContents().recycle();
                                        }
                                        ticketEnter.close();
                                        Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                        return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                    }
                                    byte[] byteArray3 = byteArrayOutputStream.toByteArray();
                                    if (zBooleanValue) {
                                        z3 = z2;
                                    } else {
                                        z3 = false;
                                    }
                                    bitmap2 = bitmap;
                                    ticketEnter = ticketEnter;
                                    byte[] bArrCopyExif3 = JpegMetadata.copyExif(bArr, byteArray3, z3, bitmap.getWidth(), bitmap.getHeight(), WARM_GRAY_M9);
                                    StringBuilder sbAppend6 = new StringBuilder().append("processed=");
                                    if (zBooleanValue) {
                                        str = "M3";
                                    } else {
                                        str = "M9";
                                    }
                                    Log.i(TAG, sbAppend6.append(str).append(" lux=").append(i).append(" cct=").append(iStyleCct).append(" zoom=").append(fMax).append(" ").append(width).append("x").append(height).append(" space=").append(bitmap2.getColorSpace().getName()).append(" bytes=").append(bArr.length).append("->").append(bArrCopyExif3.length).toString());
                                    if (bitmap2 != null) {
                                        bitmap2.recycle();
                                    }
                                    if (0 != 0) {
                                        gainmap.getGainmapContents().recycle();
                                    }
                                    ticketEnter.close();
                                    Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                    return bArrCopyExif3;
                                    str3 = "render queue done pending=";
                                    ticketEnter = ticketEnter;
                                }
                                Log.e(TAG, "post-process failed", th);
                                return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                                if (bitmapApply2 != null) {
                                    bitmapApply2.recycle();
                                }
                                if (0 != 0) {
                                    gainmap.getGainmapContents().recycle();
                                }
                                ticketEnter.close();
                                Log.i(TAG, "render queue done pending=" + renderQueue.pending());
                                return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
                            } catch (Throwable th9) {
                                th = th9;
                                str3 = "render queue done pending=";
                                ticketEnter = ticketEnter;
                                bitmap = bitmapApply2;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        str3 = "render queue done pending=";
                        ticketEnter = ticketEnter;
                        bitmapDecodeByteArray = bitmapDecodeByteArray;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    gainmap2 = null;
                }
            } catch (Throwable th13) {
                th = th13;
                str3 = "render queue done pending=";
                ticketEnter = ticketEnter;
                bitmap = null;
                gainmap = null;
            }
        } catch (InterruptedException e4) {
            Thread.currentThread().interrupt();
            queueNotice("处理已中断，本张已保留原片");
            return JpegMetadata.copyExif(bArr, bArr, zBooleanValue);
        }
    }

    private static native boolean protectShadows(Bitmap bitmap, Bitmap bitmap2, boolean z);

    static {
        try {
            System.loadLibrary("leicamodelembed");
        } catch (Throwable th) {
            Log.e(TAG, "APK native Leica runner unavailable", th);
        }
        WB_TEMPERATURE = new CaptureRequest.Key<>("org.codeaurora.qcamera3.manualWB.color_temperature", Integer.class);
        WB_PARTIAL = new CaptureRequest.Key<>("org.codeaurora.qcamera3.manualWB.partial_mwb_mode", Integer.class);
        zoomRanges = new HashMap();
        renderQueue = new RenderQueue(6, 100663296L);
    }

    static synchronized void copyCaptureStyle(CaptureRequest captureRequest, CaptureRequest captureRequest2) {
        Map<CaptureRequest, Boolean> map = captureStyles;
        Boolean bool = map.get(captureRequest);
        map.put(captureRequest2, Boolean.valueOf(bool != null ? bool.booleanValue() : useM3));
        captureTuning.put(captureRequest2, snapshotPreview());
    }

    static boolean isM9() {
        return useM3 ^ WARM_GRAY_M9;
    }

    public static float currentPreviewZoom() {
        if (SystemClock.elapsedRealtime() - liveZoomTime <= 150) {
            return liveZoom;
        }
        return Float.NaN;
    }

    public static void rememberCapture(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult, CameraCaptureSession.CaptureCallback captureCallback) {
        if (SoftwareRaw.observe(cameraCaptureSession, captureResult)) {
            float fDisplayZoom = SoftwareRaw.displayZoom(captureResult, previewZoom);
            previewZoom = fDisplayZoom;
            liveZoom = fDisplayZoom;
            liveZoomTime = SystemClock.elapsedRealtime();
            CaptureResult captureResultPreviewMetadata = SoftwareRaw.previewMetadata(cameraCaptureSession, captureResult);
            Integer num = 2;
            if (!num.equals(captureRequest.get(CaptureRequest.CONTROL_CAPTURE_INTENT))) {
                rememberPreview(captureResultPreviewMetadata);
            }
        }
    }

    static synchronized void resetPreviewMetadata() {
        previewMetadataTime = 0L;
        previewResult = null;
    }

    static synchronized void rememberPreview(CaptureResult captureResult) {
        if (captureResult == null) {
            return;
        }
        previewResult = captureResult;
        previewMetadataTime = SystemClock.elapsedRealtime();
    }

    private static synchronized CaptureResult snapshotPreview() {
        return ((previewMetadataTime > 0L ? 1 : (previewMetadataTime == 0L ? 0 : -1)) <= 0 || ((SystemClock.elapsedRealtime() - previewMetadataTime) > 1000L ? 1 : ((SystemClock.elapsedRealtime() - previewMetadataTime) == 1000L ? 0 : -1)) > 0) ? false : WARM_GRAY_M9 ? previewResult : null;
    }

    private LegendaryPostProcessor() {
    }

    static void applyFixedWhiteBalance(CaptureRequest.Builder builder) {
        builder.set(WB_TEMPERATURE, Integer.valueOf(whiteBalanceCct));
        builder.set(WB_PARTIAL, 1);
        builder.set(CaptureRequest.CONTROL_AWB_MODE, 0);
        builder.set(CaptureRequest.CONTROL_AWB_LOCK, false);
        builder.set(CaptureRequest.COLOR_CORRECTION_MODE, 1);
    }

    /* JADX WARN: Unreachable blocks removed: 5, instructions: 18 */
    public static synchronized CaptureRequest buildRequest(CaptureRequest.Builder builder) {
        LowLightCapture.restore(builder);
        SoftwareRaw.restoreFilmExposure(builder);
        CaptureRequest captureRequestBuild = CameraCompat.build(builder);
        if (captureRequestBuild.isReprocess()) {
            return captureRequestBuild;
        }
        try {
            Integer num = 2;
            boolean zEquals = num.equals(captureRequestBuild.get(CaptureRequest.CONTROL_CAPTURE_INTENT));
            RawBurstPolicy.useIspInput();
            builder.set(WB_TEMPERATURE, 0);
            builder.set(WB_PARTIAL, 0);
            builder.set(CaptureRequest.CONTROL_AWB_MODE, 1);
            builder.set(CaptureRequest.COLOR_CORRECTION_MODE, 1);
            SoftwareRaw.target(builder);
            CaptureRequest captureRequestBuild2 = CameraCompat.build(builder);
            SoftwareRaw.rememberRequest(captureRequestBuild2, builder);
            if (zEquals) {
                captureStyles.put(captureRequestBuild2, Boolean.valueOf(useM3));
                captureTuning.put(captureRequestBuild2, snapshotPreview());
            }
            return captureRequestBuild2;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Device rejected M9 white balance", e);
            return captureRequestBuild;
        }
    }

    public static synchronized void setMode(String str) {
        useM3 = "M3".equals(str);
        Log.i(TAG, "mode=" + (useM3 ? "M3" : "M9"));
    }

    private static synchronized void ensureModels() {
        if (m3ModelPath == null || m9ModelPath == null || styleModels == null) {
            try {
                Context context = (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
                if (context == null) {
                    return;
                }
                appContext = context;
                styleRunner = context.getApplicationInfo().nativeLibraryDir + "/libstyletrans_runner.so";
                File file = new File(context.getFilesDir(), "leica-v4");
                File file2 = new File(file, ".ready");
                if (!file2.isFile()) {
                    copyTree(context, "leica-v4", file);
                    file2.createNewFile();
                }
                ModelAssetCheck.ensure(context, file);
                styleModels = file.getAbsolutePath();
                monopanRunner = context.getApplicationInfo().nativeLibraryDir + "/libmonopan_runner.so";
                cachePath = context.getCacheDir().getAbsolutePath();
                m3ModelPath = copyAsset(context, "leica_filter_param_m3.bin");
                m9ModelPath = copyAsset(context, "leica_filter_param_m9_snapshot.bin");
                Log.i(TAG, "APK Leica model chain ready M3=" + new File(m3ModelPath).length() + " M9=" + new File(m9ModelPath).length());
            } catch (Throwable th) {
                Log.e(TAG, "APK Leica model initialization failed", th);
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:38:0x0057 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private static String copyAsset(Context context, String str) throws Exception {
        File file = new File(context.getFilesDir(), str);
        if (file.exists() && file.length() > 4096) {
            return file.getAbsolutePath();
        }
        InputStream inputStreamOpen = context.getAssets().open(str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                byte[] bArr = new byte[32768];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i < 0) {
                        break;
                    }
                    if (i > 0) {
                        fileOutputStream.write(bArr, 0, i);
                    }
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
                return file.getAbsolutePath();
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

    /* JADX WARN: Code duplicated, block: B:34:0x006e A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private static void copyTree(Context context, String str, File file) throws Exception {
        String[] list = context.getAssets().list(str);
        if (list.length > 0) {
            file.mkdirs();
            for (String str2 : list) {
                copyTree(context, str + "/" + str2, new File(file, str2));
            }
            return;
        }
        file.getParentFile().mkdirs();
        InputStream inputStreamOpen = context.getAssets().open(str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                byte[] bArr = new byte[65536];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i <= 0) {
                        break;
                    } else {
                        fileOutputStream.write(bArr, 0, i);
                    }
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

    private static float metadata(CaptureResult captureResult, float f, String... strArr) {
        if (captureResult != null) {
            try {
                List<CaptureResult.Key<?>> keys = captureResult.getKeys();
                for (String str : strArr) {
                    for (CaptureResult.Key<?> key : keys) {
                        if (str.equals(key.getName())) {
                            Object obj = captureResult.get(key);
                            if (obj != null && obj.getClass().isArray() && Array.getLength(obj) > 0) {
                                obj = Array.get(obj, 0);
                            }
                            if (obj instanceof Number) {
                                float fFloatValue = ((Number) obj).floatValue();
                                if (Float.isFinite(fFloatValue)) {
                                    return fFloatValue;
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                Log.w(TAG, "Capture value unavailable", e);
            }
        }
        return f;
    }

    private static void queueNotice(final String str) {
        if (appContext != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.codex.leicaapp.LegendaryPostProcessor$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    Toast.makeText(LegendaryPostProcessor.appContext, str, 0).show();
                }
            });
        }
    }

    private static boolean applyStyleOriented(Bitmap bitmap, int i, int i2, int i3) {
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            return applyStyle(bitmap, styleRunner, cachePath, styleModels, i, i2, i3);
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(-90.0f);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        try {
            if (applyStyle(bitmapCreateBitmap, styleRunner, cachePath, styleModels, (i + 90) % 360, i2, i3)) {
                Matrix matrix2 = new Matrix();
                matrix2.setRotate(90.0f);
                matrix2.postTranslate(bitmap.getWidth(), 0.0f);
                new Canvas(bitmap).drawBitmap(bitmapCreateBitmap, matrix2, null);
                return WARM_GRAY_M9;
            }
            return false;
        } finally {
            bitmapCreateBitmap.recycle();
        }
    }

    public static String previewShader(String str, int i) {
        return str;
    }

    public static void bindPreview(int i, int i2, int i3, Rect rect) {
    }
}
