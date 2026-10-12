package com.codex.leicaapp;

import java.util.concurrent.Semaphore;

/* JADX INFO: loaded from: /Leica/reverse/apktool_out/classes11.dex */
public final class RenderQueue {
    private final long byteLimit;
    private long bytes;
    private int jobs;
    private final int limit;
    private final Semaphore renderer = new Semaphore(1, true);

    public native Ticket enter(long j) throws InterruptedException;

    static /* synthetic */ int access$110(RenderQueue renderQueue) {
        int i = renderQueue.jobs;
        renderQueue.jobs = i - 1;
        return i;
    }

    static /* synthetic */ long access$222(RenderQueue renderQueue, long j) {
        long j2 = renderQueue.bytes - j;
        renderQueue.bytes = j2;
        return j2;
    }

    public RenderQueue(int i, long j) {
        this.limit = i;
        this.byteLimit = j;
    }

    public synchronized int pending() {
        return this.jobs;
    }

    public final class Ticket implements AutoCloseable {
        private boolean closed;
        private final long size;

        private Ticket(long j) {
            this.size = j;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            synchronized (RenderQueue.this) {
                if (this.closed) {
                    return;
                }
                this.closed = true;
                RenderQueue.access$110(RenderQueue.this);
                RenderQueue.access$222(RenderQueue.this, this.size);
                RenderQueue.this.renderer.release();
            }
        }
    }
}
