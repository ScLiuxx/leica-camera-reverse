package com.codex.fullm9;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class Assets {
    Assets() {
    }

    static byte[] read(Context context, String str) throws IOException {
        Throwable th = null;
        try {
            InputStream inputStreamOpen = context.getAssets().open("fullm9/" + str);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[16384];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i);
                }
                return byteArrayOutputStream.toByteArray();
            } finally {
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
            }
        } catch (Throwable th2) {
            if (0 == 0) {
                throw th2;
            }
            if (null != th2) {
                th.addSuppressed(th2);
            }
            throw null;
        }
    }

    static synchronized File model(Context context, String str) throws Exception {
        File file = new File(context.getNoBackupFilesDir(), "fullm9-p1");
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Model cache directory");
        }
        File file2 = new File(file, String.valueOf(str) + ".onnx");
        String strHash = ManifestHashes.hash(str);
        if (file2.isFile() && strHash.equals(hash(file2))) {
            return file2;
        }
        File file3 = new File(file, String.valueOf(str) + ".tmp");
        Throwable th = null;
        try {
            InputStream inputStreamOpen = context.getAssets().open("fullm9/" + str + ".onnx");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file3);
                try {
                    byte[] bArr = new byte[65536];
                    while (true) {
                        int i = inputStreamOpen.read(bArr);
                        if (i == -1) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, i);
                    }
                    fileOutputStream.getFD().sync();
                    fileOutputStream.close();
                    if (inputStreamOpen != null) {
                        inputStreamOpen.close();
                    }
                    if (!strHash.equals(hash(file3))) {
                        file3.delete();
                        throw new IOException("Model digest mismatch: " + str);
                    }
                    if (file2.exists() && !file2.delete()) {
                        throw new IOException("Cannot replace model");
                    }
                    if (file3.renameTo(file2)) {
                        return file2;
                    }
                    throw new IOException("Model cache commit");
                } catch (Throwable th2) {
                    fileOutputStream.close();
                    throw th2;
                }
            } catch (Throwable th3) {
                if (0 == 0) {
                    th = th3;
                } else if (null != th3) {
                    th.addSuppressed(th3);
                }
                if (inputStreamOpen == null) {
                    throw th;
                }
                inputStreamOpen.close();
                throw th;
            }
        } catch (Throwable th4) {
            if (0 == 0) {
                throw th4;
            }
            if (null != th4) {
                th.addSuppressed(th4);
            }
            throw null;
        }
    }

    static String hash(File file) throws Exception {
        int i;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        Throwable th = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[65536];
                while (true) {
                    int i2 = fileInputStream.read(bArr);
                    if (i2 == -1) {
                        break;
                    }
                    messageDigest.update(bArr, 0, i2);
                }
                fileInputStream.close();
                StringBuilder sb = new StringBuilder();
                for (byte b : messageDigest.digest()) {
                    sb.append(String.format(Locale.US, "%02x", Integer.valueOf(b & 255)));
                }
                return sb.toString();
            } catch (Throwable th2) {
                fileInputStream.close();
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
    }
}
