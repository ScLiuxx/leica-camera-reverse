package com.codex.fullm9;

import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class CaptureState {
    private final ArrayList<Entry> entries = new ArrayList<>();

    private static final class Entry {
        Field field;
        Object owner;
        Object value;

        private Entry() {
        }

        /* synthetic */ Entry(Entry entry) {
            this();
        }
    }

    CaptureState(Object obj) {
        Object objField = Reflect.field(obj, "a");
        String[] strArr = {"a", "b", "c", "d", "e", "j"};
        for (int i = 0; i < 6; i++) {
            remember(objField, strArr[i]);
        }
        remember(Reflect.field(obj, "b"), "b");
    }

    static void commitJpeg(Object obj, byte[] bArr) {
        if (obj == null || bArr == null || bArr.length < 2 || (bArr[0] & 255) != 255 || (bArr[1] & 255) != 216) {
            return;
        }
        Reflect.put(Reflect.field(obj, "a"), "j", 256);
    }

    private void remember(Object obj, String str) {
        if (obj == null) {
            return;
        }
        for (Class<?> superclass = obj.getClass(); superclass != null; superclass = superclass.getSuperclass()) {
            try {
                Field declaredField = superclass.getDeclaredField(str);
                declaredField.setAccessible(true);
                Entry entry = new Entry(null);
                entry.owner = obj;
                entry.field = declaredField;
                entry.value = declaredField.get(obj);
                this.entries.add(entry);
                return;
            } catch (NoSuchFieldException e) {
            } catch (Exception e2) {
                return;
            }
        }
    }

    void restore() {
        for (Entry entry : this.entries) {
            try {
                entry.field.set(entry.owner, entry.value);
            } catch (Exception e) {
                Log.w("FullM9", "Capture geometry restore", e);
            }
        }
    }
}
