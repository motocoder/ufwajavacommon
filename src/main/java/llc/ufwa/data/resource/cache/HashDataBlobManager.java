package llc.ufwa.data.resource.cache;

import java.util.List;
import java.util.Set;

import llc.ufwa.data.exception.HashBlobException;

public interface HashDataBlobManager {
   
    Set<HashDataBlob> getBlobsAt(final int blobIndex) throws HashBlobException;

    int newBucket() throws HashBlobException;

    void setBlobs(final int blobIndex, final Set<HashDataBlob> blobs) throws HashBlobException;
   
}
