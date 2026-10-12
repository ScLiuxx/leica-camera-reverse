package com.codex.leicaapp;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.ImageReader;
import android.util.Log;
import com.android.camera.module.Z;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class MomentCaptureChain {
    public static CaptureRequest buildRequest(CaptureRequest.Builder builder) {
        return enabled() ? LegendaryPostProcessor.buildRequest(builder) : CameraCompat.build(builder);
    }

    public static ImageReader createPhotoReader(int i, int i2, int i3, int i4) {
        return (!enabled() || CameraZoomCalibration.stockOptical()) ? ImageReader.newInstance(i, i2, i3, i4) : SoftwareRaw.createPhotoReader(i, i2, i3, i4);
    }

    public static CaptureRequest.Builder createRequest(CameraDevice cameraDevice, int i) throws Exception {
        if (enabled()) {
            CaptureRequest.Builder builderCreateRequest = SoftwareRaw.createRequest(cameraDevice, i);
            CameraZoomCalibration.nativeRequest(builderCreateRequest, cameraDevice.getId());
            return builderCreateRequest;
        }
        CaptureRequest.Builder builderCreateRequest2 = CameraCompat.createRequest(cameraDevice, i);
        CameraZoomCalibration.nativeRequest(builderCreateRequest2, cameraDevice.getId());
        return builderCreateRequest2;
    }

    public static void createSession(CameraDevice cameraDevice, SessionConfiguration sessionConfiguration) throws Exception {
        SessionConfiguration sessionConfigurationNativeSession = CameraZoomCalibration.nativeSession(cameraDevice, sessionConfiguration);
        if (enabled()) {
            Log.i("LeicaChain", "base54 capture session");
            SoftwareRaw.createSession(cameraDevice, sessionConfigurationNativeSession);
        } else {
            Log.i("LeicaChain", "original capture session");
            CameraCompat.session(cameraDevice, sessionConfigurationNativeSession);
        }
    }

    public static boolean enabled() {
        return Z.a == 256;
    }

    public static float logicalZoom(float f, int i) {
        if (enabled()) {
            return LegendaryPostProcessor.logicalZoom(f, i);
        }
        return Float.NaN;
    }

    public static void remember(TotalCaptureResult totalCaptureResult) {
        CameraZoomCalibration.observe(totalCaptureResult);
        if (enabled()) {
            SoftwareRaw.remember(totalCaptureResult);
        }
    }

    public static void rememberCapture(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult, CameraCaptureSession.CaptureCallback captureCallback) {
        if (enabled()) {
            LegendaryPostProcessor.rememberCapture(cameraCaptureSession, captureRequest, captureResult, captureCallback);
        }
    }
}
