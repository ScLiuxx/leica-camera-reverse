package com.codex.fullm9;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtProvider;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class M9CpuPolicy {
    static final Map<String, Config> MEASURED = new HashMap();

    M9CpuPolicy() {
    }

    static final class Config {
        final String backend;
        final int threads;

        Config(String str, int i) {
            if (!str.equals("CPU") && !str.equals("XNNPACK")) {
                throw new IllegalArgumentException("CPU backend required");
            }
            this.backend = str;
            this.threads = Math.max(1, Math.min(8, i));
        }

        public String toString() {
            return String.valueOf(this.backend) + ":" + this.threads;
        }

        public boolean equals(Object obj) {
            return (obj instanceof Config) && toString().equals(obj.toString());
        }

        public int hashCode() {
            return toString().hashCode();
        }
    }

    static int cores() {
        return Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors()));
    }

    static Config defaults() {
        return new Config("CPU", cores());
    }

    static String key(String str) {
        return "p7-cpu-v1|" + str + "|" + ManifestHashes.hash(str) + "|" + OrtEnvironment.getEnvironment().getVersion() + "|" + System.getProperty("os.arch") + "|" + System.getProperty("os.version") + "|" + Runtime.getRuntime().availableProcessors();
    }

    static File file(Context context, String str) {
        return new File(context.getNoBackupFilesDir(), "fullm9-cpu-" + str + ".properties");
    }

    static Config load(Context context, String str) throws Throwable {
        Throwable th;
        String strKey = key(str);
        Config config = MEASURED.get(strKey);
        if (config != null) {
            return config;
        }
        try {
            Properties properties = new Properties();
            try {
                FileInputStream fileInputStream = new FileInputStream(file(context, str));
                try {
                    properties.load(fileInputStream);
                    fileInputStream.close();
                    long jCurrentTimeMillis = System.currentTimeMillis() - Long.parseLong(properties.getProperty("time"));
                    if (strKey.equals(properties.getProperty("key")) && jCurrentTimeMillis >= 0 && jCurrentTimeMillis <= 604800000) {
                        String property = properties.getProperty("backend");
                        int i = Integer.parseInt(properties.getProperty("threads"));
                        if (i >= 1 && i <= cores() && ("CPU".equals(property) || "XNNPACK".equals(property))) {
                            if ("XNNPACK".equals(property) && !OrtEnvironment.getAvailableProviders().contains(OrtProvider.XNNPACK)) {
                                return null;
                            }
                            Config config2 = new Config(property, i);
                            MEASURED.put(strKey, config2);
                            return config2;
                        }
                        return null;
                    }
                    return null;
                } catch (Throwable th2) {
                    try {
                        fileInputStream.close();
                        throw th2;
                    } catch (Throwable th3) {
                        th = th2;
                        th = th3;
                        if (th == null) {
                            throw th;
                        }
                        if (th != th) {
                            th.addSuppressed(th);
                        }
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                th = null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static void save(Context context, String str, Config config) {
        MEASURED.put(key(str), config);
        try {
            Properties properties = new Properties();
            properties.setProperty("key", key(str));
            properties.setProperty("backend", config.backend);
            properties.setProperty("threads", Integer.toString(config.threads));
            properties.setProperty("time", Long.toString(System.currentTimeMillis()));
            File file = file(context, str);
            File file2 = new File(String.valueOf(file.getPath()) + ".tmp");
            Throwable th = null;
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                try {
                    properties.store(fileOutputStream, "Measured CPU configuration; no images");
                    fileOutputStream.getFD().sync();
                    fileOutputStream.close();
                    if (!file2.renameTo(file)) {
                        file2.delete();
                    }
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
        } catch (Exception e) {
            Log.w("FullM9", "CPU choice persistence unavailable", e);
        }
    }

    static void forget(Context context, String str) {
        MEASURED.remove(key(str));
        try {
            file(context, str).delete();
        } catch (Exception e) {
        }
    }

    static List<Config> candidates(Config config) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        linkedHashSet.add(new Config("CPU", Math.min(4, cores())));
        linkedHashSet.add(new Config("CPU", Math.min(6, cores())));
        if (OrtEnvironment.getAvailableProviders().contains(OrtProvider.XNNPACK)) {
            linkedHashSet.add(new Config("XNNPACK", cores()));
        }
        linkedHashSet.remove(config);
        return new ArrayList(linkedHashSet);
    }

    static float[] snapshot(ByteBuffer byteBuffer, int i) {
        float[] fArr = new float[i];
        byteBuffer.asFloatBuffer().get(fArr);
        return fArr;
    }

    static void restore(ByteBuffer byteBuffer, float[] fArr) {
        byteBuffer.asFloatBuffer().put(fArr);
    }

    static int pixel(float f) {
        return (int) Math.max(0.0d, Math.min(255.0d, Math.rint((f * 127.5f) + 128.0f)));
    }

    static boolean matches(float[] fArr, ByteBuffer byteBuffer, int i, int i2, int i3) {
        FloatBuffer floatBufferAsFloatBuffer = byteBuffer.asFloatBuffer();
        int i4 = i * i2;
        for (int i5 = 0; i5 < 3; i5++) {
            for (int i6 = 0; i6 < i; i6++) {
                for (int i7 = 0; i7 < i2; i7++) {
                    int i8 = (i5 * i4) + (i6 * i2) + i7;
                    float f = fArr[i8];
                    float f2 = floatBufferAsFloatBuffer.get(i8);
                    if (!Float.isFinite(f) || !Float.isFinite(f2) || Math.abs(f - f2) > 1.0E-4f) {
                        return false;
                    }
                    if (i7 >= i3 && i7 < i2 - i3 && i6 >= i3 && i6 < i - i3 && pixel(f) != pixel(f2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static double median(long[] jArr) {
        long[] jArr2 = (long[]) jArr.clone();
        Arrays.sort(jArr2);
        return jArr2[jArr2.length / 2];
    }

    static final class Trial {
        final List<Config> choices;
        Config current;
        boolean done;
        int extraRuns;
        int index;
        int rejected;
        int samplesUsed;
        Config winner;
        final long[] samples = new long[3];
        double best = Double.POSITIVE_INFINITY;

        Trial(Config config) {
            this.current = config;
            this.winner = config;
            this.choices = M9CpuPolicy.candidates(config);
        }

        Config next() {
            if (this.samplesUsed != 0 || this.index <= 0) {
                return null;
            }
            return this.current;
        }

        void record(long j) {
            long[] jArr = this.samples;
            int i = this.samplesUsed;
            this.samplesUsed = i + 1;
            jArr[i] = j;
            if (this.samplesUsed < 3) {
                return;
            }
            double dMedian = M9CpuPolicy.median(this.samples);
            if (dMedian < this.best * 0.9d) {
                this.best = dMedian;
                this.winner = this.current;
            }
            advance();
        }

        void reject() {
            this.rejected++;
            advance();
        }

        void advance() {
            this.samplesUsed = 0;
            if (this.index == this.choices.size()) {
                this.done = true;
                return;
            }
            List<Config> list = this.choices;
            int i = this.index;
            this.index = i + 1;
            this.current = list.get(i);
        }
    }
}
