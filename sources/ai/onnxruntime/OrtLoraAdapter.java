package ai.onnxruntime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class OrtLoraAdapter implements AutoCloseable {
    private boolean closed = false;
    private final long nativeHandle;

    private static native void close(long j, long j2);

    private static native long createLoraAdapter(long j, String str, long j2) throws OrtException;

    private static native long createLoraAdapterFromArray(long j, byte[] bArr, long j2) throws OrtException;

    private static native long createLoraAdapterFromBuffer(long j, ByteBuffer byteBuffer, int i, int i2, long j2) throws OrtException;

    static {
        try {
            OnnxRuntime.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load onnx-runtime library", e);
        }
    }

    private OrtLoraAdapter(long nativeHandle) {
        this.nativeHandle = nativeHandle;
    }

    public static OrtLoraAdapter create(byte[] loraArray) throws OrtException {
        return create(loraArray, (OrtAllocator) null);
    }

    static OrtLoraAdapter create(byte[] loraArray, OrtAllocator allocator) throws OrtException {
        Objects.requireNonNull(loraArray, "LoRA array must not be null");
        long allocatorHandle = allocator == null ? 0L : allocator.handle;
        return new OrtLoraAdapter(createLoraAdapterFromArray(OnnxRuntime.ortApiHandle, loraArray, allocatorHandle));
    }

    public static OrtLoraAdapter create(ByteBuffer loraBuffer) throws OrtException {
        return create(loraBuffer, (OrtAllocator) null);
    }

    static OrtLoraAdapter create(ByteBuffer loraBuffer, OrtAllocator allocator) throws OrtException {
        Objects.requireNonNull(loraBuffer, "LoRA buffer must not be null");
        if (loraBuffer.remaining() == 0) {
            throw new OrtException("Invalid LoRA buffer, no elements remaining.");
        }
        if (!loraBuffer.isDirect()) {
            throw new OrtException("ByteBuffer is not direct.");
        }
        long allocatorHandle = allocator == null ? 0L : allocator.handle;
        return new OrtLoraAdapter(createLoraAdapterFromBuffer(OnnxRuntime.ortApiHandle, loraBuffer, loraBuffer.position(), loraBuffer.remaining(), allocatorHandle));
    }

    public static OrtLoraAdapter create(String adapterPath) throws OrtException {
        return create(adapterPath, (OrtAllocator) null);
    }

    static OrtLoraAdapter create(String adapterPath, OrtAllocator allocator) throws OrtException {
        long allocatorHandle = allocator == null ? 0L : allocator.handle;
        return new OrtLoraAdapter(createLoraAdapter(OnnxRuntime.ortApiHandle, adapterPath, allocatorHandle));
    }

    long getNativeHandle() {
        return this.nativeHandle;
    }

    void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Trying to use a closed OrtLoraAdapter");
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (!this.closed) {
            close(OnnxRuntime.ortApiHandle, this.nativeHandle);
            this.closed = true;
            return;
        }
        throw new IllegalStateException("Trying to close an already closed OrtLoraAdapter");
    }
}