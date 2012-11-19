package llc.ufwa.javacommon.test.concurrency;

import java.util.LinkedList;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.concurrency.SequentialJobRunner;
import llc.ufwa.data.exception.JobRunningException;

import org.junit.Test;

public class SequentialJobRunnerTest {
    
    @Test
    public void testJobRunner() {
        
        try {
            
            final ParallelControl<String> control1 = new ParallelControl<String>();
            final ParallelControl<String> control2 = new ParallelControl<String>();
            final ParallelControl<String> control3 = new ParallelControl<String>();
            
            final ParallelControl<Boolean> complete = new ParallelControl<Boolean>();
            
            final SequentialJobRunner<TestJob> jobRunner =
                    new SequentialJobRunner<TestJob>(
                        Executors.newFixedThreadPool(10), 
                        new LinkedList<TestJob>()
                    ) {
    
                    @Override
                    protected void doJob(TestJob job) throws JobRunningException {
                        
                        if(job.getType().equals("1")) {
                            
                            control1.setValue("something");
                            try {
                                control1.blockOnce();
                            } 
                            catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            
                        }
                        else if(job.getType().equals("2")) {
                            
                            control2.setValue("something");
                            try {
                                control2.blockOnce();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            
                        }
                        else if(job.getType().equals("3")) {
                            
                            control3.setValue("something");
                            try {
                                control3.blockOnce();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            
                        }
                        
                    }
    
                    @Override
                    protected void onAllJobsComplete() {
                        
                        complete.setValue(true);
                        complete.unBlockOnce();
                        
                    }

                    @Override
                    protected void prepare() throws JobRunningException {
                        // TODO Auto-generated method stub
                        
                    }
                    
                };
            
            jobRunner.addJob(new TestJob("1"));
            jobRunner.addJob(new TestJob("2"));
            jobRunner.addJob(new TestJob("3"));
            
            Thread.sleep(100);
            
            TestCase.assertEquals(control1.getValue(), "something");
            TestCase.assertNull(control2.getValue());
            TestCase.assertNull(control3.getValue());
            
            control1.unBlockOnce();
            
            Thread.sleep(100);
            
            TestCase.assertEquals(control1.getValue(), "something");
            TestCase.assertEquals(control2.getValue(), "something");
            TestCase.assertNull(control3.getValue());
            
            control2.unBlockOnce();
            
            Thread.sleep(100);
            
            TestCase.assertEquals(control1.getValue(), "something");
            TestCase.assertEquals(control2.getValue(), "something");
            TestCase.assertEquals(control2.getValue(), "something");
            
            control3.unBlockOnce();
            complete.blockOnce();
            TestCase.assertTrue(complete.getValue());
            
            TestCase.assertFalse(jobRunner.hasJobs());
            
        
        } 
        catch (InterruptedException e) {
            TestCase.fail("Failure");
        }
        
    }
    
    private static class TestJob {
        
        private final String type;

        public TestJob(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
       
    }

}
