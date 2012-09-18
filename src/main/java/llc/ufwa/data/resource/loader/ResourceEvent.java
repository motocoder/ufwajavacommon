package llc.ufwa.data.resource.loader;

public class ResourceEvent<Value> {
    
	public static final int NEW_LOADED = 0;
	public static final int CACHED = 1;
	public static final int UNKNOWN = 2;
	
    private final Value val;
    private final Throwable throwable;
	private final int valueType;

    public ResourceEvent(
        final Value val,
        final Throwable throwable,
        final int valueType
    ) {
        
    	this.valueType = valueType;
        this.val = val;
        this.throwable = throwable;
        
    }
    
    public int getValueType() {
		return valueType;
	}
    
	public Value getVal() {
        return val;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
