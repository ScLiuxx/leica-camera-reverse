package com.codex.leicaapp;

import android.app.Application;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.util.SizeF;
import java.util.Iterator;
import java.util.TreeSet;

/* JADX INFO: compiled from: CameraZoomCalibration.java */
/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class CameraZoomCalibrationBaseline {
    private static final String TAG = "LeicaZoomCal";
    private static float cached = Float.NaN;

    private CameraZoomCalibrationBaseline() {
    }

    public static Float[] markers(Float[] fArr, boolean z) {
        return z ? fArr : calibratedMarkers(fArr, teleRatio());
    }

    static Float[] calibratedMarkers(Float[] fArr, float f) {
        if (fArr == null || !Float.isFinite(f) || f < 2.0f || f > 20.0f) {
            return fArr;
        }
        TreeSet treeSet = new TreeSet();
        boolean z = false;
        for (Float f2 : fArr) {
            if (f2 != null && Float.isFinite(f2.floatValue())) {
                if (Math.abs(f2.floatValue() - 3.2f) < 0.001f && Math.abs(f - f2.floatValue()) > 0.001f) {
                    treeSet.add(Float.valueOf(f));
                    z = true;
                } else {
                    treeSet.add(f2);
                }
            } else {
                return fArr;
            }
        }
        return z ? (Float[]) treeSet.toArray(new Float[0]) : fArr;
    }

    public static synchronized float teleRatio() {
        if (Float.isFinite(cached)) {
            return cached;
        }
        try {
            Application application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
            if (application == null) {
                return Float.NaN;
            }
            CameraManager cameraManager = (CameraManager) application.getSystemService("camera");
            if (cameraManager == null) {
                return Float.NaN;
            }
            TreeSet treeSet = new TreeSet();
            for (String str : cameraManager.getCameraIdList()) {
                treeSet.add(str);
                treeSet.addAll(cameraManager.getCameraCharacteristics(str).getPhysicalCameraIds());
            }
            Iterator it = treeSet.iterator();
            float f = Float.NaN;
            float f2 = 0.0f;
            float f3 = 0.0f;
            while (it.hasNext()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics((String) it.next());
                Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                float[] fArr = (float[]) cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                SizeF sizeF = (SizeF) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                if (num != null && num.intValue() == 1 && fArr != null && fArr.length != 0 && sizeF != null && sizeF.getWidth() > 0.0f && sizeF.getHeight() > 0.0f) {
                    float width = fArr[0] / sizeF.getWidth();
                    float width2 = sizeF.getWidth() * sizeF.getHeight();
                    if (Float.isFinite(width) && width > 0.0f) {
                        if (width2 > f3) {
                            f = width;
                            f3 = width2;
                        }
                        if (width > f2) {
                            f2 = width;
                        }
                    }
                }
            }
            if (Float.isFinite(f) && f > 0.0f) {
                float f4 = f2 / f;
                if (Float.isFinite(f4) && f4 >= 2.0f && f4 <= 20.0f) {
                    cached = Math.round(f4 * 10.0f) / 10.0f;
                    Log.i(TAG, "physical rear tele ratio=" + cached);
                    return cached;
                }
                return Float.NaN;
            }
            return Float.NaN;
        } catch (Exception e) {
            Log.w(TAG, "Camera characteristics unavailable", e);
            return Float.NaN;
        }
    }
}
