package com.codex.fullm9;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class BackgroundStatus {
    private static final String FILE = "m9-background-diagnostics.txt";
    private static final int LIMIT = 49152;

    static synchronized void record(Context context, String str, Throwable th) {
        if (context != null) {
            try {
                File file = new File(context.getFilesDir(), FILE);
                if (file.length() > 49152) {
                    File file2 = new File(String.valueOf(file.getPath()) + ".previous");
                    file2.delete();
                    file.renameTo(file2);
                }
                Throwable th2 = null;
                try {
                    PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
                    try {
                        printWriter.println(String.valueOf(System.currentTimeMillis()) + " " + str);
                        if (th != null) {
                            th.printStackTrace(printWriter);
                        }
                        printWriter.close();
                    } catch (Throwable th3) {
                        printWriter.close();
                        throw th3;
                    }
                } catch (Throwable th4) {
                    if (0 == 0) {
                        throw th4;
                    }
                    if (null == th4) {
                        throw null;
                    }
                    th2.addSuppressed(th4);
                }
            } catch (Exception | LinkageError | OutOfMemoryError e) {
            }
        }
    }

    private static String read(Context context, String str) {
        try {
            File file = new File(context.getFilesDir(), str);
            if (!file.isFile()) {
                return "";
            }
            Throwable th = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    byte[] bArr = new byte[(int) Math.min(file.length(), 49152L)];
                    int i = fileInputStream.read(bArr);
                    return i > 0 ? new String(bArr, 0, i, "UTF-8") : "";
                } finally {
                    fileInputStream.close();
                }
            } catch (Throwable th2) {
                if (0 == 0) {
                    throw th2;
                }
                if (null == th2) {
                    throw null;
                }
                th.addSuppressed(th2);
                throw null;
            }
        } catch (Exception e) {
            return String.valueOf(e.getClass().getSimpleName()) + ": " + e.getMessage();
        }
    }

    static String report(Context context) {
        String strDiagnostics;
        StringBuilder sb = new StringBuilder("完美移植m9 · 后台修复 P9\n");
        sb.append("应用：").append(context.getPackageName()).append("\nAndroid API：").append(Build.VERSION.SDK_INT).append("\n通知权限：").append(NotificationPermission.granted(context) ? "已允许" : "未允许").append("\n后台服务：").append(PhotoProcessingService.available() ? "运行中" : "未运行").append("\n").append(PhotoProcessingService.status()).append("\n");
        try {
            if (new File(context.getNoBackupFilesDir(), "m9-photo-jobs/jobs.db").isFile()) {
                PhotoJobStore photoJobStore = PhotoJobStore.get(context);
                sb.append("待完成：").append(photoJobStore.pending()).append("，失败保留：").append(photoJobStore.failures()).append("\n");
                strDiagnostics = photoJobStore.diagnostics();
            } else {
                strDiagnostics = "还没有建立拍照队列\n";
            }
            sb.append(strDiagnostics);
        } catch (Exception e) {
            sb.append("队列读取失败：").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        }
        sb.append("\n后台记录\n").append(read(context, FILE)).append("\n最近成片\n").append(read(context, "fullm9-last-render.txt")).append("\n最近成片错误\n").append(read(context, "fullm9-last-error.txt"));
        return sb.toString();
    }

    static Activity activity(Context context) {
        Context baseContext;
        int i = 0;
        while (i < 16 && context != null) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            if (!(context instanceof ContextWrapper) || (baseContext = ((ContextWrapper) context).getBaseContext()) == context) {
                return null;
            }
            i++;
            context = baseContext;
        }
        return null;
    }

    public static void bind(View view) {
        if (view == null) {
            return;
        }
        view.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.codex.fullm9.BackgroundStatus.1
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view2) {
                Activity activity = BackgroundStatus.activity(view2.getContext());
                if (activity == null) {
                    return false;
                }
                BackgroundStatus.show(activity);
                return true;
            }
        });
    }

    public static void show(final Activity activity) {
        final Context applicationContext = activity.getApplicationContext();
        new Thread(new Runnable() { // from class: com.codex.fullm9.BackgroundStatus.2
            @Override // java.lang.Runnable
            public void run() {
                final String strReport = BackgroundStatus.report(applicationContext);
                Activity activity2 = activity;
                final Activity activity3 = activity;
                activity2.runOnUiThread(new Runnable() { // from class: com.codex.fullm9.BackgroundStatus.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (activity3.isFinishing() || activity3.isDestroyed()) {
                            return;
                        }
                        AlertDialog.Builder message = new AlertDialog.Builder(activity3).setTitle("后台成片与通知 · P9").setMessage(strReport.length() > 2500 ? String.valueOf(strReport.substring(0, 2500)) + "\n完整记录可复制" : strReport);
                        final Activity activity4 = activity3;
                        AlertDialog.Builder positiveButton = message.setPositiveButton("通知设置", new DialogInterface.OnClickListener() { // from class: com.codex.fullm9.BackgroundStatus.2.1.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NotificationPermission.settings(activity4);
                            }
                        });
                        final Activity activity5 = activity3;
                        final String str = strReport;
                        AlertDialog.Builder neutralButton = positiveButton.setNeutralButton("复制诊断", new DialogInterface.OnClickListener() { // from class: com.codex.fullm9.BackgroundStatus.2.1.2
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((ClipboardManager) activity5.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("后台成片诊断", str));
                            }
                        });
                        final Activity activity6 = activity3;
                        neutralButton.setNegativeButton("重试成片", new DialogInterface.OnClickListener() { // from class: com.codex.fullm9.BackgroundStatus.2.1.3
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PhotoProcessingService.start(activity6, true);
                            }
                        }).show();
                    }
                });
            }
        }, "m9-background-status").start();
    }
}
