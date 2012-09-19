package llc.ufwa.data.resource.loader;

import java.util.List;

import llc.ufwa.data.exception.ResourceException;

/**
 * 
 * @author seanwagner
 *
 * @param <TKey>
 * @param <TValue>
 */
public interface ResourceLoader<TKey, TValue> {
	
    /**
     * 
     * @param key
     * @return
     * @throws ResourceException
     */
	boolean exists(TKey key) throws ResourceException;
	
	/**
	 * 
	 * @param key
	 * @return
	 * @throws ResourceException
	 */
	TValue get(TKey key) throws ResourceException;
	
	/**
	 * 
	 * @param keys
	 * @return - The list returned must have the same length as the input list of keys
	 *           and the order values returned must match their associated keys.
	 * @throws ResourceException
	 */
	List<TValue> getAll(List<TKey> keys) throws ResourceException;

}
