package com.codex.leicaapp;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Build;
import android.util.Log;
import java.util.TreeSet;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class CameraZoomCalibration {
    private static final String TAG = "LeicaZoomCal";
    private static Float[] cached;
    private static Float observedFocal;

    public static String[] cameraIds(CameraManager cameraManager, String[] strArr, int i) {
        return AdaptiveLens.enumerate(cameraManager, strArr, i);
    }

    private static synchronized Float[] discover() {
        return AdaptiveLens.markers();
    }

    public static Float[] markers(Float[] fArr, boolean z) {
        return !z ? AdaptiveLens.modeMarkers(AdaptiveLens.currentMode(), fArr) : fArr;
    }

    public static boolean nativeMomentZoom(int i) {
        return AdaptiveLens.hasCamera(i);
    }

    public static void nativeRequest(CaptureRequest.Builder builder, String str) {
        AdaptiveLens.attach(builder, str);
    }

    public static SessionConfiguration nativeSession(CameraDevice cameraDevice, SessionConfiguration sessionConfiguration) {
        return AdaptiveLens.session(cameraDevice, sessionConfiguration);
    }

    public static float teleRatio() {
        float f = Float.NaN;
        for (Float f2 : AdaptiveLens.markers()) {
            float fFloatValue = f2.floatValue();
            if (fFloatValue > 1.0f) {
                return fFloatValue;
            }
            f = Float.NaN;
        }
        return f;
    }

    static boolean baselineDevice(String str, String str2) {
        return "pandora".equals(str) && "25098PN5AC".equals(str2);
    }

    static boolean stockOpticalDevice(String str) {
        return "nezha".equals(str);
    }

    public static boolean stockOptical() {
        if (AdaptiveLens.mode(AdaptiveLens.currentMode())) {
            return false;
        }
        return stockOpticalDevice(Build.DEVICE);
    }

    private static boolean baseline() {
        return baselineDevice(Build.DEVICE, Build.MODEL);
    }

    public static boolean nativeLensTag(CameraCharacteristics cameraCharacteristics, String str) {
        if (!stockOptical() || cameraCharacteristics == null || str == null) {
            return false;
        }
        try {
            Object obj = Class.forName("ia.A0").getField("G2").get(null);
            if (str.equals(obj.getClass().getMethod("b", new Class[0]).invoke(obj, new Object[0]))) {
                Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                return num != null && num.intValue() == 1 && Boolean.TRUE.equals((Boolean) cameraCharacteristics.get(new CameraCharacteristics.Key("com.xiaomi.camera.smoothTransition.enableOptZoomratio", Boolean.class)));
            }
            Class<?> cls = Class.forName("ia.w0");
            String[] strArr = {"b2", "c2", "v", "A0", "C0", "t1", "N3"};
            for (int i = 0; i < 7; i++) {
                Object obj2 = cls.getField(strArr[i]).get(null);
                CameraCharacteristics.Key key = (CameraCharacteristics.Key) obj2.getClass().getMethod("a", new Class[0]).invoke(obj2, new Object[0]);
                if (str.equals(key.getName())) {
                    return cameraCharacteristics.get(key) != null;
                }
            }
            return false;
        } catch (Exception | LinkageError e) {
            Log.w(TAG, "Native lens tag unavailable: " + str, e);
        }
    }

    public static void observe(CaptureResult captureResult) {
        Float f;
        if (stockOptical() && captureResult != null && (f = (Float) captureResult.get(CaptureResult.LENS_FOCAL_LENGTH)) != null && !f.equals(observedFocal)) {
            observedFocal = f;
            Log.i(TAG, "sensor focal=" + f + " aperture=" + captureResult.get(CaptureResult.LENS_APERTURE) + " zoom=" + captureResult.get(CaptureResult.CONTROL_ZOOM_RATIO));
        }
    }

    static Float[] calibratedMarkers(Float[] fArr, Float[] fArr2) {
        if (fArr == null || fArr2.length == 0) {
            return fArr;
        }
        TreeSet treeSet = new TreeSet();
        for (Float f : fArr) {
            if (f != null && Float.isFinite(f.floatValue())) {
                if (Math.abs(f.floatValue() - 3.2f) > 0.001f) {
                    treeSet.add(f);
                }
            } else {
                return fArr;
            }
        }
        for (Float f2 : fArr2) {
            if (f2 != null && Float.isFinite(f2.floatValue()) && f2.floatValue() >= 2.0f && f2.floatValue() <= 20.0f) {
                treeSet.add(f2);
            }
        }
        return (Float[]) treeSet.toArray(new Float[0]);
    }

    static Float[] ratios(float f, float[] fArr) {
        TreeSet treeSet = new TreeSet();
        if (!Float.isFinite(f) || f <= 0.0f) {
            return new Float[0];
        }
        for (float f2 : fArr) {
            float f3 = f2 / f;
            if (Float.isFinite(f3) && f3 >= 2.0f && f3 <= 20.0f) {
                treeSet.add(Float.valueOf(Math.round(f3 * 10.0f) / 10.0f));
            }
        }
        return (Float[]) treeSet.toArray(new Float[0]);
    }
}
