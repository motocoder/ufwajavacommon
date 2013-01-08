package llc.ufwa.javacommon.test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import llc.ufwa.concurrency.LimitingExecutorService;

public class JavaCommonLimitingExecutorService extends LimitingExecutorService {

    public JavaCommonLimitingExecutorService(ExecutorService executors, int limit) {
        super(executors, Executors.newFixedThreadPool(100), limit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        throw new RuntimeException("Not supported");
    }

    
}
