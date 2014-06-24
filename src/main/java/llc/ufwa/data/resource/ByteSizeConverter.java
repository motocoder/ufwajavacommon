package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public class ByteSizeConverter implements Converter<byte [], Integer> {

    @Override
    public Integer convert(byte[] old) throws ResourceException {
        return old.length;
    }

    @Override
    public byte[] restore(Integer newVal) throws ResourceException {
        throw new RuntimeException("<ByteSizeConverter><1>, This should never happen");
    }

}
