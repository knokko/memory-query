package com.github.knokko.memory;

import java.io.IOException;
import java.util.Scanner;

abstract class TotalMemoryQuery {

    abstract long getMemoryUsage(long pid) throws IOException, InterruptedException;

    void assertSuccess(Process process) throws InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Scanner inputScanner = new Scanner(process.getInputStream());
            System.err.println("Memory query process failed:");
            System.err.println("---------------- Standard output ----------------");
            while (inputScanner.hasNextLine()) {
                System.err.println(inputScanner.nextLine());
            }
            inputScanner.close();

            System.err.println("---------------- Error output ----------------");
            Scanner errorScanner = new Scanner(process.getErrorStream());
            while (errorScanner.hasNextLine()) {
                System.err.println(errorScanner.nextLine());
            }
            errorScanner.close();
            System.err.println("-----------------------------------------------");

            throw new MemoryQueryException("Memory query failed with exit code " + exitCode);
        }
    }
}
