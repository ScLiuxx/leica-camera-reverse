package ai.onnxruntime.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class Fp16Conversions {
    private static final Logger logger = Logger.getLogger(Fp16Conversions.class.getName());

    private Fp16Conversions() {
    }

    public static ShortBuffer convertFloatBufferToFp16Buffer(FloatBuffer buf) {
        int pos = buf.position();
        int remaining = buf.remaining();
        ShortBuffer output = ByteBuffer.allocateDirect(remaining * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (int i = 0; i < remaining; i++) {
            output.put(i, floatToFp16(buf.get(i + pos)));
        }
        return output;
    }

    public static FloatBuffer convertFp16BufferToFloatBuffer(ShortBuffer buf) {
        int pos = buf.position();
        int remaining = buf.remaining();
        FloatBuffer output = ByteBuffer.allocateDirect(remaining * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < remaining; i++) {
            output.put(i, fp16ToFloat(buf.get(i + pos)));
        }
        return output;
    }

    public static ShortBuffer convertFloatBufferToBf16Buffer(FloatBuffer buf) {
        int pos = buf.position();
        int remaining = buf.remaining();
        ShortBuffer output = ByteBuffer.allocateDirect(remaining * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (int i = 0; i < remaining; i++) {
            output.put(i, floatToBf16(buf.get(i + pos)));
        }
        return output;
    }

    public static FloatBuffer convertBf16BufferToFloatBuffer(ShortBuffer buf) {
        int pos = buf.position();
        int remaining = buf.remaining();
        FloatBuffer output = ByteBuffer.allocateDirect(remaining * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < remaining; i++) {
            output.put(i, bf16ToFloat(buf.get(i + pos)));
        }
        return output;
    }

    public static float fp16ToFloat(short input) {
        return mlasFp16ToFloat(input);
    }

    public static short floatToFp16(float input) {
        return mlasFloatToFp16(input);
    }

    public static float mlasFp16ToFloat(short input) {
        int bits = (input & Short.MAX_VALUE) << 13;
        int exp = bits & 260046848;
        int bits2 = bits + 939524096;
        if (exp == 260046848) {
            bits2 += 939524096;
        } else if (exp == 0) {
            float tmp = Float.intBitsToFloat(bits2 + 8388608) - Float.intBitsToFloat(947912704);
            bits2 = Float.floatToIntBits(tmp);
        }
        return Float.intBitsToFloat(bits2 | ((Short.MIN_VALUE & input) << 16));
    }

    public static short mlasFloatToFp16(float input) {
        int mant_odd;
        int bits = Float.floatToIntBits(input);
        int F32_INFINITY = Float.floatToIntBits(Float.POSITIVE_INFINITY);
        int sign = Integer.MIN_VALUE & bits;
        int bits2 = bits ^ sign;
        if (bits2 >= 1199570944) {
            mant_odd = bits2 > F32_INFINITY ? 32256 : 31744;
        } else if (bits2 < 947912704) {
            float tmp = Float.intBitsToFloat(bits2) + Float.intBitsToFloat(1056964608);
            mant_odd = (short) (Float.floatToIntBits(tmp) - 1056964608);
        } else {
            int mant_odd2 = (bits2 >> 13) & 1;
            mant_odd = (short) (((bits2 - 939520001) + mant_odd2) >> 13);
        }
        short output = (short) (((short) (sign >> 16)) | mant_odd);
        return output;
    }

    public static float bf16ToFloat(short input) {
        int bits = input << 16;
        return Float.intBitsToFloat(bits);
    }

    public static short floatToBf16(float input) {
        int bits = Float.floatToIntBits(input);
        int lsb = (bits >> 16) & 1;
        int roundingBias = lsb + 32767;
        return (short) ((bits + roundingBias) >> 16);
    }
}
