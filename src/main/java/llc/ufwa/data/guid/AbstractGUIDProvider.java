package llc.ufwa.data.guid;

import java.util.UUID;

import llc.ufwa.data.dao.BucketDAO;
import llc.ufwa.data.dao.exception.DAOException;
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
    public String provide() {
        
        final UUID uuid = UUID.randomUUID();
        
        //try to create a guid 5 times, if collision happens, try again.
        for(int i = 0; i < 5; i++) {
            
            final String guid = getGuidPrefix() + uuid.toString();
            
            try {
                
                bucket.createGuid(guid);
                
                createChildren(bucket, guid);
                
            }
            catch(DAOException daoException) {
                
                logger.error("GUID MUST HAVE ALREADY BEEN USED:", daoException);
                continue;
                
            }
            
            return guid;
            
        }
        
        throw new RuntimeException("Something is wrong with your database probably");
        
    }
    
    protected abstract String getGuidPrefix();
    protected abstract boolean createChildren(final BucketDAO bucket, final String newGUID) throws DAOException;
    

}
