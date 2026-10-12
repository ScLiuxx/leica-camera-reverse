package ai.onnxruntime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Objects;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OrtEnvironment implements AutoCloseable {
    public static final String DEFAULT_NAME = "ort-java";
    private static volatile OrtEnvironment INSTANCE;
    private static volatile OrtLoggingLevel curLogLevel;
    private static volatile String curLoggingName;
    private static final Logger logger = Logger.getLogger(OrtEnvironment.class.getName());
    final OrtAllocator defaultAllocator;
    final long nativeHandle;

    /* JADX INFO: Access modifiers changed from: private */
    public static native void close(long j, long j2) throws OrtException;

    private static native long createHandle(long j, int i, String str) throws OrtException;

    private static native long createHandle(long j, int i, String str, long j2) throws OrtException;

    private static native long getDefaultAllocator(long j) throws OrtException;

    private static native void setTelemetry(long j, long j2, boolean z) throws OrtException;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    public static synchronized OrtEnvironment getEnvironment() {
        if (INSTANCE == null) {
            return getEnvironment(OrtLoggingLevel.ORT_LOGGING_LEVEL_WARNING, DEFAULT_NAME);
        }
        return INSTANCE;
    }

    public static OrtEnvironment getEnvironment(String name) {
        return getEnvironment(OrtLoggingLevel.ORT_LOGGING_LEVEL_WARNING, name);
    }

    public static OrtEnvironment getEnvironment(OrtLoggingLevel logLevel) {
        return getEnvironment(logLevel, DEFAULT_NAME);
    }

    public static synchronized OrtEnvironment getEnvironment(OrtLoggingLevel loggingLevel, String name) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new OrtEnvironment(loggingLevel, name);
                curLogLevel = loggingLevel;
                curLoggingName = name;
            } catch (OrtException e) {
                throw new IllegalStateException("Failed to create OrtEnvironment", e);
            }
        } else if (loggingLevel.getValue() != curLogLevel.getValue() || !name.equals(curLoggingName)) {
            logger.warning("Tried to change OrtEnvironment's logging level or name while a reference exists.");
        }
        return INSTANCE;
    }

    public static synchronized OrtEnvironment getEnvironment(OrtLoggingLevel loggingLevel, String name, ThreadingOptions threadOptions) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new OrtEnvironment(loggingLevel, name, threadOptions);
                curLogLevel = loggingLevel;
                curLoggingName = name;
            } catch (OrtException e) {
                throw new IllegalStateException("Failed to create OrtEnvironment", e);
            }
        } else {
            throw new IllegalStateException("Tried to specify the thread pool when creating an OrtEnvironment, but one already exists.");
        }
        return INSTANCE;
    }

    private OrtEnvironment() throws OrtException {
        this(OrtLoggingLevel.ORT_LOGGING_LEVEL_WARNING, "java-default");
    }

    private OrtEnvironment(OrtLoggingLevel loggingLevel, String name) throws OrtException {
        long jCreateHandle = createHandle(OnnxRuntime.ortApiHandle, loggingLevel.getValue(), name);
        this.nativeHandle = jCreateHandle;
        this.defaultAllocator = new OrtAllocator(getDefaultAllocator(OnnxRuntime.ortApiHandle), true);
        Runtime.getRuntime().addShutdownHook(new Thread(new OrtEnvCloser(OnnxRuntime.ortApiHandle, jCreateHandle)));
    }

    private OrtEnvironment(OrtLoggingLevel loggingLevel, String name, ThreadingOptions threadOptions) throws OrtException {
        long jCreateHandle = createHandle(OnnxRuntime.ortApiHandle, loggingLevel.getValue(), name, threadOptions.nativeHandle);
        this.nativeHandle = jCreateHandle;
        this.defaultAllocator = new OrtAllocator(getDefaultAllocator(OnnxRuntime.ortApiHandle), true);
        Runtime.getRuntime().addShutdownHook(new Thread(new OrtEnvCloser(OnnxRuntime.ortApiHandle, jCreateHandle)));
    }

    long getNativeHandle() {
        return this.nativeHandle;
    }

    public OrtSession createSession(String modelPath) throws OrtException {
        return createSession(modelPath, new OrtSession.SessionOptions());
    }

    public OrtSession createSession(String modelPath, OrtSession.SessionOptions options) throws OrtException {
        return createSession(modelPath, this.defaultAllocator, options);
    }

    OrtSession createSession(String modelPath, OrtAllocator allocator, OrtSession.SessionOptions options) throws OrtException {
        Objects.requireNonNull(modelPath, "model path must not be null");
        return new OrtSession(this, modelPath, allocator, options);
    }

    public OrtSession createSession(ByteBuffer modelBuffer, OrtSession.SessionOptions options) throws OrtException {
        return createSession(modelBuffer, this.defaultAllocator, options);
    }

    public OrtSession createSession(ByteBuffer modelBuffer) throws OrtException {
        return createSession(modelBuffer, new OrtSession.SessionOptions());
    }

    OrtSession createSession(ByteBuffer modelBuffer, OrtAllocator allocator, OrtSession.SessionOptions options) throws OrtException {
        Objects.requireNonNull(modelBuffer, "model array must not be null");
        if (modelBuffer.remaining() == 0) {
            throw new OrtException("Invalid model buffer, no elements remaining.");
        }
        if (!modelBuffer.isDirect()) {
            throw new OrtException("ByteBuffer is not direct.");
        }
        return new OrtSession(this, modelBuffer, allocator, options);
    }

    public OrtSession createSession(byte[] modelArray, OrtSession.SessionOptions options) throws OrtException {
        return createSession(modelArray, this.defaultAllocator, options);
    }

    public OrtSession createSession(byte[] modelArray) throws OrtException {
        return createSession(modelArray, new OrtSession.SessionOptions());
    }

    OrtSession createSession(byte[] modelArray, OrtAllocator allocator, OrtSession.SessionOptions options) throws OrtException {
        Objects.requireNonNull(modelArray, "model array must not be null");
        return new OrtSession(this, modelArray, allocator, options);
    }

    public OrtTrainingSession createTrainingSession(String checkpointPath, String trainPath, String evalPath, String optimizerPath) throws OrtException {
        return createTrainingSession(checkpointPath, trainPath, evalPath, optimizerPath, new OrtSession.SessionOptions());
    }

    public OrtTrainingSession createTrainingSession(String checkpointPath, String trainPath, String evalPath, String optimizerPath, OrtSession.SessionOptions options) throws OrtException {
        return createTrainingSession(checkpointPath, trainPath, evalPath, optimizerPath, this.defaultAllocator, options);
    }

    OrtTrainingSession createTrainingSession(String checkpointPath, String trainPath, String evalPath, String optimizerPath, OrtAllocator allocator, OrtSession.SessionOptions options) throws OrtException {
        if (!OnnxRuntime.trainingEnabled) {
            throw new IllegalStateException("Training is not enabled in this build of ONNX Runtime.");
        }
        Objects.requireNonNull(trainPath, "train path must not be null");
        OrtTrainingSession.OrtCheckpointState checkpointState = OrtTrainingSession.OrtCheckpointState.loadCheckpoint(checkpointPath);
        return new OrtTrainingSession(this, allocator, options, checkpointState, trainPath, evalPath, optimizerPath);
    }

    public boolean isTrainingEnabled() {
        return OnnxRuntime.trainingEnabled;
    }

    public void setTelemetry(boolean sendTelemetry) throws OrtException {
        setTelemetry(OnnxRuntime.ortApiHandle, this.nativeHandle, sendTelemetry);
    }

    public String getVersion() {
        return OnnxRuntime.version();
    }

    public String toString() {
        return "OrtEnvironment(name=" + curLoggingName + ",logLevel=" + curLogLevel + ",version=" + getVersion() + ")";
    }

    public static EnumSet<OrtProvider> getAvailableProviders() {
        return OnnxRuntime.providers.clone();
    }

    @Override // java.lang.AutoCloseable
    public void close() {
    }

    public static final class ThreadingOptions implements AutoCloseable {
        private boolean closed = false;
        private final long nativeHandle = createThreadingOptions(OnnxRuntime.ortApiHandle);

        private native void closeThreadingOptions(long j, long j2);

        private static native long createThreadingOptions(long j);

        private native void setGlobalDenormalAsZero(long j, long j2) throws OrtException;

        private native void setGlobalInterOpNumThreads(long j, long j2, int i) throws OrtException;

        private native void setGlobalIntraOpNumThreads(long j, long j2, int i) throws OrtException;

        private native void setGlobalSpinControl(long j, long j2, int i) throws OrtException;

        static {
            try {
                OnnxRuntime.init();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load onnx-runtime library", e);
            }
        }

        private void checkClosed() {
            if (this.closed) {
                throw new IllegalStateException("Trying to use a closed ThreadingOptions");
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (!this.closed) {
                closeThreadingOptions(OnnxRuntime.ortApiHandle, this.nativeHandle);
                this.closed = true;
                return;
            }
            throw new IllegalStateException("Trying to close a closed ThreadingOptions.");
        }

        public void setGlobalInterOpNumThreads(int numThreads) throws OrtException {
            checkClosed();
            if (numThreads < 0) {
                throw new IllegalArgumentException("Number of threads must be non-negative.");
            }
            setGlobalInterOpNumThreads(OnnxRuntime.ortApiHandle, this.nativeHandle, numThreads);
        }

        public void setGlobalIntraOpNumThreads(int numThreads) throws OrtException {
            checkClosed();
            if (numThreads < 0) {
                throw new IllegalArgumentException("Number of threads must be non-negative.");
            }
            setGlobalIntraOpNumThreads(OnnxRuntime.ortApiHandle, this.nativeHandle, numThreads);
        }

        public void setGlobalSpinControl(boolean z) throws OrtException {
            checkClosed();
            setGlobalSpinControl(OnnxRuntime.ortApiHandle, this.nativeHandle, z ? 1 : 0);
        }

        public void setGlobalDenormalAsZero() throws OrtException {
            checkClosed();
            setGlobalDenormalAsZero(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }
    }

    private static final class OrtEnvCloser implements Runnable {
        private final long apiHandle;
        private final long nativeHandle;

        OrtEnvCloser(long apiHandle, long nativeHandle) {
            this.apiHandle = apiHandle;
            this.nativeHandle = nativeHandle;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                OrtEnvironment.close(this.apiHandle, this.nativeHandle);
            } catch (OrtException e) {
                System.err.println("Error closing OrtEnvironment, " + e);
            }
        }
    }
}
