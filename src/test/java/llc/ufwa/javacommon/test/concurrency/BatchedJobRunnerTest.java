package llc.ufwa.javacommon.test.concurrency;


public class BatchedJobRunnerTest {
//    
//	final int TOTAL_JOBS = 2000;
//	private boolean complete = false;
//	private boolean control3 = false;
//	private boolean control2 = false;
//	private boolean control1 = false;
//	
//	@Test
//    public void testBatchedJobRunner() {
//		
//		BasicConfigurator.configure();
//
//    	for (int u = 0; u < 5; u++) {
//    		
//	        try {
//	            
//	        	final long initialTime = System.currentTimeMillis();
//	        	
//	            final ParallelControl<String> control1 = new ParallelControl<String>();
//	            final ParallelControl<String> control2 = new ParallelControl<String>();
//	            final ParallelControl<String> control3 = new ParallelControl<String>();
//	            
//	            final BatchedJobRunner<TestJob> jobRunner =
//	                    new BatchedJobRunner<TestJob>(
//	                    		10,
//	                    		TOTAL_JOBS,
//	                    		Executors.newFixedThreadPool(100)
//	                    ) {
//	    
//	                    @Override
//	                    protected void doJob(TestJob job) throws JobRunningException {
//	                        
//	                        if(job.getType().equals("1")) {
//	                            
//	                            control1.setValue("something");
//	                            try {
//	                                control1.blockOnce();
//	                            } 
//	                            catch (InterruptedException e) {
//	                                TestCase.fail(e.getMessage());
//	                            }
//	                            
//	                        }
//	                        else if(job.getType().equals("2")) {
//	                            
//	                            control2.setValue("something");
//	                            try {
//	                                control2.blockOnce();
//	                            } catch (InterruptedException e) {
//	                                TestCase.fail(e.getMessage());
//	                            }
//	                            
//	                        }
//	                        else if(job.getType().equals("3")) {
//	                            
//	                            control3.setValue("something");
//	                            try {
//	                                control3.blockOnce();
//	                            } catch (InterruptedException e) {
//	                                TestCase.fail(e.getMessage());
//	                            }
//	                            
//	                        }
//	                        
//	                    }
//	    
//	                    @Override
//	                    protected void onAllJobsComplete() {
//	                        
//	                    	setComplete(true);
//	                        
//	                    }
//	
//	                    @Override
//	                    protected void prepare() throws JobRunningException {
//	                        
//	                    }
//	
//						@Override
//						protected void onJobErroredComplete(TestJob next) {
//							
//						}
//	                    
//	                };
//	            
//	            for (int x = 0; x < (TOTAL_JOBS); x++) {
//	                jobRunner.addJob(new TestJob(String.valueOf(x)));
//	            }
//	            
//	            Thread.sleep(100);
//	            
//	            TestCase.assertEquals(control1.getValue(), "something");
//	            
//	            control1.unBlockOnce();
//	            
//	            TestCase.assertEquals(control1.getValue(), "something");
//	            TestCase.assertEquals(control2.getValue(), "something");
//	            
//	            control2.unBlockOnce();
//	            
//	            TestCase.assertEquals(control1.getValue(), "something");
//	            TestCase.assertEquals(control2.getValue(), "something");
//	            TestCase.assertEquals(control2.getValue(), "something");
//	            
//	            control3.unBlockOnce();
//	            
//	            while(jobRunner.isRunning()){
//	            	if ((System.currentTimeMillis() - initialTime) > 1000) {
//	            		TestCase.fail("runner took too long, ran jobs were " + jobRunner.getRuns());
//	            	}
//	            }
//	            
//	            Thread.sleep(1000);
//	
//	            TestCase.assertEquals(TOTAL_JOBS, jobRunner.getRuns());
//	            
//	            TestCase.assertEquals(false, jobRunner.hasJobs());
//	            TestCase.assertEquals(false, jobRunner.isRunning());
//	            TestCase.assertEquals(true, getComplete());
//	            
//	        }
//	        catch (InterruptedException e) {
//	            TestCase.fail("Failure");
//	        }
//	        
//    	}
//        
//    }
//	
//	@Test
//    public void testBatchedJobRunnerWithoutBlocking() {
//		
//		BasicConfigurator.configure();
//
//    	for (int u = 0; u < 5; u++) {
//    		
//	        try {
//	            
//	        	final long initialTime = System.currentTimeMillis();
//	        	
//	            final ParallelControl<String> control1 = new ParallelControl<String>();
//	            final ParallelControl<String> control2 = new ParallelControl<String>();
//	            final ParallelControl<String> control3 = new ParallelControl<String>();
//	            
//	            final BatchedJobRunner<TestJob> jobRunner =
//	                    new BatchedJobRunner<TestJob>(
//	                    		10,
//	                    		TOTAL_JOBS,
//	                    		Executors.newFixedThreadPool(100)
//	                    ) {
//	    
//	                    @Override
//	                    protected void doJob(TestJob job) throws JobRunningException {
//	                        
//	                        if(job.getType().equals("1")) {
//	                        	setControl1(true);
//	                        }
//	                        else if(job.getType().equals("2")) {
//	                            setControl2(true);
//	                        }
//	                        else if(job.getType().equals("3")) {
//	                        	setControl3(true);
//	                        }
//	                        
//	                    }
//	    
//	                    @Override
//	                    protected void onAllJobsComplete() {
//	                        
//	                    	setComplete(true);
//	                        
//	                    }
//	
//	                    @Override
//	                    protected void prepare() throws JobRunningException {
//	                        
//	                    }
//	
//						@Override
//						protected void onJobErroredComplete(TestJob next) {
//							
//						}
//	                    
//	                };
//	            
//	            for (int x = 0; x < (TOTAL_JOBS); x++) {
//	                jobRunner.addJob(new TestJob(String.valueOf(x)));
//	            }
//	            
//	            Thread.sleep(100);
//	            
//	            while(jobRunner.isRunning()){
//	            	if ((System.currentTimeMillis() - initialTime) > 1000) {
//	            		TestCase.fail("runner took too long, ran jobs were " + jobRunner.getRuns());
//	            	}
//	            }
//	            
//	            Thread.sleep(1000);
//
//	            TestCase.assertEquals(true, getControl1());
//	            TestCase.assertEquals(true, getControl2());
//	            TestCase.assertEquals(true, getControl3());
//	
//	            TestCase.assertEquals(TOTAL_JOBS, jobRunner.getRuns());
//	            
//	            TestCase.assertEquals(false, jobRunner.hasJobs());
//	            TestCase.assertEquals(true, getComplete());
//	            TestCase.assertEquals(false, jobRunner.isRunning());
//	            
//	        }
//	        catch (InterruptedException e) {
//	            TestCase.fail("Failure");
//	        }
//	        
//    	}
//        
//    }
//	
//	private synchronized void setComplete(boolean comp) {
//		this.complete = comp;
//	}
//	
//	private synchronized boolean getComplete() {
//		return complete;
//	}
//	
//	private synchronized void setControl1(boolean comp) {
//		this.control1 = comp;
//	}
//	
//	private synchronized void setControl2(boolean comp) {
//		this.control2 = comp;
//	}
//
//	private synchronized void setControl3(boolean comp) {
//		this.control3 = comp;
//	}
//	
//	private synchronized boolean getControl1() {
//		return control1;
//	}
//
//	private synchronized boolean getControl2() {
//		return control2;
//	}
//
//	private synchronized boolean getControl3() {
//		return control3;
//	}
//	
//    private static class TestJob {
//        
//        private final String type;
//
//        public TestJob(String type) {
//            this.type = type;
//        }
//
//        public String getType() {
//            return type;
//        }
//       
//    }
//   
}
