package ai.onnxruntime.providers;

import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtProviderOptions;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OrtTensorRTProviderOptions extends StringConfigProviderOptions {
    private static final OrtProvider PROVIDER = OrtProvider.TENSOR_RT;

    /* JADX INFO: Access modifiers changed from: private */
    public static native long create(long j) throws OrtException;

    @Override // ai.onnxruntime.providers.StringConfigProviderOptions
    protected native void applyToNative(long j, long j2, String[] strArr, String[] strArr2) throws OrtException;

    @Override // ai.onnxruntime.OrtProviderOptions
    protected native void close(long j, long j2);

    @Override // ai.onnxruntime.providers.StringConfigProviderOptions
    public /* bridge */ /* synthetic */ void add(String str, String str2) throws OrtException {
        super.add(str, str2);
    }

    @Override // ai.onnxruntime.providers.StringConfigProviderOptions
    public /* bridge */ /* synthetic */ String getOptionsString() {
        return super.getOptionsString();
    }

    @Override // ai.onnxruntime.providers.StringConfigProviderOptions
    public /* bridge */ /* synthetic */ void parseOptionsString(String str) throws OrtException {
        super.parseOptionsString(str);
    }

    @Override // ai.onnxruntime.providers.StringConfigProviderOptions
    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public OrtTensorRTProviderOptions() throws OrtException {
        this(0);
    }

    public OrtTensorRTProviderOptions(int deviceId) throws OrtException {
        super(loadLibraryAndCreate(PROVIDER, new OrtProviderOptions.OrtProviderSupplier() { // from class: ai.onnxruntime.providers.OrtTensorRTProviderOptions$$ExternalSyntheticLambda0
            @Override // ai.onnxruntime.OrtProviderOptions.OrtProviderSupplier
            public final long create() {
                return OrtTensorRTProviderOptions.create(OrtTensorRTProviderOptions.getApiHandle());
            }
        }));
        if (deviceId < 0) {
            close();
            throw new IllegalArgumentException("Device id must be non-negative, received " + deviceId);
        }
        String id = "" + deviceId;
        this.options.put("device_id", id);
    }

    @Override // ai.onnxruntime.OrtProviderOptions
    public OrtProvider getProvider() {
        return PROVIDER;
    }
}
