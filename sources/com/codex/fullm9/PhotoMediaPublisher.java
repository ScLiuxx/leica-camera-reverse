package com.codex.fullm9;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class PhotoMediaPublisher {
    private static final Uri COLLECTION = MediaStore.Images.Media.getContentUri("external_primary");
    private static final String PATH = "DCIM/Camera/";

    PhotoMediaPublisher() {
    }

    private static String name(PhotoJobStore.Job job) throws Exception {
        CaptureSnapshot captureSnapshotDecode = CaptureSnapshot.decode(job.snapshot);
        return String.format(Locale.US, "M9_%013d_%020d_%s.jpg", Long.valueOf(captureSnapshotDecode.a.g > 0 ? captureSnapshotDecode.a.g : job.created), Long.valueOf(job.seq), job.id);
    }

    /* JADX WARN: Code duplicated, block: B:38:0x007b  */
    static Uri find(Context context, PhotoJobStore.Job job) throws Exception {
        Uri uriWithAppendedId;
        Throwable th = null;
        if (job.uri != null) {
            Uri uri = Uri.parse(job.uri);
            try {
                Cursor cursorQuery = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
                if (cursorQuery != null) {
                    try {
                        if (cursorQuery.moveToFirst()) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return uri;
                        }
                    } catch (Throwable th2) {
                        if (cursorQuery == null) {
                            throw th2;
                        }
                        cursorQuery.close();
                        throw th2;
                    }
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
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
        try {
            Cursor cursorQuery2 = context.getContentResolver().query(COLLECTION, new String[]{"_id"}, "_display_name=? AND relative_path=?", new String[]{name(job), PATH}, null);
            if (cursorQuery2 != null) {
                try {
                    if (cursorQuery2.moveToFirst()) {
                        uriWithAppendedId = ContentUris.withAppendedId(COLLECTION, cursorQuery2.getLong(0));
                    } else {
                        uriWithAppendedId = null;
                    }
                } finally {
                    if (cursorQuery2 != null) {
                        cursorQuery2.close();
                    }
                }
            } else {
                uriWithAppendedId = null;
            }
            return uriWithAppendedId;
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

    static boolean matches(Context context, Uri uri, PhotoJobStore.Job job) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        Throwable th = null;
        try {
            InputStream inputStreamOpenInputStream = context.getContentResolver().openInputStream(uri);
            if (inputStreamOpenInputStream == null) {
                if (inputStreamOpenInputStream != null) {
                    inputStreamOpenInputStream.close();
                }
                return false;
            }
            try {
                byte[] bArr = new byte[65536];
                long j = 0;
                while (true) {
                    int i = inputStreamOpenInputStream.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    messageDigest.update(bArr, 0, i);
                    j += (long) i;
                }
                if (inputStreamOpenInputStream != null) {
                    inputStreamOpenInputStream.close();
                }
                return j == job.size && DurablePhotoFiles.hex(messageDigest.digest()).equals(job.outputHash);
            } catch (Throwable th2) {
                if (inputStreamOpenInputStream == null) {
                    throw th2;
                }
                inputStreamOpenInputStream.close();
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                } catch (FileNotFoundException e) {
                    return false;
                }
            }
            throw null;
        }
    }

    static boolean pending(Context context, Uri uri) throws Exception {
        Throwable th = null;
        try {
            Cursor cursorQuery = context.getContentResolver().query(uri, new String[]{"is_pending"}, null, null, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst()) {
                        boolean z = cursorQuery.getInt(0) != 0;
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return z;
                    }
                } catch (Throwable th2) {
                    if (cursorQuery == null) {
                        throw th2;
                    }
                    cursorQuery.close();
                    throw th2;
                }
            }
            throw new IOException("Gallery row vanished");
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

    static void publish(Context context, PhotoJobStore photoJobStore, PhotoJobStore.Job job) throws Exception {
        File fileOutput = photoJobStore.output(job.id);
        if (!fileOutput.isFile() || fileOutput.length() != job.size || !DurablePhotoFiles.hash(fileOutput).equals(job.outputHash)) {
            throw new IOException("Final JPEG checkpoint is incomplete");
        }
        ContentResolver contentResolver = context.getContentResolver();
        Uri uriFind = find(context, job);
        if (uriFind == null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", name(job));
            contentValues.put("mime_type", "image/jpeg");
            contentValues.put("relative_path", PATH);
            contentValues.put("is_pending", (Integer) 1);
            CaptureSnapshot captureSnapshotDecode = CaptureSnapshot.decode(job.snapshot);
            contentValues.put("datetaken", Long.valueOf(captureSnapshotDecode.a.g > 0 ? captureSnapshotDecode.a.g : job.created));
            uriFind = contentResolver.insert(COLLECTION, contentValues);
            if (uriFind == null) {
                throw new IOException("Gallery insert failed");
            }
        }
        photoJobStore.mediaUri(job.id, uriFind.toString());
        PhotoProgress.update(96, "写入相册");
        boolean zMatches = matches(context, uriFind, job);
        boolean zPending = pending(context, uriFind);
        Throwable th = null;
        if (!zMatches) {
            if (!zPending) {
                throw new IOException("Published image changed outside the camera");
            }
            ParcelFileDescriptor parcelFileDescriptorOpenFileDescriptor = contentResolver.openFileDescriptor(uriFind, "rwt");
            if (parcelFileDescriptorOpenFileDescriptor == null) {
                throw new IOException("Cannot open gallery image");
            }
            try {
                ParcelFileDescriptor.AutoCloseOutputStream autoCloseOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptorOpenFileDescriptor);
                try {
                    FileInputStream fileInputStream = new FileInputStream(fileOutput);
                    try {
                        byte[] bArr = new byte[65536];
                        while (true) {
                            int i = fileInputStream.read(bArr);
                            if (i == -1) {
                                break;
                            } else {
                                autoCloseOutputStream.write(bArr, 0, i);
                            }
                        }
                        autoCloseOutputStream.getFD().sync();
                        fileInputStream.close();
                        autoCloseOutputStream.close();
                        if (!matches(context, uriFind, job)) {
                            throw new IOException("Gallery byte verification failed");
                        }
                    } catch (Throwable th2) {
                        fileInputStream.close();
                        throw th2;
                    }
                } catch (Throwable th3) {
                    if (0 == 0) {
                        th = th3;
                    } else if (null != th3) {
                        th.addSuppressed(th3);
                    }
                    autoCloseOutputStream.close();
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
        PhotoProgress.update(99, "发布相片");
        if (zPending) {
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("is_pending", (Integer) 0);
            if (contentResolver.update(uriFind, contentValues2, null, null) != 1) {
                throw new IOException("Gallery publication failed");
            }
        }
        photoJobStore.done(job.id);
        PhotoProgress.update(100, "已保存到相册");
    }
}
