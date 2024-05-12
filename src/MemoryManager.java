import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MAX_VALUE;

public class    MemoryManager {

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
        bitmap = new int[memSize];
        Arrays.fill(bitmap, 0);

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
            case mmu.NEXT_FIT:
                result = allocNextFit(amount);
                break;
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

    private Process allocNextFit(int amount) throws NoEnoughMemoryException {
        int currentHoleStart = -1;
        int currentHoleSize = 0;

        int i = lastPos;  // Start searching from the last position
        int count = 0;    // Counter to track the number of checked positions

        // Loop around the bitmap array to simulate a circular search
        do {
            if (bitmap[i] == 0) {  // If the current slot is free
                if (currentHoleStart == -1) {  // Start of a new hole
                    currentHoleStart = i;
                }
                currentHoleSize++;

                // Check if the current hole size is enough to allocate the requested memory
                if (currentHoleSize >= amount) {
                    // Allocate memory in the bitmap
                    allocInMemMap(currentHoleStart, amount);
                    lastPos = currentHoleStart + amount;  // Update lastPos to the position after the newly allocated block
                    return new Process(currentHoleStart, amount);
                }
            } else {
                // Reset hole tracking variables
                currentHoleStart = -1;
                currentHoleSize = 0;
            }

            // Move to the next position, wrapping around if necessary
            i = (i + 1) % memSize;
            count++;
        } while (count < memSize);  // Continue until the whole bitmap is checked

        // If no suitable hole is found
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
        int idx = findProcessById(processID);
        if (idx == -1) {
            throw new ProcessNotFoundException(processID);
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

    /**
     * Sets the allocation units of the given process to -1 in the memory map.
     *
     * @param p
     */
    private void freeProcessMemory(Process p) {
        for (int i = p.getBase(); i < p.getBase() + p.getLimit(); i++) {
            bitmap[i] = 0;
        }
    }

    public int convertAddress(int processID, int virtualAddress) throws ProcessNotFoundException, IllegalAddressException {
        int idx = findProcessById(processID);
        if (idx == -1) {
            throw new ProcessNotFoundException(processID);
        }
        Process process = processes.get(idx);
        if (virtualAddress >= process.getLimit() || virtualAddress < 0) {
            throw new IllegalAddressException();
        }

        return process.getBase() + virtualAddress;
    }

    // Method to print current memory state
    public void printMemory() {
        if (processes.isEmpty()) {
            System.out.println("Memory is empty.");
        } else {
            for (Process process : processes) {
                System.out.println("Process ID: " + process.getId() + ", Base: " + process.getBase() + ", Limit: " + process.getLimit());
            }
        }
    }

    public int getMemSize() {
        return memSize;
    }

    public void setMemSize(int memSize) {
        this.memSize = memSize;
    }

    public int[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(int[] bitmap) {
        this.bitmap = bitmap;
    }

    public int getFitStrategy() {
        return fitStrategy;
    }

    public void setFitStrategy(int fitStrategy) {
        this.fitStrategy = fitStrategy;
    }
}
