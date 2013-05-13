package llc.ufwa.javacommon.test.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.resource.cache.HashDataBlob;
import llc.ufwa.data.resource.cache.HashDataBlobManager;

public class FakeHashBlobManagerImpl implements HashDataBlobManager {

    private final Map<Integer, Set<HashDataBlob>> blobs = new HashMap<Integer, Set<HashDataBlob>>();
    
    private int index;

    @Override
    public Set<HashDataBlob> getBlobsAt(int blobIndex) throws HashBlobException {
        return blobs.get(blobIndex);
    }

    @Override
    public int newBucket() throws HashBlobException {
        return index++;
    }

    @Override
    public void setBlobs(int blobIndex, Set<HashDataBlob> blobsIn) throws HashBlobException {
        blobs.put(blobIndex, blobsIn);        
    }
    
}
