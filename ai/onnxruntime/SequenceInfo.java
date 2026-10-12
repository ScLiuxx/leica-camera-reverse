package ai.onnxruntime;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class SequenceInfo implements ValueInfo {
    public final int length;
    public final MapInfo mapInfo;
    public final boolean sequenceOfMaps;
    public final OnnxJavaType sequenceType;

    SequenceInfo(int length, OnnxJavaType sequenceType) {
        this.length = length;
        this.sequenceType = sequenceType;
        this.sequenceOfMaps = false;
        this.mapInfo = null;
    }

    SequenceInfo(int length, int sequenceTypeInt) {
        this.length = length;
        this.sequenceType = OnnxJavaType.mapFromOnnxTensorType(TensorInfo.OnnxTensorType.mapFromInt(sequenceTypeInt));
        this.sequenceOfMaps = false;
        this.mapInfo = null;
    }

    SequenceInfo(int length, MapInfo mapInfo) {
        this.length = length;
        this.sequenceOfMaps = true;
        this.mapInfo = mapInfo;
        this.sequenceType = OnnxJavaType.UNKNOWN;
    }

    SequenceInfo(int length, OnnxJavaType keyType, OnnxJavaType valueType) {
        this.length = length;
        this.sequenceType = OnnxJavaType.UNKNOWN;
        this.sequenceOfMaps = true;
        this.mapInfo = new MapInfo(keyType, valueType);
    }

    public boolean isSequenceOfMaps() {
        return this.sequenceOfMaps;
    }

    public String toString() {
        StringBuilder sbAppend = new StringBuilder().append("SequenceInfo(length=");
        int i = this.length;
        String initial = sbAppend.append(i == -1 ? "UNKNOWN" : Integer.valueOf(i)).toString();
        if (this.sequenceOfMaps) {
            return initial + ",type=" + this.mapInfo.toString() + ")";
        }
        return initial + ",type=" + this.sequenceType.toString() + ")";
    }
}
