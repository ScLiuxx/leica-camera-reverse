package ai.onnxruntime;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class MapInfo implements ValueInfo {
    public final OnnxJavaType keyType;
    public final int size;
    public final OnnxJavaType valueType;

    MapInfo(OnnxJavaType keyType, OnnxJavaType valueType) {
        this.size = -1;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    MapInfo(int size, OnnxJavaType keyType, OnnxJavaType valueType) {
        this.size = size;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    MapInfo(int size, int keyTypeInt, int valueTypeInt) {
        this.size = size;
        this.keyType = OnnxJavaType.mapFromOnnxTensorType(TensorInfo.OnnxTensorType.mapFromInt(keyTypeInt));
        this.valueType = OnnxJavaType.mapFromOnnxTensorType(TensorInfo.OnnxTensorType.mapFromInt(valueTypeInt));
    }

    public String toString() {
        String initial = this.size == -1 ? "MapInfo(size=UNKNOWN" : "MapInfo(size=" + this.size;
        return initial + ",keyType=" + this.keyType.toString() + ",valueType=" + this.valueType.toString() + ")";
    }
}
