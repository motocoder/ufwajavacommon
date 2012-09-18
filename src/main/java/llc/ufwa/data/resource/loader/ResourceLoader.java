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
	 * @return
	 * @throws ResourceException
	 */
	List<TValue> getAll(List<TKey> keys) throws ResourceException;

}
