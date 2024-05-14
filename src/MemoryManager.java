import java.util.*;

import static java.lang.Integer.MAX_VALUE;

public class MemoryManager {

    /**
     * The size of the memory in KB
     */
    private int memSize;

    /**
     * This is the bitmap. For each allocation unit (1 KB) in the memory, the corresponding entry in this array is a bit indicating whether that unit is available or not (0 if it's free, 1 if it's allocated).
     */
    private int[] bitmap;

    private int fitStrategy;

    /**
     * The last position where the counter i on the memMap left off last time (for Next Fit strategy).
     * For other strategies, it is set to -1 (not used).
     */
    private int lastPos = -1;

    /**
     * This is a TreeMap of processes where in each entry, the key represents the id of the process, and the value represents the actual Process object.
     * This TreeMap is sorted by keys (process IDs).
     */
    private Map<Integer, Process> processes;

    /**
     * @param memSize     Memory size in KB
     * @param fitStrategy The allocation strategy: <p>
     *                    First fit: 1 <p>
     *                    Next fit: 2 <p>
     *                    Best fit: 3 <p>
     *                    Worst fit: 4 <p>
     */
    public MemoryManager(int memSize, int fitStrategy) {
        this.memSize = memSize;
        bitmap = new int[memSize];
        Arrays.fill(bitmap, 0);

        if (fitStrategy <= 0 || fitStrategy > 4) {
            throw new IllegalArgumentException("The allocation strategy must be between 1 and 4");
        }
        this.fitStrategy = fitStrategy;
        processes = new TreeMap<>();

        if (fitStrategy == mmu.NEXT_FIT) {
            lastPos = 0;
        }
    }

    public Process allocateMemory(int amount) throws NoEnoughMemoryException {
        Process result = switch (fitStrategy) {
            case mmu.FIRST_FIT -> allocFirstFit(amount);
            case mmu.NEXT_FIT -> allocNextFit(amount);
            case mmu.BEST_FIT -> allocBestFit(amount);
            case mmu.WORST_FIT -> allocWorstFit(amount);
            default -> null;
        };

        assert result != null;
        processes.put(result.getId(), result);
        return result;
    }

    private Process allocFirstFit(int amount) throws NoEnoughMemoryException {
        int currentHoleStart = -1;
        int currentHoleSize = 0;

        int i;
        for (i = 0; i < memSize; i++) {
            // Checking if memMap[i] is an available allocation unit
            if (bitmap[i] == 0) {
                if (currentHoleStart == -1) { // This means that this is the start of a hole
                    currentHoleStart = i;

                    // Check if we're near the end of the memory
                    if (i + amount - 1 >= memSize) {
                        break;
                    }

                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
                    if (bitmap[i + amount - 1] == 1) {
                        // If so, this hole will not fit for the new process. Thus, move directly to [i + amount] and search. This avoids unnecessary traversal if the hole is too small.
                        i += amount - 1; // -1 because it will get incremented at the next iteration.
                        currentHoleStart = -1;
                        continue;
                    }
                }
                currentHoleSize++;

            } else {
                if (amount <= currentHoleSize) {
                    // Found a hole that fits the requested amount, so choose it.
                    allocInMemMap(currentHoleStart, amount);
                    return new Process(currentHoleStart, amount);
                }

                // Reinitialize the counters
                currentHoleSize = 0;
                currentHoleStart = -1;
            }

            // Check if we've reached the end of memory and handle the case where the end of the memory is a hole
            if (i == memSize - 1 && amount <= currentHoleSize) {
                allocInMemMap(currentHoleStart, amount);
                return new Process(currentHoleStart, amount);
            }
        }

        // If we reach here, it means that there was no hole that would fit (otherwise, we would have chosen it).
        throw new NoEnoughMemoryException();
    }

    private Process allocNextFit(int amount) throws NoEnoughMemoryException {
        int startPos = lastPos;
        int freeCount = 0;
        int base = -1;

        do {
            if (lastPos == 0) {
                // The pointer returned back to the beginning of the memory, so reset the counters, so that the allocation is contiguous.
                base = -1;
                freeCount = 0;
            }

            if (bitmap[lastPos] == 0) {
                if (freeCount == 0) {
                    // It's the beginning of a hole
                    base = lastPos;
                }
                freeCount++;
                if (freeCount == amount) {
                    break;
                }
            } else {
                // It's an allocated block, so reset the counters
                freeCount = 0;
                base = -1;
            }

            lastPos = (lastPos + 1) % memSize;
        } while (lastPos != startPos);

        // Check if there are more available units:
        while (lastPos < memSize && bitmap[lastPos] == 0 && freeCount < amount) {
            lastPos++;
            freeCount++;
        }

        if (freeCount == amount) {
            allocInMemMap(base, amount);
            lastPos = (lastPos + 1) % memSize;
            return new Process(base, amount);
        }

        // If we reach here, it means that there was no hole that would fit (otherwise, we would have chosen it).
        lastPos = startPos;
        throw new NoEnoughMemoryException();
    }

    private Process allocBestFit(int amount) throws NoEnoughMemoryException {
        int currentHoleStart = -1;
        int currentHoleSize = 0;
        int bestHoleSize = MAX_VALUE;
        int bestHoleStart = -1;

        int i;
        for (i = 0; i < memSize; i++) {
            // Checking if memMap[i] is an available allocation unit
            if (bitmap[i] == 0) {
                if (currentHoleStart == -1) { // This means that this is the start of a hole
                    currentHoleStart = i;

                    // Check if we're near the end of the memory
                    if (i + amount - 1 >= memSize) {
                        break;
                    }

                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
                    if (bitmap[i + amount - 1] == 1) {
                        // If so, this hole will not fit for the new process. Thus, move directly to [i + amount] and search. This avoids unnecessary traversal if the hole is too small.
                        i += amount - 1; // -1 because it will get incremented at the next iteration.
                        currentHoleStart = -1;
                        continue;
                    }
                }
                currentHoleSize++;

            } else {
                if (amount == currentHoleSize) {
                    // We found a perfect space for the process (requested amount is equal to the size of the hole)
                    // Therefore, allocate it directly to the process.
                    allocInMemMap(currentHoleStart, amount);
                    return new Process(currentHoleStart, amount);

                } else if (amount < currentHoleSize && currentHoleSize < bestHoleSize) { // if it fits the requested amount, and it's better (less in size) than the best we've found so far
                    bestHoleSize = currentHoleSize;
                    bestHoleStart = currentHoleStart;
                }

                // Reinitialize the counters
                currentHoleSize = 0;
                currentHoleStart = -1;
            }

            // Check if we've reached the end of memory and handle the case where the end of the memory is a hole
            if (i == memSize - 1 && amount <= currentHoleSize && currentHoleSize < bestHoleSize) {
                bestHoleStart = currentHoleStart;
            }
        }


        // Checking if we found at least one space that fits
        if (bestHoleStart == -1) {
            throw new NoEnoughMemoryException();
        }

        // Updating the memory map
        allocInMemMap(bestHoleStart, amount);
        return new Process(bestHoleStart, amount);
    }

    private Process allocWorstFit(int amount) throws NoEnoughMemoryException {
        int currentHoleStart = -1;
        int currentHoleSize = 0;
        int worstFitHoleSize = 0;
        int worstFitHoleStart = -1;

        int i;
        for (i = 0; i < memSize; i++) {
            // Checking if memMap[i] is an available allocation unit
            if (bitmap[i] == 0) {
                if (currentHoleStart == -1) { // This means that this is the start of a hole
                    currentHoleStart = i;

                    // Check if we're near the end of the memory
                    if (i + amount - 1 >= memSize) {
                        break;
                    }

                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
                    if (bitmap[i + amount - 1] == 1) {
                        // If so, this hole will not fit for the new process. Thus, move directly to [i + amount] and search. This avoids unnecessary traversal if the hole is too small.
                        i += amount - 1; // -1 because it will get incremented at the next iteration.
                        currentHoleStart = -1;
                        continue;
                    }
                }
                currentHoleSize++;

            } else {
                if (amount <= currentHoleSize && currentHoleSize > worstFitHoleSize) { // if it fits the requested amount, and it's worse (bigger) than the worst we've found so far
                    worstFitHoleSize = currentHoleSize;
                    worstFitHoleStart = currentHoleStart;
                }

                // Reinitialize the counters
                currentHoleSize = 0;
                currentHoleStart = -1;
            }

            // Check if we've reached the end of memory and handle the case where the end of the memory is a hole
            if (i == memSize - 1 && amount <= currentHoleSize && currentHoleSize > worstFitHoleSize) {
                worstFitHoleStart = currentHoleStart;
            }
        }


        // Checking if we found at least one space that fits
        if (worstFitHoleStart == -1) {
            throw new NoEnoughMemoryException();
        }

        // Updating the memory map
        allocInMemMap(worstFitHoleStart, amount);
        return new Process(worstFitHoleStart, amount);
    }

    private void allocInMemMap(int base, int amount) {
        for (int i = base; i < base + amount; i++) {
            this.bitmap[i] = 1;
        }
    }

    public void deleteProcess(int processID) throws ProcessNotFoundException {
        if (!processes.containsKey(processID)) {
            throw new ProcessNotFoundException(processID);
        }
        Process process = processes.get(processID);
        processes.remove(processID);
        freeProcessMemory(process);
    }

    /**
     * Sets the allocation units of the given process to 0 in the memory map.
     *
     * @param p
     */
    private void freeProcessMemory(Process p) {
        for (int i = p.getBase(); i < p.getBase() + p.getLimit(); i++) {
            bitmap[i] = 0;
        }
    }

    public int convertAddress(int processID, int virtualAddress) throws ProcessNotFoundException, IllegalAddressException {
        if (!processes.containsKey(processID)) {
            throw new ProcessNotFoundException(processID);
        }

        Process process = processes.get(processID);
        if (virtualAddress >= process.getLimit() || virtualAddress < 0) {
            throw new IllegalAddressException();
        }

        return process.getBase() + virtualAddress;
    }

    // Method to print current memory state
    public void printMemory() {
        ArrayList<Process> sortedProcesses = new ArrayList<>(processes.values());
        // Sort the processes based on their base addresses
        sortedProcesses.sort(Comparator.comparingInt(Process::getBase));

        System.out.println("Memory Map:");
        int prevPos = 0;

        // Print the header
        System.out.print("|");
        for (Process process : sortedProcesses) {
            for (int i = prevPos; i < process.getBase(); i++) {
                System.out.print(" Free |");
            }
            for (int i = process.getBase(); i < process.getBase() + process.getLimit(); i++) {
                System.out.printf(" %-4d |", process.getId());
            }
            prevPos = process.getBase() + process.getLimit();
        }

        // Printing the last block if it was empty
        for (int i = prevPos; i < memSize; i++) {
            System.out.print(" Free |");
        }
        System.out.println();
        System.out.println("Next fit last position : " + lastPos);
    }

}
