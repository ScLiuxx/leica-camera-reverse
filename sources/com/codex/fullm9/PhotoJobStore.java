package com.codex.fullm9;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class PhotoJobStore {
    private static final String COLUMNS = "seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error";
    private static PhotoJobStore instance;
    private final SQLiteDatabase db;
    final File dir;

    static synchronized PhotoJobStore get(Context context) throws Exception {
        if (instance == null) {
            instance = new PhotoJobStore(new File(context.getNoBackupFilesDir(), "m9-photo-jobs"));
        }
        return instance;
    }

    PhotoJobStore(File file) throws Exception {
        this.dir = file;
        if (!this.dir.isDirectory() && !this.dir.mkdirs()) {
            throw new IOException("Cannot create photo queue");
        }
        this.db = SQLiteDatabase.openDatabase(new File(this.dir, "jobs.db"), new SQLiteDatabase.OpenParams.Builder().addOpenFlags(268435456).setJournalMode("WAL").setSynchronousMode("FULL").build());
        this.db.execSQL("CREATE TABLE IF NOT EXISTS jobs(seq INTEGER PRIMARY KEY AUTOINCREMENT,id TEXT UNIQUE NOT NULL,state TEXT NOT NULL,created INTEGER NOT NULL,snapshot TEXT,input_hash TEXT,output_hash TEXT,output_size INTEGER,uri TEXT,error TEXT,attempts INTEGER NOT NULL DEFAULT 0,notified INTEGER NOT NULL DEFAULT 0)");
    }

    static final class Job {
        long created;
        String error;
        String id;
        String inputHash;
        String outputHash;
        long seq;
        long size;
        String snapshot;
        String state;
        String uri;

        Job() {
        }
    }

    synchronized String reserve() throws Exception {
        String string;
        string = UUID.randomUUID().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", string);
        contentValues.put("state", "RESERVED");
        contentValues.put("created", Long.valueOf(System.currentTimeMillis()));
        if (this.db.insertOrThrow("jobs", null, contentValues) < 0) {
            throw new IOException("Capture reservation failed");
        }
        return string;
    }

    File manifest(String str) {
        return new File(this.dir, String.valueOf(str) + ".json");
    }

    File input(String str) {
        return new File(this.dir, String.valueOf(str) + ".source");
    }

    File output(String str) {
        return new File(this.dir, String.valueOf(str) + ".jpg");
    }

    private Job from(Cursor cursor) {
        Job job = new Job();
        job.seq = cursor.getLong(0);
        job.id = cursor.getString(1);
        job.state = cursor.getString(2);
        job.created = cursor.getLong(3);
        job.snapshot = cursor.getString(4);
        job.inputHash = cursor.getString(5);
        job.outputHash = cursor.getString(6);
        job.size = cursor.getLong(7);
        job.uri = cursor.getString(8);
        job.error = cursor.getString(9);
        return job;
    }

    synchronized Job find(String str) {
        Job jobFrom;
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error FROM jobs WHERE id=?", new String[]{str});
            try {
                jobFrom = cursorRawQuery.moveToFirst() ? from(cursorRawQuery) : null;
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
            } catch (Throwable th2) {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw null;
        }
        return jobFrom;
    }

    synchronized boolean accept(String str, byte[] bArr, String str2) throws Exception {
        Job jobFind = find(str);
        if (jobFind == null) {
            throw new IOException("Missing reservation");
        }
        if (!"RESERVED".equals(jobFind.state) && !"NO_INPUT".equals(jobFind.state)) {
            return true;
        }
        String strHash = DurablePhotoFiles.hash(bArr);
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("schema", 1);
        jSONObject.put("snapshot", str2);
        jSONObject.put("input_hash", strHash);
        jSONObject.put("input_size", bArr.length);
        DurablePhotoFiles.write(manifest(str), jSONObject.toString().getBytes("UTF-8"));
        DurablePhotoFiles.write(input(str), bArr);
        ContentValues contentValues = new ContentValues();
        contentValues.put("snapshot", str2);
        contentValues.put("input_hash", strHash);
        contentValues.put("state", "READY");
        contentValues.putNull("error");
        update(str, contentValues);
        return true;
    }

    private void update(String str, ContentValues contentValues) throws IOException {
        if (this.db.update("jobs", contentValues, "id=?", new String[]{str}) != 1) {
            throw new IOException("Missing photo job " + str);
        }
    }

    synchronized int pending() {
        int i;
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT COUNT(*) FROM jobs WHERE state IN ('RESERVED','READY','PROCESSING','ENCODED','PUBLISHING')", null);
            try {
                cursorRawQuery.moveToFirst();
                i = cursorRawQuery.getInt(0);
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
            } catch (Throwable th2) {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                    throw null;
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw null;
            throw th4;
        }
        return i;
    }

    synchronized int failures() {
        int i;
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT COUNT(*) FROM jobs WHERE state='FAILED'", null);
            try {
                cursorRawQuery.moveToFirst();
                i = cursorRawQuery.getInt(0);
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
            } catch (Throwable th2) {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                    throw null;
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw null;
            throw th4;
        }
        return i;
    }

    synchronized void expireReservations() throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "NO_INPUT");
        contentValues.put("error", "Camera did not deliver a complete source; not accepted");
        this.db.update("jobs", contentValues, "state='RESERVED' AND created<?", new String[]{Long.toString(System.currentTimeMillis() - 120000)});
    }

    synchronized Job next() {
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error FROM jobs WHERE state IN ('RESERVED','READY','PROCESSING','ENCODED','PUBLISHING') ORDER BY seq LIMIT 1", null);
            try {
                if (!cursorRawQuery.moveToFirst()) {
                    if (cursorRawQuery != null) {
                        cursorRawQuery.close();
                    }
                    return null;
                }
                Job jobFrom = from(cursorRawQuery);
                if ("RESERVED".equals(jobFrom.state)) {
                    jobFrom = null;
                }
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                return jobFrom;
            } catch (Throwable th2) {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                    throw null;
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw null;
            throw th4;
        }
    }

    synchronized void processing(String str) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "PROCESSING");
        update(str, contentValues);
        this.db.execSQL("UPDATE jobs SET attempts=attempts+1 WHERE id=?", new Object[]{str});
    }

    synchronized void encoded(String str, byte[] bArr) throws Exception {
        DurablePhotoFiles.write(output(str), bArr);
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "ENCODED");
        contentValues.put("output_hash", DurablePhotoFiles.hash(bArr));
        contentValues.put("output_size", Integer.valueOf(bArr.length));
        update(str, contentValues);
    }

    synchronized void mediaUri(String str, String str2) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "PUBLISHING");
        contentValues.put("uri", str2);
        update(str, contentValues);
    }

    synchronized void done(String str) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "DONE");
        contentValues.putNull("error");
        update(str, contentValues);
        cleanup(str);
    }

    synchronized void failed(String str, Throwable th) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", "FAILED");
        contentValues.put("error", String.valueOf(th.getClass().getSimpleName()) + ": " + th.getMessage());
        this.db.update("jobs", contentValues, "id=? AND state!='DONE'", new String[]{str});
    }

    synchronized void abandon(String str, String str2) throws Exception {
        Job jobFind = find(str);
        if (jobFind != null && "RESERVED".equals(jobFind.state)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("state", "NO_INPUT");
            contentValues.put("error", str2);
            update(str, contentValues);
        }
    }

    synchronized void recover(boolean z) throws Exception {
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error FROM jobs WHERE state IN ('PROCESSING','DONE','RESERVED','FAILED') ORDER BY seq", null);
            while (cursorRawQuery.moveToNext()) {
                try {
                    Job jobFrom = from(cursorRawQuery);
                    if ("DONE".equals(jobFrom.state)) {
                        cleanup(jobFrom.id);
                    } else if ("RESERVED".equals(jobFrom.state)) {
                        if (manifest(jobFrom.id).isFile() && input(jobFrom.id).isFile()) {
                            try {
                                JSONObject jSONObject = new JSONObject(new String(DurablePhotoFiles.read(manifest(jobFrom.id)), "UTF-8"));
                                if (jSONObject.getInt("schema") == 1 && input(jobFrom.id).length() == jSONObject.getLong("input_size") && DurablePhotoFiles.hash(input(jobFrom.id)).equals(jSONObject.getString("input_hash"))) {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("snapshot", jSONObject.getString("snapshot"));
                                    contentValues.put("input_hash", jSONObject.getString("input_hash"));
                                    contentValues.put("state", "READY");
                                    update(jobFrom.id, contentValues);
                                }
                            } catch (Exception e) {
                                Log.e("FullM9Queue", "Incomplete unaccepted reservation retained", e);
                            }
                        }
                    } else if (!"FAILED".equals(jobFrom.state) || z) {
                        ContentValues contentValues2 = new ContentValues();
                        contentValues2.put("state", (jobFrom.outputHash == null || !output(jobFrom.id).isFile()) ? "READY" : "ENCODED");
                        update(jobFrom.id, contentValues2);
                    }
                } catch (Throwable th2) {
                    if (cursorRawQuery == null) {
                        throw th2;
                    }
                    cursorRawQuery.close();
                    throw th2;
                }
            }
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            expireReservations();
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

    private void cleanup(String str) {
        manifest(str).delete();
        new File(manifest(str) + ".part").delete();
        input(str).delete();
        output(str).delete();
        new File(input(str) + ".part").delete();
        new File(output(str) + ".part").delete();
    }

    synchronized List<Job> unnotified() {
        ArrayList arrayList;
        arrayList = new ArrayList();
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error FROM jobs WHERE state='DONE' AND notified=0 ORDER BY seq", null);
            while (cursorRawQuery.moveToNext()) {
                try {
                    arrayList.add(from(cursorRawQuery));
                } catch (Throwable th2) {
                    if (cursorRawQuery == null) {
                        throw th2;
                    }
                    cursorRawQuery.close();
                    throw th2;
                }
            }
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
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
        return arrayList;
    }

    synchronized void notified(String str) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("notified", (Integer) 1);
        update(str, contentValues);
    }

    synchronized void retryFailures() throws Exception {
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,id,state,created,snapshot,input_hash,output_hash,output_size,uri,error FROM jobs WHERE state='FAILED' ORDER BY seq", null);
            while (cursorRawQuery.moveToNext()) {
                try {
                    Job jobFrom = from(cursorRawQuery);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("state", (jobFrom.outputHash == null || !output(jobFrom.id).isFile()) ? "READY" : "ENCODED");
                    update(jobFrom.id, contentValues);
                } catch (Throwable th2) {
                    if (cursorRawQuery != null) {
                        cursorRawQuery.close();
                    }
                    throw th2;
                }
            }
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                try {
                    th.addSuppressed(th3);
                    throw null;
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw null;
            throw th4;
        }
    }

    synchronized void close() {
        this.db.close();
    }

    synchronized String diagnostics() {
        StringBuilder sb;
        sb = new StringBuilder();
        Throwable th = null;
        try {
            Cursor cursorRawQuery = this.db.rawQuery("SELECT seq,state,error FROM jobs ORDER BY seq DESC LIMIT 12", null);
            while (cursorRawQuery.moveToNext()) {
                try {
                    sb.append("第 ").append(cursorRawQuery.getLong(0)).append(" 张：").append(cursorRawQuery.getString(1)).append(cursorRawQuery.getString(2) == null ? "" : " · " + cursorRawQuery.getString(2)).append('\n');
                } catch (Throwable th2) {
                    if (cursorRawQuery == null) {
                        throw th2;
                    }
                    cursorRawQuery.close();
                    throw th2;
                }
            }
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
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
        return sb.toString();
    }
}
