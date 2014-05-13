package llc.ufwa.data.resource.provider;

import llc.ufwa.data.exception.ResourceException;

public class SequentialIDProvider implements ResourceProvider<Long> {

	private long id;
	
	@Override
	public boolean exists() throws ResourceException {
		return true;
	}

	@Override
	public Long provide() throws ResourceException {
		return id++;
	}

}
