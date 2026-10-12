package ai.onnxruntime.providers;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public enum NNAPIFlags implements OrtFlags {
    USE_FP16(1),
    USE_NCHW(2),
    CPU_DISABLED(4),
    CPU_ONLY(8);

    public final int value;

    NNAPIFlags(int value) {
        this.value = value;
    }

    @Override // ai.onnxruntime.providers.OrtFlags
    public int getValue() {
        return this.value;
    }
}