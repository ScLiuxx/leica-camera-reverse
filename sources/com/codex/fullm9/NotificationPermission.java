package com.codex.fullm9;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class NotificationPermission {
    private static final String PERMISSION = "android.permission.POST_NOTIFICATIONS";
    private static final String PREF = "m9-background";
    public static final int REQUEST = 16809;
    private static final String RESULT = "notificationResultP9";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Map<Activity, Attempt> ATTEMPTS = new WeakHashMap();

    private static final class Attempt {
        int cancellations;
        boolean completed;
        boolean inFlight;
        int polls;
        boolean queued;
        long requested;

        private Attempt() {
        }

        /* synthetic */ Attempt(Attempt attempt) {
            this();
        }
    }

    public static boolean granted(Context context) {
        return Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(PERMISSION) == 0;
    }

    public static void attach(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 33) {
            return;
        }
        synchronized (ATTEMPTS) {
            Attempt attempt = ATTEMPTS.get(activity);
            if (attempt == null) {
                attempt = new Attempt(null);
                ATTEMPTS.put(activity, attempt);
            }
            if (!granted(activity) && !attempt.completed && !attempt.inFlight && !attempt.queued && !activity.getSharedPreferences(PREF, 0).getBoolean(RESULT, false)) {
                attempt.queued = true;
                attempt.polls = 0;
                schedule(activity, 1000L);
            }
        }
    }

    private static void schedule(Activity activity, long j) {
        final WeakReference weakReference = new WeakReference(activity);
        MAIN.postDelayed(new Runnable() { // from class: com.codex.fullm9.NotificationPermission.1
            @Override // java.lang.Runnable
            public void run() {
                Activity activity2 = (Activity) weakReference.get();
                if (activity2 != null) {
                    NotificationPermission.poll(activity2);
                }
            }
        }, j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void poll(Activity activity) {
        synchronized (ATTEMPTS) {
            Attempt attempt = ATTEMPTS.get(activity);
            if (attempt == null) {
                return;
            }
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                if (!granted(activity) && !attempt.completed) {
                    if (activity.hasWindowFocus() && activity.checkSelfPermission("android.permission.CAMERA") == 0 && !cameraRequesting(activity)) {
                        attempt.queued = false;
                        attempt.inFlight = true;
                        attempt.requested = SystemClock.elapsedRealtime();
                        try {
                            activity.requestPermissions(new String[]{PERMISSION}, REQUEST);
                            BackgroundStatus.record(activity, "已提交通知权限请求，等待系统结果", null);
                        } catch (RuntimeException e) {
                            attempt.inFlight = false;
                            BackgroundStatus.record(activity, "通知权限请求被系统拒绝，可从后台状态打开通知设置", e);
                        }
                        return;
                    }
                    int i = attempt.polls + 1;
                    attempt.polls = i;
                    if (i < 30) {
                        schedule(activity, 500L);
                    } else {
                        attempt.queued = false;
                        BackgroundStatus.record(activity, "通知请求等待相机权限或窗口焦点，下次进入重试", null);
                    }
                    return;
                }
                attempt.queued = false;
                return;
            }
            ATTEMPTS.remove(activity);
        }
    }

    private static boolean cameraRequesting(Activity activity) {
        Object objField = Reflect.field(activity, "F1");
        if (objField == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(objField.getClass().getMethod("isPermissionRequesting", new Class[0]).invoke(objField, new Object[0]));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean result(Activity activity, int i, String[] strArr, int[] iArr) {
        if (i != 16809) {
            return false;
        }
        synchronized (ATTEMPTS) {
            Attempt attempt = ATTEMPTS.get(activity);
            Attempt attempt2 = null;
            if (attempt == null) {
                attempt = new Attempt(attempt2);
                ATTEMPTS.put(activity, attempt);
            }
            attempt.inFlight = false;
            attempt.queued = false;
            int i2 = -1;
            if (strArr != null) {
                for (int i3 = 0; i3 < strArr.length; i3++) {
                    if (PERMISSION.equals(strArr[i3])) {
                        i2 = i3;
                    }
                }
            }
            if (i2 >= 0 && iArr != null && i2 < iArr.length) {
                attempt.completed = true;
                activity.getSharedPreferences(PREF, 0).edit().putBoolean(RESULT, true).putBoolean("notificationGrantedP9", iArr[i2] == 0).apply();
                BackgroundStatus.record(activity, iArr[i2] == 0 ? "通知权限已允许" : "通知权限未允许；后台计算仍独立运行", null);
                return true;
            }
            BackgroundStatus.record(activity, "通知请求未收到有效结果，没有记录成已请求", null);
            int i4 = attempt.cancellations + 1;
            attempt.cancellations = i4;
            if (i4 <= 2) {
                attempt.queued = true;
                attempt.polls = 0;
                schedule(activity, 1000L);
            }
            return true;
        }
    }

    public static void settings(Activity activity) {
        try {
            activity.startActivity(new Intent("android.settings.APP_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName()));
        } catch (RuntimeException e) {
            BackgroundStatus.record(activity, "无法打开系统通知设置", e);
        }
    }
}
