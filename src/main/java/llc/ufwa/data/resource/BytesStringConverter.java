package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public class BytesStringConverter implements Converter<byte [], String> {

    @Override
    public String convert(byte[] old) throws ResourceException {
        return new String(old);
    }

    @Override
    public byte[] restore(String newVal) throws ResourceException {
        return newVal.getBytes();
    }

}
