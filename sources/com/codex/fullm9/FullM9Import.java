package com.codex.fullm9;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class FullM9Import {
    FullM9Import() {
    }

    static void notice(final Context context, final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.codex.fullm9.FullM9Import.1
            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(context, str, 1).show();
            }
        });
    }

    static void start(Activity activity, final Uri uri) {
        final Context applicationContext = activity.getApplicationContext();
        notice(applicationContext, "开始处理完美移植m9");
        new Thread(new Runnable() { // from class: com.codex.fullm9.FullM9Import.2
            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r4v0 */
            /* JADX WARN: Type inference failed for: r4v12, types: [byte[]] */
            @Override // java.lang.Runnable
            public void run() throws Throwable {
                Uri byteArray;
                Uri uriInsert;
                Throwable th;
                Throwable th2;
                try {
                    try {
                        InputStream inputStreamOpenInputStream = applicationContext.getContentResolver().openInputStream(uri);
                        try {
                            if (inputStreamOpenInputStream == null) {
                                throw new IOException("Cannot open input");
                            }
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            byte[] bArr = new byte[65536];
                            while (true) {
                                int i = inputStreamOpenInputStream.read(bArr);
                                if (i == -1) {
                                    byteArray = byteArrayOutputStream.toByteArray();
                                    if (inputStreamOpenInputStream != null) {
                                        inputStreamOpenInputStream.close();
                                    }
                                    try {
                                        byte[] bArrRender = FullM9Engine.render(applicationContext, byteArray, null, new int[]{8, 0});
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("_display_name", "FullM9_" + System.currentTimeMillis() + ".jpg");
                                        contentValues.put("mime_type", "image/jpeg");
                                        contentValues.put("relative_path", "DCIM/Camera");
                                        contentValues.put("is_pending", (Integer) 1);
                                        uriInsert = applicationContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                                        try {
                                            if (uriInsert == null) {
                                                throw new IOException("Cannot create photo");
                                            }
                                            try {
                                                OutputStream outputStreamOpenOutputStream = applicationContext.getContentResolver().openOutputStream(uriInsert);
                                                try {
                                                    if (outputStreamOpenOutputStream == null) {
                                                        throw new IOException("Cannot save photo");
                                                    }
                                                    outputStreamOpenOutputStream.write(bArrRender);
                                                    if (outputStreamOpenOutputStream != null) {
                                                        outputStreamOpenOutputStream.close();
                                                    }
                                                    ContentValues contentValues2 = new ContentValues();
                                                    contentValues2.put("is_pending", (Integer) 0);
                                                    applicationContext.getContentResolver().update(uriInsert, contentValues2, null, null);
                                                    FullM9Import.notice(applicationContext, "完美移植m9照片已保存");
                                                    return;
                                                } catch (Throwable th3) {
                                                    if (outputStreamOpenOutputStream != null) {
                                                        try {
                                                            outputStreamOpenOutputStream.close();
                                                        } catch (Throwable th4) {
                                                            th2 = th3;
                                                            th = th4;
                                                        }
                                                    }
                                                    throw th3;
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                th2 = null;
                                            }
                                            if (th2 == null) {
                                                throw th;
                                            }
                                            if (th2 != th) {
                                                th2.addSuppressed(th);
                                            }
                                            throw th2;
                                        } catch (Exception e) {
                                            e = e;
                                            Log.e("FullM9", "Import failed", e);
                                            FullM9Import.notice(applicationContext, "处理失败：" + e.getMessage());
                                            if (uriInsert == null) {
                                                return;
                                            }
                                            applicationContext.getContentResolver().delete(uriInsert, null, null);
                                            return;
                                        } catch (LinkageError e2) {
                                            e = e2;
                                            Log.e("FullM9", "Runtime unavailable", e);
                                            FullM9Import.notice(applicationContext, "模型或RAW解码器无法使用");
                                            if (uriInsert != null) {
                                                applicationContext.getContentResolver().delete(uriInsert, null, null);
                                                return;
                                            }
                                            return;
                                        } catch (OutOfMemoryError e3) {
                                            FullM9Import.notice(applicationContext, "内存不足，处理未完成");
                                            if (uriInsert == null) {
                                                return;
                                            }
                                            applicationContext.getContentResolver().delete(uriInsert, null, null);
                                            return;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        if (byteArray != 0) {
                                            applicationContext.getContentResolver().delete(byteArray, null, null);
                                        }
                                        throw th;
                                    }
                                }
                                if (byteArrayOutputStream.size() + i > 268435456) {
                                    throw new IOException("Input too large");
                                }
                                byteArrayOutputStream.write(bArr, 0, i);
                            }
                        } catch (Throwable th7) {
                            if (inputStreamOpenInputStream != null) {
                                try {
                                    inputStreamOpenInputStream.close();
                                } catch (Throwable th8) {
                                    th = th7;
                                    th = th8;
                                }
                            }
                            throw th7;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        th = null;
                    }
                    if (th == null) {
                        throw th;
                    }
                    if (th != th) {
                        th.addSuppressed(th);
                    }
                    throw th;
                } catch (Exception e4) {
                    e = e4;
                    uriInsert = null;
                } catch (LinkageError e5) {
                    e = e5;
                    uriInsert = null;
                } catch (OutOfMemoryError e6) {
                    uriInsert = null;
                } catch (Throwable th10) {
                    th = th10;
                    byteArray = 0;
                }
            }
        }, "FullM9-import").start();
    }
}
