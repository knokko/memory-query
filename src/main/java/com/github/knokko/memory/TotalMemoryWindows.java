package com.github.knokko.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Long.parseLong;

class TotalMemoryWindows extends TotalMemoryQuery {

    @Override
    long getMemoryUsage(long pid) throws IOException, InterruptedException {
        String[] memoryQueryCommand = { "tasklist", "/FI", "\"PID eq " + pid + '"'};
        Process memoryQueryProcess = Runtime.getRuntime().exec(memoryQueryCommand);
        assertSuccess(memoryQueryProcess);

        List<String> scannedInput = new ArrayList<>();
        Scanner scanner = new Scanner(memoryQueryProcess.getInputStream());
        while (scanner.hasNextLine()) {
            scannedInput.add(scanner.nextLine());
        }
        scanner.close();

        if (scannedInput.size() == 4) {
            String relevantLine = scannedInput.get(3);
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
