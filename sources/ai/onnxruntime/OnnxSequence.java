package ai.onnxruntime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class OnnxSequence implements OnnxValue {
    private static final Logger logger = Logger.getLogger(OnnxSequence.class.getName());
    private final long allocatorHandle;
    private boolean closed = false;
    private final SequenceInfo info;
    final long nativeHandle;

    private native void close(long j, long j2);

    private native OnnxMap[] getMaps(long j, long j2, long j3) throws OrtException;

    private native OnnxTensor[] getTensors(long j, long j2, long j3) throws OrtException;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    OnnxSequence(long nativeHandle, long allocatorHandle, SequenceInfo info) {
        this.nativeHandle = nativeHandle;
        this.allocatorHandle = allocatorHandle;
        this.info = info;
    }

    @Override // ai.onnxruntime.OnnxValue
    public OnnxValue.OnnxValueType getType() {
        return OnnxValue.OnnxValueType.ONNX_TYPE_SEQUENCE;
    }

    @Override // ai.onnxruntime.OnnxValue
    public List<? extends OnnxValue> getValue() throws OrtException {
        checkClosed();
        if (this.info.sequenceOfMaps) {
            OnnxMap[] maps = getMaps(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle);
            return Collections.unmodifiableList(Arrays.asList(maps));
        }
        switch (AnonymousClass1.$SwitchMap$ai$onnxruntime$OnnxJavaType[this.info.sequenceType.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                OnnxTensor[] tensors = getTensors(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocatorHandle);
                return Collections.unmodifiableList(Arrays.asList(tensors));
            default:
                throw new OrtException("Unsupported type in a sequence, found " + this.info.sequenceType);
        }
    }

    /* JADX INFO: renamed from: ai.onnxruntime.OnnxSequence$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ai$onnxruntime$OnnxJavaType;

        static {
            int[] iArr = new int[OnnxJavaType.values().length];
            $SwitchMap$ai$onnxruntime$OnnxJavaType = iArr;
            try {
                iArr[OnnxJavaType.STRING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT64.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.DOUBLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.BOOL.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UINT8.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.INT8.ordinal()] = 7;
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
                $SwitchMap$ai$onnxruntime$OnnxJavaType[OnnxJavaType.UNKNOWN.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    @Override // ai.onnxruntime.OnnxValue
    public SequenceInfo getInfo() {
        return this.info;
    }

    public String toString() {
        return "OnnxSequence(info=" + this.info.toString() + ")";
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
            logger.warning("Closing an already closed sequence.");
        }
    }

    protected void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Trying to use a closed OnnxValue");
        }
    }
}