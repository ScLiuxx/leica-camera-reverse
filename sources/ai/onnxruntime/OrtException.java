package ai.onnxruntime;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class OrtException extends Exception {
    private static final long serialVersionUID = 1;
    private final OrtErrorCode errorCode;

    public OrtException(String message) {
        super(message);
        this.errorCode = OrtErrorCode.ORT_JAVA_UNKNOWN;
    }

    public OrtException(int code, String message) {
        this(OrtErrorCode.mapFromInt(code), message);
    }

    public OrtException(OrtErrorCode code, String message) {
        super("Error code - " + code + " - message: " + message);
        this.errorCode = code;
    }

    public OrtErrorCode getCode() {
        return this.errorCode;
    }

    public enum OrtErrorCode {
        ORT_JAVA_UNKNOWN(-1),
        ORT_OK(0),
        ORT_FAIL(1),
        ORT_INVALID_ARGUMENT(2),
        ORT_NO_SUCHFILE(3),
        ORT_NO_MODEL(4),
        ORT_ENGINE_ERROR(5),
        ORT_RUNTIME_EXCEPTION(6),
        ORT_INVALID_PROTOBUF(7),
        ORT_MODEL_LOADED(8),
        ORT_NOT_IMPLEMENTED(9),
        ORT_INVALID_GRAPH(10),
        ORT_EP_FAIL(11);

        private static final OrtErrorCode[] values = new OrtErrorCode[12];
        private final int value;

        static {
            for (OrtErrorCode ot : values()) {
                if (ot != ORT_JAVA_UNKNOWN) {
                    values[ot.value] = ot;
                }
            }
        }

        OrtErrorCode(int value) {
            this.value = value;
        }

        public static OrtErrorCode mapFromInt(int value) {
            if (value >= 0) {
                OrtErrorCode[] ortErrorCodeArr = values;
                if (value < ortErrorCodeArr.length) {
                    return ortErrorCodeArr[value];
                }
            }
            return ORT_JAVA_UNKNOWN;
        }
    }
}