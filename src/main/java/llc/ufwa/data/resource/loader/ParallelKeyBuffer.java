package llc.ufwa.data.resource.loader;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;

public class ParallelKeyBuffer<Key, Value> {
	
	private static final Logger logger = LoggerFactory.getLogger(ParallelKeyBuffer.class);
	
	public ParallelKeyBuffer(
	    final ParallelResourceLoader<Key, Value> loader,
	    final Set<Key> keys,
	    final Callback<Void, Set<Key>> onComplete
	) {
		
		final Set<Key> failed = new HashSet<Key>();
		final Set<Key> success = new HashSet<Key>();
		
		for(final Key key : keys) {
			
			try {
				
				loader.getParallel(
				    new Callback<Object, ResourceEvent<Value>>() {

						@Override
						public Object call(ResourceEvent<Value> value) {
							if(value == null) {
								failed.add(key);
							}
							else if(value.getThrowable() != null) {
								failed.add(key);
							}
							else {
								success.add(key);
							}
							
							if(failed.size() + success.size() == keys.size()) {
								onComplete.call(failed);
							}
							return null;
						}},
						key
				    );
			}
			catch (ResourceException e) {
				
				failed.add(key);
				logger.error("ERROR", e);
				
				if(failed.size() + success.size() == keys.size()) {
					onComplete.call(failed);
				}
				
			}
			
		}
		
	}

}
