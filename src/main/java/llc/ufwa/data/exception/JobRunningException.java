package llc.ufwa.data.exception;

public class JobRunningException extends Exception {

    private static final long serialVersionUID = 1070360963991023109L;
    
    public JobRunningException(Exception e) {
        super(e);
    }
    
    public JobRunningException(String e) {
        super(e);
    }
    
}
