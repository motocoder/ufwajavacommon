package llc.ufwa.data.resource.loader;

import java.util.ArrayList;
import java.util.List;

import llc.ufwa.data.exception.ResourceException;

public abstract class DefaultResourceLoader<TKey, TValue> implements ResourceLoader<TKey, TValue> {

	@Override
	public boolean exists(TKey key) throws ResourceException {
		return get(key) != null;
	}

	@Override
	public List<TValue> getAll(List<TKey> keys) throws ResourceException {
		
		final List<TValue> returnVals = new ArrayList<TValue>();
		
		for(TKey key : keys) {
			returnVals.add(get(key));
		}
		
		return returnVals;
		
	}

}
