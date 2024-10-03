## vendors of JDK and JVM
- Oracle JDK
- OpenJDK

## How JVM runs your code?

![ScreenShot](/JavaApplication_PerfTuning_MemoryMgmt/images/jvm1.PNG?raw=true)

- javac - compiles the java code to bytecode
- jvm interprets the bytecode to machine code
- jvm runs the machine code
- platform independent
- scala, java, kotlin, groovy can be compiled to bytecode and run on jvm
- C language can be compiled directly to machine code and run on the machine.with such languages that runs natively, we'll loose write once run anywhere feature.
- interpreted languages like python, ruby, javascript are not compiled to bytecode, they are interpreted directly to machine code. They are not platform independent.

- interpreter - reads the code line by line and executes it which is slow.
- to overcome this, jvm uses `JIT compiler - Just In Time compiler`. 
- It compiles the bytecode to machine code and runs it. 
- It compiles/interprets the code that is frequently used to native machine code and optimizes it. 
- It caches the compiled code for future use. 

- if u want to know what kind of compilation is happening when JVM is running ur code, use `-XX:+PrintCompilation` option in jvm.
         ` java -XX:+PrintCompilation Main`
- JVM has 2 compilers - `C1` and `C2`. 
- C1 compiler is used for quick compilation(Level 1,2,3) and put into code cache.
- C2 compiler is used for optimization/deeper compilation (Level 4).
- `-XX:+PrintCodeCache` - to see the code cache size
- `/java/jdk-<version>/bin/jconsole` - to see the code cache size in GUI.

| JVM Version  | Heap Size                            | Max Heap Size         | Compiler Used             | Suitable For                                  | Performance Impact      |
|--------------|--------------------------------------|-----------------------|---------------------------|------------------------------------------------|-------------------------|
| **32-bit**   | Less than 3GB                        | 4GB                   | C1 / Client Compiler       | Short-lived processes                          | Faster when heap < 3GB  |
| **64-bit**   | More than 4GB                        | OS Dependent           | C1 & C2 / Client and Server Compiler | Long-lived processes (e.g., web servers)      | Faster when heap > 4GB  |

- the number of threads to be used by JVM will determined by the number of CPU cores available in the machine.
- -XX:+PrintFlagsFinal - to see all the flags available in JVM.CiCompilerCount - to see the number of threads available for JVM.
- `jps` command to see the process id of all the java process running.
- 




## How the JVM manages memory?

## Stack vs Heap Memory Comparison

| Aspect               | **Stack**                                                    | **Heap**                                             |
|----------------------|--------------------------------------------------------------|------------------------------------------------------|
| **Memory Allocation** | Static, during compile-time                                  | Dynamic, during runtime                              |
| **Memory Management** | Automatically managed (Last-In, First-Out, LIFO structure)   | Manually managed (by the programmer or garbage collector) |
| **Storage**           | Stores **local variables** and **method call information**   | Stores **objects**, **instance variables**, and dynamically allocated data |
| **Access Speed**      | Fast access (sequential memory allocation)                   | Slower access (complex memory management)            |
| **Size Limit**        | Smaller, size is determined by the OS and generally limited  | Larger, size is determined by available system memory |
| **Memory Lifetime**   | Memory is freed automatically after method execution         | Memory is freed manually or by the garbage collector when no longer referenced |
| **Scope**             | Local to a thread (each thread has its own stack)            | Shared across all threads (accessible by multiple threads) |
| **Usage**             | Used for method calls, function parameters, and local variables | Used for dynamic memory allocation (objects, data structures) |
| **Overflow**          | Stack overflow occurs if too much memory is used (e.g., deep recursion) | Heap overflow occurs when the system runs out of memory to allocate |
| **Example**           | Variables like `int x` declared within a method             | Objects like `Person p = new Person()` in Java, or dynamically allocated memory using `malloc()` in C |



- Pass by value - when u pass a primitive data type to a method, a copy of the value is passed to the method. So, any changes made to the value inside the method will not reflect outside the method.
- 
- Metaspace 
    - in java 8, PermGen space is replaced by Metaspace. 
    - Metaspace is used to store class metadata, static fields, and method information. (which method will be compiled to bytecode, which method will be interpreted etc.)
    - It is not part of the heap memory.
    - Metaspace is not fixed in size and can grow or shrink based on the application's needs.
    - variables in metaspace are permanent and are not garbage collected.
    - Metaspace is garbage collected by the JVM when the classloader that loaded the classes is garbage collected.

- String Pool - 
    - String pool is a special area in the heap memory that stores unique string literals.
    - When a string is created using double quotes, it is stored in the string pool.
    - If another string with the same value is created, it will reference the same object in the string pool.
    - This helps in saving memory and improving performance by reducing the number of duplicate strings.
    - String pool is part of the heap memory and is garbage collected like other objects.
    - `-XX:+PrintStringTableStatistics` - to see the statistics of the string pool.
    - `-XX:StringTableSize=1009` - to set the size of the string pool.

- `-XX:+MaxHeapSize=512m` - to set the max heap size to 512mb / `Xmx512m` - to set the max heap size to 512mb.
- `-XX:+InitialHeapSize=256m` - to set the initial heap size to 256mb./ `Xms256m` - to set the initial heap size to 256mb.

![ScreenShot](/JavaApplication_PerfTuning_MemoryMgmt/images/memory1.PNG?raw=true)

## Garbage Collection and Heap Analysis

- Any object in the `heap` which can't be reached or accessed from `stack` is eligible for garbage collection.
- `static` object in heap are referenced from `metaspace`.
- `System.gc()` - to request the garbage collector to run.It' just an instruction to the JVM and it's upto the JVM to run the garbage collector or not.
- GC can slow down the application so running System.gc() in production is not recommended.
- finalize() method is called by the garbage collector before the object is garbage collected.
- finalize() method id deprecated in java 9 as it can cause performance issues.

- Memory Leak - 
    - Memory leak occurs when objects are not garbage collected even though they are no longer needed by the application.
    - This can happen when objects are not properly dereferenced or when there are circular references between objects.
    - Memory leaks can lead to increased memory usage, reduced performance, and even application crashes.
    - Tools like `jvisualvm`, `jconsole`, and `jmap` can be used to analyze memory usage and identify memory leaks in Java applications.
- `bin/jvisualvm` - to open the visualvm tool for Oracle JDK. For OpenJDK, download the `visualvm` tool from the internet.
- Generating and Viewing a Heap Dump 
  - `HeapDump` button in visualvm tool to generate a heap dump.
  - download `Eclipse Memory Analyzer(MAT)` tool from https://www.eclipse.org/mat to analyze the heap dump.
  - open the heap dump in MAT tool to analyze the memory usage and identify memory leaks.
  - observe 'Retained Heap' column in MAT tool to identify the objects that are causing memory leaks.


- Mark and Sweep Algorithm
    - Mark and Sweep is a garbage collection algorithm that identifies and removes unreachable objects from memory.
    - The algorithm consists of two phases: marking and sweeping.
    - In the marking phase, the garbage collector traverses the object graph starting from the root objects (e.g., local variables, static fields) and marks all reachable objects.
    - In the sweeping phase, the garbage collector scans the heap and deallocates memory for all unmarked objects, which are considered garbage.
    - Mark and Sweep is a non-generational garbage collection algorithm and can lead to fragmentation of memory.
    - It is a simple and efficient algorithm for garbage collection but can cause pauses in the application when running the garbage collector.

- Generational Garbage Collection
  - Heap is divided into multiple generations: `Young Generation`, `Old Generation`.
  - new objects are allocated in the Young Generation.
  - Young Generation gets filled up quickly as it's small in size.
  - When the Young Generation is full, a `Minor GC` is triggered only on the Young Generation.
  - Objects that survive the Minor GC are moved to the Old Generation.
  - Young Generation is emptied after the Minor GC and new objects are allocated again.
  - Minor GC is fast as it only collects the Young Generation and it's frequent without causing major pauses in the application.
  - When the Old Generation is full, a `Major GC` or `Full GC` is triggered on the entire heap.
  - Major GC is slower as it collects the entire heap and can cause longer pauses in the application.
  - It compacts the heap by moving objects around to reduce fragmentation.
  - Young Generation is divided into `Eden Space` and `Survivor Spaces(s0 and s1)`.
  - Objects are initially or when created allocated in the Eden Space.
  - When the Eden Space is full, a Minor GC is triggered and the surviving objects are moved to s0.
  - Next time, when the Eden Space is full, a Minor GC is triggered and the surviving objects are moved to s1.
  - It also moves the objects from s0 to s1 and vice versa.
  - This is efficient as it has look only 2 spaces(Eden and s0 or s1) to move the objects during the Minor GC.
  - When an object survives multiple(configurable) Minor GCs, it is moved to the Old Generation.
  - install plugin - `Visual GC` in visualvm tool to see the generations in the heap.

- `Garbage Collectors` - There are different garbage collectors available in the JVM that use different algorithms and strategies for garbage collection.
- JVM decides which garbage collector to use based on the system configuration, heap size, and other factors.
- Choosing a Garbage Collector
 
| Garbage Collector          | Description                                                                                                           | Suitable For                                                    | JVM Option                      | Comments                                                                 |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|----------------------------------|--------------------------------------------------------------------------|
| **Serial Garbage Collector** | Single-threaded garbage collector that uses a simple mark and sweep algorithm.                                       | Small applications or low memory requirements                    | `-XX:+UseSerialGC`               | -application will be paused<br/>-not recommended if performance is vital |
| **Parallel Garbage Collector** | Multi-threaded garbage collector that uses a parallel mark and sweep algorithm.                                    | Multi-core systems and medium to large heap sizes                 | `-XX:+UseParallelGC`             |                                                                          |
| **CMS Garbage Collector**      | Concurrent garbage collector that minimizes pause times by running concurrently with the application.              | Applications requiring low latency and medium to large heap sizes | `-XX:+UseConcMarkSweepGC`        | application pause will be minimal                                        |
| **G1 Garbage Collector**       | Divides the heap into regions, uses generational and concurrent garbage collection.                                | Large heap sizes and applications requiring low latency and high throughput | `-XX:+UseG1GC`            |                                                                          |
| **Z Garbage Collector**        | Experimental, concurrent mark and sweep garbage collector with low pause times.                                    | Applications requiring low latency and large heap sizes           | `-XX:+UseZGC`                    |                                                                          |
| **Shenandoah Garbage Collector** | Concurrent mark and sweep algorithm with low pause times.                                                        | Applications requiring low latency and large heap sizes           | `-XX:+UseShenandoahGC`           |                                                                          |
| **Epsilon Garbage Collector**  | Minimal garbage collector that performs no garbage collection (for testing and performance analysis).              | Testing and performance analysis                                 | `-XX:+UseEpsilonGC`              |                                                                          |

- `G1` is the default garbage collector in Java 9 and later versions. 
- in G1 garbage collector, the heap is divided into 248 regions.
- the garbage collection is done on a per-region basis'
- some of the regions are Young Generation i.e Eden, s0 and s1 regions and some are Old Generation regions.
- after each Minor GC, the garbage collector swaps the regions between Eden and s0/s1 for optimal memory management.
- during the Major GC, the garbage collector collects the regions with the most garbage first to optimize the collection process.
- if a region contains only unreferenceable objects, it is considered garbage and is collected during the Major GC.
- G1 is better than other garbage collectors as it frees up parts of the heap that are no longer needed and reduces fragmentation.
- `-XX:+UseStringDeduplication` - to enable string deduplication in G1 garbage collector.



## Measuring Performance / Profilers

- `Profiler`  - Profilers are tools that help developers analyze the performance of their applications by monitoring various metrics like CPU usage, memory usage, thread activity, and method execution times.
- Profilers can impact the performance of the application, so they should be used judiciously and only when needed.
- `JMC(Java Mission Control)` - JMC is a profiler tool that comes with the Oracle JDK and provides detailed insights into the performance of Java applications.
- for OpenJDK, download the `JMC` tool from the internet - https://github.com/JDKMissionControl/jmc

- `JMH(Java Microbenchmark Harness)` is a Java library that allows you to write benchmarks for Java code.
- It provides annotations and APIs for writing benchmarks and running them with different configurations.
- JMH runs the benchmarks multiple times to get accurate results and provides statistical analysis of the results.
https://openjdk.java.net/projects/code-tools/jmh/



## How programming choices impact performance

- `CopyOnWriteArrayList` is a thread-safe variant of ArrayList in which all mutative operations (add, set, and so on) are implemented by making a fresh copy of the underlying array.
- This is usually slower than the non-thread-safe ArrayList, but it is useful in situations where you need to iterate over the list while modifying it.
- ArrayList will grow dynamically by 50% of the current size when it reaches its capacity.

- `javap` - The Java class file disassembler that is used to examine the bytecode of a Java class file.
`javap -v Main.class`

- `java -jar cfr-0.142.jar Main.class` - The `CFR (Class File Reader)` tool is a Java decompiler that can decompile Java class files to Java source code.

## The Future and other JVM languages

- `GraalVM` - an alternative to the HotSpot JVM that provides better performance and lower memory usage.
- `GraalVM` can convert Java bytecode to native code skipping bytecode, which can improve the performance of Java applications.