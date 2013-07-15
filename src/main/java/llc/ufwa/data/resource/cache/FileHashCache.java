package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.SerializingConverter;

public class FileHashCache implements Cache<String, InputStream> {

    private FileHash<String, InputStream> hash;
    private final File dataFolder;
	private final File tempFolder;

    public FileHashCache(
        final File dataFolder,
        final File tempFolder
    ) {
        
        this.dataFolder = dataFolder;
        this.tempFolder = tempFolder;
        
        dataFolder.mkdirs();
        
        final File managerDataFile = new File(dataFolder, "data");
        final File hashFile = new File(dataFolder, "hash");
        
        final FileHashDataManager<String> manager = new FileHashDataManager<String>(managerDataFile, tempFolder, new SerializingConverter<String>());
        hash = new FileHash<String, InputStream>(hashFile, manager, 1000);

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
    public void clear() throws ResourceException {
        hash.clear();        
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
