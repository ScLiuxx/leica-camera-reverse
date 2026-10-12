package com.codex.fullm9;

import android.content.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class Tuning {
    final JSONObject tables;
    final Map<String, List<Node>> trees = new HashMap();

    static final class Node {
        double hi;
        double[] leaf;
        double lo;
        List<Node> sub = new ArrayList();

        Node(double d, double d2) {
            this.lo = d;
            this.hi = d2;
        }
    }

    Tuning(Context context) throws Exception {
        this.tables = new JSONObject(new String(Assets.read(context, "ipe_tables.json"), "UTF-8"));
    }

    static double[] array(JSONArray jSONArray) throws JSONException {
        int length = jSONArray.length();
        double[] dArr = new double[length];
        for (int i = 0; i < length; i++) {
            dArr[i] = jSONArray.getDouble(i);
        }
        return dArr;
    }

    static String[] levels(String str, String str2) {
        if (!str.equals("tele") && !str.equals("tele_lin")) {
            if (str.equals("tele_normal") && str2.startsWith("cc")) {
                return new String[]{"drc", "one", "one", "lux", "cct", "zoom"};
            }
            if (str2.startsWith("gamma")) {
                return new String[]{"one", "drc", "gain", "one", "lux", "cct"};
            }
            if (str2.startsWith("cc") || str2.startsWith("tdl")) {
                return new String[]{"drc", "flag", "lux", "cct"};
            }
            if (str2.startsWith("cv")) {
                return new String[]{"drc", "gain", "flag", "lux", "cct"};
            }
            return str2.startsWith("ltm") ? new String[]{"drc", "lux"} : new String[]{"drc", "gain", "lux"};
        }
        if (str2.startsWith("gamma")) {
            return new String[]{"drc", "gain", "one", "lux", "one"};
        }
        if (str2.startsWith("cc")) {
            return new String[]{"one", "one", "one", "lux", "cct", "one"};
        }
        if (str2.startsWith("cv")) {
            return new String[]{"one", "gain", "lux", "cct"};
        }
        return str2.startsWith("tdl") ? new String[]{"one", "one", "flag", "lux", "cct", "zoom"} : new String[]{"drc", "gain", "lux", "zoom"};
    }

    List<Node> build(String str, String str2) throws Exception {
        Node node;
        double d;
        String str3 = String.valueOf(str) + "/" + str2;
        if (this.trees.containsKey(str3)) {
            return this.trees.get(str3);
        }
        ArrayList arrayList = new ArrayList();
        JSONArray jSONArray = this.tables.getJSONObject(str).getJSONArray(str2);
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            JSONArray jSONArray2 = jSONObject.getJSONArray("path");
            List<Node> list = arrayList;
            for (int i2 = 0; i2 < jSONArray2.length(); i2++) {
                JSONArray jSONArray3 = jSONArray2.getJSONArray(i2);
                boolean z = true;
                double d2 = jSONArray3.getDouble(1);
                double d3 = jSONArray3.getDouble(2);
                Iterator<Node> it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        node = null;
                        d = d2;
                        break;
                    }
                    Node next = it.next();
                    boolean z2 = z;
                    d = d2;
                    if (next.lo == d && next.hi == d3) {
                        node = next;
                        break;
                    }
                    d2 = d;
                    z = z2;
                }
                if (node == null) {
                    node = new Node(d, d3);
                    list.add(node);
                }
                if (i2 == jSONArray2.length() - 1) {
                    node.leaf = array(jSONObject.getJSONArray("data"));
                }
                list = node.sub;
            }
        }
        sort(arrayList);
        this.trees.put(str3, arrayList);
        return arrayList;
    }

    static void sort(List<Node> list) {
        Collections.sort(list, new Comparator<Node>() { // from class: com.codex.fullm9.Tuning.1
            @Override // java.util.Comparator
            public int compare(Node node, Node node2) {
                int iCompare = Double.compare(node.lo, node2.lo);
                return iCompare == 0 ? Double.compare(node.hi, node2.hi) : iCompare;
            }
        });
        Iterator<Node> it = list.iterator();
        while (it.hasNext()) {
            sort(it.next().sub);
        }
    }

    static double[] child(Node node, double[] dArr, int i) {
        return node.leaf != null ? node.leaf : eval(node.sub, dArr, i);
    }

    static double[] eval(List<Node> list, double[] dArr, int i) {
        double d = dArr[i];
        Node node = null;
        Node node2 = null;
        for (Node node3 : list) {
            if (node3.lo <= d && d <= node3.hi) {
                return child(node3, dArr, i + 1);
            }
            if (node3.hi < d) {
                node = node3;
            }
            if (node3.lo > d && node2 == null) {
                node2 = node3;
            }
        }
        if (node == null) {
            return child(node2, dArr, i + 1);
        }
        if (node2 == null) {
            return child(node, dArr, i + 1);
        }
        double dMax = (d - node.hi) / Math.max(node2.lo - node.hi, 1.0E-9d);
        int i2 = i + 1;
        double[] dArrChild = child(node, dArr, i2);
        double[] dArrChild2 = child(node2, dArr, i2);
        int length = dArrChild.length;
        double[] dArr2 = new double[length];
        for (int i3 = 0; i3 < length; i3++) {
            dArr2[i3] = ((1.0d - dMax) * dArrChild[i3]) + (dArrChild2[i3] * dMax);
        }
        return dArr2;
    }

    double[] lookup(String str, String str2, double d, double d2, double d3) throws Exception {
        double d4;
        String[] strArrLevels = levels(str, str2);
        int length = strArrLevels.length;
        double[] dArr = new double[length];
        for (int i = 0; i < length; i++) {
            if (strArrLevels[i].equals("drc")) {
                d4 = d;
            } else if (strArrLevels[i].equals("gain")) {
                d4 = 1.0d;
            } else if (strArrLevels[i].equals("lux")) {
                d4 = d2;
            } else if (strArrLevels[i].equals("cct")) {
                d4 = d3;
            } else if (strArrLevels[i].equals("zoom")) {
                d4 = 4.0d;
            } else {
                d4 = strArrLevels[i].equals("flag") ? 0.0d : 0.5d;
            }
            dArr[i] = d4;
        }
        return eval(build(str, str2), dArr, 0);
    }

    static double[] inverse(double[] dArr) {
        double d = dArr[0];
        double d2 = dArr[1];
        double d3 = dArr[2];
        double d4 = dArr[3];
        double d5 = dArr[4];
        double d6 = dArr[5];
        double d7 = dArr[6];
        double d8 = dArr[7];
        double d9 = dArr[8];
        double d10 = (d5 * d9) - (d6 * d8);
        double d11 = d4 * d9;
        double d12 = d6 * d7;
        double d13 = (d4 * d8) - (d5 * d7);
        double d14 = ((d * d10) - ((d11 - d12) * d2)) + (d3 * d13);
        if (Math.abs(d14) >= 1.0E-10d) {
            return new double[]{d10 / d14, ((d3 * d8) - (d2 * d9)) / d14, ((d2 * d6) - (d3 * d5)) / d14, (d12 - d11) / d14, ((d9 * d) - (d3 * d7)) / d14, ((d3 * d4) - (d6 * d)) / d14, d13 / d14, ((d7 * d2) - (d8 * d)) / d14, ((d * d5) - (d2 * d4)) / d14};
        }
        throw new IllegalArgumentException("Singular color matrix");
    }

    static double[] multiply(double[] dArr, double[] dArr2) {
        double[] dArr3 = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                for (int i3 = 0; i3 < 3; i3++) {
                    int i4 = i * 3;
                    int i5 = i4 + i2;
                    dArr3[i5] = dArr3[i5] + (dArr[i4 + i3] * dArr2[(i3 * 3) + i2]);
                }
            }
        }
        return dArr3;
    }

    /* JADX WARN: Code duplicated, block: B:18:0x0085  */
    /* JADX WARN: Code duplicated, block: B:19:0x008b  */
    /* JADX WARN: Code duplicated, block: B:38:0x010a A[LOOP:3: B:35:0x0105->B:38:0x010a, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:39:0x0114 A[LOOP:2: B:32:0x00e5->B:39:0x0114, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:40:0x0126 A[LOOP:1: B:29:0x00e0->B:40:0x0126, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:41:0x0132 A[LOOP:0: B:27:0x00b3->B:41:0x0132, LOOP_END] */
    float[] config(String str, double[] dArr, double d, double d2, AE ae, boolean z) throws Exception {
        Tuning tuning;
        String str2;
        double[] dArrMultiply;
        double d3;
        float[] fArr;
        int i;
        int i2;
        double[] dArrLookup;
        double[] dArrLookup2;
        String str3;
        double[] dArrLookup3;
        int i3;
        int i4;
        String str4 = str.equals("tele") ? "tele" : "m9";
        String str5 = str.equals("tele") ? "tele_normal" : "normal";
        if (z) {
            tuning = this;
            if (tuning.tables.has(String.valueOf(str4) + "_lin")) {
                str2 = String.valueOf(str4) + "_lin";
            }
            dArrMultiply = multiply(multiply(Arrays.copyOf(tuning.lookup(str4, "cc15_ipe_v2", ae.adrc, d, d2), 9), inverse(Arrays.copyOf(lookup(str5, "cc15_ipe_v2", ae.adrc, d, d2), 9))), dArr);
            if (z) {
                d3 = 0.3d;
            } else {
                d3 = 1.0d;
            }
            if (z && d > 150.0d && ae.adrc < 1.33d) {
                d3 *= ae.adrc / 1.33d;
            }
            fArr = new float[5145];
            fArr[0] = (float) (ae.shortExposure * d3);
            i2 = 0;
            while (i2 < 9) {
                String str6 = str2;
                int i5 = i2 + 1;
                fArr[i5] = (float) dArrMultiply[i2];
                i2 = i5;
                str2 = str6;
                str4 = str4;
            }
            dArrLookup = lookup(str4, "tdl13_ipe_v2", ae.adrc, d, d2);
            String str7 = str2;
            dArrLookup2 = lookup(str7, "gamma152_ipe_v2", ae.adrc, d, d2);
            str3 = str7;
            dArrLookup3 = lookup(str4, "cv122_ipe_v2", ae.adrc, d, d2);
            i3 = 0;
            while (i3 < 768) {
                fArr[i3 + 10] = (float) dArrLookup[i3];
                i3++;
                str3 = str3;
            }
            i4 = 0;
            while (i4 < 257) {
                fArr[i4 + 778] = (float) (dArrLookup2[i4] / 1023.0d);
                i4++;
                str3 = str3;
            }
            System.arraycopy(tmc(lookup(str3, "tmc202_sw_v2", ae.adrc, d, d2), ae.adrc), 0, fArr, 1035, 4096);
            for (i = 0; i < 14; i++) {
                fArr[i + 5131] = (float) dArrLookup3[i];
            }
            return fArr;
        }
        tuning = this;
        str2 = str4;
        dArrMultiply = multiply(multiply(Arrays.copyOf(tuning.lookup(str4, "cc15_ipe_v2", ae.adrc, d, d2), 9), inverse(Arrays.copyOf(lookup(str5, "cc15_ipe_v2", ae.adrc, d, d2), 9))), dArr);
        if (z) {
            d3 = 0.3d;
        } else {
            d3 = 1.0d;
        }
        if (z) {
            d3 *= ae.adrc / 1.33d;
        }
        fArr = new float[5145];
        fArr[0] = (float) (ae.shortExposure * d3);
        i2 = 0;
        while (i2 < 9) {
            String str8 = str2;
            int i6 = i2 + 1;
            fArr[i6] = (float) dArrMultiply[i2];
            i2 = i6;
            str2 = str8;
            str4 = str4;
        }
        dArrLookup = lookup(str4, "tdl13_ipe_v2", ae.adrc, d, d2);
        String str9 = str2;
        dArrLookup2 = lookup(str9, "gamma152_ipe_v2", ae.adrc, d, d2);
        str3 = str9;
        dArrLookup3 = lookup(str4, "cv122_ipe_v2", ae.adrc, d, d2);
        i3 = 0;
        while (i3 < 768) {
            fArr[i3 + 10] = (float) dArrLookup[i3];
            i3++;
            str3 = str3;
        }
        i4 = 0;
        while (i4 < 257) {
            fArr[i4 + 778] = (float) (dArrLookup2[i4] / 1023.0d);
            i4++;
            str3 = str3;
        }
        System.arraycopy(tmc(lookup(str3, "tmc202_sw_v2", ae.adrc, d, d2), ae.adrc), 0, fArr, 1035, 4096);
        while (i < 14) {
            fArr[i + 5131] = (float) dArrLookup3[i];
        }
        return fArr;
    }

    static float[] tmc(double[] dArr, double d) {
        double dMax = Math.max(1.0E-6d, Math.min(1023.0d, ((d - 1.0d) * dArr[16]) + 1.0d + dArr[17]));
        double dMin = Math.min(Math.max(1.0E-6d, Math.min(1023.0d, dArr[20] + 1.0d)) * dMax, 256.0d);
        double d2 = dMax - 1.0d;
        double d3 = (dArr[42] - dArr[38]) * 0.25d;
        double[] dArr2 = {dMin, dMax, (dArr[39] * d2) + 1.0d, (dArr[40] * d2) + 1.0d, (d2 * dArr[41]) + 1.0d};
        int i = 7;
        double[] dArr3 = new double[7];
        double[] dArr4 = new double[7];
        dArr3[1] = seq(dArr[37] / dMin, dArr[36] + 1.0E-4d, 0.9995d);
        dArr3[2] = seq(dArr[38] / dMax, dArr3[1] + 1.0E-4d, 0.9996d);
        dArr3[3] = seq((dArr[38] + d3) / dArr2[2], dArr3[2] + 1.0E-4d, 0.9997d);
        double d4 = 2.0d;
        dArr3[4] = seq((dArr[38] + (d3 * 2.0d)) / dArr2[3], dArr3[3] + 1.0E-4d, 0.9998d);
        dArr3[5] = seq((dArr[38] + (d3 * 3.0d)) / dArr2[4], dArr3[4] + 1.0E-4d, 0.9999d);
        int i2 = 1;
        while (i2 <= 5) {
            double[] dArr5 = dArr3;
            dArr5[i2] = seq(dArr5[i2], i2 == 1 ? 1.0E-4d : dArr5[i2 - 1] + 1.0E-4d, (((double) i2) * 1.0E-4d) + 0.9994d);
            i2++;
            dArr3 = dArr5;
            d4 = 2.0d;
            i = 7;
        }
        int i3 = 6;
        dArr4[6] = 1.0d;
        dArr3[6] = 1.0d;
        int i4 = 1;
        while (i4 <= 5) {
            int i5 = i4 - 1;
            double[] dArr6 = dArr3;
            dArr4[i4] = seq(dArr3[i4] * Math.pow(dArr2[i5], dArr[0]), i4 == 1 ? 1.0E-4d : dArr4[i5] + 1.0E-4d, (((double) i4) * 1.0E-4d) + 0.9994d);
            i4++;
            dArr3 = dArr6;
            i3 = 6;
            d4 = 2.0d;
            i = 7;
        }
        double[] dArr7 = new double[i3];
        double[] dArr8 = new double[i3];
        double[] dArr9 = new double[i];
        double[] dArr10 = new double[i];
        double[] dArr11 = new double[i];
        int i6 = 0;
        while (i6 < i3) {
            int i7 = i6 + 1;
            dArr7[i6] = dArr3[i7] - dArr3[i6];
            dArr8[i6] = (dArr4[i7] - dArr4[i6]) / dArr7[i6];
            i6 = i7;
        }
        for (int i8 = 1; i8 < i3; i8++) {
            int i9 = i8 - 1;
            double d5 = (dArr7[i8] * d4) + dArr7[i9];
            double d6 = dArr7[i8] + (dArr7[i9] * d4);
            dArr9[i8] = (d5 + d6) / ((d5 / dArr8[i9]) + (d6 / dArr8[i8]));
        }
        double d7 = (((dArr7[4] + (dArr7[5] * d4)) * dArr8[5]) - (dArr7[5] * dArr8[4])) / (dArr7[4] + dArr7[5]);
        if (dArr8[4] * dArr8[5] < 0.0d && Math.abs(d7) > Math.abs(dArr8[5] * 3.0d)) {
            d7 = dArr8[5] * 3.0d;
        } else if (dArr8[5] * d7 < 0.0d) {
            d7 = 0.0d;
        }
        dArr9[i3] = d7;
        int i10 = 1;
        while (i10 < i3) {
            int i11 = i10 + 1;
            dArr10[i10] = (((dArr8[i10] * 3.0d) - (dArr9[i10] * d4)) - dArr9[i11]) / dArr7[i10];
            dArr11[i10] = ((dArr9[i10] - (dArr8[i10] * d4)) + dArr9[i11]) / (dArr7[i10] * dArr7[i10]);
            i10 = i11;
        }
        float[] fArr = new float[4096];
        for (int i12 = 0; i12 < 4096; i12++) {
            double d8 = ((double) i12) / 4095.0d;
            double d9 = (dArr4[1] * d8) / dArr3[1];
            for (int i13 = 1; i13 < i3; i13++) {
                if (d8 >= dArr3[i13] && d8 <= dArr3[i13 + 1]) {
                    double d10 = d8 - dArr3[i13];
                    d9 = dArr4[i13] + (dArr9[i13] * d10) + (dArr10[i13] * d10 * d10) + (dArr11[i13] * d10 * d10 * d10);
                }
            }
            fArr[i12] = (float) d9;
        }
        return fArr;
    }

    static double seq(double d, double d2, double d3) {
        return d > d2 ? Math.min(d, d3) : d2;
    }
}
