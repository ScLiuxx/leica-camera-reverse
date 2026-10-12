package ai.onnxruntime;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public class NodeInfo {
    private final ValueInfo info;
    private final String name;

    public NodeInfo(String name, ValueInfo info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return this.name;
    }

    public ValueInfo getInfo() {
        return this.info;
    }

    public String toString() {
        return "NodeInfo(name=" + this.name + ",info=" + this.info.toString() + ")";
    }
}
