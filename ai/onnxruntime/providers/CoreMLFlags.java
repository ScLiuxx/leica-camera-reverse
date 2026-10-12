package ai.onnxruntime.providers;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public enum CoreMLFlags implements OrtFlags {
    CPU_ONLY(1),
    ENABLE_ON_SUBGRAPH(2),
    ONLY_ENABLE_DEVICE_WITH_ANE(4),
    ONLY_ALLOW_STATIC_INPUT_SHAPES(8),
    CREATE_MLPROGRAM(16),
    CPU_AND_GPU(32);

    public final int value;

    CoreMLFlags(int value) {
        this.value = value;
    }

    @Override // ai.onnxruntime.providers.OrtFlags
    public int getValue() {
        return this.value;
    }
}
