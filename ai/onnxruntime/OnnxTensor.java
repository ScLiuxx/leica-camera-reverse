package ai.onnxruntime;

import ai.onnxruntime.platform.Fp16Conversions;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Optional;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class OnnxTensor extends OnnxTensorLike {
    private static final Logger logger = Logger.getLogger(OnnxTensor.class.getName());
    private final Buffer buffer;
    private final boolean ownsBuffer;

    private native void close(long j, long j2);

    private static native long createString(long j, long j2, String str) throws OrtException;

    private static native long createStringTensor(long j, long j2, Object[] objArr, long[] jArr) throws OrtException;

    private static native long createTensorFromBuffer(long j, long j2, Buffer buffer, int i, long j3, long[] jArr, int i2) throws OrtException;

    private native boolean getBool(long j, long j2) throws OrtException;

    private native ByteBuffer getBuffer(long j, long j2);

    private native byte getByte(long j, long j2, int i) throws OrtException;

    private native double getDouble(long j, long j2) throws OrtException;

    private native float getFloat(long j, long j2, int i) throws OrtException;

    private native int getInt(long j, long j2, int i) throws OrtException;

    private native long getLong(long j, long j2, int i) throws OrtException;

    private native short getShort(long j, long j2, int i) throws OrtException;

    private native String getString(long j, long j2) throws OrtException;

    private native void getStringArray(long j, long j2, String[] strArr) throws OrtException;

    OnnxTensor(long nativeHandle, long allocatorHandle, TensorInfo info) {
        this(nativeHandle, allocatorHandle, info, null, false);
    }

    OnnxTensor(long nativeHandle, long allocatorHandle, TensorInfo info, Buffer buffer, boolean ownsBuffer) {
        super(nativeHandle, allocatorHandle, info);
        this.buffer = buffer;
        this.ownsBuffer = ownsBuffer;
    }

    public boolean ownsBuffer() {
        return this.ownsBuffer;
    }

    public Optional<Buffer> getBufferRef() {
        return Optional.ofNullable(duplicate(this.buffer));
    }

    private static Buffer duplicate(Buffer buf) {
        if (buf instanceof ByteBuffer) {
            return ((ByteBuffer) buf).duplicate().order(ByteOrder.nativeOrder());
        }
        if (buf instanceof ShortBuffer) {
            return ((ShortBuffer) buf).duplicate();
        }
        if (buf instanceof IntBuffer) {
            return ((IntBuffer) buf).duplicate();
        }
        if (buf instanceof LongBuffer) {
            return ((LongBuffer) buf).duplicate();
        }
        if (buf instanceof FloatBuffer) {
            return ((FloatBuffer) buf).duplicate();
        }
        if (buf instanceof DoubleBuffer) {
            return ((DoubleBuffer) buf).duplicate();
        }
        throw new IllegalStateException("Unknown buffer type " + buf.getClass());
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OnnxTensor$1, reason: invalid class name */
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
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BOOL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BFLOAT16.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.FLOAT16.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT16.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT32.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT64.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.STRING.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UNKNOWN.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private Buffer castBuffer(Buffer buf) {
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[this.info.type.ordinal()]) {
            case 1:
                if (buf instanceof FloatBuffer) {
                    return buf;
                }
                if (buf instanceof ByteBuffer) {
                    return ((ByteBuffer) buf).asFloatBuffer();
                }
                break;
            case 2:
                if (buf instanceof DoubleBuffer) {
                    return buf;
                }
                if (buf instanceof ByteBuffer) {
                    return ((ByteBuffer) buf).asDoubleBuffer();
                }
                break;
            case 3:
            case 4:
            case 5:
                if (buf instanceof ByteBuffer) {
                    return buf;
                }
                break;
            case 6:
                if (buf instanceof ShortBuffer) {
                    ShortBuffer bf16Buf = (ShortBuffer) buf;
                    return Fp16Conversions.convertBf16BufferToFloatBuffer(bf16Buf);
                }
                if (buf instanceof ByteBuffer) {
                    ShortBuffer bf16Buf2 = ((ByteBuffer) buf).asShortBuffer();
                    return Fp16Conversions.convertBf16BufferToFloatBuffer(bf16Buf2);
                }
                break;
            case 7:
                if (buf instanceof ShortBuffer) {
                    ShortBuffer fp16Buf = (ShortBuffer) buf;
                    return Fp16Conversions.convertFp16BufferToFloatBuffer(fp16Buf);
                }
                if (buf instanceof ByteBuffer) {
                    ShortBuffer fp16Buf2 = ((ByteBuffer) buf).asShortBuffer();
                    return Fp16Conversions.convertFp16BufferToFloatBuffer(fp16Buf2);
                }
                break;
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                if (buf instanceof ShortBuffer) {
                    return buf;
                }
                if (buf instanceof ByteBuffer) {
                    return ((ByteBuffer) buf).asShortBuffer();
                }
                break;
            case 9:
                if (buf instanceof IntBuffer) {
                    return buf;
                }
                if (buf instanceof ByteBuffer) {
                    return ((ByteBuffer) buf).asIntBuffer();
                }
                break;
            case 10:
                if (buf instanceof LongBuffer) {
                    return buf;
                }
                if (buf instanceof ByteBuffer) {
                    return ((ByteBuffer) buf).asLongBuffer();
                }
                break;
        }
        throw new IllegalStateException("Invalid buffer type for cast operation, found " + buf.getClass() + " expected something convertible to " + this.info.type);
    }

    @Override // ai.onnxruntime.OnnxValue
    public OnnxValue.OnnxValueType getType() {
        return OnnxValue.OnnxValueType.ONNX_TYPE_TENSOR;
    }

    @Override // ai.onnxruntime.OnnxValue
    public Object getValue() throws OrtException {
        Buffer buf;
        checkClosed();
        if (this.info.isScalar()) {
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[this.info.type.ordinal()]) {
                case 1:
                    return Float.valueOf(getFloat(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value));
                case 2:
                    return Double.valueOf(getDouble(OnnxRuntime.ortApiHandle, this.nativeHandle));
                case 3:
                    return Boolean.valueOf(getBool(OnnxRuntime.ortApiHandle, this.nativeHandle));
                case 4:
                case 5:
                    return Byte.valueOf(getByte(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value));
                case 6:
                    return Float.valueOf(Fp16Conversions.bf16ToFloat(getShort(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value)));
                case 7:
                    return Float.valueOf(Fp16Conversions.fp16ToFloat(getShort(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value)));
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    return Short.valueOf(getShort(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value));
                case 9:
                    return Integer.valueOf(getInt(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value));
                case 10:
                    return Long.valueOf(getLong(OnnxRuntime.ortApiHandle, this.nativeHandle, this.info.onnxType.value));
                case 11:
                    return getString(OnnxRuntime.ortApiHandle, this.nativeHandle);
                default:
                    throw new OrtException("Extracting the value of an invalid Tensor.");
            }
        }
        Object carrier = this.info.makeCarrier();
        if (this.info.getNumElements() > 0) {
            if (this.info.type == OnnxJavaType.STRING) {
                getStringArray(OnnxRuntime.ortApiHandle, this.nativeHandle, (String[]) carrier);
                if (this.info.shape.length != 1) {
                    return OrtUtil.reshape((String[]) carrier, this.info.shape);
                }
                return carrier;
            }
            Buffer buf2 = this.buffer;
            if (buf2 == null) {
                buf = castBuffer(getBuffer());
            } else {
                buf = castBuffer(duplicate(buf2));
            }
            OrtUtil.fillArrayFromBuffer(this.info, buf, 0, carrier);
            return carrier;
        }
        return carrier;
    }

    public String toString() {
        return "OnnxTensor(info=" + this.info.toString() + ",closed=" + this.closed + ")";
    }

    @Override // ai.onnxruntime.OnnxValue, java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.closed) {
            close(OnnxRuntime.ortApiHandle, this.nativeHandle);
            this.closed = true;
        } else {
            logger.warning("Closing an already closed tensor.");
        }
    }

    public ByteBuffer getByteBuffer() {
        checkClosed();
        if (this.info.type != OnnxJavaType.STRING) {
            ByteBuffer buffer = getBuffer();
            ByteBuffer output = ByteBuffer.allocate(buffer.capacity()).order(ByteOrder.nativeOrder());
            output.put(buffer);
            output.rewind();
            return output;
        }
        return null;
    }

    public FloatBuffer getFloatBuffer() {
        checkClosed();
        if (this.info.type == OnnxJavaType.FLOAT) {
            FloatBuffer buffer = getBuffer().asFloatBuffer();
            FloatBuffer output = FloatBuffer.allocate(buffer.capacity());
            output.put(buffer);
            output.rewind();
            return output;
        }
        if (this.info.type == OnnxJavaType.FLOAT16) {
            ByteBuffer buf = getBuffer();
            ShortBuffer buffer2 = buf.asShortBuffer();
            return Fp16Conversions.convertFp16BufferToFloatBuffer(buffer2);
        }
        if (this.info.type == OnnxJavaType.BFLOAT16) {
            ByteBuffer buf2 = getBuffer();
            ShortBuffer buffer3 = buf2.asShortBuffer();
            return Fp16Conversions.convertBf16BufferToFloatBuffer(buffer3);
        }
        return null;
    }

    public DoubleBuffer getDoubleBuffer() {
        checkClosed();
        if (this.info.type == OnnxJavaType.DOUBLE) {
            DoubleBuffer buffer = getBuffer().asDoubleBuffer();
            DoubleBuffer output = DoubleBuffer.allocate(buffer.capacity());
            output.put(buffer);
            output.rewind();
            return output;
        }
        return null;
    }

    public ShortBuffer getShortBuffer() {
        checkClosed();
        if (this.info.type == OnnxJavaType.INT16 || this.info.type == OnnxJavaType.FLOAT16 || this.info.type == OnnxJavaType.BFLOAT16) {
            ShortBuffer buffer = getBuffer().asShortBuffer();
            ShortBuffer output = ShortBuffer.allocate(buffer.capacity());
            output.put(buffer);
            output.rewind();
            return output;
        }
        return null;
    }

    public IntBuffer getIntBuffer() {
        checkClosed();
        if (this.info.type == OnnxJavaType.INT32) {
            IntBuffer buffer = getBuffer().asIntBuffer();
            IntBuffer output = IntBuffer.allocate(buffer.capacity());
            output.put(buffer);
            output.rewind();
            return output;
        }
        return null;
    }

    public LongBuffer getLongBuffer() {
        checkClosed();
        if (this.info.type == OnnxJavaType.INT64) {
            LongBuffer buffer = getBuffer().asLongBuffer();
            LongBuffer output = LongBuffer.allocate(buffer.capacity());
            output.put(buffer);
            output.rewind();
            return output;
        }
        return null;
    }

    private ByteBuffer getBuffer() {
        return getBuffer(OnnxRuntime.ortApiHandle, this.nativeHandle).order(ByteOrder.nativeOrder());
    }

    public static OnnxTensor createTensor(OrtEnvironment env, Object data) throws OrtException {
        return createTensor(env, env.defaultAllocator, data);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, Object data) throws OrtException {
        Buffer buf;
        if (!allocator.isClosed()) {
            TensorInfo info = TensorInfo.constructFromJavaArray(data);
            if (info.type == OnnxJavaType.STRING) {
                if (info.shape.length == 0) {
                    return new OnnxTensor(createString(OnnxRuntime.ortApiHandle, allocator.handle, (String) data), allocator.handle, info);
                }
                return new OnnxTensor(createStringTensor(OnnxRuntime.ortApiHandle, allocator.handle, OrtUtil.flattenString(data), info.shape), allocator.handle, info);
            }
            if (info.shape.length == 0) {
                Buffer buf2 = OrtUtil.convertBoxedPrimitiveToBuffer(info.type, data);
                if (buf2 == null) {
                    throw new OrtException("Failed to convert a boxed primitive to an array, this is an error with the ORT Java API, please report this message & stack trace. JavaType = " + info.type + ", object = " + data);
                }
                buf = buf2;
            } else {
                Buffer buf3 = OrtUtil.convertArrayToBuffer(info, data);
                buf = buf3;
            }
            return new OnnxTensor(createTensorFromBuffer(OnnxRuntime.ortApiHandle, allocator.handle, buf, 0, ((long) info.type.size) * info.numElements, info.shape, info.onnxType.value), allocator.handle, info, buf, true);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor with a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, String[] data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, String[] data, long[] shape) throws OrtException {
        if (allocator.isClosed()) {
            throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
        }
        TensorInfo info = new TensorInfo(shape, OnnxJavaType.STRING, TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_STRING);
        return new OnnxTensor(createStringTensor(OnnxRuntime.ortApiHandle, allocator.handle, data, shape), allocator.handle, info);
    }

    public static OnnxTensor createTensor(OrtEnvironment env, FloatBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, FloatBuffer data, long[] shape) throws OrtException {
        if (!allocator.isClosed()) {
            OnnxJavaType type = OnnxJavaType.FLOAT;
            return createTensor(type, allocator, data, shape);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, DoubleBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, DoubleBuffer data, long[] shape) throws OrtException {
        if (!allocator.isClosed()) {
            OnnxJavaType type = OnnxJavaType.DOUBLE;
            return createTensor(type, allocator, data, shape);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, ByteBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, ByteBuffer data, long[] shape) throws OrtException {
        return createTensor(env, allocator, data, shape, OnnxJavaType.INT8);
    }

    public static OnnxTensor createTensor(OrtEnvironment env, ByteBuffer data, long[] shape, OnnxJavaType type) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape, type);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, ByteBuffer data, long[] shape, OnnxJavaType type) throws OrtException {
        if (!allocator.isClosed()) {
            return createTensor(type, allocator, data, shape);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, ShortBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape, OnnxJavaType.INT16);
    }

    public static OnnxTensor createTensor(OrtEnvironment env, ShortBuffer data, long[] shape, OnnxJavaType type) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape, type);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, ShortBuffer data, long[] shape, OnnxJavaType type) throws OrtException {
        if (!allocator.isClosed()) {
            if (type == OnnxJavaType.BFLOAT16 || type == OnnxJavaType.FLOAT16 || type == OnnxJavaType.INT16) {
                return createTensor(type, allocator, data, shape);
            }
            throw new IllegalArgumentException("Only int16, float16 or bfloat16 tensors can be created from ShortBuffer.");
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, IntBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, IntBuffer data, long[] shape) throws OrtException {
        if (!allocator.isClosed()) {
            OnnxJavaType type = OnnxJavaType.INT32;
            return createTensor(type, allocator, data, shape);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    public static OnnxTensor createTensor(OrtEnvironment env, LongBuffer data, long[] shape) throws OrtException {
        return createTensor(env, env.defaultAllocator, data, shape);
    }

    static OnnxTensor createTensor(OrtEnvironment env, OrtAllocator allocator, LongBuffer data, long[] shape) throws OrtException {
        if (!allocator.isClosed()) {
            OnnxJavaType type = OnnxJavaType.INT64;
            return createTensor(type, allocator, data, shape);
        }
        throw new IllegalStateException("Trying to create an OnnxTensor on a closed OrtAllocator.");
    }

    private static OnnxTensor createTensor(OnnxJavaType type, OrtAllocator allocator, Buffer data, long[] shape) throws OrtException {
        OrtUtil.BufferTuple tuple = OrtUtil.prepareBuffer(data, type);
        TensorInfo info = TensorInfo.constructFromBuffer(tuple.data, shape, type);
        return new OnnxTensor(createTensorFromBuffer(OnnxRuntime.ortApiHandle, allocator.handle, tuple.data, tuple.pos, tuple.byteSize, shape, info.onnxType.value), allocator.handle, info, tuple.data, tuple.isCopy);
    }
}
