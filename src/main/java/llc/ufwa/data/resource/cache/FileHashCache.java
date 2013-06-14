package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.SerializingConverter;

public class FileHashCache implements Cache<String, InputStream> {

    private final FileHash<String, InputStream> hash;
    private final File dataFolder;

    public FileHashCache(
        final File dataFolder,
        final File tempFolder
    ) {
        
        this.dataFolder = dataFolder;
        final FileHashDataManager<String> manager = new FileHashDataManager<String>(dataFolder, tempFolder, new SerializingConverter<String>());
        hash = new FileHash<String, InputStream>(dataFolder, manager, 1000);
        
    }
    @Override
    public boolean exists(String key) throws ResourceException {
        return hash.get(key) != null;
    }

    @Override
    public InputStream get(String key) throws ResourceException {
        return hash.get(key);
    }

    @Override
    public List<InputStream> getAll(List<String> keys) throws ResourceException {
        
        final List<InputStream> returnVals = new ArrayList<InputStream>();
        
        for(final String key : keys) {
            returnVals.add(hash.get(key));            
        }
        
        return returnVals;
        
    }

    @Override
    public void clear() {
        
        dataFolder.delete();
        dataFolder.mkdirs();
        
    }

    @Override
    public void remove(String key) {
        hash.remove(key);        
    }

    @Override
    public void put(String key, InputStream value) {
        hash.put(key, value);        
    }

}
