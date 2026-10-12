package com.codex.fullm9;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class PhotoProgress {
    private static final ThreadLocal<Listener> CURRENT = new ThreadLocal<>();

    interface Listener {
        void update(int i, String str);
    }

    PhotoProgress() {
    }

    static void bind(Listener listener) {
        CURRENT.set(listener);
    }

    static void clear() {
        CURRENT.remove();
    }

    static void update(int i, String str) {
        Listener listener = CURRENT.get();
        if (listener != null) {
            listener.update(i, str);
        }
    }

    static void stage(String str) {
        int i;
        if ("输入降噪".equals(str)) {
            i = 3;
        } else if ("M9影调与色彩恢复".equals(str)) {
            i = 7;
        } else if ("影调与输出降噪".equals(str)) {
            i = 80;
        } else if ("精细纹理恢复".equals(str)) {
            i = 84;
        } else if ("水印".equals(str)) {
            i = 88;
        } else {
            i = "最终HDR与JPEG编码".equals(str) ? 92 : 0;
        }
        update(i, str);
    }
}
