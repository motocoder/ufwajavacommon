package llc.ufwa.data.resource.cache;

import java.util.Map.Entry;
import java.util.Set;

import llc.ufwa.data.exception.HashBlobException;

public interface HashDataManager<Key, Value> {
   
    Set<Entry<Key, Value>> getBlobsAt(final int blobIndex) throws HashBlobException;

    int setBlobs(final int blobIndex, final Set<Entry<Key, Value>> blobs) throws HashBlobException;
   
}
