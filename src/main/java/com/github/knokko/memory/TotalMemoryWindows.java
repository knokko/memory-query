package com.github.knokko.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Long.parseLong;

class TotalMemoryWindows extends TotalMemoryQuery {

    private final Path memoryScriptFile;

    TotalMemoryWindows() {
        try {
            memoryScriptFile = Files.createTempFile("", ".bat");
        } catch (IOException what) {
            throw new MemoryQueryException("Failed to create temp file for Windows memory batch?: " + what.getMessage());
        }
    }

    @Override
    long getMemoryUsage(long pid) throws IOException, InterruptedException {
        String memoryQueryCommand = "tasklist /FI \"PID eq " + pid + "\"";
        assert memoryScriptFile != null;
        Files.writeString(memoryScriptFile, memoryQueryCommand);

        ProcessBuilder memoryQueryBuilder = new ProcessBuilder(memoryScriptFile.toAbsolutePath().toString());
        Process memoryQueryProcess = memoryQueryBuilder.start();
        assertSuccess(memoryQueryProcess);

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
    }
}
