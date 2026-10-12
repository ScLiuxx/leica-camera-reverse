package com.codex.leicaapp;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
final class AiVisualReference implements AutoCloseable {
    private OrtSession aesthetic;
    private final Object cancelLock = new Object();
    private volatile boolean cancelled = true;
    private OrtEnvironment environment;
    private OrtSession.RunOptions running;
    private OrtSession session;

    AiVisualReference() {
    }

    void start() {
        synchronized (this.cancelLock) {
            this.cancelled = false;
        }
    }

    void cancel() {
        synchronized (this.cancelLock) {
            this.cancelled = true;
            OrtSession.RunOptions runOptions = this.running;
            if (runOptions != null) {
                try {
                    runOptions.setTerminate(true);
                } catch (OrtException e) {
                }
            }
        }
    }

    private void checkCancelled() throws InterruptedException {
        if (this.cancelled) {
            throw new InterruptedException("AI disabled");
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0022, code lost:
    
        throw r3;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private OrtSession.Result run(OrtSession ortSession, Map<String, OnnxTensor> map) throws Exception {
        checkCancelled();
        OrtSession.RunOptions runOptions = new OrtSession.RunOptions();
        try {
            synchronized (this.cancelLock) {
                checkCancelled();
                this.running = runOptions;
            }
            try {
                OrtSession.Result resultRun = ortSession.run(map, runOptions);
                synchronized (this.cancelLock) {
                    this.running = null;
                }
                runOptions.close();
                return resultRun;
            } catch (Throwable th) {
                synchronized (this.cancelLock) {
                    this.running = null;
                    throw th;
                }
            }
        } catch (Throwable th2) {
            try {
                runOptions.close();
            } catch (Throwable th3) {
                th2.addSuppressed(th3);
            }
            throw th2;
        }
    }

    private OrtSession load(Context context, String str) throws Exception {
        checkCancelled();
        if (this.environment == null) {
            this.environment = OrtEnvironment.getEnvironment();
        }
        InputStream inputStreamOpen = context.getAssets().open(str);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
                try {
                    byte[] bArr = new byte[65536];
                    while (true) {
                        int i = inputStreamOpen.read(bArr);
                        if (i == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, i);
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable th) {
                            th.addSuppressed(th);
                        }
                        throw th;
                    }
                    sessionOptions.addConfigEntry("session.intra_op.allow_spinning", "0");
                    sessionOptions.addConfigEntry("session.inter_op.allow_spinning", "0");
                    boolean zContains = OrtEnvironment.getAvailableProviders().contains(OrtProvider.XNNPACK);
                    sessionOptions.setIntraOpNumThreads(zContains ? 1 : 2);
                    if (zContains) {
                        sessionOptions.addXnnpack(Collections.singletonMap("intra_op_num_threads", "2"));
                    }
                    Log.i("LeicaAi", "Loading " + str + " provider=" + (zContains ? "XNNPACK" : "CPU"));
                    OrtSession ortSessionCreateSession = this.environment.createSession(byteArrayOutputStream.toByteArray(), sessionOptions);
                    sessionOptions.close();
                    byteArrayOutputStream.close();
                    if (inputStreamOpen != null) {
                        inputStreamOpen.close();
                    }
                    return ortSessionCreateSession;
                } catch (Throwable th2) {
                    try {
                        sessionOptions.close();
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                byteArrayOutputStream.close();
                throw th4;
            }
        } catch (Throwable th5) {
            if (inputStreamOpen != null) {
                try {
                    inputStreamOpen.close();
                } catch (Throwable th6) {
                    th5.addSuppressed(th6);
                }
            }
            throw th5;
        }
    }

    float[] detect(Context context, Bitmap bitmap, float f) throws Exception {
        checkCancelled();
        Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        try {
            return detectCandidates(context, bitmap, bitmapCreateScaledBitmap, f);
        } finally {
            if (bitmapCreateScaledBitmap != bitmap) {
                bitmapCreateScaledBitmap.recycle();
            }
        }
    }

    private float[] detectCandidates(Context context, Bitmap bitmap, Bitmap bitmap2, float f) throws Exception {
        if (this.session == null) {
            this.session = load(context, "cacnet.onnx");
        }
        if (this.aesthetic == null) {
            this.aesthetic = load(context, "nima-aesthetic.onnx");
        }
        int[] iArr = new int[50176];
        bitmap2.getPixels(iArr, 0, 224, 0, 0, 224, 224);
        float[] fArr = new float[150528];
        float[] fArr2 = {0.485f, 0.456f, 0.406f};
        float[] fArr3 = {0.229f, 0.224f, 0.225f};
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 50176; i2++) {
                fArr[(i * 50176) + i2] = ((((iArr[i2] >> (16 - (i * 8))) & 255) / 255.0f) - fArr2[i]) / fArr3[i];
            }
        }
        int i3 = 4;
        OnnxTensor onnxTensorCreateTensor = OnnxTensor.createTensor(this.environment, FloatBuffer.wrap(fArr), new long[]{1, 3, 224, 224});
        try {
            OrtSession.Result resultRun = run(this.session, Collections.singletonMap("image", onnxTensorCreateTensor));
            try {
                float[] fArr4 = ((float[][]) resultRun.get(0).getValue())[0];
                for (int i4 = 0; i4 < 4; i4++) {
                    if (!Float.isFinite(fArr4[i4])) {
                        throw new IllegalStateException("Invalid CACNet box");
                    }
                    fArr4[i4] = Math.max(0.0f, Math.min(1.0f, fArr4[i4]));
                }
                if (fArr4[2] - fArr4[0] < 0.1f || fArr4[3] - fArr4[1] < 0.1f) {
                    throw new IllegalStateException("Empty CACNet box");
                }
                float[] fArrChoose = AiCropCandidates.choose(fArr4, ((float[][][][]) resultRun.get(1).getValue())[0][0]);
                if (resultRun != null) {
                    resultRun.close();
                }
                if (onnxTensorCreateTensor != null) {
                    onnxTensorCreateTensor.close();
                }
                ArrayList arrayList = new ArrayList();
                arrayList.add(new float[]{0.0f, 0.0f, 1.0f, 1.0f});
                arrayList.add(fit(fArr4, Math.max(fArr4[2] - fArr4[0], fArr4[3] - fArr4[1]), f));
                arrayList.add(fit(fArrChoose, Math.max(fArrChoose[2] - fArrChoose[0], fArrChoose[3] - fArrChoose[1]), f));
                float[] fArr5 = {0.55f, 0.7f, 0.85f};
                for (int i5 = 0; i5 < 3; i5++) {
                    arrayList.add(fit(fArrChoose, fArr5[i5], f));
                }
                ArrayList arrayList2 = new ArrayList();
                Bitmap bitmapCopy = bitmap2.copy(Bitmap.Config.RGB_565, false);
                try {
                    FaceDetector.Face[] faceArr = new FaceDetector.Face[8];
                    int iFindFaces = new FaceDetector(224, 224, 8).findFaces(bitmapCopy, faceArr);
                    int i6 = 0;
                    while (i6 < iFindFaces) {
                        if (faceArr[i6].confidence() >= 0.4f) {
                            PointF pointF = new PointF();
                            faceArr[i6].getMidPoint(pointF);
                            float fEyesDistance = faceArr[i6].eyesDistance();
                            float[] fArr6 = new float[i3];
                            float f2 = 2.0f * fEyesDistance;
                            fArr6[0] = Math.max(0.0f, (pointF.x - f2) / 224.0f);
                            fArr6[1] = Math.max(0.0f, (pointF.y - f2) / 224.0f);
                            fArr6[2] = Math.min(1.0f, (pointF.x + f2) / 224.0f);
                            fArr6[3] = Math.min(1.0f, (pointF.y + (fEyesDistance * 2.5f)) / 224.0f);
                            arrayList2.add(fArr6);
                        }
                        i6++;
                        i3 = 4;
                    }
                    bitmapCopy.recycle();
                    int size = arrayList.size();
                    float[] fArr7 = new float[size];
                    boolean[] zArr = new boolean[size];
                    for (int i7 = 0; i7 < size; i7++) {
                        checkCancelled();
                        zArr[i7] = true;
                        Iterator it = arrayList2.iterator();
                        while (it.hasNext()) {
                            if (!AiAestheticChoice.preserves((float[]) arrayList.get(i7), (float[]) it.next())) {
                                zArr[i7] = false;
                            }
                        }
                        fArr7[i7] = Float.NaN;
                        if (zArr[i7]) {
                            int i8 = 0;
                            while (true) {
                                if (i8 >= i7) {
                                    i8 = -1;
                                    break;
                                }
                                if (Arrays.equals((float[]) arrayList.get(i7), (float[]) arrayList.get(i8))) {
                                    break;
                                }
                                i8++;
                            }
                            fArr7[i7] = i8 >= 0 ? fArr7[i8] : score(bitmap, (float[]) arrayList.get(i7));
                        }
                    }
                    int iChoose = AiAestheticChoice.choose(fArr7, zArr);
                    float[] fArr8 = (float[]) arrayList.get(iChoose);
                    Log.i("LeicaAi", "NIMA scores=" + Arrays.toString(fArr7) + " winner=" + iChoose + " protectedFaces=" + arrayList2.size());
                    return new float[]{fArr8[0], fArr8[1], fArr8[2], fArr8[3], fArr7[iChoose]};
                } catch (Throwable th) {
                    bitmapCopy.recycle();
                    throw th;
                }
            } catch (Throwable th2) {
                if (resultRun == null) {
                    throw th2;
                }
                try {
                    resultRun.close();
                    throw th2;
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                    throw th2;
                }
            }
        } catch (Throwable th4) {
            if (onnxTensorCreateTensor == null) {
                throw th4;
            }
            try {
                onnxTensorCreateTensor.close();
                throw th4;
            } catch (Throwable th5) {
                th4.addSuppressed(th5);
                throw th4;
            }
        }
    }

    private static float[] fit(float[] fArr, float f, float f2) {
        float fMin = Math.min(1.0f, Math.max(f2, f)) / 2.0f;
        float f3 = 1.0f - fMin;
        float fMax = Math.max(fMin, Math.min(f3, (fArr[0] + fArr[2]) / 2.0f));
        float fMax2 = Math.max(fMin, Math.min(f3, (fArr[1] + fArr[3]) / 2.0f));
        return new float[]{fMax - fMin, fMax2 - fMin, fMax + fMin, fMax2 + fMin};
    }

    /* JADX WARN: Code duplicated, block: B:57:0x0111  */
    /* JADX WARN: Code duplicated, block: B:59:0x00fd A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:72:? A[Catch: all -> 0x0107, SYNTHETIC, TRY_LEAVE, TryCatch #2 {all -> 0x0107, blocks: (B:3:0x004f, B:8:0x006c, B:9:0x0082, B:10:0x0085, B:25:0x00d3, B:51:0x0106, B:50:0x0103, B:46:0x00fd, B:11:0x0095, B:23:0x00ce, B:42:0x00f8, B:41:0x00f5), top: B:63:0x004f, inners: #0, #3 }] */
    private float score(Bitmap bitmap, float[] fArr) throws Exception {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i = 0;
        float f = width;
        int iRound = Math.round(fArr[0] * f);
        float f2 = height;
        int iRound2 = Math.round(fArr[1] * f2);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap, iRound, iRound2, Math.min(width - iRound, Math.max(1, Math.round(fArr[2] * f) - iRound)), Math.min(height - iRound2, Math.max(1, Math.round(fArr[3] * f2) - iRound2)));
        Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmapCreateBitmap, 224, 224, true);
        try {
            int[] iArr = new int[50176];
            bitmapCreateScaledBitmap.getPixels(iArr, 0, 224, 0, 0, 224, 224);
            float[] fArr2 = new float[150528];
            for (int i2 = 0; i2 < 50176; i2++) {
                for (int i3 = 0; i3 < 3; i3++) {
                    fArr2[(i2 * 3) + i3] = (((iArr[i2] >> (16 - (i3 * 8))) & 255) / 127.5f) - 1.0f;
                }
            }
            OnnxTensor onnxTensorCreateTensor = OnnxTensor.createTensor(this.environment, FloatBuffer.wrap(fArr2), new long[]{1, 224, 224, 3});
            try {
                OrtSession.Result resultRun = run(this.aesthetic, Collections.singletonMap("image", onnxTensorCreateTensor));
                try {
                    float[] fArr3 = ((float[][]) resultRun.get(0).getValue())[0];
                    float f3 = 0.0f;
                    while (i < 10) {
                        int i4 = i + 1;
                        f3 += i4 * fArr3[i];
                        i = i4;
                    }
                    if (!Float.isFinite(f3) || f3 < 1.0f || f3 > 10.0f) {
                        throw new IllegalStateException("Invalid aesthetic score");
                    }
                    if (resultRun != null) {
                        resultRun.close();
                    }
                    if (onnxTensorCreateTensor != null) {
                        onnxTensorCreateTensor.close();
                    }
                    if (bitmapCreateScaledBitmap != bitmapCreateBitmap && bitmapCreateScaledBitmap != bitmap) {
                        bitmapCreateScaledBitmap.recycle();
                    }
                    if (bitmapCreateBitmap != bitmap) {
                        bitmapCreateBitmap.recycle();
                    }
                    return f3;
                } catch (Throwable th) {
                    if (resultRun == null) {
                        throw th;
                    }
                    try {
                        resultRun.close();
                        throw th;
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                        throw th;
                    }
                    if (bitmapCreateScaledBitmap != bitmapCreateBitmap) {
                        bitmapCreateScaledBitmap.recycle();
                    }
                    if (bitmapCreateBitmap != bitmap) {
                        bitmapCreateBitmap.recycle();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (onnxTensorCreateTensor != null) {
                    throw th3;
                }
                try {
                    onnxTensorCreateTensor.close();
                    throw th3;
                } catch (Throwable th4) {
                    th3.addSuppressed(th4);
                    throw th3;
                }
            }
            if (onnxTensorCreateTensor != null) {
                throw th3;
            }
            onnxTensorCreateTensor.close();
            throw th3;
        } catch (Throwable th5) {
            if (bitmapCreateScaledBitmap != bitmapCreateBitmap && bitmapCreateScaledBitmap != bitmap) {
                bitmapCreateScaledBitmap.recycle();
            }
            if (bitmapCreateBitmap != bitmap) {
                bitmapCreateBitmap.recycle();
            }
            throw th5;
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        OrtSession[] ortSessionArr = {this.session, this.aesthetic};
        for (int i = 0; i < 2; i++) {
            OrtSession ortSession = ortSessionArr[i];
            if (ortSession != null) {
                try {
                    ortSession.close();
                } catch (OrtException e) {
                }
            }
        }
        this.aesthetic = null;
        this.session = null;
    }
}
