package com.codex.fullm9;

import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class DurablePhotoFiles {
    DurablePhotoFiles() {
    }

    static void write(File file, byte[] bArr) throws Exception {
        File file2 = new File(String.valueOf(file.getPath()) + ".part");
        Throwable th = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            try {
                fileOutputStream.write(bArr);
                fileOutputStream.getFD().sync();
                fileOutputStream.close();
                Os.rename(file2.getAbsolutePath(), file.getAbsolutePath());
                syncDirectory(file.getParentFile());
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
    }

    static void syncDirectory(File file) throws Exception {
        FileDescriptor fileDescriptorOpen = Os.open(file.getAbsolutePath(), OsConstants.O_RDONLY, 0);
        try {
            Os.fsync(fileDescriptorOpen);
        } finally {
            Os.close(fileDescriptorOpen);
        }
    }

    static byte[] read(File file) throws IOException {
        long length = file.length();
        if (length <= 0 || length > 268435456) {
            throw new IOException("Invalid photo input size " + length);
        }
        int i = (int) length;
        byte[] bArr = new byte[i];
        Throwable th = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            int i2 = 0;
            while (i2 < i) {
                try {
                    int i3 = fileInputStream.read(bArr, i2, i - i2);
                    if (i3 <= 0) {
                        break;
                    }
                    i2 += i3;
                } catch (Throwable th2) {
                    fileInputStream.close();
                    throw th2;
                }
            }
            if (i2 != i || fileInputStream.read() != -1) {
                throw new IOException("Incomplete photo file");
            }
            fileInputStream.close();
            return bArr;
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

    static String hash(byte[] bArr) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bArr));
    }

    static String hash(File file) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        Throwable th = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[65536];
                while (true) {
                    int i = fileInputStream.read(bArr);
                    if (i == -1) {
                        fileInputStream.close();
                        return hex(messageDigest.digest());
                    }
                    messageDigest.update(bArr, 0, i);
                }
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

    static String hex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            sb.append(String.format(Locale.US, "%02x", Integer.valueOf(b & 255)));
        }
        return sb.toString();
    }
}
