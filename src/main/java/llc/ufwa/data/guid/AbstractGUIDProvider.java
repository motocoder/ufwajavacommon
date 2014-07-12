package llc.ufwa.data.guid;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import llc.ufwa.data.dao.BucketDAO;
import llc.ufwa.data.dao.exception.DAOException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.provider.DefaultResourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGUIDProvider extends DefaultResourceProvider<String> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractGUIDProvider.class);
    
    private final BucketDAO bucket;

    public AbstractGUIDProvider(BucketDAO bucket) {
        this.bucket = bucket;
    }
    
    @Override
    public String provide() throws ResourceException {
        
        final UUID uuid = UUID.randomUUID();
        
        //try to create a guid 5 times, if collision happens, try again.
        for(int i = 0; i < 5; i++) {
            
            final String guid = getGuidPrefix() + uuid.toString();
            
            try {
                
                bucket.createGuid(guid);
                                
            }
            catch(DAOException daoException) {
                
                logger.error("GUID MUST HAVE ALREADY BEEN USED:", daoException);
                continue;
                
            }
            
            try {
                createChildren(bucket, guid);
            } 
            catch (DAOException e) {
                throw new ResourceException("Failed to create children");
            }
            
            return guid;
            
        }
        
        throw new RuntimeException("Something is wrong with your database probably");
        
    }
    
    protected abstract String getGuidPrefix();
    protected abstract void createChildren(final BucketDAO bucket, final String newGUID) throws DAOException;
    
    public Set<String> provide(int count) throws ResourceException {
        
        final UUID uuid = UUID.randomUUID();
        
        //try to create a guid 5 times, if collision happens, try again.
        for(int i = 0; i < 5; i++) {
            
            final Set<String> guids = new HashSet<String>();
            
            for(int num = 0; num < count; num++) {
                
                final String guid = getGuidPrefix() + uuid.toString();
                
                guids.add(guid);
                
                try {
                    
                    bucket.createGuid(guid);
                    
                }
                catch(DAOException daoException) {
                    
                    logger.error("GUID MUST HAVE ALREADY BEEN USED:", daoException);
                    continue;
                    
                }
                
            }
            
            
            try {
                
                for(final String guid : guids) {
                    createChildren(bucket, guid);    
                }
                
            } 
            catch (DAOException e) {
                throw new ResourceException("Failed to create children");
            }
            
            return guids;
            
        }
        
        throw new RuntimeException("Something is wrong with your database probably");
        
    }
    
}
