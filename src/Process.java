public class Process {

    public static int count = 1;

    private int id;
    private int base;
    private int limit;

    public Process(int base, int limit) {
        this.id = count++;
        this.base = base;
        this.limit = limit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
