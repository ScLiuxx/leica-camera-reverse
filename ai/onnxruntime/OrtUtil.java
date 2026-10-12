package ai.onnxruntime;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OrtUtil {
    private static final Logger logger = Logger.getLogger(OrtUtil.class.getName());

    private OrtUtil() {
    }

    public static int[] transformShape(long[] shape) {
        if (shape.length == 0 || shape.length > 8) {
            throw new IllegalArgumentException("Arrays with less than 1 and greater than 8 dimensions are not supported.");
        }
        int[] newShape = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            long curDim = shape[i];
            if (curDim < 0 || curDim > 2147483647L) {
                throw new IllegalArgumentException("Invalid shape for a Java array, expected non-negative entries smaller than Integer.MAX_VALUE. Found " + Arrays.toString(shape));
            }
            newShape[i] = (int) curDim;
        }
        return newShape;
    }

    public static long[] transformShape(int[] shape) {
        if (shape.length == 0 || shape.length > 8) {
            throw new IllegalArgumentException("Arrays with less than 1 and greater than 8 dimensions are not supported.");
        }
        long[] newShape = new long[shape.length];
        for (int i = 0; i < shape.length; i++) {
            long curDim = shape[i];
            if (curDim < 1) {
                throw new IllegalArgumentException("Invalid shape for a Java array, expected positive entries smaller than Integer.MAX_VALUE. Found " + Arrays.toString(shape));
            }
            newShape[i] = curDim;
        }
        return newShape;
    }

    public static Object newBooleanArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Boolean.TYPE, intShape);
    }

    public static Object newByteArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Byte.TYPE, intShape);
    }

    public static Object newShortArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Short.TYPE, intShape);
    }

    public static Object newIntArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Integer.TYPE, intShape);
    }

    public static Object newLongArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Long.TYPE, intShape);
    }

    public static Object newFloatArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Float.TYPE, intShape);
    }

    public static Object newDoubleArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) Double.TYPE, intShape);
    }

    public static Object newStringArray(long[] shape) {
        int[] intShape = transformShape(shape);
        return Array.newInstance((Class<?>) String.class, intShape);
    }

    public static Object reshape(boolean[] input, long[] shape) {
        Object output = newBooleanArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(byte[] input, long[] shape) {
        Object output = newByteArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(short[] input, long[] shape) {
        Object output = newShortArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(int[] input, long[] shape) {
        Object output = newIntArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(long[] input, long[] shape) {
        Object output = newLongArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(float[] input, long[] shape) {
        Object output = newFloatArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(double[] input, long[] shape) {
        Object output = newDoubleArray(shape);
        reshape(input, output, 0);
        return output;
    }

    public static Object reshape(String[] input, long[] shape) {
        Object output = newStringArray(shape);
        reshape(input, output, 0);
        return output;
    }

    private static int reshape(Object input, Object output, int position) {
        if (output.getClass().isArray()) {
            Object[] outputArray = (Object[]) output;
            for (Object outputElement : outputArray) {
                Class<?> outputElementClass = outputElement.getClass();
                if (outputElementClass.isArray()) {
                    Class<?> componentType = outputElementClass.getComponentType();
                    if (componentType.isPrimitive() || componentType == String.class) {
                        int length = Array.getLength(outputElement);
                        System.arraycopy(input, position, outputElement, 0, length);
                        position += length;
                    } else {
                        position = reshape(input, outputElement, position);
                    }
                } else {
                    throw new IllegalStateException("Found element type when expecting an array. Class " + outputElementClass);
                }
            }
            return position;
        }
        throw new IllegalStateException("Found element type when expecting an array. Class " + output.getClass());
    }

    public static long elementCount(long[] shape) {
        long count = 1;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] >= 0) {
                count *= shape[i];
            } else {
                throw new IllegalArgumentException("Received negative value in shape " + Arrays.toString(shape) + " .");
            }
        }
        return count;
    }

    public static boolean validateShape(long[] shape) {
        boolean valid = true;
        int i = 0;
        while (true) {
            boolean z = false;
            if (i >= shape.length) {
                break;
            }
            boolean valid2 = valid & (shape[i] > 0);
            if (((int) shape[i]) == shape[i]) {
                z = true;
            }
            valid = valid2 & z;
            i++;
        }
        return valid && shape.length <= 8;
    }

    public static String[] flattenString(Object o) {
        if (o instanceof String[]) {
            return (String[]) o;
        }
        ArrayList<String> output = new ArrayList<>();
        flattenString((Object[]) o, output);
        return (String[]) output.toArray(new String[0]);
    }

    private static void flattenString(Object[] input, ArrayList<String> output) {
        for (Object i : input) {
            Class<?> iClazz = i.getClass();
            if (iClazz.isArray()) {
                if (iClazz.getComponentType().isArray()) {
                    flattenString((Object[]) i, output);
                } else if (iClazz.getComponentType().equals(String.class)) {
                    output.addAll(Arrays.asList((String[]) i));
                } else {
                    throw new IllegalStateException("Found a non-String, non-array element type, " + iClazz);
                }
            } else {
                throw new IllegalStateException("Found an element type where there should have been an array. Class = " + iClazz);
            }
        }
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OrtUtil$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxJavaType;

        static {
            int[] iArr = new int[OnnxJavaType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxJavaType = iArr;
            try {
                iArr[OnnxJavaType.FLOAT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT16.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT32.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT64.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BOOL.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.STRING.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UNKNOWN.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.FLOAT16.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BFLOAT16.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    static Object convertBoxedPrimitiveToArray(OnnxJavaType javaType, Object data) {
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[javaType.ordinal()]) {
            case 1:
                double[] doubleArr = {((Float) data).floatValue()};
                return doubleArr;
            case 2:
                double[] doubleArr2 = {((Double) data).doubleValue()};
                return doubleArr2;
            case 3:
            case 4:
                byte[] byteArr = {((Byte) data).byteValue()};
                return byteArr;
            case 5:
                short[] shortArr = {((Short) data).shortValue()};
                return shortArr;
            case 6:
                long[] longArr = {((Integer) data).intValue()};
                return longArr;
            case 7:
                long[] longArr2 = {((Long) data).longValue()};
                return longArr2;
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                boolean[] booleanArr = {((Boolean) data).booleanValue()};
                return booleanArr;
            default:
                return null;
        }
    }

    static Buffer convertBoxedPrimitiveToBuffer(OnnxJavaType onnxJavaType, Object obj) {
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[onnxJavaType.ordinal()]) {
            case 1:
                FloatBuffer floatBufferAsFloatBuffer = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder()).asFloatBuffer();
                floatBufferAsFloatBuffer.put(0, ((Float) obj).floatValue());
                return floatBufferAsFloatBuffer;
            case 2:
                DoubleBuffer doubleBufferAsDoubleBuffer = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder()).asDoubleBuffer();
                doubleBufferAsDoubleBuffer.put(0, ((Double) obj).doubleValue());
                return doubleBufferAsDoubleBuffer;
            case 3:
            case 4:
                ByteBuffer byteBufferOrder = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder());
                byteBufferOrder.put(0, ((Byte) obj).byteValue());
                return byteBufferOrder;
            case 5:
            case 11:
            case 12:
                ShortBuffer shortBufferAsShortBuffer = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder()).asShortBuffer();
                shortBufferAsShortBuffer.put(0, ((Short) obj).shortValue());
                return shortBufferAsShortBuffer;
            case 6:
                IntBuffer intBufferAsIntBuffer = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder()).asIntBuffer();
                intBufferAsIntBuffer.put(0, ((Integer) obj).intValue());
                return intBufferAsIntBuffer;
            case 7:
                LongBuffer longBufferAsLongBuffer = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder()).asLongBuffer();
                longBufferAsLongBuffer.put(0, ((Long) obj).longValue());
                return longBufferAsLongBuffer;
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                ByteBuffer byteBufferOrder2 = ByteBuffer.allocateDirect(onnxJavaType.size).order(ByteOrder.nativeOrder());
                byteBufferOrder2.put(0, ((Boolean) obj).booleanValue() ? (byte) 1 : (byte) 0);
                return byteBufferOrder2;
            case 9:
            case 10:
            default:
                return null;
        }
    }

    static Buffer convertArrayToBuffer(TensorInfo info, Object array) {
        Buffer buffer;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(((int) info.numElements) * info.type.size).order(ByteOrder.nativeOrder());
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[info.type.ordinal()]) {
            case 1:
                buffer = byteBuffer.asFloatBuffer();
                break;
            case 2:
                buffer = byteBuffer.asDoubleBuffer();
                break;
            case 3:
            case 4:
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                buffer = byteBuffer;
                break;
            case 5:
            case 11:
            case 12:
                buffer = byteBuffer.asShortBuffer();
                break;
            case 6:
                buffer = byteBuffer.asIntBuffer();
                break;
            case 7:
                buffer = byteBuffer.asLongBuffer();
                break;
            case 9:
            case 10:
            default:
                throw new IllegalArgumentException("Unexpected type, expected Java primitive found " + info.type);
        }
        fillBufferFromArray(info, array, 0, buffer);
        if (buffer.remaining() != 0) {
            throw new IllegalArgumentException("Failed to copy all elements into the buffer, expected to copy " + info.numElements + " into a buffer of capacity " + buffer.capacity() + " but had " + buffer.remaining() + " values left over.");
        }
        buffer.rewind();
        return buffer;
    }

    private static void fillBufferFromArray(TensorInfo tensorInfo, Object obj, int i, Buffer buffer) {
        if (i == tensorInfo.shape.length - 1) {
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[tensorInfo.type.ordinal()]) {
                case 1:
                    ((FloatBuffer) buffer).put((float[]) obj);
                    return;
                case 2:
                    ((DoubleBuffer) buffer).put((double[]) obj);
                    return;
                case 3:
                case 4:
                    ((ByteBuffer) buffer).put((byte[]) obj);
                    return;
                case 5:
                case 11:
                case 12:
                    ((ShortBuffer) buffer).put((short[]) obj);
                    return;
                case 6:
                    ((IntBuffer) buffer).put((int[]) obj);
                    return;
                case 7:
                    ((LongBuffer) buffer).put((long[]) obj);
                    return;
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    ByteBuffer byteBuffer = (ByteBuffer) buffer;
                    for (boolean z : (boolean[]) obj) {
                        byteBuffer.put(z ? (byte) 1 : (byte) 0);
                    }
                    return;
                case 9:
                case 10:
                    throw new IllegalArgumentException("Unexpected type, expected Java primitive found " + tensorInfo.type);
                default:
                    return;
            }
        }
        long j = tensorInfo.shape[i];
        long length = Array.getLength(obj);
        if (j != length) {
            throw new IllegalArgumentException("Mismatch in array sizes, expected " + j + " at dim " + i + " from shape " + Arrays.toString(tensorInfo.shape) + ", found " + length);
        }
        for (int i2 = 0; i2 < length; i2++) {
            fillBufferFromArray(tensorInfo, Array.get(obj, i2), i + 1, buffer);
        }
    }

    static void fillArrayFromBuffer(TensorInfo info, Buffer buffer, int curDim, Object array) {
        if (curDim == info.shape.length - 1) {
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[info.type.ordinal()]) {
                case 1:
                case 11:
                case 12:
                    float[] fArr = (float[]) array;
                    FloatBuffer fBuf = (FloatBuffer) buffer;
                    fBuf.get(fArr);
                    return;
                case 2:
                    double[] dArr = (double[]) array;
                    DoubleBuffer dBuf = (DoubleBuffer) buffer;
                    dBuf.get(dArr);
                    return;
                case 3:
                case 4:
                    byte[] bArr = (byte[]) array;
                    ByteBuffer bBuf = (ByteBuffer) buffer;
                    bBuf.get(bArr);
                    return;
                case 5:
                    short[] sArr = (short[]) array;
                    ShortBuffer sBuf = (ShortBuffer) buffer;
                    sBuf.get(sArr);
                    return;
                case 6:
                    int[] iArr = (int[]) array;
                    IntBuffer iBuf = (IntBuffer) buffer;
                    iBuf.get(iArr);
                    return;
                case 7:
                    long[] lArr = (long[]) array;
                    LongBuffer lBuf = (LongBuffer) buffer;
                    lBuf.get(lArr);
                    return;
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    boolean[] boolArr = (boolean[]) array;
                    ByteBuffer boolBuf = (ByteBuffer) buffer;
                    for (int i = 0; i < boolArr.length; i++) {
                        boolArr[i] = boolBuf.get() != 0;
                    }
                    return;
                case 9:
                case 10:
                    throw new IllegalArgumentException("Unexpected type, expected Java primitive found " + info.type);
                default:
                    return;
            }
        }
        long expectedSize = info.shape[curDim];
        long actualSize = Array.getLength(array);
        if (expectedSize != actualSize) {
            throw new IllegalArgumentException("Mismatch in array sizes, expected " + expectedSize + " at dim " + curDim + " from shape " + Arrays.toString(info.shape) + ", found " + actualSize);
        }
        for (int i2 = 0; i2 < actualSize; i2++) {
            fillArrayFromBuffer(info, buffer, curDim + 1, Array.get(array, i2));
        }
    }

    static int capacityFromSize(int size) {
        return (int) ((((double) size) / 0.75d) + 1.0d);
    }

    static BufferTuple prepareBuffer(Buffer data, OnnxJavaType type) {
        Buffer tmp;
        Buffer tmp2;
        int bufferPos;
        if (type == OnnxJavaType.STRING || type == OnnxJavaType.UNKNOWN) {
            throw new IllegalStateException("Cannot create a " + type + " tensor from a buffer");
        }
        int elementSize = data instanceof ByteBuffer ? 1 : type.size;
        long bufferSizeLong = ((long) data.remaining()) * ((long) elementSize);
        if (bufferSizeLong > 2147483647L - (((long) elementSize) * 8)) {
            throw new IllegalStateException("Cannot allocate a direct buffer of the requested size and type, size " + data.remaining() + ", type = " + type);
        }
        int bufferSize = data.remaining() * elementSize;
        if (data.isDirect()) {
            tmp2 = data;
            bufferPos = data.position() * elementSize;
        } else {
            int origPosition = data.position();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[type.ordinal()]) {
                case 1:
                    tmp = buffer.asFloatBuffer().put((FloatBuffer) data);
                    break;
                case 2:
                    tmp = buffer.asDoubleBuffer().put((DoubleBuffer) data);
                    break;
                case 3:
                case 4:
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    tmp = buffer.put((ByteBuffer) data);
                    break;
                case 5:
                case 11:
                case 12:
                    tmp = buffer.asShortBuffer().put((ShortBuffer) data);
                    break;
                case 6:
                    tmp = buffer.asIntBuffer().put((IntBuffer) data);
                    break;
                case 7:
                    tmp = buffer.asLongBuffer().put((LongBuffer) data);
                    break;
                case 9:
                case 10:
                default:
                    throw new IllegalStateException("Impossible to reach here, managed to cast a buffer as an incorrect type, found " + type);
            }
            data.position(origPosition);
            tmp.rewind();
            tmp2 = tmp;
            bufferPos = 0;
        }
        return new BufferTuple(tmp2, bufferPos, bufferSize, data.remaining(), tmp2 != data);
    }

    static final class BufferTuple {
        final long byteSize;
        final Buffer data;
        final boolean isCopy;
        final int pos;
        final long size;

        BufferTuple(Buffer data, int pos, long byteSize, long size, boolean isCopy) {
            this.data = data;
            this.pos = pos;
            this.byteSize = byteSize;
            this.size = size;
            this.isCopy = isCopy;
        }
    }
}
