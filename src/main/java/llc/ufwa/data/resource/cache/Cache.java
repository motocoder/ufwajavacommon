package llc.ufwa.data.resource.cache;

import llc.ufwa.data.resource.loader.ResourceLoader;

public interface Cache<TKey, TValue> extends ResourceLoader<TKey, TValue> {

    /**
     * 
     */
	void clear() ;
	
	/**
	 * 
	 * @param key
	 */
	void remove(TKey key);
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	void put(TKey key, TValue value);
	
}
