package llc.ufwa.javacommon.test.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.resource.cache.HashDataManager;

public class FakeHashManagerImpl<Key, Value> implements HashDataManager<Key, Value> {

    private final Map<Integer, Set<Entry<Key, Value>>> blobs = new HashMap<Integer, Set<Entry<Key, Value>>>();
    
    private int index;

    @Override
    public Set<Entry<Key, Value>> getBlobsAt(int blobIndex) throws HashBlobException {
        return blobs.get(blobIndex);
    }

    @Override
    public int setBlobs(int blobIndex, Set<Entry<Key, Value>> blobsIn) throws HashBlobException {
        
        if(blobIndex < 0) {
            blobIndex = index++;
        }
        
        blobs.put(blobIndex, blobsIn);        
        
        return blobIndex;
        
    }

    @Override
    public void eraseBlobs(int blobIndex) throws HashBlobException {
        blobs.remove(blobIndex);        
    }
    
}
