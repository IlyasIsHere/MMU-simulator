import java.util.ArrayList;
import java.util.Scanner;


public class mmu {
    // constants for different allocation strategies
    public static final int FIRST_FIT = 1;
    public static final int NEXT_FIT = 2;
    public static final int BEST_FIT = 3;
    public static final int WORST_FIT = 4;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Error: Wrong number of arguments. You should provide the size of the memory and the allocation strategy.");
            System.err.println("Correct usage: java mmu <size> <allocation strategy>");
            System.exit(1);
        }

        MemoryManager mm = null;
        try {
            int memSize = Integer.parseInt(args[0]);
            int allocStrategy = Integer.parseInt(args[1]);

            mm = new MemoryManager(memSize, allocStrategy);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        Scanner sc = new Scanner(System.in);
        Repl repl = new Repl(sc, mm);
        repl.start();

        sc.close();
    }
}