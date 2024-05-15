# Memory Management System

This project simulates a memory management system, a critical component of an operating system. The system manages the allocation and deallocation of memory for processes using various strategies to ensure efficient and safe memory usage.

## Table of Contents
- [Introduction](#introduction)
- [Files and Descriptions](#files-and-descriptions)
  - [IllegalAddressException.java](#illegaladdressexceptionjava)
  - [MemoryManager.java](#memorymanagerjava)
  - [mmu.java](#mmujava)
  - [NoEnoughMemoryException.java](#noenoughmemoryexceptionjava)
  - [Process.java](#processjava)
  - [ProcessNotFoundException.java](#processnotfoundexceptionjava)
  - [Repl.java](#repljava)
- [Conclusion](#conclusion)

## Introduction
This project simulates a memory management system, a critical component of an operating system. The system manages the allocation and deallocation of memory for processes using various strategies to ensure efficient and safe memory usage. The project consists of several Java files: `IllegalAddressException.java`, `MemoryManager.java`, `mmu.java`, `NoEnoughMemoryException.java`, `Process.java`, `ProcessNotFoundException.java`, and `Repl.java`. Each file has a specific role in the system, which will be detailed in the following sections.

## Files and Descriptions

### IllegalAddressException.java
**Purpose:**  
This file defines a custom exception named `IllegalAddressException`. It is used when an address outside a process's allocated memory space is accessed. This ensures that processes do not read or write to memory that hasn't been allocated to them, which is crucial for memory protection in an operating system.

**Details:**  
The `IllegalAddressException` class extends the `Exception` class, making it a checked exception. When an illegal memory address access is detected, this exception is thrown with a message detailing the error. This mechanism helps maintain the integrity of memory access and prevents processes from interfering with each other's memory space. By providing a detailed error message, it aids in debugging and maintaining robust memory management practices within the operating system.

### MemoryManager.java
**Purpose:**  
The `MemoryManager` class is the core component for managing memory in this project. It includes methods for allocating and deallocating memory based on different strategies: First Fit, Next Fit, Best Fit, and Worst Fit. The class also handles memory allocation using a bitmap to track which units of memory are free or allocated.

**Details:**  
The `MemoryManager` maintains an `ArrayList` of `MemorySegment` objects representing segments of memory. The allocation strategies are implemented as follows:
- **First Fit:** Allocates the first block of memory large enough to accommodate the process. This strategy is simple and fast but can lead to fragmentation over time.
- **Next Fit:** Similar to First Fit but starts from the last allocated position. This can help distribute memory allocation more evenly but may still suffer from fragmentation.
- **Best Fit:** Allocates the smallest block of memory that fits the process’s requirements. This strategy aims to minimize wasted space but can be slower due to the need to search the entire memory list.
- **Worst Fit:** Allocates the largest available block. This strategy can leave large holes in memory, potentially useful for future large allocations but can lead to inefficient memory use.

The `MemoryManager` class provides methods to allocate memory (`allocate`), free memory (`free`), and compact memory (`compact`). The `allocate` method throws a `NoEnoughMemoryException` if there isn't enough memory, while the `free` method throws an `IllegalAddressException` for invalid addresses. The `compact` method consolidates free memory spaces to reduce fragmentation, thus improving memory utilization.

The use of a bitmap to track free and allocated units of memory allows for efficient management and quick checks of memory status, making the allocation and deallocation processes more efficient.

### mmu.java
**Purpose:**  
This file contains the main logic for a Memory Management Unit (MMU) simulator. It interacts with the `MemoryManager` and `Process` classes to simulate creating processes, allocating memory, converting virtual addresses to physical addresses, and deallocating memory. It likely serves as the command-line interface for interacting with the memory manager, parsing commands and executing corresponding actions.

**Details:**  
The `mmu` class has a `MemoryManager` instance to handle memory operations. It provides methods to allocate memory for a process (`allocateProcess`), free memory occupied by a process (`freeProcess`), and compact memory (`compactMemory`). The `allocateProcess` method throws a `NoEnoughMemoryException` if memory allocation fails, while the `freeProcess` method throws an `IllegalAddressException` for invalid addresses and a `ProcessNotFoundException` if the process is not found.

The `mmu` acts as an intermediary between the processes and the memory manager, ensuring that each process gets the required memory allocation and that deallocation is handled correctly. It also translates virtual addresses to physical addresses, enabling processes to operate in a virtual memory space. This simulation helps in understanding how memory management units work in real operating systems and the complexities involved in managing process memory.

### NoEnoughMemoryException.java
**Purpose:**  
This file defines another custom exception used when there isn't enough free memory to allocate to a process. It ensures that the system can handle memory allocation failures gracefully.

**Details:**  
The `NoEnoughMemoryException` class extends the `Exception` class, making it a checked exception. When there is insufficient memory to fulfill an allocation request, this exception is thrown with a message indicating the failure. This allows the system to manage memory allocation attempts and handle errors without crashing. By using this exception, the memory manager can provide clear feedback on why a memory allocation failed, aiding in debugging and system reliability.

### Process.java
**Purpose:**  
This class represents a process in the system. It includes process attributes such as process ID, memory base address, limit (size), and possibly other process-related metadata. This class is essential for keeping track of each process's memory allocation and limits.

**Details:**  
The `Process` class has attributes for the process ID (`id`), memory size (`size`), and starting address of the allocated memory (`address`). The `address` is initialized to -1 to indicate an unallocated state. The class provides getter methods for each attribute and a setter method for the `address`. This encapsulation ensures that process information is managed systematically and allows the MMU to allocate and deallocate memory effectively.

By representing processes as objects, the system can easily manage multiple processes and their memory requirements. The process metadata helps in keeping track of each process's memory usage and ensures that the memory manager can allocate and free memory accurately.

### ProcessNotFoundException.java
**Purpose:**  
This exception is thrown when a non-existent process is referenced, for example, when trying to deallocate memory for a process that has already been removed or was never created.

**Details:**  
The `ProcessNotFoundException` class extends the `Exception` class, making it a checked exception. It ensures that the system can detect and handle situations where an operation references a process that does not exist. This is crucial for maintaining system stability and preventing errors due to invalid process references. By throwing this exception, the system can provide clear feedback to the user or administrator about invalid operations, helping to prevent further errors and maintain data integrity.

### Repl.java
**Purpose:**  
This file appears to implement a Read-Eval-Print Loop (REPL) for the memory management system. It allows interactive command-line input to create processes, allocate memory, and perform other memory management tasks. This would be useful for testing and demonstrating the functionality of the memory manager in a dynamic, interactive way.

**Details:**  
The `Repl` class provides a command-line interface for interacting with the memory management system. It initializes an instance of the `mmu` class with the specified memory size and uses a `Scanner` object to read user input. The `start()` method begins the REPL loop, continuously prompting the user for commands until "exit" is entered. The `processCommand(String command)` method parses and executes the given command, supporting actions such as `allocate`, `free`, and `compact`. This interactive loop allows for dynamic testing and real-time management of the memory system.

The REPL interface is particularly useful for educational purposes, allowing users to experiment with memory management commands and see their effects immediately. This hands-on approach can help users understand the complexities of memory management and the importance of efficient algorithms.

## Conclusion
This project demonstrates a comprehensive memory management system typically found in operating systems. It handles memory allocation and deallocation for processes based on various strategies and uses custom exceptions to manage errors gracefully. The interactive command-line interface provided by the `Repl` class allows for dynamic testing and demonstration of the system's functionality. Each component, from custom exceptions to the core `MemoryManager` class, plays a crucial role in ensuring efficient and safe memory management.

The project highlights the importance of memory management in operating systems and the challenges involved in ensuring efficient and reliable memory usage. By implementing various allocation strategies and handling errors through custom exceptions, the system ensures robust and flexible memory management. The REPL interface adds an interactive element, making it easier to test and understand the system's behavior in different scenarios.
