package com.codex.fullm9;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class PhotoProcessingService extends Service {
    private static final String CHANNEL = "m9-photo-processing";
    private static final int NOTIFICATION = 19409;
    private static volatile PhotoProcessingService current;
    private volatile int failureCount;
    private volatile boolean halted;
    private String jobId;
    private long lastNotice;
    private int lastStart;
    private int percent;
    private boolean recovered;
    private volatile boolean retry;
    private volatile long startupLeaseUntil;
    private PhotoJobStore store;
    private volatile int waiting;
    private PowerManager.WakeLock wake;
    private static final Object LIFECYCLE = new Object();
    private static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean BUSY = new AtomicBoolean();
    private static volatile CountDownLatch STARTED = new CountDownLatch(1);
    private final Handler main = new Handler(Looper.getMainLooper());
    private String stage = "等待原图入队";
    private final Runnable pump = new AnonymousClass1();
    private final Runnable finish = new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.2
        @Override // java.lang.Runnable
        public void run() {
            synchronized (PhotoProcessingService.LIFECYCLE) {
                if (PhotoProcessingService.this.halted) {
                    return;
                }
                if (!PhotoProcessingService.BUSY.get() && PhotoProcessingService.this.waiting == 0 && SystemClock.elapsedRealtime() >= PhotoProcessingService.this.startupLeaseUntil) {
                    try {
                        PhotoProcessingService.this.waiting = PhotoProcessingService.this.store.pending();
                        if (PhotoProcessingService.this.waiting == 0) {
                            synchronized (PhotoProcessingService.this) {
                                PhotoProcessingService.this.stage = PhotoProcessingService.this.failureCount > 0 ? "部分相片未完成，打开相机重试" : "成片完成，已保存到相册";
                                PhotoProcessingService.this.jobId = null;
                                PhotoProcessingService.this.percent = PhotoProcessingService.this.failureCount > 0 ? PhotoProcessingService.this.percent : 100;
                            }
                            PhotoProcessingService.this.halted = true;
                            if (PhotoProcessingService.this.stopSelfResult(PhotoProcessingService.this.lastStart)) {
                                if (PhotoProcessingService.current == PhotoProcessingService.this) {
                                    PhotoProcessingService.STARTED = new CountDownLatch(1);
                                }
                                PhotoProcessingService.this.stopForeground(2);
                                ((NotificationManager) PhotoProcessingService.this.getSystemService("notification")).notify(PhotoProcessingService.NOTIFICATION, PhotoProcessingService.this.notification(false));
                                BackgroundStatus.record(PhotoProcessingService.this, "本轮队列处理结束", null);
                                return;
                            }
                            PhotoProcessingService.this.halted = false;
                        }
                    } catch (Exception e) {
                        BackgroundStatus.record(PhotoProcessingService.this, "停止前读取队列失败，保留检查点", e);
                        PhotoProcessingService.this.halted = true;
                        PhotoProcessingService.this.stopForeground(1);
                        PhotoProcessingService.this.stopSelf();
                        return;
                    }
                }
                PhotoProcessingService.this.main.postDelayed(PhotoProcessingService.this.pump, 500L);
            }
        }
    };

    static boolean start(Context context, boolean z) {
        synchronized (LIFECYCLE) {
            PhotoProcessingService photoProcessingService = current;
            if (photoProcessingService != null && !photoProcessingService.halted) {
                if (z) {
                    photoProcessingService.retry = true;
                }
                photoProcessingService.startupLeaseUntil = SystemClock.elapsedRealtime() + 15000;
                kick();
                return true;
            }
            if (STARTED.getCount() == 0) {
                STARTED = new CountDownLatch(1);
            }
            try {
                if (context.startForegroundService(new Intent(context, (Class<?>) PhotoProcessingService.class).putExtra("retry", z)) == null) {
                    BackgroundStatus.record(context, "后台服务未被系统解析，未报告成启动成功", null);
                    return false;
                }
                BackgroundStatus.record(context, "已请求启动后台成片服务", null);
                return true;
            } catch (RuntimeException e) {
                BackgroundStatus.record(context, "系统拒绝启动后台成片服务", e);
                Log.e("FullM9Queue", "Foreground service start denied", e);
                return false;
            }
        }
    }

    public static String status() {
        String str;
        PhotoProcessingService photoProcessingService = current;
        if (photoProcessingService == null) {
            return "没有活动的成片任务";
        }
        synchronized (photoProcessingService) {
            str = "阶段：" + photoProcessingService.stage + " · " + photoProcessingService.percent + "% · 待完成 " + photoProcessingService.waiting + " 张";
        }
        return str;
    }

    static boolean available() {
        PhotoProcessingService photoProcessingService = current;
        return (photoProcessingService == null || photoProcessingService.halted) ? false : true;
    }

    static boolean awaitReady() throws InterruptedException {
        long jNanoTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        while (!available()) {
            long jNanoTime2 = jNanoTime - System.nanoTime();
            if (jNanoTime2 <= 0) {
                return false;
            }
            CountDownLatch countDownLatch = STARTED;
            countDownLatch.await(Math.min(jNanoTime2, TimeUnit.MILLISECONDS.toNanos(50L)), TimeUnit.NANOSECONDS);
            if (countDownLatch.getCount() == 0 && !available()) {
                Thread.sleep(10L);
            }
        }
        return true;
    }

    static void kick() {
        PhotoProcessingService photoProcessingService = current;
        if (photoProcessingService != null) {
            photoProcessingService.main.post(photoProcessingService.pump);
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL, "相片成片进度", 2);
            notificationChannel.setDescription("后台逐张生成与保存相片");
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
            startForeground(NOTIFICATION, notification(true), Build.VERSION.SDK_INT >= 35 ? 8192 : 1);
            this.store = PhotoJobStore.get(this);
            this.wake = ((PowerManager) getSystemService("power")).newWakeLock(1, String.valueOf(getPackageName()) + ":M9Photo");
            this.wake.setReferenceCounted(false);
            synchronized (LIFECYCLE) {
                current = this;
                STARTED.countDown();
            }
            BackgroundStatus.record(this, "后台服务就绪", null);
        } catch (Exception e) {
            this.halted = true;
            BackgroundStatus.record(this, "后台服务初始化失败", e);
            STARTED.countDown();
            Log.e("FullM9Queue", "Processing service unavailable", e);
            stopSelf();
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        this.lastStart = i2;
        this.startupLeaseUntil = SystemClock.elapsedRealtime() + 15000;
        if (intent != null && intent.getBooleanExtra("retry", false)) {
            this.retry = true;
        }
        this.main.post(this.pump);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized Notification notification(boolean z) {
        Notification.Builder progress;
        Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent activity = launchIntentForPackage == null ? null : PendingIntent.getActivity(this, NOTIFICATION, launchIntentForPackage, 201326592);
        progress = new Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_menu_camera).setContentTitle(this.jobId == null ? "完美移植m9 · 后台成片" : "完美移植m9 · 正在生成相片").setContentText(String.valueOf(this.stage) + " · 待完成 " + this.waiting + " 张").setOngoing(z).setOnlyAlertOnce(true).setCategory("progress").setShowWhen(false).setProgress(100, this.percent, false);
        if (Build.VERSION.SDK_INT >= 31) {
            progress.setForegroundServiceBehavior(1);
        }
        if (activity != null) {
            progress.setContentIntent(activity);
        }
        return progress.build();
    }

    /* JADX INFO: renamed from: com.codex.fullm9.PhotoProcessingService$1, reason: invalid class name */
    class AnonymousClass1 implements Runnable {
        AnonymousClass1() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!PhotoProcessingService.this.halted && PhotoProcessingService.this.store != null && PhotoProcessingService.BUSY.compareAndSet(false, true)) {
                PhotoProcessingService.WORK.execute(new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.1.1
                    /* JADX WARN: Code duplicated, block: B:87:0x0372  */
                    @Override // java.lang.Runnable
                    public void run() {
                        PowerManager.WakeLock wakeLock;
                        try {
                            try {
                                if (!PhotoProcessingService.this.recovered) {
                                    PhotoProcessingService.this.store.recover(false);
                                    PhotoProcessingService.this.recovered = true;
                                }
                                if (PhotoProcessingService.this.retry) {
                                    PhotoProcessingService.this.retry = false;
                                    PhotoProcessingService.this.store.recover(true);
                                }
                                PhotoProcessingService.this.store.expireReservations();
                                final PhotoJobStore.Job next = PhotoProcessingService.this.store.next();
                                if (next == null) {
                                    try {
                                        try {
                                            PhotoProcessingService.this.waiting = PhotoProcessingService.this.store.pending();
                                            PhotoProcessingService.this.failureCount = PhotoProcessingService.this.store.failures();
                                        } catch (Exception e) {
                                            PhotoProcessingService.this.halted = true;
                                            Log.e("FullM9Queue", "Queue state unreadable; checkpoints retained", e);
                                            PhotoProcessingService.this.main.post(new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.1.1.1
                                                @Override // java.lang.Runnable
                                                public void run() {
                                                    PhotoProcessingService.this.stopForeground(1);
                                                    PhotoProcessingService.this.stopSelf();
                                                }
                                            });
                                        }
                                        PhotoProcessingService.BUSY.set(false);
                                        if (PhotoProcessingService.this.halted) {
                                            PhotoProcessingService.kick();
                                        }
                                        PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                                        return;
                                    } catch (Throwable th) {
                                        PhotoProcessingService.BUSY.set(false);
                                        throw th;
                                    }
                                }
                                PhotoProcessingService.this.progress(next.id, 0, "读取第 " + next.seq + " 张相片");
                                PhotoProcessingService.this.wake.acquire(900000L);
                                PhotoProgress.bind(new PhotoProgress.Listener() { // from class: com.codex.fullm9.PhotoProcessingService.1.1.2
                                    @Override // com.codex.fullm9.PhotoProgress.Listener
                                    public void update(int i, String str) {
                                        PhotoProcessingService.this.progress(next.id, i, str);
                                    }
                                });
                                try {
                                    try {
                                        try {
                                            try {
                                                try {
                                                    if ("READY".equals(next.state)) {
                                                        PhotoProcessingService.this.store.processing(next.id);
                                                        byte[] bArr = DurablePhotoFiles.read(PhotoProcessingService.this.store.input(next.id));
                                                        if (!DurablePhotoFiles.hash(bArr).equals(next.inputHash)) {
                                                            throw new IOException("Input checkpoint hash mismatch");
                                                        }
                                                        byte[] bArrRender = FullM9Engine.render(PhotoProcessingService.this.getApplicationContext(), bArr, CaptureSnapshot.decode(next.snapshot), new int[]{8, 0});
                                                        PhotoProgress.update(94, "可靠保存成片");
                                                        PhotoProcessingService.this.store.encoded(next.id, bArrRender);
                                                    }
                                                    if (!PhotoProcessingService.this.halted) {
                                                        PhotoMediaPublisher.publish(PhotoProcessingService.this.getApplicationContext(), PhotoProcessingService.this.store, PhotoProcessingService.this.store.find(next.id));
                                                    }
                                                    PhotoProgress.clear();
                                                    if (PhotoProcessingService.this.wake.isHeld()) {
                                                        PhotoProcessingService.this.wake.release();
                                                    }
                                                } catch (Throwable th2) {
                                                    PhotoProgress.clear();
                                                    if (PhotoProcessingService.this.wake.isHeld()) {
                                                        PhotoProcessingService.this.wake.release();
                                                    }
                                                    throw th2;
                                                }
                                            } catch (Exception e2) {
                                                PhotoProcessingService.this.retain(next, e2);
                                                PhotoProgress.clear();
                                                if (PhotoProcessingService.this.wake.isHeld()) {
                                                    wakeLock = PhotoProcessingService.this.wake;
                                                    wakeLock.release();
                                                }
                                                PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                                            }
                                        } catch (LinkageError e3) {
                                            PhotoProcessingService.this.retain(next, e3);
                                            PhotoProgress.clear();
                                            if (PhotoProcessingService.this.wake.isHeld()) {
                                                wakeLock = PhotoProcessingService.this.wake;
                                                wakeLock.release();
                                            }
                                            PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                                        } catch (OutOfMemoryError e4) {
                                            PhotoProcessingService.this.retain(next, e4);
                                            PhotoProgress.clear();
                                            if (PhotoProcessingService.this.wake.isHeld()) {
                                                wakeLock = PhotoProcessingService.this.wake;
                                                wakeLock.release();
                                            }
                                            PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                                        }
                                        PhotoProcessingService.this.waiting = PhotoProcessingService.this.store.pending();
                                        PhotoProcessingService.this.failureCount = PhotoProcessingService.this.store.failures();
                                    } catch (Exception e5) {
                                        PhotoProcessingService.this.halted = true;
                                        Log.e("FullM9Queue", "Queue state unreadable; checkpoints retained", e5);
                                        PhotoProcessingService.this.main.post(new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.1.1.1
                                            @Override // java.lang.Runnable
                                            public void run() {
                                                PhotoProcessingService.this.stopForeground(1);
                                                PhotoProcessingService.this.stopSelf();
                                            }
                                        });
                                    }
                                    PhotoProcessingService.BUSY.set(false);
                                    if (PhotoProcessingService.this.halted) {
                                        PhotoProcessingService.kick();
                                    }
                                    PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                                } catch (Throwable th3) {
                                    PhotoProcessingService.BUSY.set(false);
                                    throw th3;
                                }
                            } catch (Exception e6) {
                                BackgroundStatus.record(PhotoProcessingService.this, "后台队列执行失败，检查点保留", e6);
                                Log.e("FullM9Queue", "Queue worker", e6);
                                try {
                                    try {
                                        PhotoProcessingService.this.waiting = PhotoProcessingService.this.store.pending();
                                        PhotoProcessingService.this.failureCount = PhotoProcessingService.this.store.failures();
                                    } catch (Exception e7) {
                                        PhotoProcessingService.this.halted = true;
                                        Log.e("FullM9Queue", "Queue state unreadable; checkpoints retained", e7);
                                        PhotoProcessingService.this.main.post(new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.1.1.1
                                            @Override // java.lang.Runnable
                                            public void run() {
                                                PhotoProcessingService.this.stopForeground(1);
                                                PhotoProcessingService.this.stopSelf();
                                            }
                                        });
                                    }
                                    PhotoProcessingService.BUSY.set(false);
                                    if (PhotoProcessingService.this.halted) {
                                        PhotoProcessingService.kick();
                                    }
                                } catch (Throwable th4) {
                                    PhotoProcessingService.BUSY.set(false);
                                    throw th4;
                                }
                            }
                        } catch (Throwable th5) {
                            try {
                                try {
                                    PhotoProcessingService.this.waiting = PhotoProcessingService.this.store.pending();
                                    PhotoProcessingService.this.failureCount = PhotoProcessingService.this.store.failures();
                                } catch (Throwable th6) {
                                    PhotoProcessingService.BUSY.set(false);
                                    throw th6;
                                }
                            } catch (Exception e8) {
                                PhotoProcessingService.this.halted = true;
                                Log.e("FullM9Queue", "Queue state unreadable; checkpoints retained", e8);
                                PhotoProcessingService.this.main.post(new Runnable() { // from class: com.codex.fullm9.PhotoProcessingService.1.1.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        PhotoProcessingService.this.stopForeground(1);
                                        PhotoProcessingService.this.stopSelf();
                                    }
                                });
                            }
                            PhotoProcessingService.BUSY.set(false);
                            if (PhotoProcessingService.this.halted) {
                                PhotoProcessingService.kick();
                            }
                            PhotoProcessingService.this.main.post(PhotoProcessingService.this.finish);
                            throw th5;
                        }
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void progress(String str, int i, String str2) {
        if (!str.equals(this.jobId)) {
            this.jobId = str;
            this.percent = 0;
            this.lastNotice = 0L;
        }
        this.percent = Math.max(this.percent, Math.min(100, i));
        this.stage = str2;
        try {
            this.waiting = this.store.pending();
        } catch (RuntimeException e) {
            Log.w("FullM9Queue", "Progress count unavailable; photo state unchanged", e);
        }
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        if (jElapsedRealtime - this.lastNotice >= 200 || this.percent == 100) {
            this.lastNotice = jElapsedRealtime;
            try {
                ((NotificationManager) getSystemService("notification")).notify(NOTIFICATION, notification(true));
            } catch (RuntimeException e2) {
                Log.w("FullM9Queue", "Progress notification", e2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void retain(PhotoJobStore.Job job, Throwable th) throws Exception {
        BackgroundStatus.record(this, "第 " + job.seq + " 张生成或保存失败，检查点保留", th);
        this.store.failed(job.id, th);
        Log.e("FullM9Queue", "Photo retained for retry: " + job.id, th);
        progress(job.id, this.percent, "生成失败，原图已保留；打开相机重试");
    }

    public void onTimeout(int i, int i2) {
        BackgroundStatus.record(this, "系统要求后台成片服务超时停止，检查点保留", null);
        this.halted = true;
        stopForeground(1);
        stopSelf(i);
    }

    @Override // android.app.Service
    public void onTimeout(int i) {
        onTimeout(i, 1);
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
    }

    @Override // android.app.Service
    public void onDestroy() {
        synchronized (LIFECYCLE) {
            this.halted = true;
            if (current == this) {
                current = null;
                STARTED = new CountDownLatch(1);
            }
        }
        this.main.removeCallbacksAndMessages(null);
        if (this.wake != null && this.wake.isHeld()) {
            this.wake.release();
        }
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }
}
