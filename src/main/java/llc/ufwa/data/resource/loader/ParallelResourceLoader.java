package llc.ufwa.data.resource.loader;

import java.util.Map;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

/**
 *  
 *  The ParallelResourceLoader interface is an extension of the ResourceLoader interface 
 *  and adds functionality to make asynchronous calls.
 *  
 *  
 *
 */

/**
 * 
 * @author seanwagner
 *
 * @param <Key>
 * @param <Value>
 */
public interface ParallelResourceLoader<Key, Value> extends ResourceLoader<Key, Value>{

    /**
     * CallbackControl getParallel(
     *  Callback<Object, ResourceEvent<Value>> onComplete, 
     *  final Key key) – onComplete is called with the newly loaded value upon completion of 
     *  the resource loader call. CallbackControl has one method, cancel which allows you to 
     *  cancel the request before it completes.
     * 
     * @return CallbackControl
     * @param onComplete
     * @param key
     */
	CallbackControl getParallel(final Callback<Object, ResourceEvent<Value>> onComplete, final Key key) throws ResourceException;
	
	/**
	 * CallbackControl existsParallel(
	 *  Callback<Object, ResourceEvent<Boolean>> onComplete, 
	 *  final Key key) – onComplete is called with the newly loaded value upon completion of 
	 *  the resource loader call. CallbackControl has one method, cancel which allows you to 
	 *  cancel the request before it completes.
	 * 
	 * @return CallbackControl
	 * @param onComplete
	 * @param key
	 */
	CallbackControl existsParallel(final Callback<Object, ResourceEvent<Boolean>> onComplete, final Key key);

	/**
	 * void getAllParallel(final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap) - 
	 *  This method works with the same principals as getParallel and existsParallel except it is 
	 *  a bulk request. There is no CallbackControl returned with it.
	 * 
	 * @param callbackMap
	 * @throws ResourceException 
	 */
	void getAllParallel(final Map<Key, Callback<Object, ResourceEvent<Value>>> callbackMap) throws ResourceException;
	
}
