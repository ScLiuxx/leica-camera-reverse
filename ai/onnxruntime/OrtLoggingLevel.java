package ai.onnxruntime;

import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public enum OrtLoggingLevel {
    ORT_LOGGING_LEVEL_VERBOSE(0),
    ORT_LOGGING_LEVEL_INFO(1),
    ORT_LOGGING_LEVEL_WARNING(2),
    ORT_LOGGING_LEVEL_ERROR(3),
    ORT_LOGGING_LEVEL_FATAL(4);

    private static final Logger logger = Logger.getLogger(OrtLoggingLevel.class.getName());
    private static final OrtLoggingLevel[] values = new OrtLoggingLevel[5];
    private final int value;

    static {
        for (OrtLoggingLevel ot : values()) {
            values[ot.value] = ot;
        }
    }

    OrtLoggingLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static OrtLoggingLevel mapFromInt(int logLevel) {
        if (logLevel >= 0) {
            OrtLoggingLevel[] ortLoggingLevelArr = values;
            if (logLevel < ortLoggingLevelArr.length) {
                return ortLoggingLevelArr[logLevel];
            }
        }
        logger.warning("Unknown logging level " + logLevel + " setting to ORT_LOGGING_LEVEL_VERBOSE");
        return ORT_LOGGING_LEVEL_VERBOSE;
    }
}
