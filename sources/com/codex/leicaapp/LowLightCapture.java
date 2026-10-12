package com.codex.leicaapp;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Log;
import android.util.Range;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
final class LowLightCapture {
    static final String MFNR = "org.codeaurora.qcamera3.sessionParameters.enableMFNR";
    private static final Map<CaptureRequest.Builder, CaptureRequest[]> overrides = new WeakHashMap();
    private static final CaptureRequest.Key<?>[] EXPOSURE_KEYS = {CaptureRequest.CONTROL_AE_MODE, CaptureRequest.SENSOR_EXPOSURE_TIME, CaptureRequest.SENSOR_SENSITIVITY, CaptureRequest.SENSOR_FRAME_DURATION, CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST};

    LowLightCapture() {
    }

    static synchronized void restore(CaptureRequest.Builder builder) {
        CaptureRequest[] captureRequestArrRemove = overrides.remove(builder);
        if (captureRequestArrRemove == null) {
            return;
        }
        CaptureRequest captureRequestBuild = builder.build();
        for (CaptureRequest.Key<?> key : EXPOSURE_KEYS) {
            if (!Objects.equals(captureRequestBuild.get(key), captureRequestArrRemove[1].get(key))) {
                return;
            }
        }
        for (CaptureRequest.Key<?> key2 : EXPOSURE_KEYS) {
            builder.set(key2, captureRequestArrRemove[0].get(key2));
        }
    }

    private static boolean has(int[] iArr, int i) {
        if (iArr != null) {
            for (int i2 : iArr) {
                if (i2 == i) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean supportsMfnr(CameraCharacteristics cameraCharacteristics) {
        if (cameraCharacteristics.getAvailableCaptureRequestKeys() != null) {
            Iterator<CaptureRequest.Key<?>> it = cameraCharacteristics.getAvailableCaptureRequestKeys().iterator();
            while (it.hasNext()) {
                if (MFNR.equals(it.next().getName())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    static CaptureRequest session(CameraDevice cameraDevice, CaptureRequest captureRequest, CameraCharacteristics cameraCharacteristics) throws CameraAccessException {
        if (!supportsMfnr(cameraCharacteristics)) {
            return captureRequest;
        }
        boolean z = false;
        if (cameraCharacteristics.getAvailableSessionKeys() != null) {
            Iterator<CaptureRequest.Key<?>> it = cameraCharacteristics.getAvailableSessionKeys().iterator();
            while (it.hasNext()) {
                if (MFNR.equals(it.next().getName())) {
                    z = true;
                }
            }
        }
        if (!z) {
            return captureRequest;
        }
        CaptureRequest.Builder builderCreateCaptureRequest = cameraDevice.createCaptureRequest(1);
        if (captureRequest != null) {
            for (CaptureRequest.Key<?> key : captureRequest.getKeys()) {
                builderCreateCaptureRequest.set(key, captureRequest.get(key));
            }
        }
        builderCreateCaptureRequest.set(new CaptureRequest.Key(MFNR, Integer.class), 1);
        return builderCreateCaptureRequest.build();
    }

    static void apply(CaptureRequest.Builder builder, CameraCharacteristics cameraCharacteristics, CaptureResult captureResult, float f, boolean z, boolean z2) {
        long[] jArrExposure;
        CaptureRequest captureRequestBuild = builder.build();
        Integer num = 1;
        if (z2) {
            builder.set(new CaptureRequest.Key(MFNR, Integer.class), num);
        }
        if (has((int[]) cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION), 1)) {
            builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, num);
        }
        Integer num2 = 2;
        if (!num2.equals(captureRequestBuild.get(CaptureRequest.CONTROL_CAPTURE_INTENT)) || !z || captureResult == null) {
            return;
        }
        Integer num3 = (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Integer num4 = (Integer) captureResult.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST);
        Long l = (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME);
        if (num3 == null || l == null) {
            return;
        }
        if (LowLightPolicy.dark(num3.intValue(), num4 == null ? 100 : num4.intValue(), l.longValue())) {
            if (has((int[]) cameraCharacteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES), 2)) {
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, 2);
            }
            if (num3.intValue() >= 800) {
                if (has((int[]) cameraCharacteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES), 0)) {
                    builder.set(CaptureRequest.EDGE_MODE, 0);
                }
                Range[] rangeArr = (Range[]) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                if (rangeArr != null) {
                    for (Range range : rangeArr) {
                        if (((Integer) range.getLower()).intValue() == 10 && ((Integer) range.getUpper()).intValue() == 30) {
                            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, range);
                            break;
                        }
                    }
                }
                Log.i("LeicaLowLight", "High ISO retain AE ISO=" + num3 + " NR=" + builder.get(CaptureRequest.NOISE_REDUCTION_MODE) + " EDGE=" + builder.get(CaptureRequest.EDGE_MODE) + " FPS=" + builder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE));
                return;
            }
            Integer num5 = (Integer) captureRequestBuild.get(CaptureRequest.CONTROL_AE_MODE);
            Integer num6 = (Integer) captureRequestBuild.get(CaptureRequest.FLASH_MODE);
            Integer num7 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
            if (num.equals(num5)) {
                if (num6 == null || num6.intValue() == 0) {
                    Integer num8 = 2;
                    if (!num8.equals(num7)) {
                        Integer num9 = 3;
                        if (!num9.equals(num7)) {
                            return;
                        }
                    }
                    if (has((int[]) cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES), 1)) {
                        Range range2 = (Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                        Range range3 = (Range) cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                        if (range2 == null || range3 == null || (jArrExposure = LowLightPolicy.exposure(num3.intValue(), l.longValue(), f, ((Integer) range2.getLower()).intValue(), ((Integer) range2.getUpper()).intValue(), ((Long) range3.getLower()).longValue(), ((Long) range3.getUpper()).longValue())) == null) {
                            return;
                        }
                        builder.set(CaptureRequest.CONTROL_AE_MODE, 0);
                        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(jArrExposure[0]));
                        builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf((int) jArrExposure[1]));
                        builder.set(CaptureRequest.SENSOR_FRAME_DURATION, Long.valueOf(Math.max(33333333L, jArrExposure[0])));
                        if (num4 != null) {
                            builder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, num4);
                        }
                        synchronized (LowLightCapture.class) {
                            overrides.put(builder, new CaptureRequest[]{captureRequestBuild, builder.build()});
                        }
                        Log.i("LeicaLowLight", "short exposure zoom=" + f + " ns=" + l + "->" + jArrExposure[0] + " ISO=" + num3 + "->" + jArrExposure[1] + " MFNR=" + z2);
                    }
                }
            }
        }
    }
}
