package com.codex.leicaapp;

import android.util.Log;
import com.xiaomi.camera.effect.EffectController;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class PhotographyStylePreview {
    public static void apply(String str) {
        EffectController effectControllerU = EffectController.u();
        if ("0".equals(str)) {
            effectControllerU.p0(22);
            effectControllerU.n0(4);
        } else if ("1".equals(str)) {
            effectControllerU.p0(-18);
            effectControllerU.n0(8);
        } else {
            effectControllerU.p0(0);
            effectControllerU.n0(0);
        }
        Log.i("PhotographyStylePreview", str);
    }
}
