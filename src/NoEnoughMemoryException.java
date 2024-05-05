public class NoEnoughMemoryException extends Exception {

    public NoEnoughMemoryException() {
        super("Error: There is no enough space in the memory.");
    }
}
