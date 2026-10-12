package ai.onnxruntime;

import ai.onnxruntime.providers.CoreMLFlags;
import ai.onnxruntime.providers.NNAPIFlags;
import ai.onnxruntime.providers.OrtCUDAProviderOptions;
import ai.onnxruntime.providers.OrtFlags;
import ai.onnxruntime.providers.OrtTensorRTProviderOptions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class OrtSession implements AutoCloseable {
    private final OrtAllocator allocator;
    private boolean closed;
    private final Set<String> inputNames;
    private OnnxModelMetadata metadata;
    private final long nativeHandle;
    private final long numInputs;
    private final long numOutputs;
    private final Set<String> outputNames;

    private native void closeSession(long j, long j2) throws OrtException;

    private native OnnxModelMetadata constructMetadata(long j, long j2, long j3) throws OrtException;

    private static native long createSession(long j, long j2, String str, long j3) throws OrtException;

    private static native long createSession(long j, long j2, ByteBuffer byteBuffer, int i, int i2, long j3) throws OrtException;

    private static native long createSession(long j, long j2, byte[] bArr, long j3) throws OrtException;

    private native String endProfiling(long j, long j2, long j3) throws OrtException;

    private native NodeInfo[] getInputInfo(long j, long j2, long j3) throws OrtException;

    private native String[] getInputNames(long j, long j2, long j3) throws OrtException;

    private native long getNumInputs(long j, long j2) throws OrtException;

    private native long getNumOutputs(long j, long j2) throws OrtException;

    private native NodeInfo[] getOutputInfo(long j, long j2, long j3) throws OrtException;

    private native String[] getOutputNames(long j, long j2, long j3) throws OrtException;

    private native long getProfilingStartTimeInNs(long j, long j2) throws OrtException;

    private native boolean[] run(long j, long j2, long j3, String[] strArr, long[] jArr, long j4, String[] strArr2, long j5, OnnxValue[] onnxValueArr, long[] jArr2, long j6) throws OrtException;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    OrtSession(OrtEnvironment env, String modelPath, OrtAllocator allocator, SessionOptions options) throws OrtException {
        this(createSession(OnnxRuntime.ortApiHandle, env.getNativeHandle(), modelPath, options.getNativeHandle()), allocator);
    }

    OrtSession(OrtEnvironment env, byte[] modelArray, OrtAllocator allocator, SessionOptions options) throws OrtException {
        this(createSession(OnnxRuntime.ortApiHandle, env.getNativeHandle(), modelArray, options.getNativeHandle()), allocator);
    }

    OrtSession(OrtEnvironment env, ByteBuffer modelBuffer, OrtAllocator allocator, SessionOptions options) throws OrtException {
        this(createSession(OnnxRuntime.ortApiHandle, env.getNativeHandle(), modelBuffer, modelBuffer.position(), modelBuffer.remaining(), options.getNativeHandle()), allocator);
    }

    private OrtSession(long nativeHandle, OrtAllocator allocator) throws OrtException {
        this.closed = false;
        this.nativeHandle = nativeHandle;
        this.allocator = allocator;
        this.numInputs = getNumInputs(OnnxRuntime.ortApiHandle, nativeHandle);
        this.inputNames = new LinkedHashSet(Arrays.asList(getInputNames(OnnxRuntime.ortApiHandle, nativeHandle, allocator.handle)));
        this.numOutputs = getNumOutputs(OnnxRuntime.ortApiHandle, nativeHandle);
        this.outputNames = new LinkedHashSet(Arrays.asList(getOutputNames(OnnxRuntime.ortApiHandle, nativeHandle, allocator.handle)));
    }

    public long getNumInputs() {
        if (!this.closed) {
            return this.numInputs;
        }
        throw new IllegalStateException("Asking for inputs from a closed OrtSession.");
    }

    public long getNumOutputs() {
        if (!this.closed) {
            return this.numOutputs;
        }
        throw new IllegalStateException("Asking for outputs from a closed OrtSession.");
    }

    public Set<String> getInputNames() {
        if (!this.closed) {
            return this.inputNames;
        }
        throw new IllegalStateException("Asking for inputs from a closed OrtSession.");
    }

    public Set<String> getOutputNames() {
        if (!this.closed) {
            return this.outputNames;
        }
        throw new IllegalStateException("Asking for outputs from a closed OrtSession.");
    }

    public Map<String, NodeInfo> getInputInfo() throws OrtException {
        if (!this.closed) {
            return wrapInMap(getInputInfo(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocator.handle));
        }
        throw new IllegalStateException("Asking for inputs from a closed OrtSession.");
    }

    public Map<String, NodeInfo> getOutputInfo() throws OrtException {
        if (!this.closed) {
            return wrapInMap(getOutputInfo(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocator.handle));
        }
        throw new IllegalStateException("Asking for outputs from a closed OrtSession.");
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs) throws OrtException {
        return run(inputs, this.outputNames);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, RunOptions runOptions) throws OrtException {
        return run(inputs, this.outputNames, runOptions);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs) throws OrtException {
        return run(inputs, requestedOutputs, Collections.emptyMap(), null);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs, RunOptions runOptions) throws OrtException {
        return run(inputs, requestedOutputs, Collections.emptyMap(), runOptions);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, Map<String, ? extends OnnxValue> pinnedOutputs) throws OrtException {
        return run(inputs, Collections.emptySet(), pinnedOutputs, null);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs, Map<String, ? extends OnnxValue> pinnedOutputs) throws OrtException {
        return run(inputs, requestedOutputs, pinnedOutputs, null);
    }

    public Result run(Map<String, ? extends OnnxTensorLike> inputs, Set<String> requestedOutputs, Map<String, ? extends OnnxValue> pinnedOutputs, RunOptions runOptions) throws OrtException {
        if (this.closed) {
            throw new IllegalStateException("Trying to score a closed OrtSession.");
        }
        if ((inputs.isEmpty() && this.numInputs != 0) || inputs.size() > this.numInputs) {
            throw new OrtException("Unexpected number of inputs, expected [1," + this.numInputs + ") found " + inputs.size());
        }
        int totalOutputs = requestedOutputs.size() + pinnedOutputs.size();
        if (totalOutputs == 0 || totalOutputs > this.numOutputs) {
            throw new OrtException("Unexpected number of requestedOutputs & pinnedOutputs, expected [1," + this.numOutputs + ") found " + totalOutputs);
        }
        String[] inputNamesArray = new String[inputs.size()];
        long[] inputHandles = new long[inputs.size()];
        int i = 0;
        for (Map.Entry<String, ? extends OnnxTensorLike> t : inputs.entrySet()) {
            if (this.inputNames.contains(t.getKey())) {
                inputNamesArray[i] = t.getKey();
                inputHandles[i] = t.getValue().getNativeHandle();
                i++;
            } else {
                throw new OrtException("Unknown input name " + t.getKey() + ", expected one of " + this.inputNames.toString());
            }
        }
        String[] outputNamesArray = new String[requestedOutputs.size() + pinnedOutputs.size()];
        OnnxValue[] outputValues = new OnnxValue[outputNamesArray.length];
        long[] outputHandles = new long[outputNamesArray.length];
        int i2 = 0;
        for (Map.Entry<String, ? extends OnnxValue> e : pinnedOutputs.entrySet()) {
            if (this.outputNames.contains(e.getKey())) {
                outputNamesArray[i2] = e.getKey();
                outputValues[i2] = e.getValue();
                outputHandles[i2] = getHandle(e.getValue());
                i2++;
            } else {
                throw new OrtException("Unknown output name " + e.getKey() + ", expected one of " + this.outputNames.toString());
            }
        }
        int i3 = i2;
        for (String s : requestedOutputs) {
            if (this.outputNames.contains(s)) {
                if (!pinnedOutputs.containsKey(s)) {
                    outputNamesArray[i3] = s;
                    i3++;
                } else {
                    throw new OrtException("Output '" + s + "' was found in both the requested outputs and the pinned outputs");
                }
            } else {
                throw new OrtException("Unknown output name " + s + ", expected one of " + this.outputNames.toString());
            }
        }
        long runOptionsHandle = runOptions == null ? 0L : runOptions.getNativeHandle();
        boolean[] ownedByResult = run(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocator.handle, inputNamesArray, inputHandles, inputNamesArray.length, outputNamesArray, outputNamesArray.length, outputValues, outputHandles, runOptionsHandle);
        return new Result(outputNamesArray, outputValues, ownedByResult);
    }

    static long getHandle(OnnxValue v) {
        if (v instanceof OnnxTensorLike) {
            return ((OnnxTensorLike) v).nativeHandle;
        }
        if (v instanceof OnnxSequence) {
            return ((OnnxSequence) v).nativeHandle;
        }
        if (v instanceof OnnxMap) {
            return ((OnnxMap) v).nativeHandle;
        }
        throw new IllegalArgumentException("Unexpected OnnxValue subclass, should be {OnnxTensorLike, OnnxSequence, OnnxMap}, found " + v.getClass());
    }

    public OnnxModelMetadata getMetadata() throws OrtException {
        if (this.metadata == null) {
            this.metadata = constructMetadata(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocator.handle);
        }
        return this.metadata;
    }

    public long getProfilingStartTimeInNs() throws OrtException {
        return getProfilingStartTimeInNs(OnnxRuntime.ortApiHandle, this.nativeHandle);
    }

    public String endProfiling() throws OrtException {
        return endProfiling(OnnxRuntime.ortApiHandle, this.nativeHandle, this.allocator.handle);
    }

    public String toString() {
        return "OrtSession(numInputs=" + this.numInputs + ",numOutputs=" + this.numOutputs + ")";
    }

    @Override // java.lang.AutoCloseable
    public void close() throws OrtException {
        if (!this.closed) {
            closeSession(OnnxRuntime.ortApiHandle, this.nativeHandle);
            this.closed = true;
            return;
        }
        throw new IllegalStateException("Trying to close an already closed OrtSession.");
    }

    private static Map<String, NodeInfo> wrapInMap(NodeInfo[] infos) {
        Map<String, NodeInfo> output = new LinkedHashMap<>(OrtUtil.capacityFromSize(infos.length));
        for (NodeInfo info : infos) {
            output.put(info.getName(), info);
        }
        return output;
    }

    public static class SessionOptions implements AutoCloseable {
        private boolean closed = false;
        private final long nativeHandle = createOptions(OnnxRuntime.ortApiHandle);
        private final List<Long> customLibraryHandles = new ArrayList();
        private final Map<String, String> configEntries = new LinkedHashMap();

        private native void addACL(long j, long j2, boolean z) throws OrtException;

        private native void addArmNN(long j, long j2, int i) throws OrtException;

        private native void addCPU(long j, long j2, int i) throws OrtException;

        private native void addCUDA(long j, long j2, int i) throws OrtException;

        private native void addCUDAV2(long j, long j2, long j3) throws OrtException;

        private native void addConfigEntry(long j, long j2, String str, String str2) throws OrtException;

        private native void addCoreML(long j, long j2, int i) throws OrtException;

        private native void addDirectML(long j, long j2, int i) throws OrtException;

        private native void addDnnl(long j, long j2, int i) throws OrtException;

        private native void addExecutionProvider(long j, long j2, String str, String[] strArr, String[] strArr2) throws OrtException;

        private native void addExternalInitializers(long j, long j2, String[] strArr, long[] jArr) throws OrtException;

        private native void addFreeDimensionOverrideByName(long j, long j2, String str, long j3) throws OrtException;

        private native void addInitializer(long j, long j2, String str, long j3) throws OrtException;

        private native void addNnapi(long j, long j2, int i) throws OrtException;

        private native void addOpenVINO(long j, long j2, String str) throws OrtException;

        private native void addROCM(long j, long j2, int i) throws OrtException;

        private native void addTensorrt(long j, long j2, int i) throws OrtException;

        private native void addTensorrtV2(long j, long j2, long j3) throws OrtException;

        private native void addTvm(long j, long j2, String str) throws OrtException;

        private native void closeCustomLibraries(long[] jArr);

        private native void closeOptions(long j, long j2);

        private native long createOptions(long j);

        private native void disablePerSessionThreads(long j, long j2) throws OrtException;

        private native void disableProfiling(long j, long j2) throws OrtException;

        private native void enableProfiling(long j, long j2, String str) throws OrtException;

        private native long registerCustomOpLibrary(long j, long j2, String str) throws OrtException;

        private native void registerCustomOpsUsingFunction(long j, long j2, String str) throws OrtException;

        private native void setCPUArenaAllocator(long j, long j2, boolean z) throws OrtException;

        private native void setDeterministicCompute(long j, long j2, boolean z) throws OrtException;

        private native void setExecutionMode(long j, long j2, int i) throws OrtException;

        private native void setInterOpNumThreads(long j, long j2, int i) throws OrtException;

        private native void setIntraOpNumThreads(long j, long j2, int i) throws OrtException;

        private native void setLoggerId(long j, long j2, String str) throws OrtException;

        private native void setMemoryPatternOptimization(long j, long j2, boolean z) throws OrtException;

        private native void setOptimizationLevel(long j, long j2, int i) throws OrtException;

        private native void setOptimizationModelFilePath(long j, long j2, String str) throws OrtException;

        private native void setSessionLogLevel(long j, long j2, int i) throws OrtException;

        private native void setSessionLogVerbosityLevel(long j, long j2, int i) throws OrtException;

        public enum OptLevel {
            NO_OPT(0),
            BASIC_OPT(1),
            EXTENDED_OPT(2),
            ALL_OPT(99);

            private final int id;

            OptLevel(int id) {
                this.id = id;
            }

            public int getID() {
                return this.id;
            }
        }

        public enum ExecutionMode {
            SEQUENTIAL(0),
            PARALLEL(1);

            private final int id;

            ExecutionMode(int id) {
                this.id = id;
            }

            public int getID() {
                return this.id;
            }
        }

        static {
            try {
                OnnxRuntime.init();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load onnx-runtime library", e);
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (!this.closed) {
                if (this.customLibraryHandles.size() > 0) {
                    long[] longArray = new long[this.customLibraryHandles.size()];
                    for (int i = 0; i < this.customLibraryHandles.size(); i++) {
                        longArray[i] = this.customLibraryHandles.get(i).longValue();
                    }
                    closeCustomLibraries(longArray);
                }
                closeOptions(OnnxRuntime.ortApiHandle, this.nativeHandle);
                this.closed = true;
                return;
            }
            throw new IllegalStateException("Trying to close a closed SessionOptions.");
        }

        private void checkClosed() {
            if (this.closed) {
                throw new IllegalStateException("Trying to use a closed SessionOptions");
            }
        }

        long getNativeHandle() {
            return this.nativeHandle;
        }

        public void setExecutionMode(ExecutionMode mode) throws OrtException {
            checkClosed();
            setExecutionMode(OnnxRuntime.ortApiHandle, this.nativeHandle, mode.getID());
        }

        public void setOptimizationLevel(OptLevel level) throws OrtException {
            checkClosed();
            setOptimizationLevel(OnnxRuntime.ortApiHandle, this.nativeHandle, level.getID());
        }

        public void setInterOpNumThreads(int numThreads) throws OrtException {
            checkClosed();
            setInterOpNumThreads(OnnxRuntime.ortApiHandle, this.nativeHandle, numThreads);
        }

        public void setIntraOpNumThreads(int numThreads) throws OrtException {
            checkClosed();
            setIntraOpNumThreads(OnnxRuntime.ortApiHandle, this.nativeHandle, numThreads);
        }

        public void setOptimizedModelFilePath(String outputPath) throws OrtException {
            checkClosed();
            setOptimizationModelFilePath(OnnxRuntime.ortApiHandle, this.nativeHandle, outputPath);
        }

        public void setLoggerId(String loggerId) throws OrtException {
            checkClosed();
            setLoggerId(OnnxRuntime.ortApiHandle, this.nativeHandle, loggerId);
        }

        public void enableProfiling(String filePath) throws OrtException {
            checkClosed();
            enableProfiling(OnnxRuntime.ortApiHandle, this.nativeHandle, filePath);
        }

        public void disableProfiling() throws OrtException {
            checkClosed();
            disableProfiling(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }

        public void setMemoryPatternOptimization(boolean memoryPatternOptimization) throws OrtException {
            checkClosed();
            setMemoryPatternOptimization(OnnxRuntime.ortApiHandle, this.nativeHandle, memoryPatternOptimization);
        }

        public void setCPUArenaAllocator(boolean useArena) throws OrtException {
            checkClosed();
            setCPUArenaAllocator(OnnxRuntime.ortApiHandle, this.nativeHandle, useArena);
        }

        public void setSessionLogLevel(OrtLoggingLevel logLevel) throws OrtException {
            checkClosed();
            setSessionLogLevel(OnnxRuntime.ortApiHandle, this.nativeHandle, logLevel.getValue());
        }

        public void setSessionLogVerbosityLevel(int logLevel) throws OrtException {
            checkClosed();
            setSessionLogVerbosityLevel(OnnxRuntime.ortApiHandle, this.nativeHandle, logLevel);
        }

        public void registerCustomOpLibrary(String path) throws OrtException {
            checkClosed();
            Objects.requireNonNull(path, "path must not be null");
            long customHandle = registerCustomOpLibrary(OnnxRuntime.ortApiHandle, this.nativeHandle, path);
            this.customLibraryHandles.add(Long.valueOf(customHandle));
        }

        public void registerCustomOpsUsingFunction(String registrationFuncName) throws OrtException {
            checkClosed();
            registerCustomOpsUsingFunction(OnnxRuntime.ortApiHandle, this.nativeHandle, registrationFuncName);
        }

        public void setSymbolicDimensionValue(String dimensionName, long dimensionValue) throws OrtException {
            checkClosed();
            addFreeDimensionOverrideByName(OnnxRuntime.ortApiHandle, this.nativeHandle, dimensionName, dimensionValue);
        }

        public void setDeterministicCompute(boolean value) throws OrtException {
            checkClosed();
            setDeterministicCompute(OnnxRuntime.ortApiHandle, this.nativeHandle, value);
        }

        public void disablePerSessionThreads() throws OrtException {
            checkClosed();
            disablePerSessionThreads(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }

        public void addConfigEntry(String configKey, String configValue) throws OrtException {
            checkClosed();
            addConfigEntry(OnnxRuntime.ortApiHandle, this.nativeHandle, configKey, configValue);
            this.configEntries.put(configKey, configValue);
        }

        public Map<String, String> getConfigEntries() {
            checkClosed();
            return Collections.unmodifiableMap(this.configEntries);
        }

        public void addExternalInitializers(Map<String, OnnxTensorLike> initializers) throws OrtException {
            checkClosed();
            if (initializers.isEmpty()) {
                return;
            }
            String[] names = new String[initializers.size()];
            long[] handles = new long[initializers.size()];
            int i = 0;
            for (Map.Entry<String, OnnxTensorLike> e : initializers.entrySet()) {
                names[i] = e.getKey();
                handles[i] = e.getValue().nativeHandle;
                i++;
            }
            addExternalInitializers(OnnxRuntime.ortApiHandle, this.nativeHandle, names, handles);
        }

        public void addInitializer(String name, OnnxTensorLike initializer) throws OrtException {
            checkClosed();
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Initializer name was blank");
            }
            addInitializer(OnnxRuntime.ortApiHandle, this.nativeHandle, name, initializer.getNativeHandle());
        }

        public void addCUDA() throws OrtException {
            addCUDA(0);
        }

        public void addCUDA(int deviceNum) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractCUDA()) {
                addCUDA(OnnxRuntime.ortApiHandle, this.nativeHandle, deviceNum);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find CUDA shared provider");
        }

        public void addCUDA(OrtCUDAProviderOptions cudaOpts) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractCUDA()) {
                cudaOpts.applyToNative();
                addCUDAV2(OnnxRuntime.ortApiHandle, this.nativeHandle, cudaOpts.nativeHandle);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find CUDA shared provider");
        }

        public void addROCM() throws OrtException {
            addROCM(0);
        }

        public void addROCM(int deviceNum) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractROCM()) {
                addROCM(OnnxRuntime.ortApiHandle, this.nativeHandle, deviceNum);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find ROCM shared provider");
        }

        public void addCPU(boolean z) throws OrtException {
            checkClosed();
            addCPU(OnnxRuntime.ortApiHandle, this.nativeHandle, z ? 1 : 0);
        }

        public void addDnnl(boolean z) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractDNNL()) {
                addDnnl(OnnxRuntime.ortApiHandle, this.nativeHandle, z ? 1 : 0);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find DNNL shared provider");
        }

        public void addOpenVINO(String deviceId) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractOpenVINO()) {
                addOpenVINO(OnnxRuntime.ortApiHandle, this.nativeHandle, deviceId);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find OpenVINO shared provider");
        }

        public void addTensorrt(int deviceNum) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractTensorRT()) {
                addTensorrt(OnnxRuntime.ortApiHandle, this.nativeHandle, deviceNum);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find TensorRT shared provider");
        }

        public void addTensorrt(OrtTensorRTProviderOptions tensorRTOpts) throws OrtException {
            checkClosed();
            if (OnnxRuntime.extractTensorRT()) {
                tensorRTOpts.applyToNative();
                addTensorrtV2(OnnxRuntime.ortApiHandle, this.nativeHandle, tensorRTOpts.nativeHandle);
                return;
            }
            throw new OrtException(OrtException.OrtErrorCode.ORT_EP_FAIL, "Failed to find TensorRT shared provider");
        }

        public void addNnapi() throws OrtException {
            addNnapi(EnumSet.noneOf(NNAPIFlags.class));
        }

        public void addNnapi(EnumSet<NNAPIFlags> flags) throws OrtException {
            checkClosed();
            addNnapi(OnnxRuntime.ortApiHandle, this.nativeHandle, OrtFlags.aggregateToInt(flags));
        }

        public void addTvm(String settings) throws OrtException {
            checkClosed();
            addTvm(OnnxRuntime.ortApiHandle, this.nativeHandle, settings);
        }

        public void addDirectML(int deviceId) throws OrtException {
            checkClosed();
            addDirectML(OnnxRuntime.ortApiHandle, this.nativeHandle, deviceId);
        }

        public void addACL(boolean enableFastMath) throws OrtException {
            checkClosed();
            addACL(OnnxRuntime.ortApiHandle, this.nativeHandle, enableFastMath);
        }

        public void addArmNN(boolean z) throws OrtException {
            checkClosed();
            addArmNN(OnnxRuntime.ortApiHandle, this.nativeHandle, z ? 1 : 0);
        }

        public void addCoreML() throws OrtException {
            addCoreML(EnumSet.noneOf(CoreMLFlags.class));
        }

        public void addCoreML(EnumSet<CoreMLFlags> flags) throws OrtException {
            checkClosed();
            addCoreML(OnnxRuntime.ortApiHandle, this.nativeHandle, OrtFlags.aggregateToInt(flags));
        }

        private void addExecutionProvider(String providerName, Map<String, String> providerOptions) throws OrtException {
            checkClosed();
            String[] providerOptionKey = new String[providerOptions.size()];
            String[] providerOptionVal = new String[providerOptions.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : providerOptions.entrySet()) {
                providerOptionKey[i] = entry.getKey();
                providerOptionVal[i] = entry.getValue();
                i++;
            }
            addExecutionProvider(OnnxRuntime.ortApiHandle, this.nativeHandle, providerName, providerOptionKey, providerOptionVal);
        }

        public void addXnnpack(Map<String, String> providerOptions) throws OrtException {
            addExecutionProvider("XNNPACK", providerOptions);
        }

        public void addQnn(Map<String, String> providerOptions) throws OrtException {
            OnnxRuntime.extractQNN();
            addExecutionProvider("QNN", providerOptions);
        }

        public void addCoreML(Map<String, String> providerOptions) throws OrtException {
            addExecutionProvider("CoreML", providerOptions);
        }

        public void addWebGPU(Map<String, String> providerOptions) throws OrtException {
            addExecutionProvider("WebGPU", providerOptions);
        }
    }

    public static class RunOptions implements AutoCloseable {
        private boolean closed = false;
        private final long nativeHandle = createRunOptions(OnnxRuntime.ortApiHandle);

        private native void addActiveLoraAdapter(long j, long j2, long j3) throws OrtException;

        private native void addRunConfigEntry(long j, long j2, String str, String str2) throws OrtException;

        private static native void close(long j, long j2);

        private static native long createRunOptions(long j) throws OrtException;

        private native int getLogLevel(long j, long j2) throws OrtException;

        private native int getLogVerbosityLevel(long j, long j2) throws OrtException;

        private native String getRunTag(long j, long j2) throws OrtException;

        private native void setLogLevel(long j, long j2, int i) throws OrtException;

        private native void setLogVerbosityLevel(long j, long j2, int i) throws OrtException;

        private native void setRunTag(long j, long j2, String str) throws OrtException;

        private native void setTerminate(long j, long j2, boolean z) throws OrtException;

        static {
            try {
                OnnxRuntime.init();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load onnx-runtime library", e);
            }
        }

        long getNativeHandle() {
            return this.nativeHandle;
        }

        public void setLogLevel(OrtLoggingLevel level) throws OrtException {
            checkClosed();
            setLogLevel(OnnxRuntime.ortApiHandle, this.nativeHandle, level.getValue());
        }

        public OrtLoggingLevel getLogLevel() throws OrtException {
            checkClosed();
            return OrtLoggingLevel.mapFromInt(getLogLevel(OnnxRuntime.ortApiHandle, this.nativeHandle));
        }

        public void setLogVerbosityLevel(int level) throws OrtException {
            checkClosed();
            setLogVerbosityLevel(OnnxRuntime.ortApiHandle, this.nativeHandle, level);
        }

        public int getLogVerbosityLevel() throws OrtException {
            checkClosed();
            return getLogVerbosityLevel(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }

        public void setRunTag(String runTag) throws OrtException {
            checkClosed();
            setRunTag(OnnxRuntime.ortApiHandle, this.nativeHandle, runTag);
        }

        public String getRunTag() throws OrtException {
            checkClosed();
            return getRunTag(OnnxRuntime.ortApiHandle, this.nativeHandle);
        }

        public void setTerminate(boolean terminate) throws OrtException {
            checkClosed();
            setTerminate(OnnxRuntime.ortApiHandle, this.nativeHandle, terminate);
        }

        public void addRunConfigEntry(String key, String value) throws OrtException {
            checkClosed();
            addRunConfigEntry(OnnxRuntime.ortApiHandle, this.nativeHandle, key, value);
        }

        public void addActiveLoraAdapter(OrtLoraAdapter loraAdapter) throws OrtException {
            checkClosed();
            loraAdapter.checkClosed();
            addActiveLoraAdapter(OnnxRuntime.ortApiHandle, this.nativeHandle, loraAdapter.getNativeHandle());
        }

        private void checkClosed() {
            if (this.closed) {
                throw new IllegalStateException("Trying to use a closed RunOptions");
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (!this.closed) {
                close(OnnxRuntime.ortApiHandle, this.nativeHandle);
                this.closed = true;
                return;
            }
            throw new IllegalStateException("Trying to close an already closed RunOptions");
        }
    }

    public static class Result implements AutoCloseable, Iterable<Map.Entry<String, OnnxValue>> {
        private static final Logger logger = Logger.getLogger(Result.class.getName());
        private boolean closed;
        private final List<OnnxValue> list;
        private final Map<String, OnnxValue> map;
        private final boolean[] ownedByResult;

        Result(String[] names, OnnxValue[] values, boolean[] ownedByResult) {
            if (names.length != values.length || names.length != ownedByResult.length) {
                throw new IllegalArgumentException("Expected same number of names, values and ownedByResult, found names.length = " + names.length + ", values.length = " + values.length + ", ownedByResult.length = " + ownedByResult.length);
            }
            this.map = new LinkedHashMap(OrtUtil.capacityFromSize(names.length));
            this.list = new ArrayList(Arrays.asList(values));
            this.ownedByResult = ownedByResult;
            for (int i = 0; i < names.length; i++) {
                this.map.put(names[i], values[i]);
            }
            this.closed = false;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (!this.closed) {
                this.closed = true;
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.ownedByResult[i]) {
                        OnnxValue value = this.list.get(i);
                        value.close();
                    }
                }
                return;
            }
            logger.warning("Closing an already closed Result");
        }

        @Override // java.lang.Iterable
        public Iterator<Map.Entry<String, OnnxValue>> iterator() {
            if (!this.closed) {
                return this.map.entrySet().iterator();
            }
            throw new IllegalStateException("Result is closed");
        }

        public OnnxValue get(int index) {
            if (!this.closed) {
                return this.list.get(index);
            }
            throw new IllegalStateException("Result is closed");
        }

        public boolean isResultOwner(int index) {
            if (!this.closed) {
                return this.ownedByResult[index];
            }
            throw new IllegalStateException("Result is closed");
        }

        public int size() {
            return this.map.size();
        }

        public Optional<OnnxValue> get(String key) {
            if (!this.closed) {
                OnnxValue value = this.map.get(key);
                if (value != null) {
                    return Optional.of(value);
                }
                return Optional.empty();
            }
            throw new IllegalStateException("Result is closed");
        }
    }
}