package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.graphics.Gainmap;
import android.os.Build;
import android.util.Log;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class HdrGainmaps {
    private HdrGainmaps() {
    }

    public static Gainmap capture(Bitmap bitmap, boolean z) {
        if (Build.VERSION.SDK_INT < 34 || !bitmap.hasGainmap()) {
            return null;
        }
        Gainmap gainmap = bitmap.getGainmap();
        Bitmap gainmapContents = gainmap.getGainmapContents();
        Bitmap bitmapCopy = gainmapContents.copy(Bitmap.Config.ARGB_8888, true);
        if (bitmapCopy == null) {
            throw new IllegalStateException("HDR gainmap copy failed");
        }
        float[] ratioMin = gainmap.getRatioMin();
        float[] ratioMax = gainmap.getRatioMax();
        float[] gamma = gainmap.getGamma();
        float[] epsilonSdr = gainmap.getEpsilonSdr();
        float[] epsilonHdr = gainmap.getEpsilonHdr();
        float fMax = 0.0f;
        float fMin = 0.0f;
        for (int i = 0; i < 3; i++) {
            fMin = Math.min(fMin, (float) Math.log(ratioMin[i]));
            fMax = Math.max(fMax, (float) Math.log(ratioMax[i]));
        }
        if (!Float.isFinite(fMin) || !Float.isFinite(fMax) || fMax <= fMin) {
            bitmapCopy.recycle();
            throw new IllegalStateException("Invalid HDR gain range");
        }
        int width = gainmapContents.getWidth() * gainmapContents.getHeight();
        int[] iArr = new int[width];
        int i2 = 3;
        float f = fMax;
        float f2 = fMin;
        gainmapContents.getPixels(iArr, 0, gainmapContents.getWidth(), 0, 0, gainmapContents.getWidth(), gainmapContents.getHeight());
        float[] fArr = new float[3];
        float[] fArr2 = new float[3];
        for (int i3 = 0; i3 < 3; i3++) {
            fArr[i3] = (float) Math.log(ratioMin[i3]);
            fArr2[i3] = ((float) Math.log(ratioMax[i3])) - fArr[i3];
        }
        boolean z2 = gainmapContents.getConfig() == Bitmap.Config.ALPHA_8;
        float[] fArr3 = new float[3];
        int i4 = 0;
        while (i4 < width) {
            int i5 = iArr[i4];
            int i6 = 0;
            while (i6 < i2) {
                fArr3[i6] = fArr[i6] + (fArr2[i6] * ((float) Math.pow((255 & (i5 >> (z2 ? 24 : 16 - (i6 * 8)))) / 255.0f, gamma[i6])));
                i6++;
                bitmapCopy = bitmapCopy;
                i5 = i5;
                i2 = 3;
            }
            Bitmap bitmap2 = bitmapCopy;
            if (z) {
                float f3 = (fArr3[0] * 0.2126f) + (fArr3[1] * 0.7152f) + (fArr3[2] * 0.0722f);
                fArr3[2] = f3;
                fArr3[1] = f3;
                fArr3[0] = f3;
            }
            int iMax = -16777216;
            for (int i7 = 0; i7 < 3; i7++) {
                iMax |= Math.max(0, Math.min(255, Math.round(((fArr3[i7] - f2) * 255.0f) / (f - f2)))) << (16 - (i7 * 8));
            }
            iArr[i4] = iMax;
            i4++;
            bitmapCopy = bitmap2;
            i2 = 3;
        }
        Bitmap bitmap3 = bitmapCopy;
        bitmap3.setPixels(iArr, 0, bitmap3.getWidth(), 0, 0, bitmap3.getWidth(), bitmap3.getHeight());
        Gainmap gainmap2 = new Gainmap(bitmap3);
        float fExp = (float) Math.exp(f2);
        float fExp2 = (float) Math.exp(f);
        gainmap2.setRatioMin(fExp, fExp, fExp);
        gainmap2.setRatioMax(fExp2, fExp2, fExp2);
        gainmap2.setGamma(1.0f, 1.0f, 1.0f);
        gainmap2.setEpsilonSdr(epsilonSdr[0], epsilonSdr[1], epsilonSdr[2]);
        gainmap2.setEpsilonHdr(epsilonHdr[0], epsilonHdr[1], epsilonHdr[2]);
        gainmap2.setMinDisplayRatioForHdrTransition(gainmap.getMinDisplayRatioForHdrTransition());
        gainmap2.setDisplayRatioForFullHdr(gainmap.getDisplayRatioForFullHdr());
        Log.i("LegendaryPost", "HDR source retained map=" + bitmap3.getWidth() + "x" + bitmap3.getHeight() + " maxRatio=" + fExp2);
        return gainmap2;
    }

    public static void attach(Bitmap bitmap, Gainmap gainmap) {
        if (Build.VERSION.SDK_INT >= 34) {
            bitmap.setGainmap(gainmap);
        }
    }
}
