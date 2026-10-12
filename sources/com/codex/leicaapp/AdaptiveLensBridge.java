package com.codex.leicaapp;

import Rh.e;
import android.util.Range;
import java.util.LinkedHashMap;
import r2.B0;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes14.dex */
public final class AdaptiveLensBridge {
    public static B0.c lensMap(int i) {
        String[][] strArrSpecs;
        int length;
        if (!AdaptiveLens.enabled(i) || (length = (strArrSpecs = AdaptiveLens.specs()).length) == 0) {
            return null;
        }
        B0.c cVar = new B0.c();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        cVar.a = linkedHashMap;
        for (int i2 = 0; i2 < length; i2++) {
            String[] strArr = strArrSpecs[i2];
            B0.a aVar = new B0.a();
            String str = strArr[0];
            aVar.a = str;
            aVar.c = Integer.parseInt(strArr[1]);
            int i3 = e.pref_camera_zoom_mode_entry_tele_abbr;
            if ("wide".equals(str)) {
                i3 = e.pref_camera_zoom_mode_entry_wide_abbr;
            }
            if ("ultra".equals(str)) {
                i3 = e.pref_camera_zoom_mode_entry_ultra_abbr;
            }
            aVar.b = i3;
            Float fValueOf = Float.valueOf(strArr[2]);
            Comparable upper = AdaptiveLens.range().getUpper();
            int i4 = i2 + 1;
            if (i4 < length) {
                upper = Float.valueOf(strArrSpecs[i4][2]);
            }
            aVar.d = new Range(fValueOf, upper);
            linkedHashMap.put(str, aVar);
        }
        return cVar;
    }

    public static B0.b wheel(int i, float[] fArr, String str, float f) {
        if (!AdaptiveLens.enabled(i) || AdaptiveLens.markers().length == 0) {
            return null;
        }
        float fWheelZoom = AdaptiveLens.wheelZoom(fArr, str, f);
        B0.b bVar = new B0.b();
        bVar.b = fWheelZoom;
        bVar.a = AdaptiveLens.tagForZoom(fWheelZoom);
        bVar.c = 0;
        return bVar;
    }
}
