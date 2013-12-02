package llc.ufwa.concurrency;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeverCrashingExecutor implements Executor {

    public static final Logger logger = LoggerFactory.getLogger(NeverCrashingExecutor.class);
    private final Executor internal;
    
    public NeverCrashingExecutor(final Executor internal) {
        this.internal = internal;
    }
    
    @Override
    public void execute(final Runnable command) {
        
        internal.execute(
            new Runnable() {

                @Override
                public void run() {
                    
                    try {
                        command.run();
                    }
                    catch(Throwable t) {
                        logger.error("ERROR IN EXECUTOR ", t);
                    }
                    
                }
            }
        );
        
    }

}
