# memory-query
Query process memory usage of Java applications. Currently, the Java standard library doesn't provide a way to obtain
the memory usage of the process. You only have:
- `MemoryMXBean`, which gives the memory usage of Java *objects*
- `Runtime.getRuntime().totalMemory()`, which only returns the amount of memory in the JVM (and misses some stuff like direct byte buffers)

This mini library provides a way to query the process memory usage of the Java process, which doesn't miss anything: `TotalMemory.getProcessMemoryUsage()`.
- On Windows, the reported usage is given by `tasklist` and similar to that of the task manager.
- On Unix, the reported usage is the RSS given by `ps`

The process memory usage can be a useful metric for monitoring purposes and detecting nasty memory leaks that are not detected
by `MemoryMXBean` and `Runtime.getRuntime().totalMemory()` (and are usually not included in Java heap dumps). Note however
that this library only gives the total memory usage, but does not actually tell you what the memory is used for. By frequently
querying the memory usage, you can at least find *when* the memory increases, which might help tracking it down.

Watching the process memory usage is mostly useful when the application deals a lot with native libraries (whose memory usage is not
tracked by the JVM), but it can also be useful to detect 'JVM leaks' in applications that don't use any native code at all,
for instance https://www.evanjones.ca/java-bytebuffer-leak.html

Furthermore, this library contains a `MemorySnapshot` class, which holds the process memory usage at a specific instant
alongside the Java *object* usage reported by `MemoryMXBean`. Snapshots can be taken using `MemorySnapshot.take()`.
