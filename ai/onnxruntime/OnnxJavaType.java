package ai.onnxruntime;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public enum OnnxJavaType {
    FLOAT(1, Float.TYPE, 4),
    DOUBLE(2, Double.TYPE, 8),
    INT8(3, Byte.TYPE, 1),
    INT16(4, Short.TYPE, 2),
    INT32(5, Integer.TYPE, 4),
    INT64(6, Long.TYPE, 8),
    BOOL(7, Boolean.TYPE, 1),
    STRING(8, String.class, 4),
    UINT8(9, Byte.TYPE, 1),
    FLOAT16(10, Short.TYPE, 2),
    BFLOAT16(11, Short.TYPE, 2),
    UNKNOWN(0, Object.class, 0);

    private static final OnnxJavaType[] values;
    public final Class<?> clazz;
    public final int size;
    public final int value;

    static {
        OnnxJavaType[] tmpValues = values();
        values = new OnnxJavaType[tmpValues.length];
        for (OnnxJavaType ot : tmpValues) {
            values[ot.value] = ot;
        }
    }

    OnnxJavaType(int value, Class cls, int size) {
        this.value = value;
        this.clazz = cls;
        this.size = size;
    }

    public static OnnxJavaType mapFromInt(int value) {
        if (value > 0) {
            OnnxJavaType[] onnxJavaTypeArr = values;
            if (value < onnxJavaTypeArr.length) {
                return onnxJavaTypeArr[value];
            }
        }
        return UNKNOWN;
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OnnxJavaType$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType;

        static {
            int[] iArr = new int[TensorInfo.OnnxTensorType.values().length];
            $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType = iArr;
            try {
                iArr[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT8.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_INT8.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT16.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_INT16.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT32.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_INT32.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_UINT64.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_INT64.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_BFLOAT16.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_DOUBLE.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_STRING.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_BOOL.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_UNDEFINED.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_COMPLEX64.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[TensorInfo.OnnxTensorType.ONNX_TENSOR_ELEMENT_DATA_TYPE_COMPLEX128.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
        }
    }

    public static OnnxJavaType mapFromOnnxTensorType(TensorInfo.OnnxTensorType onnxValue) {
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$TensorInfo$OnnxTensorType[onnxValue.ordinal()]) {
            case 1:
                return UINT8;
            case 2:
                return INT8;
            case 3:
            case 4:
                return INT16;
            case 5:
            case 6:
                return INT32;
            case 7:
            case TensorInfo.MAX_DIMENSIONS /* 8 */:
                return INT64;
            case 9:
                return FLOAT16;
            case 10:
                return BFLOAT16;
            case 11:
                return FLOAT;
            case 12:
                return DOUBLE;
            case 13:
                return STRING;
            case 14:
                return BOOL;
            default:
                return UNKNOWN;
        }
    }

    public static OnnxJavaType mapFromClass(Class<?> clazz) {
        if (clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)) {
            return INT8;
        }
        if (clazz.equals(Short.TYPE) || clazz.equals(Short.class)) {
            return INT16;
        }
        if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
            return INT32;
        }
        if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
            return INT64;
        }
        if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
            return FLOAT;
        }
        if (clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
            return DOUBLE;
        }
        if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
            return BOOL;
        }
        if (clazz.equals(String.class)) {
            return STRING;
        }
        return UNKNOWN;
    }
}
