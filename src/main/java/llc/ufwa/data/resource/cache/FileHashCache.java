package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.data.exception.CorruptedDataException;
import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.SerializingConverter;

public class FileHashCache implements Cache<String, InputStream> {

    private static final Logger logger = LoggerFactory.getLogger(FileHashCache.class);
    
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
    	
        try {
            return hash.get(key) != null;
        }
    	catch(CorruptedDataException e) {
    	    
    	    logger.error("CORRUPTED DATA, CLEARING", e);
    	    clear();
    	    
    	    return false;
    	    
    	}
    	catch (HashBlobException e) {
            throw new ResourceException("ERROR", e);
        }
        
    }

    @Override
    public InputStream get(String key) throws ResourceException {
        
        try {
            return hash.get(key);
        } 
        catch(CorruptedDataException e) {
            
            logger.error("CORRUPTED DATA, CLEARING", e);
            clear();
            
            return null;
            
        }
        catch (HashBlobException e) {
            throw new ResourceException("ERROR", e);
        }
        
    }

    @Override
    public List<InputStream> getAll(List<String> keys) throws ResourceException {
        
        final List<InputStream> returnVals = new ArrayList<InputStream>();
        
        for(final String key : keys) {
            try {
                returnVals.add(hash.get(key));
            }
            catch(CorruptedDataException e) {
                
                logger.error("CORRUPTED DATA, CLEARING", e);
                clear();
                
                return null;
                
            }
            catch (HashBlobException e) {
                throw new ResourceException("ERROR", e);
            }         
        }
        
        return returnVals;
        
    }

    @Override
    public void clear() throws ResourceException {
        
        try {
            hash.clear();
        } 
        catch (HashBlobException e) {
            throw new ResourceException("ERROR", e);
        } 
        
    }

    @Override
    public void remove(String key) throws ResourceException {
        
        try {
            hash.remove(key);
        }
        catch(CorruptedDataException e) {
            
            logger.error("CORRUPTED DATA, CLEARING", e);
            clear();
            
            return;
            
        }
        catch (HashBlobException e) {
            throw new ResourceException("ERROR", e);
        }
        
    }

    @Override
    public void put(String key, InputStream value) throws ResourceException {
        
        try {
            hash.put(key, value);
        }
        catch(CorruptedDataException e) {
            
            logger.error("CORRUPTED DATA, CLEARING", e);
            clear();
            
            return;
            
        }
        catch (HashBlobException e) {
            throw new ResourceException("ERROR", e);
        }
        
    }

}
