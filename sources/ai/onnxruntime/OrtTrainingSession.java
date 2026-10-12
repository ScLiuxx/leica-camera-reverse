package ai.onnxruntime;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OrtTrainingSession implements AutoCloseable {
    private final OrtAllocator allocator;
    private final OrtCheckpointState checkpoint;
    private boolean closed;
    private final Set<String> evalInputNames;
    private final Set<String> evalOutputNames;
    private final String evalPath;
    private final long nativeHandle;
    private final String optimizerPath;
    private final Set<String> trainInputNames;
    private final Set<String> trainOutputNames;
    private final String trainPath;

    private native void closeSession(long j, long j2);

    private static native long createTrainingSession(long j, long j2, long j3, long j4, long j5, String str, String str2, String str3);

    private native boolean[] evalStep(long j, long j2, long j3, long j4, String[] strArr, long[] jArr, long j5, String[] strArr2, long j6, OnnxValue[] onnxValueArr, long[] jArr2, long j7) throws OrtException;

    private native void exportModelForInference(long j, long j2, long j3, String str, long j4, String[] strArr) throws OrtException;

    private native String[] getEvalInputNames(long j, long j2, long j3, long j4) throws OrtException;

    private native String[] getEvalOutputNames(long j, long j2, long j3, long j4) throws OrtException;

    private native float getLearningRate(long j, long j2, long j3);

    private native String[] getTrainInputNames(long j, long j2, long j3, long j4) throws OrtException;

    private native String[] getTrainOutputNames(long j, long j2, long j3, long j4) throws OrtException;

    private native void lazyResetGrad(long j, long j2, long j3) throws OrtException;

    private native void optimizerStep(long j, long j2, long j3, long j4) throws OrtException;

    private native void registerLinearLRScheduler(long j, long j2, long j3, long j4, long j5, float f) throws OrtException;

    private native void schedulerStep(long j, long j2, long j3) throws OrtException;

    private native void setLearningRate(long j, long j2, long j3, float f) throws OrtException;

    private static native void setSeed(long j, long j2, long j3) throws OrtException;

    private native boolean[] trainStep(long j, long j2, long j3, long j4, String[] strArr, long[] jArr, long j5, String[] strArr2, long j6, OnnxValue[] onnxValueArr, long[] jArr2, long j7);

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    OrtTrainingSession(OrtEnvironment env, OrtAllocator allocator, OrtSession.SessionOptions options, OrtCheckpointState checkpoint, String trainPath, String evalPath, String optimizerPath) throws OrtException {
        this(createTrainingSession(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, env.getNativeHandle(), options.getNativeHandle(), checkpoint.nativeHandle, trainPath, evalPath, optimizerPath), allocator, checkpoint, trainPath, evalPath, optimizerPath);
    }

    private OrtTrainingSession(long nativeHandle, OrtAllocator allocator, OrtCheckpointState checkpoint, String trainPath, String evalPath, String optimizerPath) throws OrtException {
        this.closed = false;
        this.nativeHandle = nativeHandle;
        this.allocator = allocator;
        this.checkpoint = checkpoint;
        this.trainPath = trainPath;
        this.evalPath = evalPath;
        this.optimizerPath = optimizerPath;
        this.trainInputNames = Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(getTrainInputNames(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, nativeHandle, allocator.handle))));
        this.trainOutputNames = Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(getTrainOutputNames(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, nativeHandle, allocator.handle))));
        this.evalInputNames = Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(getEvalInputNames(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, nativeHandle, allocator.handle))));
        this.evalOutputNames = Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(getEvalOutputNames(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, nativeHandle, allocator.handle))));
    }

    public Set<String> getTrainInputNames() {
        return this.trainInputNames;
    }

    public Set<String> getTrainOutputNames() {
        return this.trainOutputNames;
    }

    public Set<String> getEvalInputNames() {
        return this.evalInputNames;
    }

    public Set<String> getEvalOutputNames() {
        return this.evalOutputNames;
    }

    public void addProperty(String name, float value) throws OrtException {
        this.checkpoint.addProperty(name, value);
    }

    public void addProperty(String name, int value) throws OrtException {
        this.checkpoint.addProperty(name, value);
    }

    public void addProperty(String name, String value) throws OrtException {
        this.checkpoint.addProperty(name, value);
    }

    public float getFloatProperty(String name) throws OrtException {
        return this.checkpoint.getFloatProperty(this.allocator, name);
    }

    public int getIntProperty(String name) throws OrtException {
        return this.checkpoint.getIntProperty(this.allocator, name);
    }

    public String getStringProperty(String name) throws OrtException {
        return this.checkpoint.getStringProperty(this.allocator, name);
    }

    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Trying to use a closed OrtTrainingSession");
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (!this.closed) {
            closeSession(OnnxRuntime.ortTrainingApiHandle, this.nativeHandle);
            this.checkpoint.close();
            this.closed = true;
            return;
        }
        throw new IllegalStateException("Trying to close an already closed OrtSession.");
    }

    public void saveCheckpoint(Path outputPath, boolean saveOptimizer) throws OrtException {
        checkClosed();
        this.checkpoint.saveCheckpoint(outputPath, saveOptimizer);
    }

    public void lazyResetGrad() throws OrtException {
        checkClosed();
        lazyResetGrad(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle);
    }

    public static void setSeed(long seed) throws OrtException {
        setSeed(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, seed);
    }

    public OrtSession.Result trainStep(Map<String, ? extends OnnxTensorLike> inputs) throws OrtException {
        return trainStep(inputs, this.trainOutputNames, Collections.emptyMap(), null);
    }

    public OrtSession.Result trainStep(Map<String, ? extends OnnxTensorLike> inputs, OrtSession.RunOptions runOptions) throws OrtException {
        return trainStep(inputs, this.trainOutputNames, Collections.emptyMap(), runOptions);
    }

    public OrtSession.Result trainStep(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs) throws OrtException {
        return trainStep(inputs, requestedOutputs, Collections.emptyMap(), null);
    }

    public OrtSession.Result trainStep(Map<String, ? extends OnnxTensorLike> inputs, Map<String, ? extends OnnxValue> pinnedOutputs) throws OrtException {
        return trainStep(inputs, Collections.emptySet(), pinnedOutputs, null);
    }

    public OrtSession.Result trainStep(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs, Map<String, ? extends OnnxValue> pinnedOutputs, OrtSession.RunOptions runOptions) throws OrtException {
        checkClosed();
        if ((inputs.isEmpty() && this.trainInputNames.size() != 0) || inputs.size() > this.trainInputNames.size()) {
            throw new OrtException("Unexpected number of inputs, expected [1," + this.trainInputNames.size() + ") found " + inputs.size());
        }
        int numTrainOutputs = this.trainOutputNames.size();
        int totalOutputs = requestedOutputs.size() + pinnedOutputs.size();
        if (totalOutputs == 0 || totalOutputs > numTrainOutputs) {
            throw new OrtException("Unexpected number of requestedOutputs & pinnedOutputs, expected [1," + numTrainOutputs + ") found " + totalOutputs);
        }
        String[] inputNamesArray = new String[inputs.size()];
        long[] inputHandles = new long[inputs.size()];
        int i = 0;
        for (Map.Entry<String, ? extends OnnxTensorLike> t : inputs.entrySet()) {
            if (this.trainInputNames.contains(t.getKey())) {
                inputNamesArray[i] = t.getKey();
                inputHandles[i] = t.getValue().getNativeHandle();
                i++;
            } else {
                throw new OrtException("Unknown input name " + t.getKey() + ", expected one of " + this.trainInputNames);
            }
        }
        String[] outputNamesArray = new String[requestedOutputs.size() + pinnedOutputs.size()];
        OnnxValue[] outputValues = new OnnxValue[outputNamesArray.length];
        long[] outputHandles = new long[outputNamesArray.length];
        int i2 = 0;
        for (Map.Entry<String, ? extends OnnxValue> e : pinnedOutputs.entrySet()) {
            if (this.trainOutputNames.contains(e.getKey())) {
                outputNamesArray[i2] = e.getKey();
                outputValues[i2] = e.getValue();
                outputHandles[i2] = OrtSession.getHandle(e.getValue());
                i2++;
            } else {
                throw new OrtException("Unknown output name " + e.getKey() + ", expected one of " + this.trainOutputNames.toString());
            }
        }
        int i3 = i2;
        for (String s : requestedOutputs) {
            if (this.trainOutputNames.contains(s)) {
                if (!pinnedOutputs.containsKey(s)) {
                    outputNamesArray[i3] = s;
                    i3++;
                } else {
                    throw new OrtException("Output '" + s + "' was found in both the requested outputs and the pinned outputs");
                }
            } else {
                throw new OrtException("Unknown output name " + s + ", expected one of " + this.trainOutputNames.toString());
            }
        }
        long runOptionsHandle = runOptions == null ? 0L : runOptions.getNativeHandle();
        boolean[] ownedByResult = trainStep(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, this.allocator.handle, inputNamesArray, inputHandles, inputNamesArray.length, outputNamesArray, outputNamesArray.length, outputValues, outputHandles, runOptionsHandle);
        return new OrtSession.Result(outputNamesArray, outputValues, ownedByResult);
    }

    public OrtSession.Result evalStep(Map<String, ? extends OnnxTensorLike> inputs) throws OrtException {
        return evalStep(inputs, this.evalOutputNames, Collections.emptyMap(), null);
    }

    public OrtSession.Result evalStep(Map<String, ? extends OnnxTensorLike> inputs, OrtSession.RunOptions runOptions) throws OrtException {
        return evalStep(inputs, this.evalOutputNames, Collections.emptyMap(), runOptions);
    }

    public OrtSession.Result evalStep(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs) throws OrtException {
        return evalStep(inputs, requestedOutputs, Collections.emptyMap(), null);
    }

    public OrtSession.Result evalStep(Map<String, ? extends OnnxTensorLike> inputs, Map<String, ? extends OnnxValue> pinnedOutputs) throws OrtException {
        return evalStep(inputs, Collections.emptySet(), pinnedOutputs, null);
    }

    public OrtSession.Result evalStep(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs, Map<String, ? extends OnnxValue> pinnedOutputs, OrtSession.RunOptions runOptions) throws OrtException {
        checkClosed();
        if ((inputs.isEmpty() && this.evalInputNames.size() != 0) || inputs.size() > this.evalInputNames.size()) {
            throw new OrtException("Unexpected number of inputs, expected [1," + this.evalInputNames.size() + ") found " + inputs.size());
        }
        int numEvalOutputs = this.evalOutputNames.size();
        int totalOutputs = requestedOutputs.size() + pinnedOutputs.size();
        if (totalOutputs == 0 || totalOutputs > numEvalOutputs) {
            throw new OrtException("Unexpected number of requestedOutputs & pinnedOutputs, expected [1," + numEvalOutputs + ") found " + totalOutputs);
        }
        String[] inputNamesArray = new String[inputs.size()];
        long[] inputHandles = new long[inputs.size()];
        int i = 0;
        for (Map.Entry<String, ? extends OnnxTensorLike> t : inputs.entrySet()) {
            if (this.evalInputNames.contains(t.getKey())) {
                inputNamesArray[i] = t.getKey();
                inputHandles[i] = t.getValue().getNativeHandle();
                i++;
            } else {
                throw new OrtException("Unknown input name " + t.getKey() + ", expected one of " + this.evalInputNames.toString());
            }
        }
        String[] outputNamesArray = new String[requestedOutputs.size() + pinnedOutputs.size()];
        OnnxValue[] outputValues = new OnnxValue[outputNamesArray.length];
        long[] outputHandles = new long[outputNamesArray.length];
        int i2 = 0;
        for (Map.Entry<String, ? extends OnnxValue> e : pinnedOutputs.entrySet()) {
            if (this.evalOutputNames.contains(e.getKey())) {
                outputNamesArray[i2] = e.getKey();
                outputValues[i2] = e.getValue();
                outputHandles[i2] = OrtSession.getHandle(e.getValue());
                i2++;
            } else {
                throw new OrtException("Unknown output name " + e.getKey() + ", expected one of " + this.evalOutputNames.toString());
            }
        }
        int i3 = i2;
        for (String s : requestedOutputs) {
            if (this.evalOutputNames.contains(s)) {
                if (!pinnedOutputs.containsKey(s)) {
                    outputNamesArray[i3] = s;
                    i3++;
                } else {
                    throw new OrtException("Output '" + s + "' was found in both the requested outputs and the pinned outputs");
                }
            } else {
                throw new OrtException("Unknown output name " + s + ", expected one of " + this.evalOutputNames.toString());
            }
        }
        long runOptionsHandle = runOptions == null ? 0L : runOptions.getNativeHandle();
        boolean[] ownedByResult = evalStep(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, this.allocator.handle, inputNamesArray, inputHandles, inputNamesArray.length, outputNamesArray, outputNamesArray.length, outputValues, outputHandles, runOptionsHandle);
        return new OrtSession.Result(outputNamesArray, outputValues, ownedByResult);
    }

    public void setLearningRate(float learningRate) throws OrtException {
        checkClosed();
        setLearningRate(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, learningRate);
    }

    public float getLearningRate() throws OrtException {
        checkClosed();
        return getLearningRate(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle);
    }

    public void optimizerStep() throws OrtException {
        optimizerStep(null);
    }

    public void optimizerStep(OrtSession.RunOptions runOptions) throws OrtException {
        checkClosed();
        long runOptionsHandle = runOptions == null ? 0L : runOptions.getNativeHandle();
        optimizerStep(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, runOptionsHandle);
    }

    public void registerLinearLRScheduler(long warmupSteps, long totalSteps, float initialLearningRate) throws OrtException {
        registerLinearLRScheduler(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, warmupSteps, totalSteps, initialLearningRate);
    }

    public void schedulerStep() throws OrtException {
        checkClosed();
        schedulerStep(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle);
    }

    public void exportModelForInference(Path outputPath, String[] outputNames) throws OrtException {
        checkClosed();
        if (outputNames.length == 0) {
            throw new IllegalArgumentException("Requires at least one output name");
        }
        String outputStr = outputPath.toString();
        exportModelForInference(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, outputStr, outputNames.length, outputNames);
    }

    static final class OrtCheckpointState implements AutoCloseable {
        private static final Logger logger = Logger.getLogger(OrtCheckpointState.class.getName());
        private boolean closed = false;
        final long nativeHandle;

        private native void addProperty(long j, long j2, long j3, String str, float f) throws OrtException;

        private native void addProperty(long j, long j2, long j3, String str, int i) throws OrtException;

        private native void addProperty(long j, long j2, long j3, String str, String str2) throws OrtException;

        private native void close(long j, long j2);

        private native float getFloatProperty(long j, long j2, long j3, long j4, String str) throws OrtException;

        private native int getIntProperty(long j, long j2, long j3, long j4, String str) throws OrtException;

        private native String getStringProperty(long j, long j2, long j3, long j4, String str) throws OrtException;

        private static native long loadCheckpoint(long j, long j2, String str) throws OrtException;

        private native void saveCheckpoint(long j, long j2, long j3, String str, boolean z) throws OrtException;

        OrtCheckpointState(long nativeHandle) {
            this.nativeHandle = nativeHandle;
        }

        static OrtCheckpointState loadCheckpoint(Path checkpointPath) throws OrtException {
            String pathStr = checkpointPath.toString();
            return loadCheckpoint(pathStr);
        }

        static OrtCheckpointState loadCheckpoint(String checkpoint) throws OrtException {
            if (OnnxRuntime.trainingEnabled) {
                Objects.requireNonNull(checkpoint, "checkpoint path must not be null");
                return new OrtCheckpointState(loadCheckpoint(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, checkpoint));
            }
            throw new IllegalStateException("Training is not enabled in this build of ONNX Runtime.");
        }

        public void saveCheckpoint(Path outputPath, boolean saveOptimizer) throws OrtException {
            checkClosed();
            Objects.requireNonNull(outputPath, "checkpoint path must not be null");
            String outputStr = outputPath.toString();
            saveCheckpoint(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, outputStr, saveOptimizer);
        }

        public void addProperty(String name, float value) throws OrtException {
            checkClosed();
            addProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, name, value);
        }

        public void addProperty(String name, int value) throws OrtException {
            checkClosed();
            addProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, name, value);
        }

        public void addProperty(String name, String value) throws OrtException {
            checkClosed();
            addProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, name, value);
        }

        public float getFloatProperty(OrtAllocator allocator, String name) throws OrtException {
            checkClosed();
            return getFloatProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, allocator.handle, name);
        }

        public int getIntProperty(OrtAllocator allocator, String name) throws OrtException {
            checkClosed();
            return getIntProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, allocator.handle, name);
        }

        public String getStringProperty(OrtAllocator allocator, String name) throws OrtException {
            checkClosed();
            return getStringProperty(OnnxRuntime.ortApiHandle, OnnxRuntime.ortTrainingApiHandle, this.nativeHandle, allocator.handle, name);
        }

        private void checkClosed() {
            if (this.closed) {
                throw new IllegalStateException("Trying to use a closed OrtCheckpointState");
            }
        }

        public synchronized boolean isClosed() {
            return this.closed;
        }

        @Override // java.lang.AutoCloseable
        public synchronized void close() {
            if (!this.closed) {
                close(OnnxRuntime.ortTrainingApiHandle, this.nativeHandle);
                this.closed = true;
            } else {
                logger.warning("Closing a checkpoint twice");
            }
        }
    }
}