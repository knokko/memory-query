package com.github.knokko.memory;

/**
 * This exception is thrown when this library failed to query the process memory usage of this Java process.
 * This should rarely happen, but when it does, the most likely causes are:
 * <ul>
 *     <li>The (version of) the OS is not supported</li>
 *     <li>I made a programming error</li>
 *     <li>Extreme conditions (e.g. the maximum number of OS processes has been reached)</li>
 * </ul>
 * In the first 2 cases, an issue is pull request would be welcome. In the last case, take better care of your
 * computer :)
 */
public class MemoryQueryException extends RuntimeException {

    public MemoryQueryException(String message) {
        super(message);
    }
}
