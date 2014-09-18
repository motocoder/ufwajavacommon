package llc.ufwa.javacommon.test.concurrency;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.BatchedJobRunner;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.JobRunningException;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BatchedJobRunnerTest {

    private static final Logger logger = LoggerFactory.getLogger(BatchedJobRunnerTest.class);

	
	final int TOTAL_JOBS = 750;
	final int BATCH_SIZE = 25;
	
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
                    		Executors.newFixedThreadPool(100), 
                    		false
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
		
	@Test
    public void testBatchedJobRunnerWithoutBlocking() {
		
		BasicConfigurator.configure();

		final Logger logger = LoggerFactory
				.getLogger(BatchedJobRunnerTest.class);

    	for (int u = 0; u < 5; u++) {
    		
	        final long initialTime = System.currentTimeMillis();
			
			setControl1(false);
			setControl2(false);
			setControl3(false);
			
			final BatchedJobRunner<TestJob> jobRunner =
			        new BatchedJobRunner<TestJob>(
			        		new LinkedList<TestJob>(),
			        		10,
			        		TOTAL_JOBS,
			        		BATCH_SIZE,
			        		Executors.newFixedThreadPool(100),
			        		true
			        ) {
   
			        @Override
			        protected void doJobs(List<TestJob> jobs) throws JobRunningException {
			            
			        	for (TestJob job : jobs) {
				            if(job.getType().equals("control1")) {
				            	setControl1(true);
				            }
				            else if(job.getType().equals("control2")) {
				                setControl2(true);
				            }
				            else if(job.getType().equals("control3")) {
				            	setControl3(true);
				            }
				            
			        	}
			            
			        }
   
			        @Override
			        protected void onAllJobsComplete() {
			            
			        	setComplete(true);
			            
			        }

			        @Override
			        protected void prepare() throws JobRunningException {
			            
			        }

					@Override
					protected void onJobErroredComplete(List<TestJob> next) {
						TestCase.fail("job error");
					}
			        
			    };

			    TestCase.assertEquals(false, getControl1());
			    TestCase.assertEquals(false, getControl2());
			    TestCase.assertEquals(false, getControl3());

			for (int x = 1; x < (4); x++) {
				jobRunner.addJob(new TestJob("control" + x));
			}
			    
			for (int x = 0; x < (TOTAL_JOBS); x++) {
			    jobRunner.addJob(new TestJob(String.valueOf(x)));
			}
			
			TestJob jobTest = new TestJob("test");

			jobRunner.addJob(jobTest);
			
			jobRunner.start(true);
        
			while(jobRunner.isRunning()) {
			}
			
			TestCase.assertEquals(true, getControl1());
			TestCase.assertEquals(true, getControl2());
			TestCase.assertEquals(true, getControl3());
			
			TestCase.assertEquals(false, jobRunner.hasJobs());
			TestCase.assertEquals(true, getComplete());
			TestCase.assertEquals(false, jobRunner.isRunning());
	        
    	}
        
    }
	
	@Test
    public void testBatchedJobRunnerWithLessThanBatch() throws InterruptedException {
		
		BasicConfigurator.configure();

		final Logger logger = LoggerFactory
				.getLogger(BatchedJobRunnerTest.class);

    	for (int u = 0; u < 5000; u++) {
    		
    		logger.debug("NEW TESTTTTTTT");
    		
	        final long initialTime = System.currentTimeMillis();
			
			setControl1(false);
			setControl2(false);
			setControl3(false);
			
			final BatchedJobRunner<TestJob> jobRunner =
			        new BatchedJobRunner<TestJob>(
			        		new LinkedList<TestJob>(),
			        		1,
			        		TOTAL_JOBS,
			        		BATCH_SIZE,
			        		Executors.newFixedThreadPool(100),
			        		true
			        ) {
   
			        @Override
			        protected void doJobs(List<TestJob> jobs) throws JobRunningException {
			            
			        	for (TestJob job : jobs) {
				            if(job.getType().equals("control1")) {
				            	setControl1(true);
				            }
				            else if(job.getType().equals("control2")) {
				                setControl2(true);
				            }
				            else if(job.getType().equals("control3")) {
				            	setControl3(true);
				            }
				            
			        	}
			            
			        }
   
			        @Override
			        protected void onAllJobsComplete() {
			            
			        	setComplete(true);
			            
			        }

			        @Override
			        protected void prepare() throws JobRunningException {
			            
			        }

					@Override
					protected void onJobErroredComplete(List<TestJob> next) {
						TestCase.fail("job error");
					}
			        
			    };

			TestCase.assertEquals(false, getControl1());
			TestCase.assertEquals(false, getControl2());
			TestCase.assertEquals(false, getControl3());

			for (int x = 0; x < BATCH_SIZE - 1; x++) {
				jobRunner.addJob(new TestJob("control" + x));
			}

			jobRunner.start(true);
			
			while(jobRunner.isRunning()) {
			}
			
			TestCase.assertEquals(true, getControl1());
			TestCase.assertEquals(true, getControl2());
			TestCase.assertEquals(true, getControl3());
			
			TestCase.assertEquals(false, jobRunner.hasJobs());
			TestCase.assertEquals(true, getComplete());
			TestCase.assertEquals(false, jobRunner.isRunning());
			
			Thread.sleep(5);
	        
    	}
        
    }
	
	private synchronized void setComplete(boolean comp) {
		this.complete = comp;
	}
	
	private synchronized boolean getComplete() {
		return complete;
	}
	
	private synchronized void setControl1(boolean comp) {
		this.control1 = comp;
	}
	
	private synchronized void setControl2(boolean comp) {
		this.control2 = comp;
	}

	private synchronized void setControl3(boolean comp) {
		this.control3 = comp;
	}
	
	private synchronized boolean getControl1() {
		return control1;
	}

	private synchronized boolean getControl2() {
		return control2;
	}

	private synchronized boolean getControl3() {
		return control3;
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
