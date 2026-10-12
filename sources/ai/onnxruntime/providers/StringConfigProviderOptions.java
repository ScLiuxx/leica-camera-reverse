package ai.onnxruntime.providers;

import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProviderOptions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
abstract class StringConfigProviderOptions extends OrtProviderOptions {
    protected final Map<String, String> options;

    protected abstract void applyToNative(long j, long j2, String[] strArr, String[] strArr2) throws OrtException;

    protected StringConfigProviderOptions(long nativeHandle) {
        super(nativeHandle);
        this.options = new LinkedHashMap();
    }

    public void add(String key, String value) throws OrtException {
        checkClosed();
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");
        this.options.put(key, value);
    }

    public void parseOptionsString(String serializedForm) throws OrtException {
        String[] options = serializedForm.split(";");
        for (String o : options) {
            if (o.contains("=")) {
                String[] curOption = o.split("=");
                if (curOption.length == 2 && !curOption[0].isEmpty() && !curOption[1].isEmpty()) {
                    add(curOption[0], curOption[1]);
                } else {
                    throw new IllegalArgumentException("Failed to parse option from string '" + o + "'");
                }
            }
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + getOptionsString() + ")";
    }

    public String getOptionsString() {
        return (String) this.options.entrySet().stream().map(new Function() { // from class: ai.onnxruntime.providers.StringConfigProviderOptions$$ExternalSyntheticLambda0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return StringConfigProviderOptions.lambda$getOptionsString$0((Map.Entry) obj);
            }
        }).collect(Collectors.joining(";", "", ";"));
    }

    static /* synthetic */ String lambda$getOptionsString$0(Map.Entry e) {
        return ((String) e.getKey()) + "=" + ((String) e.getValue());
    }

    @Override // ai.onnxruntime.OrtProviderOptions
    protected void applyToNative() throws OrtException {
        if (!this.options.isEmpty()) {
            String[] keys = new String[this.options.size()];
            String[] values = new String[this.options.size()];
            int i = 0;
            for (Map.Entry<String, String> e : this.options.entrySet()) {
                keys[i] = e.getKey();
                values[i] = e.getValue();
                i++;
            }
            applyToNative(getApiHandle(), this.nativeHandle, keys, values);
        }
    }
}