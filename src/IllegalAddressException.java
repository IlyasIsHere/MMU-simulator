public class IllegalAddressException extends Exception {
    public IllegalAddressException() {
        super("The address you're trying to access is outside the process's address space.");
    }
}
