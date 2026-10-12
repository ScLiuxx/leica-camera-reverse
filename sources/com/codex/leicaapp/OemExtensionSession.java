package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraExtensionCharacteristics;
import android.hardware.camera2.CameraExtensionSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.ExtensionSessionConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.view.PixelCopy;
import android.view.Surface;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
final class OemExtensionSession extends CameraCaptureSession {
    private boolean closed;
    private final SessionConfiguration configuration;
    private boolean copying;
    private final CameraDevice device;
    private final Range<Integer> evRange;
    private final Rational evStep;
    private CameraExtensionSession extension;
    private boolean failed;
    private long frames;
    private long holdExposureUntil;
    private Integer lastEv;
    private long meterLogTime;
    private TotalCaptureResult meterResult;
    private long meterTime;
    private Surface preview;
    private CaptureRequest repeating;
    private CameraCaptureSession.CaptureCallback repeatingCallback;
    private Executor repeatingExecutor;
    private final Set<CaptureRequest.Key> supported;
    private final Handler meterHandler = new Handler(Looper.getMainLooper());
    private final M9AutoExposure filmExposure = new M9AutoExposure();
    private float exposureBias = 0.33333334f;
    private final Runnable meterTick = new Runnable() { // from class: com.codex.leicaapp.OemExtensionSession$$ExternalSyntheticLambda3
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.sampleExposure();
        }
    };

    static void open(CameraDevice cameraDevice, final SessionConfiguration sessionConfiguration, CameraExtensionCharacteristics cameraExtensionCharacteristics, CameraCharacteristics cameraCharacteristics, final Runnable runnable) throws CameraAccessException {
        Set<CaptureRequest.Key> availableCaptureRequestKeys = cameraExtensionCharacteristics.getAvailableCaptureRequestKeys(0);
        if (availableCaptureRequestKeys != null) {
            if (availableCaptureRequestKeys.contains(cameraCharacteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE) != null ? CaptureRequest.CONTROL_ZOOM_RATIO : CaptureRequest.SCALER_CROP_REGION)) {
                cameraDevice.createExtensionSession(new ExtensionSessionConfiguration(0, sessionConfiguration.getOutputConfigurations(), sessionConfiguration.getExecutor(), new CameraExtensionSession.StateCallback() { // from class: com.codex.leicaapp.OemExtensionSession.1
                    @Override // android.hardware.camera2.CameraExtensionSession.StateCallback
                    public void onConfigured(CameraExtensionSession cameraExtensionSession) {
                        OemExtensionSession.this.extension = cameraExtensionSession;
                        Log.i("LeicaExtension", "OEM automatic session ready");
                        sessionConfiguration.getStateCallback().onConfigured(OemExtensionSession.this);
                        sessionConfiguration.getStateCallback().onReady(OemExtensionSession.this);
                    }

                    @Override // android.hardware.camera2.CameraExtensionSession.StateCallback
                    public void onConfigureFailed(CameraExtensionSession cameraExtensionSession) {
                        OemExtensionSession.this.failed = true;
                        Log.w("LeicaExtension", "OEM configuration unavailable; using existing JPEG route");
                        runnable.run();
                    }

                    @Override // android.hardware.camera2.CameraExtensionSession.StateCallback
                    public void onClosed(CameraExtensionSession cameraExtensionSession) {
                        if (OemExtensionSession.this.failed) {
                            return;
                        }
                        sessionConfiguration.getStateCallback().onClosed(OemExtensionSession.this);
                    }
                }));
                return;
            }
        }
        runnable.run();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 7 */
    private CaptureRequest request(CaptureRequest captureRequest, boolean z) throws Exception {
        Integer num;
        Object obj;
        CaptureRequest.Builder builderCreateRequest = CameraCompat.createRequest(this.device, z ? 2 : 1);
        builderCreateRequest.setTag(captureRequest.getTag());
        for (CaptureRequest.Key key : this.supported) {
            if (!key.equals(CaptureRequest.SENSOR_EXPOSURE_TIME) && !key.equals(CaptureRequest.SENSOR_SENSITIVITY) && !key.equals(CaptureRequest.SENSOR_FRAME_DURATION) && !key.equals(CaptureRequest.TONEMAP_CURVE) && !key.getName().startsWith("org.codeaurora.qcamera3.manualWB.") && !key.equals(CaptureRequest.CONTROL_AWB_MODE) && !key.equals(CaptureRequest.CONTROL_AWB_LOCK) && !key.getName().startsWith("android.colorCorrection.") && (obj = captureRequest.get(key)) != null) {
                builderCreateRequest.set(key, obj);
            }
        }
        builderCreateRequest.set(CaptureRequest.CONTROL_AE_MODE, 1);
        builderCreateRequest.set(CaptureRequest.CONTROL_AWB_MODE, 1);
        if (this.supported.contains(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) && this.evRange != null && this.evStep != null) {
            Integer num2 = (Integer) captureRequest.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
            if (!LegendaryPostProcessor.isM9() || (num2 != null && num2.intValue() != 0)) {
                this.filmExposure.reset();
                this.exposureBias = 0.33333334f;
            }
            int iExposureCompensation = M9CaptureTuning.exposureCompensation(num2 == null ? 0 : num2.intValue(), this.evStep.floatValue(), ((Integer) this.evRange.getLower()).intValue(), ((Integer) this.evRange.getUpper()).intValue(), this.exposureBias);
            builderCreateRequest.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(iExposureCompensation));
            if (z || (num = this.lastEv) == null || num.intValue() != iExposureCompensation) {
                Log.i("LeicaExtension", "Film EV request=" + (iExposureCompensation * this.evStep.floatValue()) + " steps=" + iExposureCompensation + " userSteps=" + num2 + " still=" + z);
            }
            this.lastEv = Integer.valueOf(iExposureCompensation);
        } else if (z) {
            Log.w("LeicaExtension", "Film EV unavailable: extension does not advertise compensation");
        }
        for (Surface surface : targets(captureRequest)) {
            if (!z || surface != this.preview) {
                builderCreateRequest.addTarget(surface);
            }
        }
        CaptureRequest captureRequestBuild = CameraCompat.build(builderCreateRequest);
        if (z) {
            LegendaryPostProcessor.copyCaptureStyle(captureRequest, captureRequestBuild);
            Log.i("LeicaExtension", "OEM still WB request mode=" + captureRequestBuild.get(CaptureRequest.CONTROL_AWB_MODE) + " lock=" + captureRequestBuild.get(CaptureRequest.CONTROL_AWB_LOCK) + " OEM auto WB; creative WB is applied after JPEG");
        }
        return captureRequestBuild;
    }

    static /* synthetic */ long access$204(OemExtensionSession oemExtensionSession) {
        long j = oemExtensionSession.frames + 1;
        oemExtensionSession.frames = j;
        return j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void sampleExposure() {
        try {
            if (!this.closed && this.repeating != null) {
                if (!this.copying && SystemClock.elapsedRealtime() >= this.holdExposureUntil) {
                    final CaptureRequest captureRequest = this.repeating;
                    final TotalCaptureResult totalCaptureResult = SystemClock.elapsedRealtime() - this.meterTime <= 1000 ? this.meterResult : null;
                    final Bitmap bitmapCreateBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
                    this.copying = true;
                    try {
                        PixelCopy.request(this.preview, bitmapCreateBitmap, new PixelCopy.OnPixelCopyFinishedListener() { // from class: com.codex.leicaapp.OemExtensionSession$$ExternalSyntheticLambda4
                            @Override // android.view.PixelCopy.OnPixelCopyFinishedListener
                            public final void onPixelCopyFinished(int i) {
                                this.f$0.m17lambda$sampleExposure$0$comcodexleicaappOemExtensionSession(captureRequest, totalCaptureResult, bitmapCreateBitmap, i);
                            }
                        }, this.meterHandler);
                    } catch (RuntimeException e) {
                        this.copying = false;
                        bitmapCreateBitmap.recycle();
                        scheduleMeter();
                    }
                    return;
                }
                scheduleMeter();
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    /* JADX INFO: renamed from: lambda$sampleExposure$0$com-codex-leicaapp-OemExtensionSession, reason: not valid java name */
    /* synthetic */ void m17lambda$sampleExposure$0$comcodexleicaappOemExtensionSession(CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult, Bitmap bitmap, int i) {
        synchronized (this) {
            this.copying = false;
            try {
                if (!this.closed && this.repeating == captureRequest && SystemClock.elapsedRealtime() >= this.holdExposureUntil) {
                    int[] iArr = null;
                    Integer num = totalCaptureResult == null ? null : (Integer) totalCaptureResult.get(CaptureResult.SENSOR_SENSITIVITY);
                    Long l = totalCaptureResult == null ? null : (Long) totalCaptureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME);
                    Integer num2 = (Integer) captureRequest.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
                    Integer num3 = (Integer) captureRequest.get(CaptureRequest.FLASH_MODE);
                    if (i == 0) {
                        iArr = new int[4096];
                        bitmap.getPixels(iArr, 0, 64, 0, 0, 64, 64);
                    }
                    int[] iArr2 = iArr;
                    if (LegendaryPostProcessor.isM9() && ((num2 == null || num2.intValue() == 0) && (num3 == null || num3.intValue() == 0))) {
                        this.filmExposure.update(iArr2, 64, 64, num == null ? 0 : num.intValue(), l == null ? 0L : l.longValue(), SystemClock.elapsedRealtime());
                    } else {
                        this.filmExposure.reset();
                    }
                    float fBias = this.filmExposure.bias();
                    if (SystemClock.elapsedRealtime() - this.meterLogTime >= 10000) {
                        this.meterLogTime = SystemClock.elapsedRealtime();
                        Log.i("LeicaExtension", "EV meter copy=" + i + " ISO=" + num + " exposureNs=" + l + " targetBias=" + fBias);
                    }
                    float f = this.exposureBias;
                    float fMax = f + Math.max(-0.16666667f, Math.min(0.16666667f, fBias - f));
                    if (Math.abs(fMax - this.exposureBias) > 0.001f && this.supported.contains(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)) {
                        float f2 = this.exposureBias;
                        this.exposureBias = fMax;
                        try {
                            int[] iArr3 = {-1};
                            iArr3[0] = this.extension.setRepeatingRequest(request(captureRequest, false), this.repeatingExecutor, callbacks(captureRequest, this.repeatingCallback, iArr3));
                            Log.i("LeicaExtension", "Adaptive EV bias=" + this.exposureBias + " ISO=" + num + " exposureNs=" + l);
                        } catch (CameraAccessException | RuntimeException e) {
                            this.exposureBias = f2;
                            Log.w("LeicaExtension", "Adaptive EV deferred", e);
                        }
                    }
                    bitmap.recycle();
                    scheduleMeter();
                    return;
                }
                bitmap.recycle();
                scheduleMeter();
            } catch (Throwable th) {
                bitmap.recycle();
                scheduleMeter();
                throw th;
            }
        }
    }

    private void scheduleMeter() {
        this.meterHandler.removeCallbacks(this.meterTick);
        if (this.closed || this.repeating == null) {
            return;
        }
        this.meterHandler.postDelayed(this.meterTick, 500L);
    }

    private OemExtensionSession(CameraDevice cameraDevice, SessionConfiguration sessionConfiguration, Set<CaptureRequest.Key> set, CameraCharacteristics cameraCharacteristics) {
        this.device = cameraDevice;
        this.configuration = sessionConfiguration;
        this.supported = set;
        LegendaryPostProcessor.resetPreviewMetadata();
        this.evRange = (Range) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        this.evStep = (Rational) cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
    }

    private Executor executor(final Handler handler) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return new Executor() { // from class: com.codex.leicaapp.OemExtensionSession$$ExternalSyntheticLambda0
            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                handler.post(runnable);
            }
        };
    }

    private Collection<Surface> targets(CaptureRequest captureRequest) {
        try {
            return (Collection) CaptureRequest.class.getMethod("getTargets", new Class[0]).invoke(captureRequest, new Object[0]);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot inspect existing request surfaces", e);
        }
    }

    /* JADX INFO: renamed from: com.codex.leicaapp.OemExtensionSession$2, reason: invalid class name */
    class AnonymousClass2 extends CameraExtensionSession.ExtensionCaptureCallback {
        final /* synthetic */ CameraCaptureSession.CaptureCallback val$callback;
        final /* synthetic */ CaptureRequest val$original;
        final /* synthetic */ int[] val$sequence;

        AnonymousClass2(CameraCaptureSession.CaptureCallback captureCallback, CaptureRequest captureRequest, int[] iArr) {
            this.val$callback = captureCallback;
            this.val$original = captureRequest;
            this.val$sequence = iArr;
        }

        @Override // android.hardware.camera2.CameraExtensionSession.ExtensionCaptureCallback
        public void onCaptureStarted(CameraExtensionSession cameraExtensionSession, CaptureRequest captureRequest, long j) {
            CameraCaptureSession.CaptureCallback captureCallback = this.val$callback;
            if (captureCallback != null) {
                OemExtensionSession oemExtensionSession = OemExtensionSession.this;
                captureCallback.onCaptureStarted(oemExtensionSession, this.val$original, j, OemExtensionSession.access$204(oemExtensionSession));
            }
        }

        @Override // android.hardware.camera2.CameraExtensionSession.ExtensionCaptureCallback
        public void onCaptureResultAvailable(CameraExtensionSession cameraExtensionSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            Integer num = 2;
            if (!num.equals(this.val$original.get(CaptureRequest.CONTROL_CAPTURE_INTENT))) {
                LegendaryPostProcessor.rememberPreview(totalCaptureResult);
                synchronized (OemExtensionSession.this) {
                    if (this.val$original == OemExtensionSession.this.repeating) {
                        OemExtensionSession.this.meterResult = totalCaptureResult;
                        OemExtensionSession.this.meterTime = SystemClock.elapsedRealtime();
                    }
                }
            }
            if (num.equals(this.val$original.get(CaptureRequest.CONTROL_CAPTURE_INTENT))) {
                Log.i("LeicaExtension", "Film EV resultSteps=" + totalCaptureResult.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION) + " WB result mode=" + totalCaptureResult.get(CaptureResult.CONTROL_AWB_MODE) + " gains=" + totalCaptureResult.get(CaptureResult.COLOR_CORRECTION_GAINS));
            }
            CameraCaptureSession.CaptureCallback captureCallback = this.val$callback;
            if (captureCallback != null) {
                captureCallback.onCaptureCompleted(OemExtensionSession.this, this.val$original, totalCaptureResult);
            }
        }

        @Override // android.hardware.camera2.CameraExtensionSession.ExtensionCaptureCallback
        public void onCaptureSequenceCompleted(CameraExtensionSession cameraExtensionSession, int i) {
            CameraCaptureSession.CaptureCallback captureCallback = this.val$callback;
            if (captureCallback != null) {
                OemExtensionSession oemExtensionSession = OemExtensionSession.this;
                captureCallback.onCaptureSequenceCompleted(oemExtensionSession, i, oemExtensionSession.frames);
            }
        }

        @Override // android.hardware.camera2.CameraExtensionSession.ExtensionCaptureCallback
        public void onCaptureSequenceAborted(CameraExtensionSession cameraExtensionSession, int i) {
            CameraCaptureSession.CaptureCallback captureCallback = this.val$callback;
            if (captureCallback != null) {
                captureCallback.onCaptureSequenceAborted(OemExtensionSession.this, i);
            }
        }

        /* JADX INFO: renamed from: lambda$onCaptureFailed$0$com-codex-leicaapp-OemExtensionSession$2, reason: not valid java name */
        /* synthetic */ void m19x25c18d21() {
            OemExtensionSession.this.configuration.getStateCallback().onReady(OemExtensionSession.this);
        }

        @Override // android.hardware.camera2.CameraExtensionSession.ExtensionCaptureCallback
        public void onCaptureFailed(CameraExtensionSession cameraExtensionSession, CaptureRequest captureRequest) {
            Log.e("LeicaExtension", "OEM capture failed");
            CameraCaptureSession.CaptureCallback captureCallback = this.val$callback;
            if (captureCallback != null) {
                captureCallback.onCaptureSequenceAborted(OemExtensionSession.this, this.val$sequence[0]);
            }
            OemExtensionSession.this.configuration.getExecutor().execute(new Runnable() { // from class: com.codex.leicaapp.OemExtensionSession$2$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m19x25c18d21();
                }
            });
        }
    }

    private CameraExtensionSession.ExtensionCaptureCallback callbacks(CaptureRequest captureRequest, CameraCaptureSession.CaptureCallback captureCallback, int[] iArr) {
        return new AnonymousClass2(captureCallback, captureRequest, iArr);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public CameraDevice getDevice() {
        return this.device;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int capture(CaptureRequest captureRequest, CameraCaptureSession.CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        return captureSingleRequest(captureRequest, executor(handler), captureCallback);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public synchronized int captureSingleRequest(CaptureRequest captureRequest, Executor executor, CameraCaptureSession.CaptureCallback captureCallback) throws CameraAccessException {
        int iCapture;
        Integer num = 2;
        boolean zEquals = num.equals(captureRequest.get(CaptureRequest.CONTROL_CAPTURE_INTENT));
        Log.i("LeicaExtension", "capture still=" + zEquals + " targets=" + targets(captureRequest).size());
        if (zEquals) {
            this.holdExposureUntil = SystemClock.elapsedRealtime() + 5000;
        }
        int[] iArr = {-1};
        iCapture = this.extension.capture(request(captureRequest, zEquals), executor, callbacks(captureRequest, captureCallback, iArr));
        iArr[0] = iCapture;
        return iCapture;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingRequest(CaptureRequest captureRequest, CameraCaptureSession.CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        return setSingleRepeatingRequest(captureRequest, executor(handler), captureCallback);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public synchronized int setSingleRepeatingRequest(CaptureRequest captureRequest, Executor executor, CameraCaptureSession.CaptureCallback captureCallback) throws CameraAccessException {
        int repeatingRequest;
        if (targets(captureRequest).size() != 1) {
            throw new IllegalArgumentException("OEM preview requires one surface");
        }
        this.preview = targets(captureRequest).iterator().next();
        int[] iArr = {-1};
        repeatingRequest = this.extension.setRepeatingRequest(request(captureRequest, false), executor, callbacks(captureRequest, captureCallback, iArr));
        iArr[0] = repeatingRequest;
        this.repeating = captureRequest;
        this.repeatingExecutor = executor;
        this.repeatingCallback = captureCallback;
        scheduleMeter();
        this.configuration.getExecutor().execute(new Runnable() { // from class: com.codex.leicaapp.OemExtensionSession$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m18xbafbee83();
            }
        });
        return repeatingRequest;
    }

    /* JADX INFO: renamed from: lambda$setSingleRepeatingRequest$2$com-codex-leicaapp-OemExtensionSession, reason: not valid java name */
    /* synthetic */ void m18xbafbee83() {
        this.configuration.getStateCallback().onActive(this);
    }

    private CaptureRequest single(List<CaptureRequest> list) {
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new IllegalArgumentException("OEM extension does not accept application bursts");
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureBurst(List<CaptureRequest> list, CameraCaptureSession.CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        return capture(single(list), captureCallback, handler);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int captureBurstRequests(List<CaptureRequest> list, Executor executor, CameraCaptureSession.CaptureCallback captureCallback) throws CameraAccessException {
        return captureSingleRequest(single(list), executor, captureCallback);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingBurst(List<CaptureRequest> list, CameraCaptureSession.CaptureCallback captureCallback, Handler handler) throws CameraAccessException {
        return setRepeatingRequest(single(list), captureCallback, handler);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public int setRepeatingBurstRequests(List<CaptureRequest> list, Executor executor, CameraCaptureSession.CaptureCallback captureCallback) throws CameraAccessException {
        return setSingleRepeatingRequest(single(list), executor, captureCallback);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public synchronized void stopRepeating() throws CameraAccessException {
        this.repeating = null;
        this.meterResult = null;
        this.meterHandler.removeCallbacks(this.meterTick);
        this.filmExposure.reset();
        this.exposureBias = 0.33333334f;
        this.extension.stopRepeating();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void abortCaptures() throws CameraAccessException {
        stopRepeating();
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public boolean isReprocessable() {
        return false;
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public Surface getInputSurface() {
        return null;
    }

    /* JADX INFO: renamed from: lambda$prepare$3$com-codex-leicaapp-OemExtensionSession, reason: not valid java name */
    /* synthetic */ void m16lambda$prepare$3$comcodexleicaappOemExtensionSession(Surface surface) {
        this.configuration.getStateCallback().onSurfacePrepared(this, surface);
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void prepare(final Surface surface) {
        this.configuration.getExecutor().execute(new Runnable() { // from class: com.codex.leicaapp.OemExtensionSession$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m16lambda$prepare$3$comcodexleicaappOemExtensionSession(surface);
            }
        });
    }

    @Override // android.hardware.camera2.CameraCaptureSession
    public void finalizeOutputConfigurations(List<OutputConfiguration> list) {
        throw new UnsupportedOperationException("OEM outputs are fixed");
    }

    @Override // android.hardware.camera2.CameraCaptureSession, java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.closed) {
            this.closed = true;
            this.repeating = null;
            this.meterHandler.removeCallbacks(this.meterTick);
            try {
                CameraExtensionSession cameraExtensionSession = this.extension;
                if (cameraExtensionSession != null) {
                    cameraExtensionSession.close();
                }
            } catch (CameraAccessException e) {
                Log.w("LeicaExtension", "Close failed", e);
            }
        }
    }
}
