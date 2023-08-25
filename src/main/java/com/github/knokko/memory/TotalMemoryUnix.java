package com.github.knokko.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Long.parseLong;

class TotalMemoryUnix extends TotalMemoryQuery {

    long getMemoryUsage(long pid) throws IOException, InterruptedException {
        String[] command = { "ps", "-p", Long.toString(pid), "-o", "rss" };
        Process memoryUsageProcess = Runtime.getRuntime().exec(command);
        assertSuccess(memoryUsageProcess);

        List<String> scannedInput = new ArrayList<>();
        Scanner scanner = new Scanner(memoryUsageProcess.getInputStream());
        while (scanner.hasNextLine()) {
            scannedInput.add(scanner.nextLine());
        }
        scanner.close();

        if (scannedInput.size() != 2) throw new MemoryQueryException("Unexpected memory query format (4)");
        if (!scannedInput.get(0).contains("RSS")) throw new MemoryQueryException("Unexpected memory query format (5)");
        try {
            return 1024 * parseLong(scannedInput.get(1).trim());
        } catch (NumberFormatException invalid) {
            throw new MemoryQueryException("Unexpected memory query format (6)");
        }
    }
}
