package com.codex.leicaapp;

import android.graphics.Bitmap;
import android.graphics.Gainmap;
import android.hardware.camera2.CaptureResult;
import android.util.Log;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes13.dex */
public final class LeicaDaylight {
    static void apply(Bitmap bitmap, int i, boolean z, boolean z2) {
        float fMax;
        if (bitmap == null || bitmap.isRecycled() || !bitmap.isMutable()) {
            return;
        }
        float f = 0.0f;
        if (z2) {
            fMax = 0.008f;
        } else {
            fMax = (!z || i < 2000 || i > 12000) ? 0.0f : Math.max(-0.12f, Math.min(0.12f, (200.0f - (1000000.0f / i)) * 7.0E-4f));
        }
        float fExp = (float) Math.exp(fMax);
        float fExp2 = (float) Math.exp(-fMax);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[width];
        float fFade = 0.0f;
        if (z2) {
            fFade = ((i < 2000 || i > 12000) ? 0.55f : 1.0f - fade(3200.0f, 5200.0f, i)) * 8.0f;
        }
        int i2 = 0;
        while (i2 < height) {
            int i3 = i2;
            int[] iArr2 = iArr;
            bitmap.getPixels(iArr, 0, width, 0, i2, width, 1);
            int i4 = 0;
            while (i4 < width) {
                int i5 = iArr2[i4];
                float f2 = (i5 >> 16) & 255;
                float f3 = i5 & 255;
                float f4 = f2 * fExp;
                float f5 = f3 * fExp2;
                float f6 = ((f2 - f4) * 0.2126f) + ((f3 - f5) * 0.0722f);
                float f7 = f4 + f6;
                float f8 = ((i5 >> 8) & 255) + f6;
                float f9 = f5 + f6;
                float f10 = (0.2126f * f7) + (0.7152f * f8) + (0.0722f * f9);
                float fMax2 = Math.max(f, 1.0f - (f10 / 80.0f));
                float fMax3 = Math.max(f, (f10 - 175.0f) / 80.0f);
                float f11 = (f10 - 115.0f) / 115.0f;
                float f12 = (f10 - 78.0f) / 78.0f;
                float f13 = (f10 - 172.0f) / 100.0f;
                float f14 = 1.0f - (f11 * f11);
                int i6 = height;
                float fMax4 = Math.max(0.0f, f14);
                float f15 = fExp2;
                float fMax5 = Math.max(0.0f, 1.0f - (f12 * f12));
                float f16 = fExp;
                float fMax6 = Math.max(0.0f, 1.0f - (f13 * f13));
                float f17 = (((((10.0f * fMax2) * fMax2) + f10) - ((6.0f * fMax5) * fMax5)) + ((3.0f * fMax6) * fMax6)) - ((22.0f * fMax3) * fMax3);
                if (z2) {
                    float f18 = f17 + (fMax5 * 4.0f * fMax5) + (fMax6 * 5.0f * fMax6) + (fMax3 * 20.0f * fMax3);
                    f17 = (f18 * (((0.4f * f18) / 255.0f) + 0.58f)) + 5.0f;
                }
                float fFade2 = fade(18.0f, 88.0f, Math.max(f7, Math.max(f8, f9)) - Math.min(f7, Math.min(f8, f9)));
                int i7 = width;
                int i8 = i4;
                float fFade3 = ((((((fMax4 * 0.06f) * fFade2) * (1.0f - fade(120.0f, 220.0f, f10))) + 1.01f) - (((fade(80.0f, 180.0f, f10) * 0.16f) * fFade2) * (1.0f - (fade(20.0f, 100.0f, f7 - f8) * 0.75f)))) - ((Math.max(0.0f, Math.min(1.0f, ((f9 - f7) - 14.0f) / 80.0f)) * Math.max(0.0f, Math.min(1.0f, ((f9 - f8) - 4.0f) / 75.0f))) * 0.18f)) - ((fade(220.0f, 255.0f, Math.max(f7, Math.max(f8, f9))) * 0.07f) * fFade2);
                if (z2) {
                    fFade3 *= Math.max(0.88f, f17 / Math.max(1.0f, f10));
                }
                if (!z2) {
                    float fFade4 = ((f17 + (fade(48.0f, 98.0f, f10) * 1.6f)) - (fade(74.0f, 198.0f, f10) * 5.4f)) - (fade(178.0f, 246.0f, f10) * 2.4f);
                    float fFade5 = fade(46.0f, 108.0f, f10) * (1.0f - fade(170.0f, 242.0f, f10)) * fade(6.0f, 40.0f, ((Math.min(f7, Math.min(f8, f9)) - f10) * fFade3) + fFade4) * (1.0f - fade(223.0f, 251.0f, ((Math.max(f7, Math.max(f8, f9)) - f10) * fFade3) + fFade4));
                    float fFade6 = fade(5.0f, 62.0f, f7 - f8) * fade(4.0f, 54.0f, f8 - f9) * fade(38.0f, 220.0f, f10);
                    float fFade7 = fFade5 * (1.0f - (fFade6 * 0.55f)) * (1.0f - ((fade(82.0f, 238.0f, f10) * (1.0f - fade(10.0f, 42.0f, Math.max(f7, Math.max(f8, f9)) - Math.min(f7, Math.min(f8, f9))))) * 0.45f));
                    float f19 = fFade3 * (1.0f - (fFade6 * 0.12f)) * (((1.0f - (fFade2 * 0.62f)) * 0.22f * fFade7) + 1.0f);
                    float fFade8 = fade(-8.0f, 44.0f, f8 - f7) * fade(8.0f, 62.0f, f8 - f9) * fFade7;
                    float f20 = f7 - (fFade8 * 3.4f);
                    float f21 = f8 + (fFade8 * 0.9299217f);
                    float f22 = f9 + (fFade8 * 0.8f);
                    float f23 = (fFade4 - ((fFade7 * fFade2) * 7.5f)) - (fFade8 * 1.7f);
                    float fFade9 = fade(8.0f, 76.0f, f22 - f20) * fade(-8.0f, 32.0f, f22 - f21) * fFade7;
                    f7 = f20 + (fFade9 * 1.4f);
                    f8 = f21 - (fFade9 * 0.45f);
                    f9 = f22 + (fFade9 * 0.33518004f);
                    float fFade10 = (1.0f - fade(10.0f, 34.0f, Math.max(Math.max(f7, f8), f9) - Math.min(Math.min(f7, f8), f9))) * fade(198.0f, 242.0f, f10);
                    float f24 = (f23 - (fFade9 * 2.0f)) + (fFade10 * 1.5f);
                    float f25 = f19 * (1.0f - (fFade10 * 0.07f));
                    float fFade11 = fade(12.0f, 52.0f, f9 - f7) * fade(-2.0f, 18.0f, f9 - f8) * fade(42.0f, 68.0f, f10) * (1.0f - fade(96.0f, 128.0f, f10));
                    float f26 = f24 + (fFade11 * 0.85f);
                    float f27 = f25 * (1.0f - (fFade11 * 0.1f));
                    float f28 = (((i8 + 0.5f) / i7) * 2.0f) - 1.0f;
                    float f29 = (((i3 + 0.5f) / i6) * 2.0f) - 1.0f;
                    float fFade12 = 1.0f - ((fade(0.16f, 1.0f, ((f28 * f28) + (f29 * f29)) * 0.5f) * (1.0f - (fade(170.0f, 255.0f, f10) * 0.38f))) * 0.04f);
                    f17 = f26 * fFade12;
                    fFade3 = f27 * fFade12;
                }
                int iChannel = channel(((f7 - f10) * fFade3) + f17);
                int iChannel2 = channel(((f8 - f10) * fFade3) + f17);
                int iChannel3 = channel(((f9 - f10) * fFade3) + f17);
                if (z2) {
                    float fFade13 = (1.0f - fade(20.0f, 55.0f, Math.max(iChannel, Math.max(iChannel2, iChannel3)) - Math.min(iChannel, Math.min(iChannel2, iChannel3)))) * (1.0f - fade(7.0f, 25.0f, Math.abs(iChannel - iChannel2))) * 5.0f * fade(50.0f, 170.0f, f17) * (1.0f - fade(215.0f, 250.0f, f17));
                    iChannel = channel(iChannel + (0.55f * fFade13));
                    iChannel2 = channel(iChannel2 + (0.1f * fFade13));
                    iChannel3 = channel(iChannel3 - (fFade13 * 0.65f));
                }
                if (z2) {
                    float f30 = iChannel;
                    float f31 = iChannel2;
                    float f32 = iChannel3;
                    float f33 = (0.2126f * f30) + (0.7152f * f31) + (0.0722f * f32);
                    float fMax7 = Math.max(Math.min(Math.min(Math.min((1.0f - fade(16.0f, 56.0f, Math.max(Math.max(f30, f31), f32) - Math.min(Math.min(f30, f31), f32))) * fade(38.0f, 95.0f, f33) * (1.0f - fade(185.0f, 243.0f, f33)) * fFade, 254.0f - f30), f32 - 1.0f), (f31 - 1.0f) / 0.19630873f), 0.0f);
                    iChannel = channel(f30 + fMax7);
                    iChannel2 = channel(f31 - (fMax7 * 0.19630873f));
                    iChannel3 = channel(f32 - fMax7);
                }
                float f34 = iChannel;
                float f35 = iChannel2;
                float f36 = iChannel3;
                float f37 = (0.2126f * f34) + (0.7152f * f35) + (0.0722f * f36);
                float fFade14 = fade(24.0f, 104.0f, f37) * (1.0f - (fade(184.0f, 255.0f, f37) * 0.78f));
                float f38 = 0.06f;
                if (z2) {
                    f38 = 0.18f;
                }
                float f39 = 1.0f - (fFade14 * f38);
                iArr2[i8] = (channel(f34 * f39) << 16) | (i5 & (-16777216)) | (channel(f35 * f39) << 8) | channel(f36 * f39);
                i4 = i8 + 1;
                width = i7;
                height = i6;
                fExp = f16;
                fExp2 = f15;
                f = 0.0f;
            }
            int i9 = width;
            bitmap.setPixels(iArr2, 0, i9, 0, i3, i9, 1);
            i2 = i3 + 1;
            iArr = iArr2;
            f = 0.0f;
        }
        Log.i("LeicaDaylight", "M9 JPEG warmth sceneK=" + i + " daylight=" + z + " indoor=" + z2 + " red=" + fExp + " blue=" + fExp2);
    }

    static void finish(Bitmap bitmap, boolean z) {
        if (bitmap == null || bitmap.isRecycled() || !bitmap.hasGainmap()) {
            return;
        }
        Gainmap gainmap = bitmap.getGainmap();
        gainmap.setRatioMin(1.0f, 1.0f, 1.0f);
        float f = z ? 1.06f : 1.0f;
        gainmap.setRatioMax(f, f, f);
        Log.i("LeicaDaylight", "M9 HDR gainmap retained, indoor=" + z + " max=" + f);
    }

    private LeicaDaylight() {
    }

    public static void finish(Bitmap bitmap, CaptureResult captureResult) {
        Integer num = captureResult == null ? null : (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Long l = captureResult != null ? (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) : null;
        finish(bitmap, isIndoor(num == null ? 0 : num.intValue(), l == null ? 0L : l.longValue()));
    }

    public static void apply(Bitmap bitmap, int i, CaptureResult captureResult) {
        Integer num = captureResult == null ? null : (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Long l = captureResult != null ? (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) : null;
        boolean zIsDaylight = isDaylight(num == null ? 0 : num.intValue(), l == null ? 0L : l.longValue());
        boolean zIsIndoor = isIndoor(num != null ? num.intValue() : 0, l != null ? l.longValue() : 0L);
        apply(bitmap, i, zIsDaylight, zIsIndoor);
        Log.i("LeicaDaylight", "M9 ISO=" + num + " exposureNs=" + l + " daylight=" + zIsDaylight + " indoor=" + zIsIndoor);
    }

    public static int styleLux(int i, String str, CaptureResult captureResult) {
        Integer num = captureResult == null ? null : (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Long l = captureResult != null ? (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) : null;
        return styleLux(i, str, num == null ? 0 : num.intValue(), l == null ? 0L : l.longValue());
    }

    public static int styleCct(int i, String str, CaptureResult captureResult) {
        Integer num = captureResult == null ? null : (Integer) captureResult.get(CaptureResult.SENSOR_SENSITIVITY);
        Long l = captureResult != null ? (Long) captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) : null;
        return styleCct(i, str, num == null ? 0 : num.intValue(), l == null ? 0L : l.longValue());
    }

    static int styleCct(int i, String str, int i2, long j) {
        if ("reference".equals(str) && isIndoor(i2, j)) {
            return 4200;
        }
        return i;
    }

    static int styleLux(int i, String str, int i2, long j) {
        if ("reference".equals(str) && isIndoor(i2, j)) {
            return 400;
        }
        return i;
    }

    static boolean isDaylight(int i, long j) {
        return i > 0 && i <= 64 && j > 0 && j <= 12000000;
    }

    static boolean isIndoor(int i, long j) {
        return i >= 160 || j >= 12000000;
    }

    private static int channel(float f) {
        return Math.max(0, Math.min(255, Math.round(f)));
    }

    private static float fade(float f, float f2, float f3) {
        float fMax = Math.max(0.0f, Math.min(1.0f, (f3 - f) / (f2 - f)));
        return fMax * fMax * (3.0f - (fMax * 2.0f));
    }
}
