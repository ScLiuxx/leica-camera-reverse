package ai.onnxruntime;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class OnnxMap implements OnnxValue {
    private static final Logger logger = Logger.getLogger(OnnxMap.class.getName());
    final long allocatorHandle;
    private boolean closed;
    private final MapInfo info;
    final long nativeHandle;
    private final boolean stringKeys;
    private final OnnxMapValueType valueType;

    private native void close(long j, long j2);

    private native double[] getDoubleValues(long j, long j2, long j3) throws OrtException;

    private native float[] getFloatValues(long j, long j2, long j3) throws OrtException;

    private native long[] getLongKeys(long j, long j2, long j3) throws OrtException;

    private native long[] getLongValues(long j, long j2, long j3) throws OrtException;

    private native String[] getStringKeys(long j, long j2, long j3) throws OrtException;

    private native String[] getStringValues(long j, long j2, long j3) throws OrtException;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    public enum OnnxMapValueType {
        INVALID(0),
        STRING(1),
        LONG(2),
        FLOAT(3),
        DOUBLE(4);

        private static final OnnxMapValueType[] values = new OnnxMapValueType[5];
        final int value;

        static {
            for (OnnxMapValueType ot : values()) {
                values[ot.value] = ot;
            }
        }

        OnnxMapValueType(int value) {
            this.value = value;
        }

        public static OnnxMapValueType mapFromInt(int value) {
            if (value > 0) {
                OnnxMapValueType[] onnxMapValueTypeArr = values;
                if (value < onnxMapValueTypeArr.length) {
                    return onnxMapValueTypeArr[value];
                }
            }
            return INVALID;
        }

        public static OnnxMapValueType mapFromOnnxJavaType(OnnxJavaType type) {
            switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[type.ordinal()]) {
                case 1:
                    return FLOAT;
                case 2:
                    return DOUBLE;
                case 3:
                    return LONG;
                case 4:
                    return STRING;
                default:
                    return INVALID;
            }
        }
    }

    OnnxMap(long nativeHandle, long allocatorHandle, MapInfo info) {
        this.nativeHandle = nativeHandle;
        this.allocatorHandle = allocatorHandle;
        this.info = info;
        this.stringKeys = info.keyType == OnnxJavaType.STRING;
        this.valueType = OnnxMapValueType.mapFromOnnxJavaType(info.valueType);
        this.closed = false;
    }

    public int size() {
        return this.info.size;
    }

    @Override // ai.onnxruntime.OnnxValue
    public OnnxValue.OnnxValueType getType() {
        return OnnxValue.OnnxValueType.ONNX_TYPE_MAP;
    }

    @Override // ai.onnxruntime.OnnxValue
    public Map<? extends Object, ? extends Object> getValue() throws OrtException {
        checkClosed();
        Object[] keys = getMapKeys();
        Object[] values = getMapValues();
        HashMap<Object, Object> map = new HashMap<>(OrtUtil.capacityFromSize(keys.length));
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    private Object[] getMapKeys() throws OrtException {
        if (this.stringKeys) {
            return getStringKeys(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle);
        }
        return Arrays.stream(getLongKeys(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle)).boxed().toArray();
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OnnxMap$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxJavaType;
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType;

        static {
            int[] iArr = new int[OnnxMapValueType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType = iArr;
            try {
                iArr[OnnxMapValueType.STRING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType[OnnxMapValueType.LONG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType[OnnxMapValueType.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType[OnnxMapValueType.DOUBLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            int[] iArr2 = new int[OnnxJavaType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxJavaType = iArr2;
            try {
                iArr2[OnnxJavaType.FLOAT.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT64.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.STRING.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 6;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT16.ordinal()] = 7;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT32.ordinal()] = 8;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BOOL.ordinal()] = 9;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UNKNOWN.ordinal()] = 10;
            } catch (NoSuchFieldError e14) {
            }
        }
    }

    private Object[] getMapValues() throws OrtException {
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxMap$OnnxMapValueType[this.valueType.ordinal()]) {
            case 1:
                return getStringValues(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle);
            case 2:
                return Arrays.stream(getLongValues(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle)).boxed().toArray();
            case 3:
                float[] floats = getFloatValues(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle);
                Float[] boxed = new Float[floats.length];
                for (int i = 0; i < floats.length; i++) {
                    boxed[i] = Float.valueOf(floats[i]);
                }
                return boxed;
            case 4:
                return Arrays.stream(getDoubleValues(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle)).boxed().toArray();
            default:
                throw new RuntimeException("Invalid or unknown valueType: " + this.valueType);
        }
    }

    @Override // ai.onnxruntime.OnnxValue
    public MapInfo getInfo() {
        return this.info;
    }

    public String toString() {
        return "ONNXMap(size=" + size() + ",info=" + this.info.toString() + ")";
    }

    @Override // ai.onnxruntime.OnnxValue
    public synchronized boolean isClosed() {
        return this.closed;
    }

    @Override // ai.onnxruntime.OnnxValue, java.lang.AutoCloseable
    public synchronized void close() {
        if (!this.closed) {
            close(OnnxRuntime.ortApiHandle, this.nativeHandle);
            this.closed = true;
        } else {
            logger.warning("Closing an already closed map.");
        }
    }

    protected void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Trying to use a closed OnnxValue");
        }
    }
}
