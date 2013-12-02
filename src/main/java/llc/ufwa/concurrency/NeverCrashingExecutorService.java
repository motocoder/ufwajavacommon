package llc.ufwa.concurrency;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeverCrashingExecutorService implements ExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(NeverCrashingExecutorService.class);
    
    private final ExecutorService internal;

    public NeverCrashingExecutorService(ExecutorService internal) {//TODO add callback for exception handled
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

    @Override
    public void shutdown() {
        internal.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return internal.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return internal.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return internal.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return internal.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return internal.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return internal.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return internal.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return internal.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return internal.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return internal.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return internal.invokeAny(tasks, timeout, unit);
    }

}
