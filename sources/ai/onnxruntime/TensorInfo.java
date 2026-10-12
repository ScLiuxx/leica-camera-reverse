package ai.onnxruntime;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class TensorInfo implements ValueInfo {
    public static final int MAX_DIMENSIONS = 8;
    final String[] dimensionNames;
    private final boolean hasNames;
    final long numElements;
    public final OnnxTensorType onnxType;
    final long[] shape;
    public final OnnxJavaType type;

    public enum OnnxTensorType {
        ONNX_TENSOR_ELEMENT_DATA_TYPE_UNDEFINED(0),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT8(1),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_INT8(2),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT16(3),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_INT16(4),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT32(5),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_INT32(6),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT64(7),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_INT64(8),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16(9),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT(10),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_DOUBLE(11),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_STRING(12),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_BOOL(13),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_COMPLEX64(14),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_COMPLEX128(15),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_BFLOAT16(16),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT8E4M3FN(17),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT8E4M3FNUZ(18),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT8E5M2(19),
        ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT8E5M2FNUZ(20);

        private static final OnnxTensorType[] values = new OnnxTensorType[21];
        public final int value;

        static {
            for (OnnxTensorType ot : values()) {
                values[ot.value] = ot;
            }
        }

        OnnxTensorType(int value) {
            this.value = value;
        }

        public static OnnxTensorType mapFromInt(int value) {
            if (value > 0) {
                OnnxTensorType[] onnxTensorTypeArr = values;
                if (value < onnxTensorTypeArr.length) {
                    return onnxTensorTypeArr[value];
                }
            }
            return ONNX_TENSOR_ELEMENT_DATA_TYPE_UNDEFINED;
        }

        public static OnnxTensorType mapFromJavaType(OnnxJavaType type) {
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[type.ordinal()]) {
                case 1:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT;
                case 2:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_DOUBLE;
                case 3:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_INT8;
                case 4:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT8;
                case 5:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_INT16;
                case 6:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_INT32;
                case 7:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_INT64;
                case TensorInfo.MAX_DIMENSIONS /* 8 */:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_BOOL;
                case 9:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_STRING;
                case 10:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16;
                case 11:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_BFLOAT16;
                default:
                    return ONNX_TENSOR_ELEMENT_DATA_TYPE_UNDEFINED;
            }
        }
    }

    /* JADX INFO: renamed from: ai.onnxruntime.TensorInfo$1, reason: invalid class name */
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
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 4;
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
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.FLOAT16.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BFLOAT16.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UNKNOWN.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    TensorInfo(long[] shape, OnnxJavaType type, OnnxTensorType onnxType) {
        this.shape = shape;
        String[] strArr = new String[shape.length];
        this.dimensionNames = strArr;
        Arrays.fill(strArr, "");
        this.hasNames = false;
        this.type = type;
        this.onnxType = onnxType;
        this.numElements = elementCount(shape);
    }

    TensorInfo(long[] shape, String[] names, int typeInt) {
        this.shape = shape;
        this.dimensionNames = names;
        boolean hasNames = false;
        for (String s : names) {
            if (!s.isEmpty()) {
                hasNames = true;
                break;
            }
        }
        this.hasNames = hasNames;
        OnnxTensorType onnxTensorTypeMapFromInt = OnnxTensorType.mapFromInt(typeInt);
        this.onnxType = onnxTensorTypeMapFromInt;
        this.type = OnnxJavaType.mapFromOnnxTensorType(onnxTensorTypeMapFromInt);
        this.numElements = elementCount(shape);
    }

    public long[] getShape() {
        long[] jArr = this.shape;
        return Arrays.copyOf(jArr, jArr.length);
    }

    public String[] getDimensionNames() {
        String[] strArr = this.dimensionNames;
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    public String toString() {
        String output = "TensorInfo(javaType=" + this.type.toString() + ",onnxType=" + this.onnxType.toString() + ",shape=" + Arrays.toString(this.shape);
        if (this.hasNames) {
            output = output + ",dimNames=[" + ((String) Arrays.stream(this.dimensionNames).map(new Function() { // from class: ai.onnxruntime.TensorInfo$$ExternalSyntheticLambda0
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return TensorInfo.lambda$toString$0((String) obj);
                }
            }).collect(Collectors.joining(","))) + "]";
        }
        return output + ")";
    }

    static /* synthetic */ String lambda$toString$0(String a) {
        if (a.isEmpty()) {
            return "\"\"";
        }
        return a;
    }

    public boolean isScalar() {
        return this.shape.length == 0;
    }

    private boolean validateShape() {
        return OrtUtil.validateShape(this.shape);
    }

    private static long elementCount(long[] shape) {
        long output = 1;
        for (long j : shape) {
            output *= j;
        }
        return output;
    }

    public long getNumElements() {
        return this.numElements;
    }

    public Object makeCarrier() throws OrtException {
        if (!validateShape() && this.numElements != 0) {
            throw new OrtException("This tensor is not representable in Java, it's too big - shape = " + Arrays.toString(this.shape));
        }
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[this.type.ordinal()]) {
            case 1:
            case 10:
            case 11:
                return OrtUtil.newFloatArray(this.shape);
            case 2:
                return OrtUtil.newDoubleArray(this.shape);
            case 3:
            case 4:
                return OrtUtil.newByteArray(this.shape);
            case 5:
                return OrtUtil.newShortArray(this.shape);
            case 6:
                return OrtUtil.newIntArray(this.shape);
            case 7:
                return OrtUtil.newLongArray(this.shape);
            case MAX_DIMENSIONS /* 8 */:
                return OrtUtil.newBooleanArray(this.shape);
            case 9:
                return new String[(int) OrtUtil.elementCount(this.shape)];
            case 12:
                throw new OrtException("Can't construct a carrier for an invalid type.");
            default:
                throw new OrtException("Unsupported type - " + this.type);
        }
    }

    public static TensorInfo constructFromJavaArray(Object obj) throws OrtException {
        Class<?> objClass = obj.getClass();
        if (!objClass.isArray()) {
            OnnxJavaType javaType = OnnxJavaType.mapFromClass(objClass);
            if (javaType == OnnxJavaType.UNKNOWN) {
                throw new OrtException("Cannot convert " + objClass + " to a OnnxTensor.");
            }
            return new TensorInfo(new long[0], javaType, OnnxTensorType.mapFromJavaType(javaType));
        }
        int dimensions = 0;
        while (objClass.isArray()) {
            objClass = objClass.getComponentType();
            dimensions++;
        }
        if (!objClass.isPrimitive() && !objClass.equals(String.class)) {
            throw new OrtException("Cannot create an OnnxTensor from a base type of " + objClass);
        }
        if (dimensions > 8) {
            throw new OrtException("Cannot create an OnnxTensor with more than 8 dimensions. Found " + dimensions + " dimensions.");
        }
        OnnxJavaType javaType2 = OnnxJavaType.mapFromClass(objClass);
        long[] shape = new long[dimensions];
        extractShape(shape, 0, obj);
        return new TensorInfo(shape, javaType2, OnnxTensorType.mapFromJavaType(javaType2));
    }

    public static TensorInfo constructFromBuffer(Buffer buffer, long[] shape, OnnxJavaType type) throws OrtException {
        if (type == OnnxJavaType.STRING || type == OnnxJavaType.UNKNOWN) {
            throw new OrtException("Cannot create a tensor from a string or unknown buffer.");
        }
        long elementCount = OrtUtil.elementCount(shape);
        long bufferRemaining = buffer.remaining();
        if (elementCount != bufferRemaining) {
            long elemRemaining = bufferRemaining / ((long) type.size);
            if (elementCount != elemRemaining) {
                throw new OrtException("Shape " + Arrays.toString(shape) + ", requires " + elementCount + " elements but the buffer has " + bufferRemaining + " elements.");
            }
        }
        return new TensorInfo(Arrays.copyOf(shape, shape.length), type, OnnxTensorType.mapFromJavaType(type));
    }

    public static <T extends Buffer> TensorInfo constructFromSparseTensor(OnnxSparseTensor.SparseTensor<T> tensor) throws OrtException {
        long[] shape = tensor.getDenseShape();
        long elementCount = OrtUtil.elementCount(shape);
        long bufferRemaining = tensor.getValues().remaining();
        if (elementCount < bufferRemaining) {
            throw new OrtException("Shape " + Arrays.toString(shape) + ", has at most " + elementCount + " elements but the buffer has " + bufferRemaining + " elements.");
        }
        return new TensorInfo(Arrays.copyOf(shape, shape.length), tensor.getType(), OnnxTensorType.mapFromJavaType(tensor.getType()));
    }

    private static void extractShape(long[] shape, int curDim, Object obj) throws OrtException {
        if (shape.length != curDim) {
            int curLength = Array.getLength(obj);
            if (curLength == 0) {
                throw new OrtException("Supplied array has a zero dimension at " + curDim + ", all dimensions must be positive");
            }
            if (shape[curDim] == 0) {
                shape[curDim] = curLength;
            } else if (shape[curDim] != curLength) {
                throw new OrtException("Supplied array is ragged, expected " + shape[curDim] + ", found " + curLength);
            }
            for (int i = 0; i < curLength; i++) {
                extractShape(shape, curDim + 1, Array.get(obj, i));
            }
        }
    }
}
