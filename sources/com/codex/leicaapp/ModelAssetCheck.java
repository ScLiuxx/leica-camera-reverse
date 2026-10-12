package com.codex.leicaapp;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class ModelAssetCheck {
    public static void ensure(Context context, File file) throws IOException {
        if (CameraZoomCalibration.baselineDevice(Build.DEVICE, Build.MODEL)) {
            return;
        }
        verifyTree(context, "leica-v4", file);
        Log.i("LeicaModelCompat", "assets verified; device=" + Build.DEVICE + " model=" + Build.MODEL + " sdk=" + Build.VERSION.SDK_INT + " hardware=" + Build.HARDWARE);
    }

    /* JADX WARN: Code duplicated, block: B:107:0x0139 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:82:0x0149  */
    private static void verifyTree(Context context, String str, File file) throws IOException {
        String[] list = context.getAssets().list(str);
        if (list != null && list.length > 0) {
            if (!file.isDirectory() && !file.mkdirs()) {
                throw new IOException("Cannot create model directory");
            }
            for (String str2 : list) {
                verifyTree(context, str + "/" + str2, new File(file, str2));
            }
            return;
        }
        InputStream inputStreamOpen = context.getAssets().open(str);
        try {
            byte[] bArrHash = hash(inputStreamOpen);
            if (inputStreamOpen != null) {
                inputStreamOpen.close();
            }
            if (file.isFile()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    boolean zEquals = Arrays.equals(bArrHash, hash(fileInputStream));
                    fileInputStream.close();
                    if (zEquals) {
                        return;
                    }
                } catch (Throwable th) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
            File fileCreateTempFile = File.createTempFile("model-", ".tmp", file.getParentFile());
            try {
                InputStream inputStreamOpen2 = context.getAssets().open(str);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileCreateTempFile);
                    try {
                        byte[] bArr = new byte[65536];
                        while (true) {
                            int i = inputStreamOpen2.read(bArr);
                            if (i == -1) {
                                break;
                            } else {
                                fileOutputStream.write(bArr, 0, i);
                            }
                            if (inputStreamOpen2 != null) {
                                try {
                                    inputStreamOpen2.close();
                                } catch (Throwable th3) {
                                    th.addSuppressed(th3);
                                }
                            }
                            throw th;
                        }
                        fileOutputStream.getFD().sync();
                        fileOutputStream.close();
                        if (inputStreamOpen2 != null) {
                            inputStreamOpen2.close();
                        }
                        FileInputStream fileInputStream2 = new FileInputStream(fileCreateTempFile);
                        try {
                            if (!Arrays.equals(bArrHash, hash(fileInputStream2))) {
                                throw new IOException("Model checksum mismatch: " + str);
                            }
                            fileInputStream2.close();
                            if (!fileCreateTempFile.renameTo(file)) {
                                throw new IOException("Cannot replace model: " + str);
                            }
                            Log.w("LeicaModelCompat", "Repaired model asset=" + str);
                            if (fileCreateTempFile.exists()) {
                                fileCreateTempFile.delete();
                                return;
                            }
                            return;
                        } catch (Throwable th4) {
                            try {
                                fileInputStream2.close();
                            } catch (Throwable th5) {
                                th4.addSuppressed(th5);
                            }
                            throw th4;
                        }
                    } catch (Throwable th6) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th7) {
                            th6.addSuppressed(th7);
                        }
                        throw th6;
                    }
                } catch (Throwable th8) {
                    if (inputStreamOpen2 != null) {
                        inputStreamOpen2.close();
                    }
                    throw th8;
                }
            } catch (Throwable th9) {
                if (fileCreateTempFile.exists()) {
                    fileCreateTempFile.delete();
                }
                throw th9;
            }
            if (fileCreateTempFile.exists()) {
                fileCreateTempFile.delete();
            }
            throw th9;
        } catch (Throwable th10) {
            if (inputStreamOpen != null) {
                try {
                    inputStreamOpen.close();
                } catch (Throwable th11) {
                    th10.addSuppressed(th11);
                }
            }
            throw th10;
        }
    }

    static byte[] hash(InputStream inputStream) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bArr = new byte[65536];
            while (true) {
                int i = inputStream.read(bArr);
                if (i == -1) {
                    return messageDigest.digest();
                }
                messageDigest.update(bArr, 0, i);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
