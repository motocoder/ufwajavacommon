package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public class LongStringConverter implements Converter<Long, String> {

	@Override
	public String convert(Long old) throws ResourceException {
		return old.toString();
	}

	@Override
	public Long restore(String newVal) throws ResourceException {
		return Long.parseLong(newVal);
	}

}
