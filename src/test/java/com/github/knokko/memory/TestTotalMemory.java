package com.github.knokko.memory;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static com.github.knokko.memory.TotalMemory.getProcessMemoryUsage;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTotalMemory {

    @Test
    public void testGetProcessMemoryUsage() {
        long processMemory = getProcessMemoryUsage();

        // I don't know exactly how much process memory this unit test will use, but it should be somewhere between
        // 1 MB and 1 GB
        assertTrue(processMemory > 1_000_000);
        assertTrue(processMemory < 1_000_000_000);
    }

    @Test
    public void testProcessMemoryIncreasesWithByteBuffers() {
        long originalProcessMemory = getProcessMemoryUsage();
        long originalTotalMemory = Runtime.getRuntime().totalMemory();

        ByteBuffer buffer = ByteBuffer.allocateDirect(300_000_000);

        // Sometimes, the extra memory usage is not reported until the memory is actually used
        while (buffer.hasRemaining()) buffer.putInt(123456);

        long newProcessMemory = getProcessMemoryUsage();
        long newTotalMemory = Runtime.getRuntime().totalMemory();

        long increasedMemory = newProcessMemory - originalProcessMemory;
        long totalMemoryIncrease = newTotalMemory - originalTotalMemory;
        System.out.println("Process memory increased by " + increasedMemory + " and 'total' memory increased by " + totalMemoryIncrease);

        assertTrue(increasedMemory > 250_000_000);
        assertTrue(increasedMemory < 350_000_000);

        // This illustrates the limitations of Runtime.getRuntime().totalMemory()
        assertTrue(totalMemoryIncrease > -50_000_000);
        assertTrue(totalMemoryIncrease < 50_000_000);
    }
}
