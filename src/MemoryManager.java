import java.util.ArrayList;

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

    public Process allocBestFit(int amount) {
        int currentBase = -1, currentSize = 0, bestSize = 0, bestBase = -1;
        boolean started = false;

        for (int i = 0; i < memSize; i++) {
            if (memMap[i] == -1) {
                if (!started) {
                    started = true;
                    currentBase = i;

                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
                    if (memMap[i + amount - 1] != -1) {
                        // If so, this hole will not fit for the new process. Thus, move directly to [i + amount] and search. This avoids unnecessary traversal if the hole is too small.
                        i += amount - 1; // -1 because it will get incremented at the next iteration.
                    }
                } else {
                    currentSize++;
                }

            } else {
                if (amount == currentSize) {
                    // We found a perfect space for the process (requested amount is equal to the size of the hole)
                    // Therefore, allocate it directly to the process.

                    allocInMemMap(currentBase, amount, Process.count);
                    return new Process(currentBase, amount);
                }
                if (amount <= currentSize) { // if the hole we found is big enough for the requested amount
                    if (currentSize < bestSize) { // if it's better (less in size) than the best we've found so far
                        bestSize = currentSize;
                        bestBase = currentBase;
                    }
                }

                // Reinitialize the counters
                currentSize = 0;
                started = false;
            }
        }

        // Last check, in case the end of the memory is a hole
        if (amount <= currentSize) { // if the hole we found is big enough for the requested amount
            if (currentSize < bestSize) { // if it's better (less in size) than the best we've found so far
                bestSize = currentSize;
                bestBase = currentBase;
            }
        }

        // TODO: check if found a hole or not at all
        // TODO: update the memMap
        // TODO: only keep track of size, because we'll not need bestSize
        return new Process(bestBase, amount);
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
