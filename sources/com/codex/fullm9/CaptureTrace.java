package com.codex.fullm9;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class CaptureTrace {
    private static final ThreadLocal<String> STAGE = new ThreadLocal<>();
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final ThreadLocal<Times> TIMES = new ThreadLocal<>();

    CaptureTrace() {
    }

    static final class Times {
        long changed;
        final long entered;
        final long requested;
        final long id = CaptureTrace.SEQUENCE.incrementAndGet();
        final StringBuilder stages = new StringBuilder();

        Times(long j) {
            this.requested = j;
            long jNanoTime = System.nanoTime();
            this.changed = jNanoTime;
            this.entered = jNanoTime;
        }
    }

    static String timings() {
        Times times = TIMES.get();
        return times == null ? "" : "shot=" + times.id + " queue_ms=" + ((times.entered - times.requested) / 1000000) + " request_to_render_ms=" + ((System.nanoTime() - times.requested) / 1000000) + " stages=" + ((Object) times.stages) + " current=" + STAGE.get() + ":" + ((System.nanoTime() - times.changed) / 1000000) + "ms";
    }

    static void notice(final Context context, final String str) {
        if (context == null) {
            return;
        }
        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.codex.fullm9.CaptureTrace.1
                @Override // java.lang.Runnable
                public void run() {
                    Toast.makeText(context, str, 1).show();
                }
            });
        } catch (RuntimeException e) {
            Log.w("FullM9", "Capture notice unavailable", e);
        }
    }

    static void start(Context context, boolean z) {
        start(context, z, System.nanoTime());
    }

    static void start(Context context, boolean z, long j) {
        TIMES.set(new Times(j));
        stage(context, "读取相片");
        if (z) {
            notice(context, "正在生成完美移植m9，请稍候");
        }
    }

    static void stage(Context context, String str) {
        PhotoProgress.stage(str);
        Times times = TIMES.get();
        String str2 = STAGE.get();
        if (times != null) {
            long jNanoTime = System.nanoTime();
            if (str2 != null) {
                times.stages.append(str2).append(':').append((jNanoTime - times.changed) / 1000000).append("ms,");
            }
            times.changed = jNanoTime;
        }
        STAGE.set(str);
        write(context, "processing: " + str, null);
    }

    static void complete(Context context, boolean z) {
        write(context, "completed: M9 render ready for camera save", null);
        STAGE.remove();
        TIMES.remove();
        if (z) {
            notice(context, "完美移植m9处理完成，正在保存");
        }
    }

    static void failed(Context context, Throwable th) {
        String str = STAGE.get();
        write(context, "failed: " + str, th);
        notice(context, "完美移植m9未完成：" + str + "（" + th.getClass().getSimpleName() + "）");
        STAGE.remove();
        TIMES.remove();
    }

    static void write(Context context, String str, Throwable th) {
        Log.i("FullM9", String.valueOf(str) + " " + timings());
        if (context == null) {
            return;
        }
        Throwable th2 = null;
        try {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir == null) {
                externalFilesDir = context.getFilesDir();
            }
            try {
                PrintWriter printWriter = new PrintWriter(new File(externalFilesDir, "fullm9-capture-status.txt"), "UTF-8");
                try {
                    printWriter.println(str);
                    printWriter.println(timings());
                    if (th != null) {
                        th.printStackTrace(printWriter);
                    }
                } finally {
                    printWriter.close();
                }
            } catch (Throwable th3) {
                if (0 == 0) {
                    throw th3;
                }
                if (null != th3) {
                    th2.addSuppressed(th3);
                }
                throw null;
            }
        } catch (Exception e) {
            Log.w("FullM9", "Capture stage log unavailable", e);
        }
    }
}
