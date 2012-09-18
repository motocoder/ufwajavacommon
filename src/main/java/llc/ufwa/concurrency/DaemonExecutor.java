package llc.ufwa.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Executor that runs on DaemonThreads.
 * 
 * @author seanwagner
 *
 */
public class DaemonExecutor implements Executor {
    
    private final Executor internal;
    
    public DaemonExecutor(int threads) {
        this.internal = 
            Executors.newFixedThreadPool(
            threads, 
            new DaemonThreadFactory()
        );
    }

    @Override
    public void execute(Runnable command) {
        internal.execute(command);
    }

}
