import java.util.ArrayList;
import java.util.Scanner;


public class mmu {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    // constants for different allocation strategies
    public static final int FIRST_FIT = 1;
    public static final int NEXT_FIT = 2;
    public static final int BEST_FIT = 3;
    public static final int WORST_FIT = 4;

    public static void main(String[] args) {
        if (args.length != 2) {
            printErr("Error: Wrong number of arguments. You should provide the size of the memory and the allocation strategy.");
            printErr("Correct usage: java mmu <size> <allocation strategy>");
            printErr("e.g: java mmu 200 3");
            System.exit(1);
        }

        MemoryManager mm = null;
        try {
            int memSize = Integer.parseInt(args[0]);
            int allocStrategy = Integer.parseInt(args[1]);

            mm = new MemoryManager(memSize, allocStrategy);
        } catch (Exception e) {
            printErr("Error: " + e.getMessage());
            System.exit(1);
        }

        Scanner sc = new Scanner(System.in);
        Repl repl = new Repl(sc, mm);
        repl.start();

        sc.close();
    }

    // Custom printing method to print red-colored text in the terminal
    public static void printErr(String s) {
        System.out.println(ANSI_RED + s + ANSI_RESET);
    }
}

