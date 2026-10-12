package com.codex.fullm9;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
public final class NativeM9 {
    static native void applyFilter(Bitmap bitmap, byte[] bArr, float[] fArr);

    static native void frameClose(long j);

    static native long frameCreate(Bitmap bitmap);

    static native void frameRecover(long j, Bitmap bitmap);

    static native int[] hdrSample(Bitmap bitmap, int i, int i2);

    static native void outputDenoise(Bitmap bitmap, int i);

    static native void rawClose(long j);

    static native float[] rawNeutral(byte[] bArr);

    static native long rawOpen(byte[] bArr, float[] fArr);

    static native void rawRender(long j, float[] fArr, float[] fArr2, Bitmap bitmap, Bitmap bitmap2);

    static native int[] rawSize(long j);

    static native float[] rawStats(long j);

    static native void restoreDetail(Bitmap bitmap, Bitmap bitmap2, Bitmap bitmap3);

    static native void restoreTexture(Bitmap bitmap, Bitmap bitmap2);

    static native void styleInput(long j, ByteBuffer byteBuffer);

    static native void styleOutput(long j, ByteBuffer byteBuffer);

    static native void tileInput(long j, int i, ByteBuffer byteBuffer);

    static native void tileOutput(long j, int i, ByteBuffer byteBuffer);

    static {
        System.loadLibrary("fullm9");
    }
}
