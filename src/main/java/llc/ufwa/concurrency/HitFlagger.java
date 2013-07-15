package llc.ufwa.concurrency;

import java.util.HashSet;
import java.util.Set;

public class HitFlagger {
    
    private final Set<Hit> hits = new HashSet<Hit>();
    private final long duration;
    private final int count;
    
    public HitFlagger(
        final long duration,
        final int count
    ) {
        
        this.duration = duration;
        this.count = count;
        
    }
    
    public synchronized boolean hit() {
        
        final boolean returnVal;
        
        hits.add(new Hit(System.currentTimeMillis()));
        
        System.out.println("hitsSize " + hits.size());
        
        if(hits.size() >= count) {
            
            boolean allInDuration = true;
            
            final Set<Hit> toRemove = new HashSet<Hit>();
            
            for(final Hit hit : hits) {
                
                System.out.println("time " + (System.currentTimeMillis() - hit.getTime()));
                //if hit is within the duration keep it. Otherwise remove it
                if(System.currentTimeMillis() - hit.getTime() > duration) {
                    
                    System.out.println("removing " + hit);
                    allInDuration = false;
                    toRemove.add(hit);
                    
                }
                
            }
            
            hits.removeAll(toRemove);
            
            returnVal = allInDuration;
            
        }
        else {
            returnVal = false;
        }
        
        return returnVal;        
        
    }
    
    private static class Hit {
        
        private final long time;

        public Hit(final long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }
    
}
