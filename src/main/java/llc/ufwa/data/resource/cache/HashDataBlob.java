package llc.ufwa.data.resource.cache;

public class HashDataBlob {
    
    private final int index;
    private final String key;
    private final int dataLength;

    /**
     * 
     * @param index
     * @param key
     * @param dataLength
     */
    public HashDataBlob(
        final int index,
        final String key, 
        final int dataLength
    ) {
        
        this.index = index;
        this.key = key;
        this.dataLength = dataLength;
        
    }

    public int getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    public int getDataLength() {
        return dataLength;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return key.equals(obj);
    }
    
    

}
