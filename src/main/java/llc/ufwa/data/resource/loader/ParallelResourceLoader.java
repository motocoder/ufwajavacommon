package llc.ufwa.data.resource.loader;

import java.util.Map;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

/**
 * 
 * @author seanwagner
 *
 * @param <Key>
 * @param <Value>
 */
public interface ParallelResourceLoader<Key, Value> extends ResourceLoader<Key, Value>{

    /**
     * 
     * @param onComplete
     * @param key
     */
	CallbackControl getParallel(final Callback<Object, ResourceEvent<Value>> onComplete, final Key key) throws ResourceException;
	
	/**
	 * 
	 * @param onComplete
	 * @param key
	 */
	CallbackControl existsParallel(final Callback<Object, ResourceEvent<Boolean>> onComplete, final Key key);

	/**
	 * 
	 * @param callbackMap
	 * @throws ResourceException 
	 */
	void getAllParallel(final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap) throws ResourceException;
	
}
