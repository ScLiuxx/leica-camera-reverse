package ai.onnxruntime;

import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public enum OrtProvider {
    CPU("CPUExecutionProvider"),
    CUDA("CUDAExecutionProvider"),
    DNNL("DnnlExecutionProvider"),
    OPEN_VINO("OpenVINOExecutionProvider"),
    VITIS_AI("VitisAIExecutionProvider"),
    TENSOR_RT("TensorrtExecutionProvider"),
    NNAPI("NnapiExecutionProvider"),
    RK_NPU("RknpuExecutionProvider"),
    DIRECT_ML("DmlExecutionProvider"),
    MI_GRAPH_X("MIGraphXExecutionProvider"),
    ACL("ACLExecutionProvider"),
    ARM_NN("ArmNNExecutionProvider"),
    ROCM("ROCMExecutionProvider"),
    CORE_ML("CoreMLExecutionProvider"),
    XNNPACK("XnnpackExecutionProvider"),
    AZURE("AzureExecutionProvider"),
    QNN("QNNExecutionProvider"),
    WEBGPU("WebGpuExecutionProvider");

    private static final Map<String, OrtProvider> valueMap = new HashMap(values().length);
    private final String name;

    static {
        for (OrtProvider p : values()) {
            valueMap.put(p.name, p);
        }
    }

    OrtProvider(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static OrtProvider mapFromName(String name) {
        OrtProvider provider = valueMap.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown execution provider - " + name);
        }
        return provider;
    }
}
