import java.util.ArrayList;
import java.util.Arrays;

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

    /**
     * The last position where the counter i on the memMap left off last time (for Next Fit strategy).
     * For other strategies, it is set to -1 (not used).
     */
    private int lastPos = -1;
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
        Arrays.fill(memMap, -1);

        if (fitStrategy <= 0 || fitStrategy > 4) {
            throw new IllegalArgumentException("The allocation strategy must be between 1 and 4");
        }
        this.fitStrategy = fitStrategy;
        processes = new ArrayList<>();

        if (fitStrategy == mmu.NEXT_FIT) {
            lastPos = 0;
        }
    }

    public Process allocateMemory(int amount) throws NoEnoughMemoryException {
        Process result = null;

        switch (fitStrategy) {
            case mmu.FIRST_FIT:
                result = allocFirstFit(amount);
                break;
//            case mmu.NEXT_FIT:
//                result = allocNextFit(amount);
//                break;
            case mmu.BEST_FIT:
                result = allocBestFit(amount);
                break;
            case mmu.WORST_FIT:
                result = allocWorstFit(amount);
                break;
        }
        processes.add(result);
        return result;
    }

    private Process allocFirstFit(int amount) throws NoEnoughMemoryException {
        int currentHoleStart = -1;
        int currentHoleSize = 0;

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
                if (amount <= currentHoleSize) {
                    // Found a hole that fits the requested amount, so choose it.
                    allocInMemMap(currentHoleStart, amount, Process.count);
                    return new Process(currentHoleStart, amount);
                }

                // Reinitialize the counters
                currentHoleSize = 0;
                currentHoleStart = -1;
            }

            // Check if we've reached the end of memory and handle the case where the end of the memory is a hole
            if (i == memSize - 1 && amount <= currentHoleSize) {
                allocInMemMap(currentHoleStart, amount, Process.count);
                return new Process(currentHoleStart, amount);
            }
        }

        // If we reach here, it means that there was no hole that would fit (otherwise, we would have chosen it).
        throw new NoEnoughMemoryException();
    }

//    private Process allocNextFit(int amount) throws NoEnoughMemoryException {
//        int currentHoleStart = -1;
//        int currentHoleSize = 0;
//
//        for (int i = lastPos, count = 0; count < memSize; i = (i + 1) % memSize, count++) {
//
//        }
//
//        for (i = lastPos; i < memSize; i++) {
//            // Checking if memMap[i] is an available allocation unit
//            if (memMap[i] == -1) {
//                if (currentHoleStart == -1) { // This means that this is the start of a hole
//                    currentHoleStart = i;
//
//                    // Check if we're near the end of the memory
//                    if (i + amount - 1 >= memSize) {
//                        break;
//                    }
//
//                    // Check if the unit at [start of this hole + the requested amount - 1] is already allocated
//                    if (memMap[i + amount - 1] != -1) {
//                        // If so, this hole will not fit for the new process. Thus, move directly to [i + amount] and search. This avoids unnecessary traversal if the hole is too small.
//                        i += amount - 1; // -1 because it will get incremented at the next iteration.
//                        currentHoleStart = -1;
//                        continue;
//                    }
//                }
//                currentHoleSize++;
//
//            } else {
//                if (amount <= currentHoleSize) {
//                    // Found a hole that fits the requested amount, so choose it.
//                    allocInMemMap(currentHoleStart, amount, Process.count);
//                    return new Process(currentHoleStart, amount);
//                }
//
//                // Reinitialize the counters
//                currentHoleSize = 0;
//                currentHoleStart = -1;
//            }
//
//            // Check if we've reached the end of memory and handle the case where the end of the memory is a hole
//            if (i == memSize - 1 && amount <= currentHoleSize) {
//                allocInMemMap(currentHoleStart, amount, Process.count);
//                return new Process(currentHoleStart, amount);
//            }
//        }
//
//        // If we reach here, it means that there was no hole that would fit (otherwise, we would have chosen it).
//        throw new NoEnoughMemoryException();
//    }

    private Process allocBestFit(int amount) throws NoEnoughMemoryException {
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
            throw new NoEnoughMemoryException();
        }

        // Updating the memory map
        allocInMemMap(bestHoleStart, amount, Process.count);
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
        allocInMemMap(worstFitHoleStart, amount, Process.count);
        return new Process(worstFitHoleStart, amount);
    }

    private void allocInMemMap(int base, int amount, int processID) {
        for (int i = base; i < base + amount; i++) {
            this.memMap[i] = processID;
        }
    }

    public void deleteProcess(int processID) throws ProcessNotFoundException {
        int idx = findProcessById(processID);
        if (idx == -1) {
            throw new ProcessNotFoundException();
        }
        Process process = processes.get(idx);
        processes.remove(idx);
        freeProcessMemory(process);
    }

    private int findProcessById(int id) {
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    // TODO: should i really store the process id in the allocated units???

    /**
     * Sets the allocation units of the given process to -1 in the memory map.
     *
     * @param p
     */
    private void freeProcessMemory(Process p) {
        for (int i = p.getBase(); i < p.getBase() + p.getLimit(); i++) {
            memMap[i] = -1;
        }
    }

    public int convertAddress(int processID, int virtualAddress) throws ProcessNotFoundException, IllegalAddressException {
        int idx = findProcessById(processID);
        if (idx == -1) {
            throw new ProcessNotFoundException();
        }
        Process process = processes.get(idx);
        if (virtualAddress >= process.getLimit() || virtualAddress < 0) {
            throw new IllegalAddressException();
        }

        return process.getBase() + virtualAddress;
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
