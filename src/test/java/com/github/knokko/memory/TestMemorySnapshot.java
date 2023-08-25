package com.github.knokko.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMemorySnapshot {

    @Test
    public void testTake() {
        MemorySnapshot memory = MemorySnapshot.take();
        long unknown = memory.getUnknownMemory();
        long total = memory.processMemory;
        memory.debugDump();

        // I don't know what the exact value should be, but I'm pretty sure it should be between 1 MB and 1 GB
        assertTrue(unknown > 1_000_000);
        assertTrue(unknown < 1_000_000_000);

        // Also, I'm pretty sure that the unknown memory should be between 40% and 90% of the process memory
        assertTrue(unknown > 0.4 * total);
        assertTrue(unknown < 0.9 * total);
    }
}
