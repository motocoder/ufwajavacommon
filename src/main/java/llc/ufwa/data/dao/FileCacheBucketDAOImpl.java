package llc.ufwa.data.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import llc.ufwa.data.dao.exception.DAOException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.util.DataUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCacheBucketDAOImpl implements BucketDAO {

	private static final Logger logger = LoggerFactory.getLogger(FileCacheBucketDAOImpl.class);
	
	private final Cache<String, InputStream> cache;

	public FileCacheBucketDAOImpl(
	    final Cache<String, InputStream> cache
	) {
		this.cache = cache;
	}
	
	@Override
	public Object get(String guid) throws DAOException {
		
		try {
		    
		    final InputStream bytes = cache.get(guid);
		    
		    if(bytes == null) {
		        return null;
		    }
		    
			return DataUtils.deserialize(bytes);
			
		}
		catch (ClassNotFoundException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		} 
		catch (IOException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
	}

	@Override
	public InputStream getStream(String guid) throws DAOException {
		
		try {
			return cache.get(guid);
		} 
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}

	@Override
	public void save(String guid, Object value) throws DAOException {
		
	    logger.debug("saving " + value);
	    
		try {
		    
		    if(value == null) {
		        cache.remove(guid);
		    }
		    else {
		        		        
		        final InputStream toOut = DataUtils.serializeToStream(value);
		        logger.debug("serialized");
		        
		        cache.put(guid, toOut);
		        
		    }
		    
		} 
		catch (ResourceException e) {

			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		catch (IOException e) {

			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}

	@Override
	public void save(String guid, InputStream value) throws DAOException {
		
		try {
		    
		    if(value == null) {
		        cache.remove(guid);
		    }
		    else {
		        cache.put(guid, value);
		    }
		    
		} 
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}

	

	@Override
	public void delete(String guid) throws DAOException {
		
		try {
			cache.remove(guid);
		} 
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}

	@Override
	public void clear() throws DAOException {
	    
		try {
			cache.clear();
		}
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}

	@Override
	public boolean exists(String gUID) throws DAOException {
		
		try {
			return cache.exists(gUID);
		}
		catch (ResourceException e) {
			
			logger.error("ERROR:", e);
			throw new DAOException("ERROR:", e);
			
		}
		
	}
	
	@Override
    public void createGuid(String guid) throws DAOException {
        
        try {
            
            if(cache.exists(guid)) {
                throw new DAOException("Guid exists");  
            }
            else {
                cache.put(guid, new ByteArrayInputStream(new byte[] {1, 2, 3}));
            }
            
        } 
        catch (ResourceException e) {
            
            logger.error("ERROR:", e);
            throw new DAOException("ERROR:", e);
            
        }
        
    }
	
}
