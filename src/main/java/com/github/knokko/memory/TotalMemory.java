package com.github.knokko.memory;

import java.io.IOException;

public final class TotalMemory {

    static final TotalMemoryQuery INSTANCE;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            INSTANCE = new TotalMemoryWindows();
        } else {
            INSTANCE = new TotalMemoryUnix();
        }
    }

    /**
     * Returns an estimation of the memory usage of this Java process, in bytes.
     *
     * <p>
     *      Unlike <b>MemoryMXBean</b>, this method asks the OS how much memory this process is <i>really</i> using,
     *      rather than just the size of the memory pools for Java objects.
     * </p>
     *
     * <p>
     *     Unlike <b>Runtime.getRuntime().totalMemory()</b>, this method also counts the memory of which the JVM is
     *     not aware. This can be useful when you are using native libraries, since the JVM usually can't track their
     *     memory usage. Also, this method counts the memory usage of <b>ByteBuffer.allocateDirect</b>, whereas
     *     <b>Runtime.getRuntime().totalMemory()</b> does not.
     * </p>
     *
     * <ul>
     *     <li>
     *         On Windows, the result of this method is given by <b>tasklist</b>
     *         and similar to the memory usage reported by the task manager.
     *     </li>
     *     <li>On Unix, the result of this method is the <b>RSS</b> given by ps.</li>
     * </ul>
     *
     * @throws MemoryQueryException When the memory query failed for some reason
     */
    public static long getProcessMemoryUsage() throws MemoryQueryException {
        try {
            return INSTANCE.getMemoryUsage(ProcessHandle.current().pid());
        } catch (IOException processError) {
            throw new MemoryQueryException("IO: " + processError.getMessage());
        } catch (InterruptedException interrupted) {
            throw new MemoryQueryException("Interrupted: " + interrupted.getMessage());
        }
    }
}
