package llc.ufwa.concurrency;

/**
 * 
 * @author seanwagner
 *
 * @param <Source>
 * @param <Value>
 */
public interface Callback<ReturnType, Value> {
	
    ReturnType call(Value value);

}
