package llc.ufwa.data.resource.cache;

public class FileSegment {
    
    private final int length;
    private final int index;

    public FileSegment(final int length, final int index) {
        
        this.length = length; 
        this.index = index;
        
    }

    public int getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }

}
