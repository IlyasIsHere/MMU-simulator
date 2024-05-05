public class ProcessNotFoundException extends Exception {
    public ProcessNotFoundException(int id) {
        super("No process found with id " + id);
    }
}
