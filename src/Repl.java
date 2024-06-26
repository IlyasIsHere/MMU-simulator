import java.util.Scanner;

enum Command {
    EXIT,
    CREATE,
    DELETE,
    CONVERT,
    PRINT_MEMORY,
    UNKNOWN
}

public class Repl {

    public Scanner sc;
    public MemoryManager memoryManager;

    public Repl(Scanner sc, MemoryManager memoryManager) {
        this.sc = sc;
        this.memoryManager = memoryManager;
    }

    public static Command parseCommand(String input) {
        return switch (input) {
            case "cr" -> Command.CREATE;
            case "dl" -> Command.DELETE;
            case "cv" -> Command.CONVERT;
            case "pm" -> Command.PRINT_MEMORY;
            case "exit" -> Command.EXIT;
            default -> Command.UNKNOWN;
        };
    }

    public void start() {
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().toLowerCase();
            String[] parts = input.split(" ");
            Command command = parseCommand(parts[0]);
            int id;

            try {
                switch (command) {
                    case CREATE:
                        if (parts.length != 2) {
                            throw new Exception("Expected 1 argument, got " + (parts.length - 1));
                        }
                        int amount = Integer.parseInt(parts[1]);
                        Process createdProcess = memoryManager.allocateMemory(amount);

                        System.out.println("Created process ID: " + createdProcess.getId());
                        System.out.println("Base: " + createdProcess.getBase());
                        System.out.println("Limit: " + createdProcess.getLimit());
                        break;

                    case DELETE:
                        if (parts.length != 2) {
                            throw new Exception("Expected 1 argument, got " + (parts.length - 1));
                        }
                        id = Integer.parseInt(parts[1]);
                        memoryManager.deleteProcess(id);
                        break;

                    case CONVERT:
                        if (parts.length != 3) {
                            throw new Exception("Expected 2 argument, got " + (parts.length - 1));
                        }
                        id = Integer.parseInt(parts[1]);
                        int virtualAddress = Integer.parseInt(parts[2]);
                        int physicalAddress = memoryManager.convertAddress(id, virtualAddress);
                        System.out.println(physicalAddress);
                        break;

                    case PRINT_MEMORY:
                        memoryManager.printMemory();
                        break;

                    case EXIT:
                        System.exit(0);
                        break;

                    case UNKNOWN:
                        throw new Exception("Unknown command");
                }
            } catch (Exception e) {
                mmu.printErr("Error: " + e.getMessage());
                continue;
            }
        }
    }
}

