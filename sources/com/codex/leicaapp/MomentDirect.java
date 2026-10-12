package com.codex.leicaapp;

import Sh.f;
import Sh.t;
import i3.b;
import java.util.ArrayList;

/* JADX INFO: compiled from: MomentDirectCompatP11 */
/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes18.dex */
public final class MomentDirect {
    public static void clearNativePostFilters(t tVar) {
        if (tVar.b.g == 256) {
            tVar.A(b.P);
            tVar.v(b.R);
            tVar.N(b.S);
            tVar.P(b.T);
            tVar.H(b.U);
            f fVar = tVar.d;
            if (fVar != null) {
                fVar.h = new ArrayList();
                fVar.j = new ArrayList();
                n3.b.a aVar = fVar.k;
                if (aVar != null) {
                    aVar.q = b.W;
                    aVar.j = b.X;
                    aVar.l = b.V;
                    aVar.r = 0;
                    aVar.m = 0;
                    aVar.n = 0;
                }
            }
        }
    }
}
