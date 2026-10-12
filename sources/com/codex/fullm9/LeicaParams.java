package com.codex.fullm9;

import android.content.Context;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class LeicaParams {
    final int lutCount;
    final int lutOff;
    final byte[] raw;
    final Map<String, List<Bin>> scenes;
    final int shadeOff;
    final List<List<Bin>> shadingTrees;
    final List<Float> zooms;

    static final class Bin {
        int hi;
        int index;
        int lo;
        List<Bin> sub;

        Bin(int i, int i2, int i3) {
            this.lo = i;
            this.hi = i2;
            this.index = i3;
        }
    }

    LeicaParams(Context context) throws IOException {
        this(context, false);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.codex.fullm9.LeicaParams, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.codex.fullm9.LeicaParams] */
    /* JADX WARN: Type inference failed for: r0v11 */
    /* JADX WARN: Type inference failed for: r0v12 */
    /* JADX WARN: Type inference failed for: r0v13 */
    /* JADX WARN: Type inference failed for: r0v14 */
    /* JADX WARN: Type inference failed for: r0v3 */
    /* JADX WARN: Type inference failed for: r0v4, types: [com.codex.fullm9.LeicaParams] */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* JADX WARN: Type inference failed for: r0v9 */
    LeicaParams(Context context, boolean z) throws IOException {
        ?? r0;
        ?? obj = new Object();
        obj.scenes = new HashMap();
        obj.zooms = new ArrayList();
        obj.shadingTrees = new ArrayList();
        obj.raw = Assets.read(context, z ? "leica_filter_param_m9_preview.bin" : "leica_filter_param_m9_snapshot.bin");
        ByteBuffer byteBufferOrder = ByteBuffer.wrap(obj.raw).order(ByteOrder.LITTLE_ENDIAN);
        int iU = u(byteBufferOrder);
        int[] iArr = new int[iU];
        int i = 0;
        ?? r1 = obj;
        while (i < iU) {
            iArr[i] = u(byteBufferOrder);
            i++;
            r1 = this;
        }
        int iU2 = u(byteBufferOrder);
        r1.lutOff = u(byteBufferOrder);
        int i2 = 17;
        if (u(byteBufferOrder) != 17) {
            throw new IOException("M9 LUT size");
        }
        int iU3 = u(byteBufferOrder);
        int[] iArr2 = new int[iU3];
        int i3 = 0;
        while (i3 < iU3) {
            r0 = r1;
            iArr2[i3] = u(byteBufferOrder);
            i3++;
            i2 = 17;
            r0 = this;
        }
        r0 = r1;
        int i4 = 0;
        int i5 = iU2;
        int iMax = 0;
        ?? r2 = r0;
        while (i4 < iU) {
            int i6 = iArr[i4];
            byteBufferOrder.position(i5);
            byte[] bArr = new byte[64];
            byteBufferOrder.get(bArr);
            int i7 = 0;
            while (i7 < 64 && bArr[i7] != 0) {
                i7++;
            }
            List<Bin> listTree = tree(byteBufferOrder);
            r2.scenes.put(new String(bArr, 0, i7, "UTF-8"), listTree);
            Iterator<Bin> it = listTree.iterator();
            while (it.hasNext()) {
                Iterator<Bin> it2 = it.next().sub.iterator();
                while (it2.hasNext()) {
                    iMax = Math.max(iMax, it2.next().index + 1);
                }
            }
            i5 += i6;
            i4++;
            i2 = 17;
            r2 = this;
        }
        for (int i8 = 0; i8 < iU3; i8++) {
            int i9 = iArr2[i8];
            byteBufferOrder.position(i5);
            r2.zooms.add(Float.valueOf(byteBufferOrder.getFloat()));
            r2.shadingTrees.add(tree(byteBufferOrder));
            i5 += i9;
        }
        r2.lutCount = iMax;
        r2.shadeOff = r2.lutOff + (iMax * i2 * i2 * i2 * 3);
    }

    static int u(ByteBuffer byteBuffer) {
        return byteBuffer.getShort() & 65535;
    }

    static List<Bin> tree(ByteBuffer byteBuffer) {
        int iU = u(byteBuffer);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < iU; i++) {
            Bin bin = new Bin(u(byteBuffer), u(byteBuffer), -1);
            int iU2 = u(byteBuffer);
            bin.sub = new ArrayList();
            for (int i2 = 0; i2 < iU2; i2++) {
                bin.sub.add(new Bin(u(byteBuffer), u(byteBuffer), u(byteBuffer)));
            }
            arrayList.add(bin);
        }
        return arrayList;
    }

    static Bin[] find(List<Bin> list, int i) {
        int i2 = 0;
        while (i2 < list.size()) {
            Bin bin = list.get(i2);
            if (i <= bin.hi) {
                return (i >= bin.lo || i2 == 0) ? new Bin[]{bin, bin} : new Bin[]{list.get(i2 - 1), bin};
            }
            i2++;
        }
        Bin bin2 = list.get(list.size() - 1);
        return new Bin[]{bin2, bin2};
    }

    static float fraction(int i, Bin bin, Bin bin2) {
        int i2 = bin2.lo - bin.hi;
        if (i2 == 0) {
            return 0.0f;
        }
        return (i - bin.hi) / i2;
    }

    static LinkedHashMap<Integer, Float> corners(List<Bin> list, int i, int i2) {
        Bin[] binArrFind = find(list, i);
        Bin[] binArrFind2 = find(binArrFind[0].sub, i2);
        Bin[] binArrFind3 = find(binArrFind[1].sub, i2);
        float fFraction = fraction(i, binArrFind[0], binArrFind[1]);
        float fFraction2 = fraction(i2, binArrFind2[0], binArrFind2[1]);
        float fFraction3 = fraction(i2, binArrFind3[0], binArrFind3[1]);
        int[] iArr = {binArrFind2[0].index, binArrFind2[1].index, binArrFind3[0].index, binArrFind3[1].index};
        float f = 1.0f - fFraction;
        float[] fArr = {(1.0f - fFraction2) * f, f * fFraction2, (1.0f - fFraction3) * fFraction, fFraction * fFraction3};
        LinkedHashMap<Integer, Float> linkedHashMap = new LinkedHashMap<>();
        for (int i3 = 0; i3 < 4; i3++) {
            linkedHashMap.put(Integer.valueOf(iArr[i3]), Float.valueOf((linkedHashMap.containsKey(Integer.valueOf(iArr[i3])) ? linkedHashMap.get(Integer.valueOf(iArr[i3])).floatValue() : 0.0f) + fArr[i3]));
        }
        return linkedHashMap;
    }

    byte[] lut(String str, int i, int i2) {
        List<Bin> list = this.scenes.get(str);
        if (list == null) {
            list = this.scenes.get("common");
        }
        LinkedHashMap<Integer, Float> linkedHashMapCorners = corners(list, i, i2);
        byte[] bArr = new byte[14739];
        if (linkedHashMapCorners.size() == 1) {
            System.arraycopy(this.raw, this.lutOff + (linkedHashMapCorners.keySet().iterator().next().intValue() * 14739), bArr, 0, 14739);
            return bArr;
        }
        float[] fArr = new float[14739];
        for (Map.Entry<Integer, Float> entry : linkedHashMapCorners.entrySet()) {
            int iIntValue = this.lutOff + (entry.getKey().intValue() * 14739);
            for (int i3 = 0; i3 < 14739; i3++) {
                fArr[i3] = fArr[i3] + (entry.getValue().floatValue() * (this.raw[iIntValue + i3] & 255));
            }
        }
        for (int i4 = 0; i4 < 14739; i4++) {
            bArr[i4] = (byte) fArr[i4];
        }
        return bArr;
    }

    float[] shading(float f, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < this.zooms.size(); i4++) {
            if (f >= this.zooms.get(i4).floatValue()) {
                i3 = i4;
            }
        }
        float[] fArr = new float[8];
        ByteBuffer byteBufferOrder = ByteBuffer.wrap(this.raw).order(ByteOrder.LITTLE_ENDIAN);
        for (Map.Entry<Integer, Float> entry : corners(this.shadingTrees.get(i3), i, i2).entrySet()) {
            byteBufferOrder.position(this.shadeOff + (entry.getKey().intValue() * 32));
            for (int i5 = 0; i5 < 8; i5++) {
                fArr[i5] = fArr[i5] + (entry.getValue().floatValue() * byteBufferOrder.getFloat());
            }
        }
        return fArr;
    }
}
