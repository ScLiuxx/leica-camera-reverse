package ai.onnxruntime;

import java.io.IOException;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public abstract class OnnxTensorLike implements OnnxValue {
    protected final long allocatorHandle;
    protected boolean closed = false;
    protected final TensorInfo info;
    protected final long nativeHandle;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    OnnxTensorLike(long nativeHandle, long allocatorHandle, TensorInfo info) {
        this.nativeHandle = nativeHandle;
        this.allocatorHandle = allocatorHandle;
        this.info = info;
    }

    long getNativeHandle() {
        return this.nativeHandle;
    }

    @Override // ai.onnxruntime.OnnxValue
    public TensorInfo getInfo() {
        return this.info;
    }

    @Override // ai.onnxruntime.OnnxValue
    public synchronized boolean isClosed() {
        return this.closed;
    }

    protected void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Trying to use a closed OnnxValue");
        }
    }
}
