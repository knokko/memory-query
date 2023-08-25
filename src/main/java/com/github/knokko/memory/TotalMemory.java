package com.github.knokko.memory;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static java.lang.Long.parseLong;

public final class TotalMemory {
    static final boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    private static final Path memoryScriptFile;
    private static final long pid = ProcessHandle.current().pid();

    static {
        if (isWindows) {
            try {
                memoryScriptFile = Files.createTempFile("", ".bat");
            } catch (IOException what) {
                throw new MemoryQueryException("Failed to create temp file for Windows memory batch?: " + what.getMessage());
            }
        } else memoryScriptFile = null;
    }

    private static long getProcessMemoryUsageWindows() throws IOException, InterruptedException {
        String memoryQueryCommand = "tasklist /FI \"PID eq " + pid + "\"";
        assert memoryScriptFile != null;
        Files.writeString(memoryScriptFile, memoryQueryCommand);

        ProcessBuilder memoryQueryBuilder = new ProcessBuilder(memoryScriptFile.toAbsolutePath().toString());
        Process memoryQueryProcess = memoryQueryBuilder.start();
        int exitCode = memoryQueryProcess.waitFor();
        if (exitCode == 0) {

            List<String> scannedInput = new ArrayList<>();
            Scanner scanner = new Scanner(memoryQueryProcess.getInputStream());
            while (scanner.hasNextLine()) {
                scannedInput.add(scanner.nextLine());
            }
            scanner.close();

            if (scannedInput.size() == 6) {
                String relevantLine = scannedInput.get(5);
                int lastSpaceIndex = relevantLine.lastIndexOf(' ');
                if (lastSpaceIndex != -1) {
                    int memorySpaceIndex = relevantLine.substring(0, lastSpaceIndex).lastIndexOf(' ');
                    if (memorySpaceIndex != -1) {
                        String memoryUsage = relevantLine.substring(memorySpaceIndex + 1);

                        if (!memoryUsage.endsWith(" K")) {
                            throw new MemoryQueryException("Expected $memoryUsage to end with \" K\"");
                        }

                        String rawMemoryUsage = memoryUsage.substring(0, memoryUsage.length() - 2)
                                .replace(",", "").replace(".", "");

                        return 1024 * parseLong(rawMemoryUsage);
                    } else throw new MemoryQueryException("Unexpected memory query format (3): " + relevantLine);
                } else throw new MemoryQueryException("Unexpected memory query format (2): " + relevantLine);
            } else throw new MemoryQueryException("Unexpected memory query format (1): " + scannedInput);
        } else throw new MemoryQueryException("Memory query failed with exit code " + exitCode);
    }

    private static long getProcessMemoryUsageLinux() throws IOException, InterruptedException {
        String[] command = { "ps", "-q", Long.toString(pid), "-eo", "size" };
        Process memoryUsageProcess = Runtime.getRuntime().exec(command);
        int exitCode = memoryUsageProcess.waitFor();
        if (exitCode == 0) {
            List<String> scannedInput = new ArrayList<>();
            Scanner scanner = new Scanner(memoryUsageProcess.getInputStream());
            while (scanner.hasNextLine()) {
                scannedInput.add(scanner.nextLine());
            }
            scanner.close();

            if (scannedInput.size() != 3) throw new MemoryQueryException("Unexpected memory query format (4)");
            if (!scannedInput.get(0).contains("SIZE")) throw new MemoryQueryException("Unexpected memory query format (5)");
            try {
                long dummyUsage = parseLong(scannedInput.get(2).trim());
                if (dummyUsage != 0L) throw new MemoryQueryException("Unexpected memory query format (6)");
                return 1024 * parseLong(scannedInput.get(1).trim());
            } catch (NumberFormatException invalid) {
                throw new MemoryQueryException("Unexpected memory query format (7)");
            }
        } else throw new MemoryQueryException("Memory query failed with exit code $exitCode");
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
     *     <li>On Windows, the result of this method is similar to the memory usage reported by the task manager.</li>
     *     <li>On Linux, the result of this method is similar to the memory usage reported by htop.</li>
     * </ul>
     *
     * @throws MemoryQueryException When the memory query failed for some reason
     */
    public static long getProcessMemoryUsage() throws MemoryQueryException {
        try {
            if (isWindows) {
                return getProcessMemoryUsageWindows();
            } else {
                return getProcessMemoryUsageLinux();
            }
        } catch (IOException processError) {
            throw new MemoryQueryException("IO: " + processError.getMessage());
        } catch (InterruptedException interrupted) {
            throw new MemoryQueryException("Interrupted: " + interrupted.getMessage());
        }
    }
}
