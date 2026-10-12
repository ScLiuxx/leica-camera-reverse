package com.codex.leicaapp;

import Cw.H;
import Qg.r;
import Sh.C;
import Sh.f;
import Sh.t;
import Vh.e;
import android.hardware.camera2.CaptureResult;
import android.util.Log;
import com.codex.lmport.MomentBridge;
import g2.a;
import java.util.ArrayList;
import k6.k;
import r2.m;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class MadridBridge {
    public static void process(t tVar) {
        m mVar;
        byte[] bArrD;
        f fVar;
        ArrayList arrayList;
        C c;
        e eVar;
        byte[] bArrD2;
        byte[] bArrProcessJpeg;
        C c2;
        e eVar2;
        byte[] bArrD3;
        byte[] bArrProcess;
        boolean z = false;
        int i = tVar.b.g;
        Log.i("MadridBridge", Integer.toString(i));
        if (i != 167 && i == 256 && (eVar2 = (c2 = tVar.a).i) != null && (bArrD3 = eVar2.d()) != null && (bArrProcess = LegendaryPostProcessor.process(bArrD3, tVar)) != bArrD3) {
            e eVarA = Vh.f.a(bArrProcess);
            H.i(eVar2, eVarA);
            c2.i = eVarA;
            z = true;
        }
        int i2 = tVar.b.g;
        if ((i2 == 163 || i2 == 167 || i2 == 171) && (mVar = (m) a.a().x(m.class)) != null) {
            String persistValue = mVar.getPersistValue(i2);
            C c3 = tVar.a;
            e eVar3 = c3.i;
            if (eVar3 != null && (bArrD = eVar3.d()) != null) {
                String string = null;
                if (tVar.b.g == 167 && (fVar = tVar.d) != null && (arrayList = fVar.j) != null) {
                    string = arrayList.toString();
                }
                byte[] bArrProcessCombined = PalettePostProcessor.processCombined(bArrD, string, persistValue);
                if (bArrProcessCombined != bArrD) {
                    e eVarA2 = Vh.f.a(bArrProcessCombined);
                    H.i(eVar3, eVarA2);
                    c3.i = eVarA2;
                    z = true;
                }
            }
        }
        if (tVar.b.g == 167 && (eVar = (c = tVar.a).i) != null && (bArrD2 = eVar.d()) != null && (bArrProcessJpeg = OriginalWatermark.processJpeg(bArrD2, tVar)) != bArrD2) {
            e eVarA3 = Vh.f.a(bArrProcessJpeg);
            H.i(eVar, eVarA3);
            c.i = eVarA3;
            z = true;
        }
        if (z) {
            tVar.e.setXmpMetaUtil((r) null);
        }
    }

    public static void snapshot(t tVar, k kVar) {
        r2.C c;
        l9.a aVarT;
        MomentBridge.snapshot(tVar);
        if (tVar.b.g != 256 || (c = (r2.C) a.a().x(r2.C.class)) == null) {
            return;
        }
        tVar.legendaryStyle = c.getComponentValue(256);
        if (kVar == null || (aVarT = kVar.T()) == null) {
            return;
        }
        CaptureResult captureResultB = aVarT.B();
        tVar.legendaryPreviewResult = captureResultB;
        LegendaryPostProcessor.rememberPreview(captureResultB);
    }
}
