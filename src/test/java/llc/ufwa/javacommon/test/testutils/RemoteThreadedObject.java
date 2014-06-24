package llc.ufwa.javacommon.test.testutils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class RemoteThreadedObject {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoteThreadedObject.class);
    
    private final List<Object> fromLocal = new ArrayList<Object>();
    private final List<Object> fromRemote = new ArrayList<Object>();
    
    public RemoteThreadedObject() {
        
    }
    
    public void addFromLocal(Object ob) {
        synchronized(fromLocal) {
            fromLocal.add(ob);
            fromLocal.notify();
        }
    }
    
    public void addFromRemote(Object ob) {
        synchronized(fromRemote) {
            fromRemote.add(ob);
            fromRemote.notify();
        }
    }
    
    public Object getFromLocal() {
        synchronized(fromLocal) {
            
            while(fromLocal.size() == 0) {
                try {
                    fromLocal.wait();
                }
                catch (InterruptedException e) {
                    logger.error("ERROR");
                }
            }
            
            return fromLocal.remove(0);
        }
    }
    
    public Object getFromRemote() {
        synchronized(fromRemote) {
            
            while(fromRemote.size() == 0) {
                try {
                    fromRemote.wait();
                } 
                catch (InterruptedException e) {
                    logger.error("ERROR");
                }
            }
            
            return fromRemote.remove(0);
        }
    }
    
    public boolean hasFromRemote() {
        
        final boolean returnVal;
        
        synchronized(fromRemote) {
            returnVal = fromRemote.size() > 0;
        }
        
        return returnVal;
    }
}
