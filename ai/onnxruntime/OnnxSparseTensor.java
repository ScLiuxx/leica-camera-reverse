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
import java.util.Arrays;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OnnxSparseTensor extends OnnxTensorLike {
    private static final Logger logger = Logger.getLogger(OnnxSparseTensor.class.getName());
    private final Buffer indices;
    private final LongBuffer innerIndices;
    private final SparseTensorType sparseTensorType;
    private final Buffer values;

    private native void close(long j, long j2);

    private static native long createCSRCSparseTensorFromBuffer(long j, long j2, Buffer buffer, int i, long j3, Buffer buffer2, int i2, long j4, Buffer buffer3, int i3, long[] jArr, long[] jArr2, int i4) throws OrtException;

    private static native long createSparseTensorFromBuffer(long j, long j2, Buffer buffer, int i, long j3, Buffer buffer2, int i2, long[] jArr, long[] jArr2, long[] jArr3, int i3, int i4) throws OrtException;

    private native ByteBuffer getIndicesBuffer(long j, long j2);

    private native long[] getIndicesShape(long j, long j2);

    private native ByteBuffer getInnerIndicesBuffer(long j, long j2);

    private native long[] getInnerIndicesShape(long j, long j2);

    private native ByteBuffer getValuesBuffer(long j, long j2);

    private native long[] getValuesShape(long j, long j2);

    OnnxSparseTensor(long nativeHandle, long allocatorHandle, int sparseType, TensorInfo info) {
        this(nativeHandle, allocatorHandle, SparseTensorType.mapFromInt(sparseType), info, null, null, null);
    }

    OnnxSparseTensor(long nativeHandle, long allocatorHandle, SparseTensorType sparseType, TensorInfo info, Buffer indices, Buffer values) {
        this(nativeHandle, allocatorHandle, sparseType, info, indices, null, values);
    }

    OnnxSparseTensor(long nativeHandle, long allocatorHandle, SparseTensorType sparseType, TensorInfo info, Buffer indices, LongBuffer innerIndices, Buffer values) {
        super(nativeHandle, allocatorHandle, info);
        this.sparseTensorType = sparseType;
        this.indices = indices;
        this.innerIndices = innerIndices;
        this.values = values;
    }

    public static <T extends Buffer> OnnxSparseTensor createSparseTensor(OrtEnvironment env, SparseTensor<T> tensor) throws OrtException {
        return createSparseTensor(env, env.defaultAllocator, tensor);
    }

    static <T extends Buffer> OnnxSparseTensor createSparseTensor(OrtEnvironment env, OrtAllocator allocator, SparseTensor<T> tensor) throws OrtException {
        if (!allocator.isClosed()) {
            TensorInfo info = TensorInfo.constructFromSparseTensor(tensor);
            OnnxJavaType indicesType = tensor.getIndicesType();
            OrtUtil.BufferTuple indicesTuple = OrtUtil.prepareBuffer(tensor.getIndices(), indicesType);
            OrtUtil.BufferTuple valuesTuple = OrtUtil.prepareBuffer(tensor.getValues(), info.type);
            if (!(indicesTuple.data instanceof LongBuffer) && !(indicesTuple.data instanceof IntBuffer)) {
                throw new IllegalStateException("Unexpected type of indices buffer, found " + indicesTuple.data.getClass() + ", expected IntBuffer or LongBuffer");
            }
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[tensor.getSparsityType().ordinal()]) {
                case 1:
                case 2:
                    return new OnnxSparseTensor(createSparseTensorFromBuffer(OnnxRuntime.ortApiHandle, allocator.handle, indicesTuple.data, indicesTuple.pos, indicesTuple.size, valuesTuple.data, valuesTuple.pos, info.shape, tensor.getIndicesShape(), tensor.getValuesShape(), info.onnxType.value, tensor.getSparsityType().value), allocator.handle, tensor.getSparsityType(), info, indicesTuple.data, valuesTuple.data);
                case 3:
                    OrtUtil.BufferTuple innerIndicesTuple = OrtUtil.prepareBuffer(((CSRCTensor) tensor).getInnerIndices(), indicesType);
                    return new OnnxSparseTensor(createCSRCSparseTensorFromBuffer(OnnxRuntime.ortApiHandle, allocator.handle, indicesTuple.data, indicesTuple.pos, indicesTuple.size, innerIndicesTuple.data, innerIndicesTuple.pos, innerIndicesTuple.size, valuesTuple.data, valuesTuple.pos, info.shape, tensor.getValuesShape(), info.onnxType.value), allocator.handle, tensor.getSparsityType(), info, indicesTuple.data, (LongBuffer) innerIndicesTuple.data, valuesTuple.data);
                default:
                    throw new IllegalArgumentException("Cannot create an UNDEFINED sparse tensor.");
            }
        }
        throw new IllegalStateException("Trying to create an OnnxSparseTensor on a closed OrtAllocator.");
    }

    @Override // ai.onnxruntime.OnnxValue
    public OnnxValue.OnnxValueType getType() {
        return OnnxValue.OnnxValueType.ONNX_TYPE_SPARSETENSOR;
    }

    @Override // ai.onnxruntime.OnnxValue
    public SparseTensor<? extends Buffer> getValue() throws OrtException {
        checkClosed();
        Buffer buffer = getValuesBuffer();
        long[] indicesShape = getIndicesShape(OnnxRuntime.ortApiHandle, this.nativeHandle);
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[this.sparseTensorType.ordinal()]) {
            case 1:
                return new COOTensor((LongBuffer) getIndicesBuffer(), indicesShape, buffer, this.info.shape, this.info.type, buffer.remaining());
            case 2:
                long[] valuesShape = getValuesShape(OnnxRuntime.ortApiHandle, this.nativeHandle);
                return new BlockSparseTensor((IntBuffer) getIndicesBuffer(), indicesShape, buffer, valuesShape, this.info.shape, this.info.type, buffer.remaining());
            case 3:
                return new CSRCTensor((LongBuffer) getIndicesBuffer(), getInnerIndicesBuffer(), buffer, this.info.shape, this.info.type, buffer.remaining());
            default:
                throw new IllegalStateException("Undefined sparsity type in this sparse tensor.");
        }
    }

    @Override // ai.onnxruntime.OnnxValue, java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.closed) {
            close(OnnxRuntime.ortApiHandle, this.nativeHandle);
            this.closed = true;
        } else {
            logger.warning("Closing an already closed OnnxSparseTensor.");
        }
    }

    public SparseTensorType getSparseTensorType() {
        return this.sparseTensorType;
    }

    public Buffer getIndicesBuffer() {
        checkClosed();
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[this.sparseTensorType.ordinal()]) {
            case 1:
            case 3:
                LongBuffer longBuf = getIndicesBuffer(OnnxRuntime.ortApiHandle, this.nativeHandle).order(ByteOrder.nativeOrder()).asLongBuffer();
                LongBuffer output = LongBuffer.allocate(longBuf.capacity());
                output.put(longBuf);
                output.rewind();
                return output;
            case 2:
                IntBuffer intBuf = getIndicesBuffer(OnnxRuntime.ortApiHandle, this.nativeHandle).order(ByteOrder.nativeOrder()).asIntBuffer();
                IntBuffer output2 = IntBuffer.allocate(intBuf.capacity());
                output2.put(intBuf);
                output2.rewind();
                return output2;
            default:
                throw new IllegalStateException("UNDEFINED sparse tensor type.");
        }
    }

    public LongBuffer getInnerIndicesBuffer() {
        checkClosed();
        if (this.sparseTensorType == SparseTensorType.CSRC) {
            LongBuffer buf = getInnerIndicesBuffer(OnnxRuntime.ortApiHandle, this.nativeHandle).order(ByteOrder.nativeOrder()).asLongBuffer();
            LongBuffer output = LongBuffer.allocate(buf.capacity());
            output.put(buf);
            output.rewind();
            return output;
        }
        throw new IllegalStateException("Inner indices are only available for CSRC sparse tensors, this sparse tensor is " + this.sparseTensorType);
    }

    public Buffer getValuesBuffer() {
        checkClosed();
        ByteBuffer buffer = getValuesBuffer(OnnxRuntime.ortApiHandle, this.nativeHandle).order(ByteOrder.nativeOrder());
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[this.info.type.ordinal()]) {
            case 1:
                FloatBuffer floatBuf = buffer.asFloatBuffer();
                FloatBuffer output = FloatBuffer.allocate(floatBuf.capacity());
                output.put(floatBuf);
                output.rewind();
                return output;
            case 2:
                ShortBuffer shortBuffer = buffer.asShortBuffer();
                return Fp16Conversions.convertFp16BufferToFloatBuffer(shortBuffer);
            case 3:
                ShortBuffer shortBuffer2 = buffer.asShortBuffer();
                return Fp16Conversions.convertBf16BufferToFloatBuffer(shortBuffer2);
            case 4:
                DoubleBuffer doubleBuf = buffer.asDoubleBuffer();
                DoubleBuffer output2 = DoubleBuffer.allocate(doubleBuf.capacity());
                output2.put(doubleBuf);
                output2.rewind();
                return output2;
            case 5:
                ShortBuffer shortBuf = buffer.asShortBuffer();
                ShortBuffer output3 = ShortBuffer.allocate(shortBuf.capacity());
                output3.put(shortBuf);
                output3.rewind();
                return output3;
            case 6:
                IntBuffer intBuf = buffer.asIntBuffer();
                IntBuffer output4 = IntBuffer.allocate(intBuf.capacity());
                output4.put(intBuf);
                output4.rewind();
                return output4;
            case 7:
                LongBuffer longBuf = buffer.asLongBuffer();
                LongBuffer output5 = LongBuffer.allocate(longBuf.capacity());
                output5.put(longBuf);
                output5.rewind();
                return output5;
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
            case 9:
            case 10:
                ByteBuffer output6 = ByteBuffer.allocate(buffer.capacity());
                output6.put(buffer);
                output6.rewind();
                return output6;
            case 11:
                throw new IllegalStateException("Unsupported data type String");
            default:
                throw new IllegalStateException("Unsupported data type");
        }
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OnnxSparseTensor$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxJavaType;
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType;

        static {
            int[] iArr = new int[OnnxJavaType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxJavaType = iArr;
            try {
                iArr[OnnxJavaType.FLOAT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.FLOAT16.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BFLOAT16.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.DOUBLE.ordinal()] = 4;
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
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 10;
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
            int[] iArr2 = new int[SparseTensorType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType = iArr2;
            try {
                iArr2[SparseTensorType.COO.ordinal()] = 1;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[SparseTensorType.BLOCK_SPARSE.ordinal()] = 2;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[SparseTensorType.CSRC.ordinal()] = 3;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxSparseTensor$SparseTensorType[SparseTensorType.UNDEFINED.ordinal()] = 4;
            } catch (NoSuchFieldError e16) {
            }
        }
    }

    public long[] getIndicesShape() {
        checkClosed();
        return getIndicesShape(OnnxRuntime.ortApiHandle, this.nativeHandle);
    }

    public long[] getInnerIndicesShape() {
        checkClosed();
        if (this.sparseTensorType == SparseTensorType.CSRC) {
            return getInnerIndicesShape(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }
        throw new IllegalStateException("Inner indices are only available for CSRC sparse tensors, this sparse tensor is " + this.sparseTensorType);
    }

    public long[] getValuesShape() {
        checkClosed();
        return getValuesShape(OnnxRuntime.ortApiHandle, this.nativeHandle);
    }

    /* JADX WARN: Enum visitor error
    jadx.core.utils.exceptions.JadxRuntimeException: Can't remove SSA var: r1v1 ai.onnxruntime.OnnxSparseTensor$SparseTensorType, still in use, count: 1, list:
      (r1v1 ai.onnxruntime.OnnxSparseTensor$SparseTensorType) from 0x0030: FILLED_NEW_ARRAY 
      (r0v0 ai.onnxruntime.OnnxSparseTensor$SparseTensorType)
      (r1v1 ai.onnxruntime.OnnxSparseTensor$SparseTensorType)
      (r3v1 ai.onnxruntime.OnnxSparseTensor$SparseTensorType)
      (r0v0 ai.onnxruntime.OnnxSparseTensor$SparseTensorType)
      (r5v1 ai.onnxruntime.OnnxSparseTensor$SparseTensorType)
     A[WRAPPED] (LINE:594) elemType: ai.onnxruntime.OnnxSparseTensor$SparseTensorType
    	at jadx.core.utils.InsnRemover.removeSsaVar(InsnRemover.java:164)
    	at jadx.core.utils.InsnRemover.unbindResult(InsnRemover.java:129)
    	at jadx.core.utils.InsnRemover.lambda$unbindInsns$1(InsnRemover.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
    	at jadx.core.utils.InsnRemover.unbindInsns(InsnRemover.java:100)
    	at jadx.core.utils.InsnRemover.removeAllAndUnbind(InsnRemover.java:257)
    	at jadx.core.dex.visitors.EnumVisitor.convertToEnum(EnumVisitor.java:187)
    	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
     */
    /* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
    public static final class SparseTensorType {
        UNDEFINED(0),
        COO(1),
        CSRC(2),
        BLOCK_SPARSE(4);

        private static final SparseTensorType[] values;
        public final int value;

        public static SparseTensorType valueOf(String name) {
            return (SparseTensorType) Enum.valueOf(SparseTensorType.class, name);
        }

        public static SparseTensorType[] values() {
            return (SparseTensorType[]) $VALUES.clone();
        }

        static {
            SparseTensorType sparseTensorType = UNDEFINED;
            values = new SparseTensorType[]{sparseTensorType, sparseTensorType, sparseTensorType, sparseTensorType, sparseTensorType};
        }

        private SparseTensorType(int value) {
            super(str, i);
            this.value = value;
        }

        public static SparseTensorType mapFromInt(int value) {
            if (value > 0) {
                SparseTensorType[] sparseTensorTypeArr = values;
                if (value < sparseTensorTypeArr.length) {
                    return sparseTensorTypeArr[value];
                }
            }
            return UNDEFINED;
        }
    }

    public static abstract class SparseTensor<T extends Buffer> {
        private final long[] denseShape;
        final T indices;
        private final long[] indicesShape;
        private final long numNonZero;
        private final OnnxJavaType type;
        final Buffer values;
        private final long[] valuesShape;

        public abstract OnnxJavaType getIndicesType();

        public abstract SparseTensorType getSparsityType();

        SparseTensor(T indices, long[] indicesShape, Buffer values, long[] valuesShape, long[] denseShape, OnnxJavaType type, long numNonZero) {
            this.indices = indices;
            this.indicesShape = indicesShape;
            this.values = values;
            this.valuesShape = valuesShape;
            this.denseShape = denseShape;
            this.type = type;
            this.numNonZero = numNonZero;
            if (values.remaining() != numNonZero) {
                throw new IllegalArgumentException("Expected numNonZero and data.remaining to be equal, found " + numNonZero + " and " + values.remaining() + " respectively");
            }
            if (type == OnnxJavaType.STRING) {
                throw new IllegalArgumentException("String SparseTensors are not supported.");
            }
        }

        public long[] getDenseShape() {
            return this.denseShape;
        }

        public OnnxJavaType getType() {
            return this.type;
        }

        public long getNumNonZeroElements() {
            return this.numNonZero;
        }

        public T getIndices() {
            return this.indices;
        }

        public Buffer getValues() {
            return this.values;
        }

        public long[] getValuesShape() {
            return this.valuesShape;
        }

        public long[] getIndicesShape() {
            return this.indicesShape;
        }
    }

    public static final class COOTensor extends SparseTensor<LongBuffer> {
        public COOTensor(LongBuffer indices, long[] indicesShape, Buffer values, long[] denseShape, OnnxJavaType type, long numNonZero) {
            super(indices, indicesShape, values, new long[]{numNonZero}, denseShape, type, numNonZero);
            if (indicesShape.length > 2 || indicesShape.length == 0 || indicesShape[0] != numNonZero) {
                throw new IllegalArgumentException("Invalid indices shape, expected [numNonZero, dimension] or [numNonZero] found " + Arrays.toString(indicesShape));
            }
            long elementCount = OrtUtil.elementCount(indicesShape);
            if (elementCount != indices.remaining()) {
                throw new IllegalArgumentException("Unexpected number of indices found in buffer, expected " + elementCount + " found " + indices.remaining());
            }
            if (values.remaining() != numNonZero) {
                throw new IllegalArgumentException("Expected data.remaining() - " + values.remaining() + " to equal numNonZero - " + numNonZero);
            }
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public OnnxJavaType getIndicesType() {
            return OnnxJavaType.INT64;
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public SparseTensorType getSparsityType() {
            return SparseTensorType.COO;
        }
    }

    public static final class CSRCTensor extends SparseTensor<LongBuffer> {
        private final LongBuffer innerIndices;

        public CSRCTensor(LongBuffer outerIndices, LongBuffer innerIndices, Buffer values, long[] denseShape, OnnxJavaType type, long numNonZero) {
            super(outerIndices, new long[]{outerIndices.remaining()}, values, new long[]{numNonZero}, denseShape, type, numNonZero);
            this.innerIndices = innerIndices;
            long expectedRows = denseShape[0] + 1;
            if (outerIndices.remaining() != expectedRows) {
                throw new IllegalArgumentException("Outer indices should be equal to the number of rows + 1 in the dense shape, found " + outerIndices.remaining() + ", expected " + expectedRows);
            }
            if (innerIndices.remaining() != numNonZero) {
                throw new IllegalArgumentException("Inner indices should be equal to the number of non-zero elements, found " + innerIndices.remaining() + ", expected " + numNonZero);
            }
        }

        public long[] getInnerIndicesShape() {
            return new long[]{this.innerIndices.remaining()};
        }

        public LongBuffer getInnerIndices() {
            return this.innerIndices;
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public OnnxJavaType getIndicesType() {
            return OnnxJavaType.INT64;
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public SparseTensorType getSparsityType() {
            return SparseTensorType.CSRC;
        }
    }

    public static final class BlockSparseTensor extends SparseTensor<IntBuffer> {
        public BlockSparseTensor(IntBuffer indices, long[] indicesShape, Buffer values, long[] valuesShape, long[] denseShape, OnnxJavaType type, long numNonZero) {
            super(indices, indicesShape, values, valuesShape, denseShape, type, numNonZero);
            if (OrtUtil.elementCount(valuesShape) != numNonZero) {
                throw new IllegalArgumentException("Expected " + numNonZero + " entries in the data shape, found " + Arrays.toString(valuesShape));
            }
            if (numNonZero != values.remaining()) {
                throw new IllegalArgumentException("Expected " + numNonZero + " elements in the data buffer, found " + values.remaining());
            }
            if (OrtUtil.elementCount(indicesShape) != indices.remaining()) {
                throw new IllegalArgumentException("Expected " + OrtUtil.elementCount(indicesShape) + " elements in the indices buffer, found " + indices.remaining());
            }
            if (valuesShape.length < 3) {
                throw new IllegalArgumentException("Expected [numBlocks, blockSize, blockSize] or larger, but data shape was " + Arrays.toString(valuesShape));
            }
            if (indicesShape.length < 2) {
                throw new IllegalArgumentException("Expected [numBlocks, co-ordinates] or larger, but indices shape was " + Arrays.toString(indicesShape));
            }
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public OnnxJavaType getIndicesType() {
            return OnnxJavaType.INT32;
        }

        @Override // ai.onnxruntime.OnnxSparseTensor.SparseTensor
        public SparseTensorType getSparsityType() {
            return SparseTensorType.BLOCK_SPARSE;
        }
    }
}
