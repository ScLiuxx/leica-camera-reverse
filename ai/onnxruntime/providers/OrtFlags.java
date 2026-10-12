package ai.onnxruntime.providers;

import java.util.EnumSet;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public interface OrtFlags {
    int getValue();

    static <E extends Enum<E> & OrtFlags> int aggregateToInt(EnumSet<E> set) {
        int value = 0;
        for (E flag : set) {
            value |= flag.getValue();
        }
        return value;
    }
}
