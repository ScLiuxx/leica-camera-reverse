package com.codex.fullm9;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.StatFs;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class BackgroundCapture {
    private static final Map<Object, Future<String>> IDS = Collections.synchronizedMap(new WeakHashMap());
    private static final ExecutorService RESERVATIONS = Executors.newSingleThreadExecutor();
    private static final Map<String, Binding> CAPTURES = new HashMap();
    private static final Map<Object, Boolean> ACCEPTED = Collections.synchronizedMap(new WeakHashMap());

    private static final class Binding {
        boolean ambiguous;
        final Future<String> id;
        final int[] selected;

        Binding(Future<String> future, int[] iArr) {
            this.id = future;
            this.selected = (int[]) iArr.clone();
        }
    }

    static String captureKey(Object obj) {
        Object objField = Reflect.field(obj, "a");
        Object objField2 = Reflect.field(obj, "k");
        long jNumber = CaptureSnapshot.number(objField, "g");
        String strString = CaptureSnapshot.string(objField2, "g");
        if (jNumber <= 0 || strString == null || strString.isEmpty()) {
            return null;
        }
        return String.valueOf(jNumber) + "|" + strString;
    }

    static void pin(Object obj, int[] iArr) throws Exception {
        Field declaredField = Class.forName("com.codex.lmport.MomentBridge").getDeclaredField("SHOTS");
        declaredField.setAccessible(true);
        ((Map) declaredField.get(null)).put(obj, (int[]) iArr.clone());
    }

    static Future<String> resolve(Object obj) throws Exception {
        Future<String> future = IDS.get(obj);
        if (future != null) {
            return future;
        }
        String strCaptureKey = captureKey(obj);
        if (strCaptureKey == null) {
            return null;
        }
        synchronized (CAPTURES) {
            Binding binding = CAPTURES.get(strCaptureKey);
            if (binding != null && !binding.ambiguous) {
                int[] iArrChoice = choice(obj);
                if (iArrChoice != null && !Arrays.equals(iArrChoice, binding.selected)) {
                    return null;
                }
                pin(obj, binding.selected);
                IDS.put(obj, binding.id);
                BackgroundStatus.record(app(), "按本张拍摄时间和文件名恢复保存任务关联", null);
                return binding.id;
            }
            return null;
        }
    }

    static Future<String> reserve(final Context context) {
        return RESERVATIONS.submit(new Callable<String>() { // from class: com.codex.fullm9.BackgroundCapture.1
            @Override // java.util.concurrent.Callable
            public String call() throws Exception {
                try {
                    String strReserve = PhotoJobStore.get(context).reserve();
                    BackgroundStatus.record(context, "快门任务已预约", null);
                    PhotoProcessingService.kick();
                    return strReserve;
                } catch (Exception e) {
                    BackgroundStatus.record(context, "快门预约失败，尚未接管照片", e);
                    throw e;
                }
            }
        });
    }

    static Context app() throws Exception {
        return ((Application) Reflect.call(Class.forName("com.xiaomi.camera.basic.Global"), "getApplication", new Class[0], new Object[0])).getApplicationContext();
    }

    static int[] choice(Object obj) throws Exception {
        Field declaredField = Class.forName("com.codex.lmport.MomentBridge").getDeclaredField("SHOTS");
        declaredField.setAccessible(true);
        Object obj2 = ((Map) declaredField.get(null)).get(obj);
        if (obj2 instanceof int[]) {
            return (int[]) ((int[]) obj2).clone();
        }
        return null;
    }

    static boolean eligible(Object obj) throws Exception {
        int[] iArrChoice = choice(obj);
        return CaptureSnapshot.integer(Reflect.field(obj, "b"), "g") == 256 && iArrChoice != null && iArrChoice.length > 0 && iArrChoice[0] == 8;
    }

    public static boolean requiresImmediateExif(Object obj) {
        try {
            return resolve(obj) != null && eligible(obj) && PhotoProcessingService.available();
        } catch (Exception e) {
            return false;
        }
    }

    public static void attach(Activity activity) {
        if (activity == null) {
            return;
        }
        NotificationPermission.attach(activity);
        try {
            if (new File(activity.getNoBackupFilesDir(), "m9-photo-jobs/jobs.db").isFile() || FullM9Ui.choice(activity)[0] == 8) {
                PhotoProcessingService.start(activity, true);
            }
        } catch (Exception e) {
            BackgroundStatus.record(activity, "进入相机时准备或恢复后台队列失败", e);
            Log.e("FullM9Queue", "Queue resume unavailable", e);
            CaptureTrace.notice(activity, "后台成片恢复失败，长按右下角影调查看原因");
        }
    }

    public static void snapshot(Object obj) {
        try {
            if (eligible(obj)) {
                Context contextApp = app();
                if (!PhotoProcessingService.start(contextApp, false)) {
                    BackgroundStatus.record(contextApp, "快门时后台服务启动失败，保留原保存路径", null);
                    return;
                }
                synchronized (IDS) {
                    if (!IDS.containsKey(obj)) {
                        Future<String> futureReserve = reserve(contextApp);
                        IDS.put(obj, futureReserve);
                        String strCaptureKey = captureKey(obj);
                        if (strCaptureKey != null) {
                            synchronized (CAPTURES) {
                                Binding binding = CAPTURES.get(strCaptureKey);
                                if (binding == null) {
                                    CAPTURES.put(strCaptureKey, new Binding(futureReserve, choice(obj)));
                                } else if (binding.id != futureReserve) {
                                    binding.ambiguous = true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            try {
                BackgroundStatus.record(app(), "拍摄快照未能进入后台队列", e);
            } catch (Exception e2) {
            }
            Log.e("FullM9Queue", "Capture reservation unavailable", e);
        }
    }

    public static void copy(Object obj, Object obj2) {
        synchronized (IDS) {
            Future<String> future = IDS.get(obj);
            if (future != null && obj2 != null) {
                IDS.put(obj2, future);
            }
        }
        if (obj2 == null || !Boolean.TRUE.equals(ACCEPTED.get(obj))) {
            return;
        }
        ACCEPTED.put(obj2, true);
    }

    /* JADX WARN: Code duplicated, block: B:70:0x0183 A[ADDED_TO_REGION] */
    /* JADX WARN: Code duplicated, block: B:73:0x018f A[Catch: Exception -> 0x01b8, TryCatch #2 {Exception -> 0x01b8, blocks: (B:71:0x0185, B:73:0x018f, B:75:0x0197, B:77:0x019f, B:79:0x01ac), top: B:87:0x0185 }] */
    public static boolean handoff(Object obj) {
        String str;
        PhotoJobStore.Job jobFind;
        Context context = null;
        try {
            if (Boolean.TRUE.equals(ACCEPTED.get(obj))) {
                return true;
            }
            Future<String> futureResolve = resolve(obj);
            if (eligible(obj)) {
                Context contextApp = app();
                if (futureResolve == null) {
                    try {
                        BackgroundStatus.record(contextApp, "保存入口有本张 M9 选择但缺快门预约，尝试可靠补交接", null);
                        if (PhotoProcessingService.available() || PhotoProcessingService.start(contextApp, false)) {
                            futureResolve = reserve(contextApp);
                            IDS.put(obj, futureResolve);
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e = e;
                        str = null;
                        context = contextApp;
                        BackgroundStatus.record(context, "完整原图交接失败，保留原保存路径", e);
                        Log.e("FullM9Queue", "Capture handoff failed", e);
                        if (context != null) {
                            try {
                                jobFind = PhotoJobStore.get(context).find(str);
                                if (jobFind == null) {
                                }
                                PhotoJobStore.get(context).abandon(str, e.toString());
                            } catch (Exception e2) {
                                Log.e("FullM9Queue", "Queue error recovery", e2);
                            }
                        }
                        return false;
                    }
                }
                str = futureResolve.get();
                try {
                    Object objField = Reflect.field(obj, "e");
                    if (CaptureSnapshot.bool(Reflect.field(obj, "j"), "p")) {
                        return fallback(contextApp, str, "External capture URI");
                    }
                    Object objOptional = CaptureSnapshot.optional(objField, "getLivePhotoData");
                    if (objOptional != null && Boolean.TRUE.equals(CaptureSnapshot.optional(objOptional, "isLiveShotTask"))) {
                        return fallback(contextApp, str, "Live Photo contract");
                    }
                    PhotoJobStore.Job jobFind2 = PhotoJobStore.get(contextApp).find(str);
                    if (jobFind2 != null && !"RESERVED".equals(jobFind2.state) && !"NO_INPUT".equals(jobFind2.state)) {
                        ACCEPTED.put(obj, true);
                        PhotoProcessingService.kick();
                        return true;
                    }
                    if (!PhotoProcessingService.awaitReady()) {
                        return fallback(contextApp, str, "Foreground service is not running");
                    }
                    Object objField2 = Reflect.field(Reflect.field(obj, "a"), "i");
                    if (objField2 == null) {
                        return fallback(contextApp, str, "No complete source");
                    }
                    Method method = Class.forName("Vh.e").getMethod("d", new Class[0]);
                    method.setAccessible(true);
                    byte[] bArr = (byte[]) method.invoke(objField2, new Object[0]);
                    if (bArr != null && bArr.length >= 4) {
                        if (bArr.length > 268435456) {
                            return fallback(contextApp, str, "Input exceeds durable queue limit");
                        }
                        PhotoJobStore photoJobStore = PhotoJobStore.get(contextApp);
                        if (new StatFs(photoJobStore.dir.getAbsolutePath()).getAvailableBytes() < (((long) bArr.length) * 3) + 67108864) {
                            return fallback(contextApp, str, "Insufficient durable queue space");
                        }
                        Reflect.call(Class.forName("com.codex.leicaapp.MomentDirect"), "clearNativePostFilters", new Class[]{Class.forName("Sh.t")}, obj);
                        photoJobStore.accept(str, bArr, CaptureSnapshot.freeze(obj, bArr).encode());
                        ACCEPTED.put(obj, true);
                        BackgroundStatus.record(contextApp, "完整原图已可靠交给后台 " + str, null);
                        PhotoProcessingService.kick();
                        return true;
                    }
                    return fallback(contextApp, str, "No complete source bytes");
                } catch (Exception e3) {
                    e = e3;
                    context = contextApp;
                    BackgroundStatus.record(context, "完整原图交接失败，保留原保存路径", e);
                    Log.e("FullM9Queue", "Capture handoff failed", e);
                    if (context != null) {
                        jobFind = PhotoJobStore.get(context).find(str);
                        if (jobFind == null) {
                        }
                        PhotoJobStore.get(context).abandon(str, e.toString());
                    }
                    return false;
                }
            }
            return false;
        } catch (Exception e4) {
            e = e4;
            str = null;
        }
        BackgroundStatus.record(context, "完整原图交接失败，保留原保存路径", e);
        Log.e("FullM9Queue", "Capture handoff failed", e);
        if (context != null && str != null) {
            jobFind = PhotoJobStore.get(context).find(str);
            if (jobFind == null && !"RESERVED".equals(jobFind.state) && !"NO_INPUT".equals(jobFind.state)) {
                ACCEPTED.put(obj, true);
                PhotoProcessingService.kick();
                return true;
            }
            PhotoJobStore.get(context).abandon(str, e.toString());
        }
        return false;
    }

    static boolean fallback(Context context, String str, String str2) throws Exception {
        PhotoJobStore.get(context).abandon(str, str2);
        BackgroundStatus.record(context, "未接管照片，原保存路径继续：" + str2, null);
        Log.w("FullM9Queue", "Retain original save path: " + str2);
        return false;
    }
}
