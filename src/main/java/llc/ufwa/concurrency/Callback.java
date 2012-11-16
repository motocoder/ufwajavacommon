package llc.ufwa.concurrency;

/**
 * 
 * @author seanwagner
 *
 * @param <Source>
 * @param <Value>
 */
public interface Callback<Source, Value> {
	
	boolean call(Source source, Value value);

}
