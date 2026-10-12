package com.codex.fullm9;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class M9Graph {
    private static long generation;
    private static ScheduledFuture<?> idle;
    private static ByteBuffer inputBuffer;
    private static ByteBuffer outputBuffer;
    private static final Map<String, Entry> CACHE = new HashMap();
    private static final Set<String> FAILED = new HashSet();
    private static final ThreadLocal<String> TIMING = new ThreadLocal<>();
    private static final ScheduledExecutorService IDLE = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() { // from class: com.codex.fullm9.M9Graph.1
        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "M9-model-idle");
            thread.setDaemon(true);
            return thread;
        }
    });

    M9Graph() {
    }

    static final class Entry {
        final String backend;
        final M9CpuPolicy.Config config;
        final String name;
        final OrtSession session;
        final int threads;

        Entry(String str, M9CpuPolicy.Config config, OrtSession ortSession) {
            this.name = str;
            this.backend = config.backend;
            this.threads = config.threads;
            this.config = config;
            this.session = ortSession;
        }
    }

    static ByteBuffer bytes(int i) {
        return ByteBuffer.allocateDirect(i * 4).order(ByteOrder.nativeOrder());
    }

    static synchronized void release() {
        generation++;
        if (idle != null) {
            idle.cancel(false);
            idle = null;
        }
        Iterator<Entry> it = CACHE.values().iterator();
        while (it.hasNext()) {
            try {
                it.next().session.close();
            } catch (OrtException e) {
                Log.w("FullM9", "Model release", e);
            }
        }
        CACHE.clear();
        inputBuffer = null;
        outputBuffer = null;
    }

    private static void idleRelease() {
        if (idle != null) {
            idle.cancel(false);
        }
        final long j = generation + 1;
        generation = j;
        idle = IDLE.schedule(new Runnable() { // from class: com.codex.fullm9.M9Graph.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (M9Graph.class) {
                    if (M9Graph.generation == j) {
                        M9Graph.release();
                    }
                }
            }
        }, 60L, TimeUnit.SECONDS);
    }

    private static Entry create(Context context, String str, M9CpuPolicy.Config config) throws Exception {
        String str2 = config.backend;
        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
        int i = config.threads;
        try {
            sessionOptions.setIntraOpNumThreads(str2.equals("XNNPACK") ? Math.min(2, M9CpuPolicy.cores()) : i);
            sessionOptions.setInterOpNumThreads(1);
            sessionOptions.setCPUArenaAllocator(false);
            sessionOptions.setMemoryPatternOptimization(false);
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            int i2 = 544;
            sessionOptions.setSymbolicDimensionValue("height", str.equals("colorfix") ? 544 : 864);
            if (!str.equals("colorfix")) {
                i2 = 1120;
            }
            sessionOptions.setSymbolicDimensionValue("width", i2);
            if (str2.equals("XNNPACK")) {
                sessionOptions.addConfigEntry("session.intra_op.allow_spinning", "0");
                sessionOptions.addXnnpack(Collections.singletonMap("intra_op_num_threads", Integer.toString(i)));
            }
            OrtEnvironment.getEnvironment().setTelemetry(false);
            return new Entry(str, config, OrtEnvironment.getEnvironment().createSession(Assets.model(context, str).getAbsolutePath(), sessionOptions));
        } finally {
            sessionOptions.close();
        }
    }

    private static Entry get(Context context, String str) throws Exception {
        Entry entry = CACHE.get(str);
        if (entry != null) {
            return entry;
        }
        if (!str.equals("colorfix")) {
            Entry entryRemove = CACHE.remove(str.equals("low") ? "high" : "low");
            if (entryRemove != null) {
                entryRemove.session.close();
            }
        }
        M9CpuPolicy.Config configLoad = M9CpuPolicy.load(context, str);
        if (configLoad == null) {
            configLoad = M9CpuPolicy.defaults();
        }
        if (FAILED.contains(String.valueOf(str) + ":" + configLoad)) {
            configLoad = M9CpuPolicy.defaults();
        }
        try {
            Entry entryCreate = create(context, str, configLoad);
            CACHE.put(str, entryCreate);
            Log.i("FullM9", String.valueOf(str) + " backend=" + configLoad + " (FP32 CPU only)");
            return entryCreate;
        } catch (Exception e) {
            if (configLoad.backend.equals("CPU")) {
                throw e;
            }
            return fallback(context, str, configLoad, e);
        } catch (LinkageError e2) {
            if (configLoad.backend.equals("CPU")) {
                throw e2;
            }
            return fallback(context, str, configLoad, e2);
        }
    }

    private static Entry fallback(Context context, String str, M9CpuPolicy.Config config, Throwable th) throws Exception {
        FAILED.add(String.valueOf(str) + ":" + config);
        M9CpuPolicy.forget(context, str);
        Log.w("FullM9", "CPU optimization unavailable; use ORT CPU", th);
        Entry entryCreate = create(context, str, M9CpuPolicy.defaults());
        CACHE.put(str, entryCreate);
        return entryCreate;
    }

    private static void close(Entry entry) {
        try {
            entry.session.close();
        } catch (OrtException e) {
            Log.w("FullM9", "CPU session close", e);
        }
    }

    private static Entry replace(Context context, Entry entry, M9CpuPolicy.Config config) throws Exception {
        CACHE.remove(entry.name);
        close(entry);
        Entry entryCreate = create(context, entry.name, config);
        CACHE.put(entry.name, entryCreate);
        return entryCreate;
    }

    static void run(OrtSession ortSession, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3) throws Exception {
        FloatBuffer floatBufferAsFloatBuffer = byteBuffer.asFloatBuffer();
        floatBufferAsFloatBuffer.limit(i * i2 * i3);
        FloatBuffer floatBufferAsFloatBuffer2 = byteBuffer2.asFloatBuffer();
        floatBufferAsFloatBuffer2.limit(i2 * 3 * i3);
        String next = ortSession.getOutputNames().iterator().next();
        Throwable th = null;
        try {
            long j = i2;
            long j2 = i3;
            OnnxTensor onnxTensorCreateTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), floatBufferAsFloatBuffer, new long[]{1, i, j, j2});
            try {
                OnnxTensor onnxTensorCreateTensor2 = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), floatBufferAsFloatBuffer2, new long[]{1, 3, j, j2});
                try {
                    OrtSession.Result resultRun = ortSession.run(Collections.singletonMap("input", onnxTensorCreateTensor), Collections.singletonMap(next, onnxTensorCreateTensor2));
                    try {
                        if (resultRun.get(0) != onnxTensorCreateTensor2) {
                            throw new IllegalStateException("M9 output binding failed");
                        }
                        if (resultRun != null) {
                            resultRun.close();
                        }
                        if (onnxTensorCreateTensor2 != null) {
                            onnxTensorCreateTensor2.close();
                        }
                        if (onnxTensorCreateTensor != null) {
                            onnxTensorCreateTensor.close();
                        }
                    } catch (Throwable th2) {
                        if (resultRun == null) {
                            throw th2;
                        }
                        resultRun.close();
                        throw th2;
                    }
                } catch (Throwable th3) {
                    if (0 == 0) {
                        th = th3;
                    } else if (null != th3) {
                        th.addSuppressed(th3);
                    }
                    if (onnxTensorCreateTensor2 == null) {
                        throw th;
                    }
                    onnxTensorCreateTensor2.close();
                    throw th;
                }
            } catch (Throwable th4) {
                if (0 == 0) {
                    th = th4;
                } else if (null != th4) {
                    th.addSuppressed(th4);
                }
                if (onnxTensorCreateTensor == null) {
                    throw th;
                }
                onnxTensorCreateTensor.close();
                throw th;
            }
        } catch (Throwable th5) {
            if (0 == 0) {
                throw th5;
            }
            if (null != th5) {
                th.addSuppressed(th5);
            }
            throw null;
        }
    }

    private static Entry execute(Context context, Entry entry, int i, int i2, int i3) throws Exception {
        int i4;
        int i5;
        int i6;
        try {
            i4 = i;
            i5 = i2;
            i6 = i3;
            try {
                run(entry.session, inputBuffer, outputBuffer, i4, i5, i6);
                return entry;
            } catch (OrtException e) {
                e = e;
                OrtException ortException = e;
                if (entry.backend.equals("CPU")) {
                    throw ortException;
                }
                CACHE.remove(entry.name);
                close(entry);
                Entry entryFallback = fallback(context, entry.name, entry.config, ortException);
                run(entryFallback.session, inputBuffer, outputBuffer, i4, i5, i6);
                return entryFallback;
            }
        } catch (OrtException e2) {
            e = e2;
            i4 = i;
            i5 = i2;
            i6 = i3;
        }
    }

    /* JADX WARN: Code duplicated, block: B:32:0x00e9  */
    /* JADX WARN: Code duplicated, block: B:35:0x00f5  */
    private static Entry trial(Context context, Entry entry, M9CpuPolicy.Trial trial) throws Exception {
        Entry entryExecute = entry;
        M9CpuPolicy.Config next = trial.next();
        while (next != null && FAILED.contains(String.valueOf(entryExecute.name) + ":" + next)) {
            trial.reject();
            next = trial.done ? null : trial.next();
        }
        if (next != null && !next.equals(entryExecute.config)) {
            try {
                entryExecute = execute(context, entryExecute, 6, 544, 544);
                float[] fArrSnapshot = M9CpuPolicy.snapshot(outputBuffer, 887808);
                trial.extraRuns++;
                try {
                    Entry entryReplace = replace(context, entryExecute, next);
                    long jNanoTime = System.nanoTime();
                    run(entryReplace.session, inputBuffer, outputBuffer, 6, 544, 544);
                    long jNanoTime2 = System.nanoTime() - jNanoTime;
                    if (!M9CpuPolicy.matches(fArrSnapshot, outputBuffer, 544, 544, 16)) {
                        throw new IllegalStateException("CPU candidate changes consumed pixels");
                    }
                    trial.record(jNanoTime2);
                    return entryReplace;
                } catch (Exception e) {
                    Log.w("FullM9", "Reject CPU configuration " + next, e);
                    FAILED.add(String.valueOf(entryExecute.name) + ":" + next);
                    CACHE.remove(entryExecute.name);
                    if (0 != 0) {
                        close(null);
                    }
                    M9CpuPolicy.restore(outputBuffer, fArrSnapshot);
                    if (!trial.done) {
                        trial.reject();
                    }
                    Entry entryCreate = create(context, entryExecute.name, trial.winner);
                    CACHE.put(entryExecute.name, entryCreate);
                    return entryCreate;
                } catch (LinkageError e2) {
                    Log.w("FullM9", "Reject CPU configuration " + next, e2);
                    FAILED.add(String.valueOf(entryExecute.name) + ":" + next);
                    CACHE.remove(entryExecute.name);
                    if (0 != 0) {
                        close(null);
                    }
                    M9CpuPolicy.restore(outputBuffer, fArrSnapshot);
                    if (!trial.done) {
                        trial.reject();
                    }
                    Entry entryCreate2 = create(context, entryExecute.name, trial.winner);
                    CACHE.put(entryExecute.name, entryCreate2);
                    return entryCreate2;
                } catch (OutOfMemoryError e3) {
                    Log.w("FullM9", "CPU calibration memory limit", e3);
                    trial.done = true;
                    FAILED.add(String.valueOf(entryExecute.name) + ":" + next);
                    CACHE.remove(entryExecute.name);
                    if (0 != 0) {
                        close(null);
                    }
                    M9CpuPolicy.restore(outputBuffer, fArrSnapshot);
                    if (!trial.done) {
                        trial.reject();
                    }
                    Entry entryCreate3 = create(context, entryExecute.name, trial.winner);
                    CACHE.put(entryExecute.name, entryCreate3);
                    return entryCreate3;
                }
            } catch (OutOfMemoryError e4) {
                trial.done = true;
                return entryExecute;
            }
        }
        return entryExecute;
    }

    static int[] visibleTiles(int i, int i2) {
        if (i < 1 || i2 < 1 || i > 4096 || i2 > 3072) {
            throw new IllegalArgumentException("M9 dimensions");
        }
        int i3 = (4096 - i) / 2;
        int i4 = (3072 - i2) / 2;
        int[] iArr = new int[48];
        int i5 = 0;
        for (int i6 = i4 / 512; i6 <= ((i4 + i2) - 1) / 512; i6++) {
            int i7 = i3 / 512;
            while (i7 <= ((i3 + i) - 1) / 512) {
                iArr[i5] = (i6 * 8) + i7;
                i7++;
                i5++;
            }
        }
        return Arrays.copyOf(iArr, i5);
    }

    static String lastTiming() {
        String str = TIMING.get();
        return str == null ? "" : str;
    }

    static void clearTiming() {
        TIMING.remove();
    }

    /* JADX WARN: Code duplicated, block: B:119:0x026f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:36:0x00b0  */
    /* JADX WARN: Code duplicated, block: B:42:0x011f  */
    /* JADX WARN: Code duplicated, block: B:43:0x0121 A[Catch: all -> 0x0169, TryCatch #7 {all -> 0x0169, blocks: (B:40:0x00d4, B:44:0x0123, B:43:0x0121), top: B:123:0x00d4 }] */
    /* JADX WARN: Code duplicated, block: B:47:0x0161 A[Catch: all -> 0x0282, TRY_ENTER, TryCatch #3 {all -> 0x0282, blocks: (B:4:0x0005, B:6:0x0011, B:7:0x0016, B:47:0x0161, B:48:0x0164), top: B:116:0x0005 }] */
    /* JADX WARN: Code duplicated, block: B:53:0x016e  */
    /* JADX WARN: Code duplicated, block: B:56:0x017a A[Catch: all -> 0x00ac, TryCatch #8 {all -> 0x00ac, blocks: (B:31:0x009c, B:33:0x00a4, B:37:0x00b1, B:39:0x00bf, B:54:0x0170, B:56:0x017a, B:58:0x0180, B:60:0x0186, B:68:0x01a1, B:70:0x01b0, B:72:0x01b4, B:73:0x01bd, B:75:0x01ef, B:78:0x01f5, B:80:0x01ff, B:81:0x0205), top: B:125:0x009c }] */
    /* JADX WARN: Code duplicated, block: B:65:0x019a  */
    /* JADX WARN: Code duplicated, block: B:68:0x01a1 A[Catch: all -> 0x00ac, TryCatch #8 {all -> 0x00ac, blocks: (B:31:0x009c, B:33:0x00a4, B:37:0x00b1, B:39:0x00bf, B:54:0x0170, B:56:0x017a, B:58:0x0180, B:60:0x0186, B:68:0x01a1, B:70:0x01b0, B:72:0x01b4, B:73:0x01bd, B:75:0x01ef, B:78:0x01f5, B:80:0x01ff, B:81:0x0205), top: B:125:0x009c }] */
    /* JADX WARN: Code duplicated, block: B:70:0x01b0 A[Catch: all -> 0x00ac, TryCatch #8 {all -> 0x00ac, blocks: (B:31:0x009c, B:33:0x00a4, B:37:0x00b1, B:39:0x00bf, B:54:0x0170, B:56:0x017a, B:58:0x0180, B:60:0x0186, B:68:0x01a1, B:70:0x01b0, B:72:0x01b4, B:73:0x01bd, B:75:0x01ef, B:78:0x01f5, B:80:0x01ff, B:81:0x0205), top: B:125:0x009c }] */
    /* JADX WARN: Code duplicated, block: B:75:0x01ef A[Catch: all -> 0x00ac, TryCatch #8 {all -> 0x00ac, blocks: (B:31:0x009c, B:33:0x00a4, B:37:0x00b1, B:39:0x00bf, B:54:0x0170, B:56:0x017a, B:58:0x0180, B:60:0x0186, B:68:0x01a1, B:70:0x01b0, B:72:0x01b4, B:73:0x01bd, B:75:0x01ef, B:78:0x01f5, B:80:0x01ff, B:81:0x0205), top: B:125:0x009c }] */
    /* JADX WARN: Code duplicated, block: B:87:0x0253  */
    static synchronized Bitmap process(Context context, Bitmap bitmap, int i, int i2, float f) throws Exception {
        Class<M9Graph> cls;
        long j;
        long jFrameCreate;
        Bitmap bitmap2;
        M9CpuPolicy.Trial trial;
        long jNanoTime;
        int length;
        int i3;
        int i4;
        Bitmap bitmapCreateBitmap;
        int i5;
        boolean z;
        boolean z2;
        long jNanoTime2;
        Context context2 = context;
        Class<M9Graph> cls2 = M9Graph.class;
        synchronized (cls2) {
            try {
                try {
                    generation++;
                    if (idle != null) {
                        idle.cancel(false);
                    }
                    TIMING.remove();
                    long jNanoTime3 = System.nanoTime();
                    try {
                        jFrameCreate = NativeM9.frameCreate(bitmap);
                        try {
                            if (inputBuffer == null) {
                                try {
                                    inputBuffer = bytes(3870720);
                                } catch (Throwable th) {
                                    th = th;
                                    cls = cls2;
                                    bitmap2 = null;
                                    j = 0;
                                }
                            }
                            if (outputBuffer == null) {
                                outputBuffer = bytes(2903040);
                            }
                            NativeM9.styleInput(jFrameCreate, inputBuffer);
                            Entry entry = get(context2, (i <= 260 || i2 >= 4692) ? "high" : "low");
                            long jNanoTime4 = System.nanoTime();
                            Entry entryExecute = execute(context2, entry, 4, 864, 1120);
                            NativeM9.styleOutput(jFrameCreate, outputBuffer);
                            long jNanoTime5 = (System.nanoTime() - jNanoTime4) / 1000000;
                            PhotoProgress.update(12, "M9风格模型完成");
                            Entry entryReplace = get(context2, "colorfix");
                            int[] iArrVisibleTiles = visibleTiles(bitmap.getWidth(), bitmap.getHeight());
                            j = 0;
                            if (iArrVisibleTiles.length < 16) {
                                trial = null;
                                jNanoTime = System.nanoTime();
                                length = iArrVisibleTiles.length;
                                i3 = 0;
                                i4 = 0;
                                boolean z3 = false;
                                while (i3 < length) {
                                    long j2 = jNanoTime3;
                                    int i6 = iArrVisibleTiles[i3];
                                    NativeM9.tileInput(jFrameCreate, i6, inputBuffer);
                                    if (trial != null) {
                                        z = true;
                                        if (trial.done) {
                                        }
                                    } else {
                                        z = true;
                                    }
                                    z2 = false;
                                    if (!z2) {
                                        jNanoTime2 = System.nanoTime();
                                        entryReplace = execute(context2, entryReplace, 6, 544, 544);
                                        if (trial != null) {
                                            trial.record(System.nanoTime() - jNanoTime2);
                                        }
                                    }
                                    NativeM9.tileOutput(jFrameCreate, i6, outputBuffer);
                                    int i7 = i4 + 1;
                                    PhotoProgress.update(((i7 * 65) / iArrVisibleTiles.length) + 12, "色彩恢复 " + i7 + "/" + iArrVisibleTiles.length + " 分块");
                                    if (trial == null) {
                                        cls = cls2;
                                    } else {
                                        cls = cls2;
                                    }
                                    i3++;
                                    context2 = context;
                                    i4 = i7;
                                    cls2 = cls;
                                    jNanoTime3 = j2;
                                    jNanoTime = jNanoTime;
                                }
                                long jNanoTime6 = (System.nanoTime() - jNanoTime) / 1000000;
                                bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                                NativeM9.frameRecover(jFrameCreate, bitmapCreateBitmap);
                                ThreadLocal<String> threadLocal = TIMING;
                                long j3 = jNanoTime3;
                                StringBuilder sbAppend = new StringBuilder("tiles=").append(iArrVisibleTiles.length).append("/48 style_backend=").append(entryExecute.backend).append(" style_threads=").append(entryExecute.threads).append(" color_backend=").append(entryReplace.backend).append(" color_threads=").append(entryReplace.threads).append(" calibration_extra_runs=");
                                if (trial == null) {
                                    i5 = 0;
                                } else {
                                    i5 = trial.extraRuns;
                                }
                                threadLocal.set(sbAppend.append(i5).append(" style_ms=").append(jNanoTime5).append(" color_ms=").append(jNanoTime6).append(" graph_ms=").append((System.nanoTime() - j3) / 1000000).toString());
                                Log.i("FullM9", lastTiming());
                                if (jFrameCreate != 0) {
                                    NativeM9.frameClose(jFrameCreate);
                                }
                                idleRelease();
                                return bitmapCreateBitmap;
                            }
                            try {
                                if (M9CpuPolicy.load(context2, "colorfix") == null) {
                                    trial = new M9CpuPolicy.Trial(entryReplace.config);
                                } else {
                                    trial = null;
                                }
                                jNanoTime = System.nanoTime();
                                length = iArrVisibleTiles.length;
                                i3 = 0;
                                i4 = 0;
                                boolean z4 = false;
                                while (i3 < length) {
                                    long j4 = jNanoTime3;
                                    int i8 = iArrVisibleTiles[i3];
                                    NativeM9.tileInput(jFrameCreate, i8, inputBuffer);
                                    if (trial != null) {
                                        z = true;
                                        if (trial.done && trial.next() != null) {
                                            int i9 = trial.extraRuns;
                                            entryReplace = trial(context2, entryReplace, trial);
                                            jNanoTime = jNanoTime;
                                            z2 = trial.extraRuns != i9;
                                        }
                                        if (!z2) {
                                            jNanoTime2 = System.nanoTime();
                                            entryReplace = execute(context2, entryReplace, 6, 544, 544);
                                            if (trial != null && !trial.done) {
                                                trial.record(System.nanoTime() - jNanoTime2);
                                            }
                                        }
                                        NativeM9.tileOutput(jFrameCreate, i8, outputBuffer);
                                        int i10 = i4 + 1;
                                        PhotoProgress.update(((i10 * 65) / iArrVisibleTiles.length) + 12, "色彩恢复 " + i10 + "/" + iArrVisibleTiles.length + " 分块");
                                        if (trial == null && trial.done && !z4) {
                                            if (!entryReplace.config.equals(trial.winner)) {
                                                entryReplace = replace(context2, entryReplace, trial.winner);
                                            }
                                            M9CpuPolicy.save(context2, "colorfix", entryReplace.config);
                                            cls = cls2;
                                            try {
                                                Log.i("FullM9", "CPU calibration winner=" + entryReplace.config + " median_ms=" + (trial.best / 1000000.0d) + " extra_runs=" + trial.extraRuns + " rejected=" + trial.rejected);
                                                z4 = z;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                bitmap2 = null;
                                                if (bitmap2 != null) {
                                                    try {
                                                        bitmap2.recycle();
                                                    } catch (Throwable th3) {
                                                        if (jFrameCreate != j) {
                                                            NativeM9.frameClose(jFrameCreate);
                                                        }
                                                        idleRelease();
                                                        throw th3;
                                                    }
                                                }
                                                release();
                                                throw th;
                                            }
                                        } else {
                                            cls = cls2;
                                        }
                                        i3++;
                                        context2 = context;
                                        i4 = i10;
                                        cls2 = cls;
                                        jNanoTime3 = j4;
                                        jNanoTime = jNanoTime;
                                    } else {
                                        z = true;
                                    }
                                    z2 = false;
                                    if (!z2) {
                                        jNanoTime2 = System.nanoTime();
                                        entryReplace = execute(context2, entryReplace, 6, 544, 544);
                                        if (trial != null) {
                                            trial.record(System.nanoTime() - jNanoTime2);
                                        }
                                    }
                                    NativeM9.tileOutput(jFrameCreate, i8, outputBuffer);
                                    int i11 = i4 + 1;
                                    PhotoProgress.update(((i11 * 65) / iArrVisibleTiles.length) + 12, "色彩恢复 " + i11 + "/" + iArrVisibleTiles.length + " 分块");
                                    if (trial == null) {
                                        cls = cls2;
                                    } else {
                                        cls = cls2;
                                    }
                                    i3++;
                                    context2 = context;
                                    i4 = i11;
                                    cls2 = cls;
                                    jNanoTime3 = j4;
                                    jNanoTime = jNanoTime;
                                }
                                long jNanoTime7 = (System.nanoTime() - jNanoTime) / 1000000;
                                bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                                try {
                                    NativeM9.frameRecover(jFrameCreate, bitmapCreateBitmap);
                                    ThreadLocal<String> threadLocal2 = TIMING;
                                    long j5 = jNanoTime3;
                                    StringBuilder sbAppend2 = new StringBuilder("tiles=").append(iArrVisibleTiles.length).append("/48 style_backend=").append(entryExecute.backend).append(" style_threads=").append(entryExecute.threads).append(" color_backend=").append(entryReplace.backend).append(" color_threads=").append(entryReplace.threads).append(" calibration_extra_runs=");
                                    if (trial == null) {
                                        i5 = 0;
                                    } else {
                                        i5 = trial.extraRuns;
                                    }
                                    threadLocal2.set(sbAppend2.append(i5).append(" style_ms=").append(jNanoTime5).append(" color_ms=").append(jNanoTime7).append(" graph_ms=").append((System.nanoTime() - j5) / 1000000).toString());
                                    Log.i("FullM9", lastTiming());
                                    if (jFrameCreate != 0) {
                                        NativeM9.frameClose(jFrameCreate);
                                    }
                                    idleRelease();
                                    return bitmapCreateBitmap;
                                } catch (Throwable th4) {
                                    th = th4;
                                    cls = cls2;
                                    bitmap2 = bitmapCreateBitmap;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                cls = cls2;
                                bitmap2 = null;
                                if (bitmap2 != null) {
                                    bitmap2.recycle();
                                }
                                release();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            cls = cls2;
                            j = 0;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        cls = cls2;
                        j = 0;
                        jFrameCreate = 0;
                    }
                    if (bitmap2 != null) {
                        bitmap2.recycle();
                    }
                    release();
                    throw th;
                } catch (Throwable th8) {
                    th = th8;
                    cls = cls2;
                    throw th;
                }
            } catch (Throwable th9) {
                th = th9;
            }
        }
    }
}
