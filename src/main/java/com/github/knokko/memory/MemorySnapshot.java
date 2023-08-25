package com.github.knokko.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import static com.github.knokko.memory.TotalMemory.*;

/**
 * A 'snapshot' of the memory usage of the program at a certain point in time.
 * You should use <b>MemorySnapshot.take()</b> to take a snapshot. This class
 * stores 4 types of memory usage:
 * <ul>
 *     <li>The heap memory: this is the memory used for storing Java objects</li>
 *     <li>The non-heap memory: the memory that is used by the JVM, but not for Java objects</li>
 *     <li>The process memory: the memory usage that is reported by the OS</li>
 *     <li>The unknown memory: basically <i>process memory</i> - <i>heap memory</i> - <i>non_heap memory</i></li>
 * </ul>
 * You can freely read each of the memory types. Finally, you can use the <b>debugDump()</b> method to simply print
 * all 4 of them to <b>System.out</b>.
 */
public class MemorySnapshot {

    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public static MemorySnapshot take() {
        Long processUsage = null;
        MemoryQueryException processMemoryFailedException = null;
        try {
            processUsage = getProcessMemoryUsage();
        } catch (MemoryQueryException failed) {
            processMemoryFailedException = failed;
        }
        return new MemorySnapshot(
                memoryBean.getHeapMemoryUsage(), memoryBean.getNonHeapMemoryUsage(), processUsage,
                processMemoryFailedException
        );
    }

    /**
     * The amount of memory (in bytes) that is used to store Java objects
     * (obtained from <b>MemoryMXBean.getHeapMemoryUsage()</b>). This amount should continuously increase,
     * but drop at every GC.
     */
    public final MemoryUsage heapMemory;

    /**
     * The amount of memory (in bytes) that is used by the JVM for purposes other than storing Java objects
     * (obtained from <b>MemoryMXBean.getNonHeapMemoryUsage()</b>). This is usually a small portion of the total memory.
     */
    public final MemoryUsage nonHeapMemory;

    /**
     * The total amount of memory (in bytes) used by this Java process (obtained from the OS in a platform-specific
     * way using <b>TotalMemory.getProcessMemoryUsage()</b>). This amount is usually considerably larger than the
     * <b>heapMemory</b>. This amount is supposed to be rather stable: if it continuously increases, you probably have
     * a memory leak.<br>
     *
     * <b>If the OS memory query failed, this will be null.</b>
     */
    public final Long processMemory;

    /**
     * If the OS memory query failed, this will contain the cause. If the OS memory query succeeded, this will be null.
     */
    public final MemoryQueryException processMemoryFailedException;

    public MemorySnapshot(
            MemoryUsage heapMemory, MemoryUsage nonHeapMemory,
            Long processMemory, MemoryQueryException processMemoryFailedException
    ) {
        if ((processMemory == null) == (processMemoryFailedException == null)) {
            throw new IllegalArgumentException("Exactly 1 of processMemory and processMemoryFailedException must be null");
        }

        this.heapMemory = heapMemory;
        this.nonHeapMemory = nonHeapMemory;
        this.processMemory = processMemory;
        this.processMemoryFailedException = processMemoryFailedException;
    }

    /**
     * Gets the 'unknown' memory usage (in bytes). This is just <i>processMemory - heapMemory - nonHeapMemory</i>.
     * <p>
     *     Unlike the heapMemory and nonHeapMemory, this amount is expected to be rather stable, and should not change
     *     abruptly at every GC. Watching this periodically is a nice way to detect 'native' memory leaks.
     * </p>
     */
    public Long getUnknownMemory() {
        if (processMemory == null) return null;
        return processMemory - heapMemory.getUsed() - nonHeapMemory.getUsed();
    }

    private String format(long value) {
        if (value == -1) return "none";
        return String.format("%.3f MB", (double) value / (1024.0 * 1024.0));
    }

    private String formatUsage(MemoryUsage usage) {
        return String.format(
                "(used: %s, committed: %s, max: %s)", format(usage.getUsed()),
                format(usage.getCommitted()), format(usage.getMax())
        );
    }

    @Override
    public String toString() {
        return String.format(
                "MemorySnapshot(heap: %s, non-heap: %s, process: %s", formatUsage(heapMemory),
                formatUsage(nonHeapMemory), processMemory != null ? format(processMemory) : "unknown"
        );
    }

    public void debugDump() {
        System.out.println("Heap memory: " + formatUsage(heapMemory));
        System.out.println("Non-heap memory: " + formatUsage(nonHeapMemory));
        if (processMemory != null) {
            System.out.println("Process memory: " + format(processMemory));
            System.out.println("Unknown memory: " + format(getUnknownMemory()));
        } else {
            System.out.println("Failed to get process memory: " + processMemoryFailedException.getMessage());
        }
    }
}
