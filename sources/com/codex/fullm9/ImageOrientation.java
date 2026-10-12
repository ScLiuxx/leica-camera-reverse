package com.codex.fullm9;

import ai.onnxruntime.TensorInfo;
import android.graphics.Matrix;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes17.dex */
final class ImageOrientation {
    ImageOrientation() {
    }

    static float[] coefficients(int i) {
        switch (i) {
            case 2:
                return new float[]{-1.0f, 0.0f, 0.0f, 1.0f};
            case 3:
                return new float[]{-1.0f, 0.0f, 0.0f, -1.0f};
            case 4:
                return new float[]{1.0f, 0.0f, 0.0f, -1.0f};
            case 5:
                return new float[]{0.0f, 1.0f, 1.0f, 0.0f};
            case 6:
                return new float[]{0.0f, -1.0f, 1.0f, 0.0f};
            case 7:
                return new float[]{0.0f, -1.0f, -1.0f, 0.0f};
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                return new float[]{0.0f, 1.0f, -1.0f, 0.0f};
            default:
                return new float[]{1.0f, 0.0f, 0.0f, 1.0f};
        }
    }

    static Matrix matrix(int i) {
        float[] fArrCoefficients = coefficients(i);
        Matrix matrix = new Matrix();
        matrix.setValues(new float[]{fArrCoefficients[0], fArrCoefficients[1], 0.0f, fArrCoefficients[2], fArrCoefficients[3], 0.0f, 0.0f, 0.0f, 1.0f});
        return matrix;
    }
}
