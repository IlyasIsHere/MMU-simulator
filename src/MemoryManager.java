import java.util.ArrayList;

import static java.lang.Integer.MAX_VALUE;

public class MemoryManager {

    /**
     * The size of the memory in KB
     */
    private int memSize;

    /**
     * For each allocation unit (1 KB) in the memory, the corresponding entry in this array is the id of the process to which it's allocated. If that allocation unit is free, the value in the memMap is -1.
     */
    private int[] memMap;

    private int fitStrategy;
    private ArrayList<Process> processes;

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
        memMap = new int[memSize];
        this.fitStrategy = fitStrategy;
        processes = new ArrayList<>();
    }

    public int allocFirstFit(int amount) {

    }

    public int allocNextFit(int amount) {

    }

    public Process allocBestFit(int amount) throws Exception {
        int currentHoleStart = -1;
        int currentHoleSize = 0;
        int bestHoleSize = MAX_VALUE;
        int bestHoleStart = -1;

        int i;
        for (i = 0; i < memSize; i++) {
            // Checking if memMap[i] is an available allocation unit
            if (memMap[i] == -1) {
                if (currentHoleStart == -1) { // This means that this is the start of a hole
                    currentHoleStart = i;

                    // Check if we're near the end of the memory
                    if (i + amount - 1 >= memSize) {
                        break;
                    }

                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
                    if (memMap[i + amount - 1] != -1) {
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
                    allocInMemMap(currentHoleStart, amount, Process.count);
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
            throw new Exception("Error: There is no enough space in the memory.");
        }

        // Updating the memory map
        allocInMemMap(bestHoleStart, amount, Process.count);
        return new Process(bestHoleStart, amount);
    }

    public int allocWorstFit(int amount) {

    }

    public void allocInMemMap(int base, int amount, int processID) {
        for (int i = base; i < base + amount; i++) {
            this.memMap[i] = processID;
        }
    }

    public int getMemSize() {
        return memSize;
    }

    public void setMemSize(int memSize) {
        this.memSize = memSize;
    }

    public int[] getMemMap() {
        return memMap;
    }

    public void setMemMap(int[] memMap) {
        this.memMap = memMap;
    }

    public int getFitStrategy() {
        return fitStrategy;
    }

    public void setFitStrategy(int fitStrategy) {
        this.fitStrategy = fitStrategy;
    }
}
