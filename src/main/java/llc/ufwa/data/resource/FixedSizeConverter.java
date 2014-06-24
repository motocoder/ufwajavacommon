package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public class FixedSizeConverter<Value> implements Converter<Integer, Value> {

	private final int size;

	public FixedSizeConverter(final int size) {
		
		this.size = size;
		
	}
	@Override
	public Integer restore(Value old) throws ResourceException {
		return size;
	}

	@Override
	public Value convert(Integer newVal) throws ResourceException {
		throw new RuntimeException("Not supported");
	}

}
