package llc.ufwa.data.resource.cache;

public class FileSegment {
    
    private final long length;
    private final long index;

    public FileSegment(final long length, final long index) {
        
        this.length = length; 
        this.index = index;
        
    }

    public long getLength() {
        return length;
    }

    public long getIndex() {
        return index;
    }

}
