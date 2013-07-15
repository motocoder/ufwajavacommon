package llc.ufwa.data.resource.cache;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.ResourceLoader;

public interface Cache<TKey, TValue> extends ResourceLoader<TKey, TValue> {

    /**
     * @throws ResourceException 
     * 
     */
	void clear() throws ResourceException;
	
	/**
	 * 
	 * @param key
	 */
	void remove(TKey key) throws ResourceException;
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	void put(TKey key, TValue value) throws ResourceException;
	
}
