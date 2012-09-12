package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public class ByteSizeConverter implements Converter<byte [], Integer> {

    @Override
    public Integer convert(byte[] old) throws ResourceException {
        return old.length;
    }

    @Override
    public byte[] restore(Integer newVal) throws ResourceException {
        throw new RuntimeException("This should never happen");
    }

}
