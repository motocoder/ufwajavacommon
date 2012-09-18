package llc.ufwa.util;

public class StopWatch
{
    
    private long start;
    private long stop;
    
    public StopWatch() {
        
        reset();
        
    }
    
    public void start() {
        
        if(start > 0) {
            throw new IllegalStateException("You have already started the StopWatch!");
        }
        
        start = System.currentTimeMillis();
        
    }
    
    public void reset() {
        
        this.start = -1L;
        this.stop = -1L;
        
    }
    
    public void stop() {
        
        if(stop > 0) {
            throw new IllegalStateException("You had already stopped the StopWatch!");
        }
        
        stop = System.currentTimeMillis();
        
    }
    
    public long getTime() {
        
        if(start < 0) {
            throw new IllegalStateException("You must start the StopWatch to get a time!");
        }
        else {
            
            final long returnVal;
            
            if(stop < 0) {
                returnVal = (System.currentTimeMillis() - start);
            }
            else {
                returnVal = (stop - start);
            }
            
            return returnVal;
            
        }
        
    }

}
