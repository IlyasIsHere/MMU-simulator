public class NoEnoughMemoryException extends Exception {

    public NoEnoughMemoryException() {
        super("There is no enough space in the memory.");
    }
}
