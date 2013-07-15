package llc.ufwa.data.resource;

import llc.ufwa.data.exception.ResourceException;

public interface Converter<Old, New> {

    /**
     * 
     * @param old
     * @return
     */
    New convert(Old old) throws ResourceException;
    
    /**
     * 
     * @param newVal
     * @return
     */
    Old restore(New newVal) throws ResourceException;
    
}
