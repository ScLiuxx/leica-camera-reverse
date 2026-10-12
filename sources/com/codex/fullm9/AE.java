package com.codex.fullm9;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class AE {
    double adrc;
    double shortExposure;

    AE() {
    }

    static double i1(double d, JSONArray jSONArray, JSONArray jSONArray2) throws Exception {
        if (d <= jSONArray.getDouble(0)) {
            return jSONArray2.getDouble(0);
        }
        for (int i = 1; i < Math.min(jSONArray.length(), jSONArray2.length()); i++) {
            if (d <= jSONArray.getDouble(i)) {
                int i2 = i - 1;
                double d2 = jSONArray.getDouble(i2);
                double dMax = (d - d2) / Math.max(jSONArray.getDouble(i) - d2, 1.0E-9d);
                return (jSONArray2.getDouble(i2) * (1.0d - dMax)) + (jSONArray2.getDouble(i) * dMax);
            }
        }
        return jSONArray2.getDouble(jSONArray2.length() - 1);
    }

    static double i2(double d, double d2, JSONArray jSONArray, JSONArray jSONArray2, JSONArray jSONArray3) throws Exception {
        JSONArray jSONArray4 = new JSONArray();
        for (int i = 0; i < jSONArray3.length(); i++) {
            jSONArray4.put(i1(d, jSONArray, jSONArray3.getJSONArray(i)));
        }
        return i1(d2, jSONArray2, jSONArray4);
    }

    static double pct(float[] fArr, double d, double d2) {
        int length = fArr.length - 256;
        double d3 = length;
        int i = (int) (d * d3);
        int iMin = Math.min(length - 1, Math.max(0, i)) + 256;
        int iMin2 = Math.min(length, Math.max((int) (d2 * d3), i + 1)) + 256;
        double d4 = 0.0d;
        for (int i2 = iMin; i2 < iMin2; i2++) {
            d4 += (double) fArr[i2];
        }
        return d4 / ((double) Math.max(1, iMin2 - iMin));
    }

    static double weighted(float[] fArr, double d, double d2, JSONObject jSONObject) throws Exception {
        JSONArray jSONArray = jSONObject.getJSONArray(String.valueOf("weight_luma_calculation.") + "center_wghted_mtr_tbl");
        JSONArray jSONArray2 = jSONObject.getJSONArray(String.valueOf("weight_luma_calculation.") + "lux_node");
        double dI1 = i1(d2, jSONArray2, jSONObject.getJSONArray(String.valueOf("weight_luma_calculation.") + "bright_start"));
        double dI2 = i1(d2, jSONArray2, jSONObject.getJSONArray(String.valueOf("weight_luma_calculation.") + "bright_end"));
        double dI3 = i1(d2, jSONArray2, jSONObject.getJSONArray(String.valueOf("weight_luma_calculation.") + "bright_weight"));
        int i = 0;
        double d3 = 0.0d;
        double d4 = 0.0d;
        while (i < 256) {
            double d5 = ((double) fArr[i]) * d;
            double d6 = dI3;
            double d7 = jSONArray.getDouble(i) * (((d6 - 1.0d) * Math.max(0.0d, Math.min(1.0d, (d5 - dI1) / Math.max(dI2 - dI1, 1.0E-6d)))) + 1.0d);
            d3 += d5 * d7;
            d4 += d7;
            i++;
            dI3 = d6;
        }
        return d3 / Math.max(d4, 1.0E-9d);
    }

    static AE meter(Context context, float[] fArr, double d, String str) throws Exception {
        AE ae;
        double d2;
        AE ae2 = new AE();
        if (fArr.length <= 256) {
            throw new IllegalArgumentException("RAW metering sample");
        }
        if (fArr[fArr.length - 1] <= 1.0E-6d) {
            ae2.shortExposure = 1.0d;
            ae2.adrc = 1.0d;
            return ae2;
        }
        JSONObject jSONObject = new JSONObject(new String(Assets.read(context, str.equals("tele") ? "ae_tele.json" : "ae_m9.json"), "UTF-8"));
        JSONObject jSONObject2 = jSONObject.getJSONObject("metering");
        JSONObject jSONObject3 = jSONObject.getJSONObject("luma");
        JSONArray jSONArray = jSONObject2.getJSONArray(String.valueOf("hist_target_by_lux.hist_short_long_adjustment.") + "hist_lux_node");
        double dI1 = i1(d, jSONArray, jSONObject2.getJSONArray(String.valueOf("hist_target_by_lux.hist_short_long_adjustment.") + "hist_base_bright_start_pct"));
        double dI2 = i1(d, jSONArray, jSONObject2.getJSONArray(String.valueOf("hist_target_by_lux.hist_short_long_adjustment.") + "hist_dark_tone_end_pct"));
        double dPct = pct(fArr, dI1, 1.0d);
        double dMax = dPct / Math.max(pct(fArr, 0.0d, dI2), 0.001d);
        JSONArray jSONArray2 = jSONArray;
        double dI3 = i2(d, dMax, jSONObject2.getJSONArray(String.valueOf("base_target_by_lux.") + "base_target_lux_node"), jSONObject2.getJSONArray(String.valueOf("base_target_by_lux.") + "base_target_dr_node"), jSONObject2.getJSONArray(String.valueOf("base_target_by_lux.") + "base_target"));
        JSONObject jSONObject4 = jSONObject.getJSONObject("stylization");
        double dI4 = dI3 * (jSONObject4.optBoolean("enable_stylization.value", jSONObject4.optBoolean("enable_stylization", false)) ? i1(d, jSONObject4.getJSONArray("style_base.style_base_target_lux_node"), jSONObject4.getJSONArray("style_base.style_base_target_scale").getJSONArray(0)) : 1.0d);
        double dMax2 = dI4 / Math.max(weighted(fArr, 1.0d, d, jSONObject3), 1.0E-6d);
        int i = 0;
        while (true) {
            if (i >= 20) {
                ae = ae2;
                d2 = dMax2;
                break;
            }
            ae = ae2;
            double dMin = Math.min((dMax2 * dI4) / Math.max(weighted(fArr, dMax2, d, jSONObject3), 1.0E-6d), 1024.0d);
            if (Math.abs((dMin / Math.max(dMax2, 1.0E-9d)) - 1.0d) < 1.0E-4d) {
                d2 = dMin;
                break;
            }
            i++;
            ae2 = ae;
            jSONArray2 = jSONArray2;
            dMax2 = dMin;
        }
        double dI5 = i2(d, dMax, jSONArray2, jSONObject2.getJSONArray(String.valueOf("hist_target_by_lux.hist_short_long_adjustment.") + "hist_dr_node"), jSONObject2.getJSONArray(String.valueOf("hist_target_by_lux.hist_short_long_adjustment.") + "hist_bright_tone_ref_target")) / Math.max(dPct, 1.0E-6d);
        JSONObject jSONObject5 = jSONObject.getJSONObject("drc");
        ae.adrc = Math.max(1.0d, Math.min(i1(d, jSONObject5.getJSONArray("normal_drc_ctrl.nor_drc_lux_node"), jSONObject5.getJSONArray("normal_drc_ctrl.max_drc_gain").getJSONArray(0)), d2 / Math.max(dI5, 1.0E-9d)));
        ae.shortExposure = d2 / ae.adrc;
        if (Double.isFinite(ae.shortExposure) && Double.isFinite(ae.adrc)) {
            return ae;
        }
        throw new IllegalArgumentException("Non-finite AE");
    }
}
