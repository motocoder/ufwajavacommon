package llc.ufwa.javacommon.test.concurrency;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.BatchedJobRunner;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.concurrency.ParallelJobRunner;
import llc.ufwa.data.exception.JobRunningException;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BatchedJobRunnerTest {

    private static final Logger logger = LoggerFactory.getLogger(BatchedJobRunnerTest.class);
    
	final int TOTAL_JOBS = 100;
	private boolean complete = false;
	private boolean control3 = false;
	private boolean control2 = false;
	private boolean control1 = false;
	
	@Test
    public void testBatchedJobRunner() {
		
		BasicConfigurator.configure();

		try {
            
            final ParallelControl<String> control1 = new ParallelControl<String>();
            final ParallelControl<String> control2 = new ParallelControl<String>();
            final ParallelControl<String> control3 = new ParallelControl<String>();
            
            final ParallelControl<Boolean> complete = new ParallelControl<Boolean>();
            
            final BatchedJobRunner<TestJob> jobRunner =
                    new BatchedJobRunner<TestJob>(
                    		new LinkedList<TestJob>(),
                    		5,
                    		100,
                    		5,
                    		Executors.newFixedThreadPool(100)
                    ) {
                        
                    @Override
                    protected void onAllJobsComplete() {
                        
                        complete.setValue(true);
                        complete.unBlockOnce();
                        
                    }

                    @Override
                    protected void prepare() throws JobRunningException {
                        // TODO Auto-generated method stub
                        
                    }

					@Override
					protected void doJobs(List<TestJob> jobs)
							throws JobRunningException {
						
						for (TestJob job : jobs) {
							
							if(job.getType().equals("1")) {
	                            
	                            control1.setValue("something");
	                            try {
	                                control1.blockOnce();
	                            } 
	                            catch (InterruptedException e) {
	                                TestCase.fail(e.getMessage());
	                            }
	                            
	                        }
	                        else if(job.getType().equals("2")) {
	                            
	                            control2.setValue("something");
	                            try {
	                                control2.blockOnce();
	                            } catch (InterruptedException e) {
	                                TestCase.fail(e.getMessage());
	                            }
	                            
	                        }
	                        else if(job.getType().equals("3")) {
	                            
	                            control3.setValue("something");
	                            try {
	                                control3.blockOnce();
	                            } catch (InterruptedException e) {
	                                TestCase.fail(e.getMessage());
	                            }
	                            
	                        }
							
						}
						
					}

					@Override
					protected void onJobErroredComplete(List<TestJob> next) {
						// TODO Auto-generated method stub
						
					}
                    
                };
            
            jobRunner.addJob(new TestJob("1"));
            jobRunner.addJob(new TestJob("2"));
            jobRunner.addJob(new TestJob("3"));
            
            Thread.sleep(100);
            
            TestCase.assertEquals(control1.getValue(), "something");
            
            control1.unBlockOnce();
            
            Thread.sleep(100);
            
            TestCase.assertEquals(control1.getValue(), "something");
            TestCase.assertEquals(control2.getValue(), "something");
            
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
