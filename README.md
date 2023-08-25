# Query memory usage
## Limitations of the Java standard library
Query process memory usage of Java applications. Currently, the Java standard library doesn't provide a way to obtain
the memory usage of the process. You only have:
- `MemoryMXBean`, which gives the memory usage of Java *objects*
- `Runtime.getRuntime().totalMemory()`, which only returns the amount of memory in the JVM (and misses some stuff like direct byte buffers)

## Query process memory usage
This mini library provides a way to query the process memory usage of the Java process, which doesn't miss anything: `TotalMemory.getProcessMemoryUsage()`.
- On Windows, the reported usage is given by `tasklist` and similar to that of the task manager.
- On Unix, the reported usage is the RSS given by `ps`

## Purpose of process memory usage
The process memory usage can be a useful metric for monitoring purposes and detecting nasty memory leaks that are not detected
by `MemoryMXBean` and `Runtime.getRuntime().totalMemory()` (and are usually not included in Java heap dumps). Note however
that this library only gives the total memory usage, but does not actually tell you what the memory is used for. By frequently
querying the memory usage, you can at least find *when* the memory increases, which might help tracking it down.

Watching the process memory usage is mostly useful when the application deals a lot with native libraries (whose memory usage is not
tracked by the JVM), but it can also be useful to detect 'JVM leaks' in applications that don't use any native code at all,
for instance https://www.evanjones.ca/java-bytebuffer-leak.html

## Taking snapshots
Furthermore, this library contains a `MemorySnapshot` class, which holds the process memory usage at a specific instant
alongside the Java *object* usage reported by `MemoryMXBean`. Snapshots can be taken using `MemorySnapshot.take()`.

# Java versions and operating systems
This library requires Java 11 or later. It is tested (with Github Actions) on Windows, Ubuntu, and MacOS with Java 11, 17, and 20.
The other Java versions between 11 and 20 will probably work as well, but are not explicitly tested. Java 8 can **not** be supported
because it doesn't have a proper way to query the process ID. Java 9 and 10 could be supported with some extra work, but I didn't
bother because these are end of life anyway.

# Add to your build
## Gradle
```
...
repositories {
  ...
  maven { url 'https://jitpack.io' }
}
...
dependencies {
  ...
  implementation 'com.github.knokko:memory-query:v1.1'
}
```

## Maven
```
...
<repositories>
  ...
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
...
<dependency>
  <groupId>com.github.knokko</groupId>
  <artifactId>memory-query</artifactId>
  <version>v1.1</version>
</dependency>
```
