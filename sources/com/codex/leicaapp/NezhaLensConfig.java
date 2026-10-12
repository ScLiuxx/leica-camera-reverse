package com.codex.leicaapp;

import android.util.SparseArray;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class NezhaLensConfig {
    public static SparseArray<Float[]> X0() {
        SparseArray sparseArray = new SparseArray();
        Float fValueOf = Float.valueOf(0.6f);
        Float fValueOf2 = Float.valueOf(1.0f);
        Float fValueOf3 = Float.valueOf(2.0f);
        Float fValueOf4 = Float.valueOf(3.2f);
        Float fValueOf5 = Float.valueOf(4.3f);
        sparseArray.put(256, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5});
        Float fValueOf6 = Float.valueOf(8.6f);
        sparseArray.put(163, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, fValueOf6});
        sparseArray.put(175, new Float[]{fValueOf, fValueOf2, fValueOf4, fValueOf5});
        sparseArray.put(173, new Float[]{fValueOf, fValueOf2, fValueOf4, fValueOf5});
        sparseArray.put(172, new Float[]{fValueOf, fValueOf2, fValueOf4, fValueOf5});
        sparseArray.put(171, new Float[]{fValueOf2, fValueOf3, fValueOf4, fValueOf5});
        sparseArray.put(167, new Float[]{fValueOf, fValueOf2, fValueOf4, fValueOf5});
        sparseArray.put(180, new Float[]{fValueOf, fValueOf2, fValueOf4, fValueOf5});
        sparseArray.put(231, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, fValueOf6});
        return AdaptiveLens.overlayStops(sparseArray);
    }

    public static Map<Integer, SparseArray<String[]>> c1() {
        HashMap map = new HashMap();
        SparseArray sparseArray = new SparseArray();
        sparseArray.put(1, new String[]{"1.0", "28mm", "35mm"});
        sparseArray.put(3, new String[]{"3.2-75mm", "3.7-85mm", "3.9-90mm"});
        sparseArray.put(5, new String[]{"8.6-200mm", "17.2-400mm"});
        map.put(163, sparseArray);
        SparseArray sparseArray2 = new SparseArray();
        sparseArray2.put(0, new String[]{"1.0", "28mm", "35mm"});
        sparseArray2.put(2, new String[]{"3.2-75mm", "3.7-85mm", "3.9-90mm"});
        map.put(171, sparseArray2);
        SparseArray sparseArray3 = new SparseArray();
        sparseArray3.put(2, new String[]{"3.2-75mm", "3.7-85mm", "3.9-90mm"});
        map.put(167, sparseArray3);
        map.put(180, sparseArray3);
        map.put(172, sparseArray3);
        SparseArray sparseArray4 = new SparseArray();
        sparseArray4.put(3, new String[]{"3.2-75mm", "3.7-85mm", "3.9-90mm"});
        map.put(162, sparseArray4);
        map.put(169, sparseArray4);
        map.put(186, sparseArray4);
        map.put(231, sparseArray4);
        map.put(175, sparseArray4);
        SparseArray sparseArray5 = new SparseArray();
        sparseArray5.put(1, new String[]{"1.0", "28mm", "35mm"});
        sparseArray5.put(3, new String[]{"3.2-75mm", "3.7-85mm", "3.9-90mm"});
        map.put(256, sparseArray5);
        map.put(256, map.get(163));
        return AdaptiveLens.overlayNames(map);
    }

    public static SparseArray<SparseArray<Float[]>> d1() {
        SparseArray sparseArray = new SparseArray();
        SparseArray sparseArray2 = new SparseArray();
        Float fValueOf = Float.valueOf(0.6f);
        Float fValueOf2 = Float.valueOf(1.0f);
        Float fValueOf3 = Float.valueOf(3.2f);
        Float fValueOf4 = Float.valueOf(4.3f);
        Float fValueOf5 = Float.valueOf(8.6f);
        sparseArray2.put(0, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, Float.valueOf(17.2f)});
        sparseArray.put(163, sparseArray2);
        SparseArray sparseArray3 = new SparseArray();
        sparseArray3.put(0, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5});
        sparseArray.put(256, sparseArray3);
        return AdaptiveLens.overlayRanges(sparseArray);
    }

    public static SparseArray<Float[]> scaleStops() {
        SparseArray sparseArray = new SparseArray();
        Float fValueOf = Float.valueOf(0.6f);
        Float fValueOf2 = Float.valueOf(1.0f);
        Float fValueOf3 = Float.valueOf(1.2f);
        Float fValueOf4 = Float.valueOf(1.5f);
        Float fValueOf5 = Float.valueOf(2.0f);
        Float fValueOf6 = Float.valueOf(3.2f);
        Float fValueOf7 = Float.valueOf(3.7f);
        Float fValueOf8 = Float.valueOf(3.9f);
        Float fValueOf9 = Float.valueOf(4.3f);
        Float fValueOf10 = Float.valueOf(8.6f);
        Float fValueOf11 = Float.valueOf(17.2f);
        Float fValueOf12 = Float.valueOf(30.0f);
        Float fValueOf13 = Float.valueOf(60.0f);
        Float fValueOf14 = Float.valueOf(120.0f);
        sparseArray.put(163, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, fValueOf6, fValueOf7, fValueOf8, fValueOf9, fValueOf10, fValueOf11, fValueOf12, fValueOf13, fValueOf14});
        sparseArray.put(256, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, fValueOf6, fValueOf7, fValueOf8, fValueOf9, fValueOf10, fValueOf11, Float.valueOf(20.0f)});
        sparseArray.put(171, new Float[]{fValueOf2, fValueOf3, fValueOf4, fValueOf5, fValueOf6, fValueOf7, fValueOf8, fValueOf9, Float.valueOf(6.0f)});
        sparseArray.put(231, new Float[]{fValueOf, fValueOf2, fValueOf3, fValueOf4, fValueOf5, Float.valueOf(3.0f), Float.valueOf(3.6f), fValueOf9, fValueOf10, fValueOf11, fValueOf12, fValueOf13, fValueOf14});
        return AdaptiveLens.overlayStops(sparseArray);
    }
}
