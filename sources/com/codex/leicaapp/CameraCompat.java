package com.codex.leicaapp;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes14.dex */
public final class CameraCompat {
    public static CaptureRequest.Builder createRequest(CameraDevice cameraDevice, int i) throws Exception {
        CaptureRequest.Builder builderCreateCaptureRequest = cameraDevice.createCaptureRequest(i);
        AdaptiveLens.attach(builderCreateCaptureRequest, cameraDevice.getId());
        return builderCreateCaptureRequest;
    }

    public static CaptureRequest build(CaptureRequest.Builder builder) {
        AdaptiveLens.apply(builder);
        return builder.build();
    }

    public static void session(CameraDevice cameraDevice, SessionConfiguration sessionConfiguration) throws Exception {
        cameraDevice.createCaptureSession(AdaptiveLens.session(cameraDevice, sessionConfiguration));
    }

    public static void pin(OutputConfiguration outputConfiguration, String str) {
        if (AdaptiveLens.enabled(AdaptiveLens.currentMode())) {
            return;
        }
        outputConfiguration.setPhysicalCameraId(str);
    }
}
