package com.qaverse.smart.logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SmartTimer implements AutoCloseable {
    private final String name;
    private final long startNanos;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    SmartTimer(String name) {
        this.name = name;
        this.startNanos = System.nanoTime();
    }

    public long stop() {
        if (!stopped.compareAndSet(false, true)) {
            return 0L;
        }
        long elapsed = System.nanoTime() - startNanos;
        SmartLog.timed(name, elapsed);
        return elapsed;
    }

    public long elapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    @Override
    public void close() {
        stop();
    }
}
