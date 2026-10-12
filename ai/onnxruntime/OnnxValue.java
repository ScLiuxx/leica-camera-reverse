package ai.onnxruntime;

import java.util.Map;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public interface OnnxValue extends AutoCloseable {
    @Override // java.lang.AutoCloseable
    void close();

    ValueInfo getInfo();

    OnnxValueType getType();

    Object getValue() throws OrtException;

    boolean isClosed();

    public enum OnnxValueType {
        ONNX_TYPE_UNKNOWN(0),
        ONNX_TYPE_TENSOR(1),
        ONNX_TYPE_SEQUENCE(2),
        ONNX_TYPE_MAP(3),
        ONNX_TYPE_OPAQUE(4),
        ONNX_TYPE_SPARSETENSOR(5),
        ONNX_TYPE_OPTIONAL(6);

        public final int value;

        OnnxValueType(int value) {
            this.value = value;
        }
    }

    static void close(Iterable<? extends OnnxValue> itr) {
        for (OnnxValue t : itr) {
            t.close();
        }
    }

    static void close(Map<String, ? extends OnnxValue> map) {
        for (OnnxValue t : map.values()) {
            t.close();
        }
    }
}
